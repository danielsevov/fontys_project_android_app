// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { Filter, repository, FilterBuilder } from '@loopback/repository';
import { SpotRepository, PermissionRepository, UserPermissionsRepository, AdminLogsRepository, UserRepository, ImageRepository } from '../repositories';
import { Spot, Check, UserPermissions, Permission, AdminLogs, User, Image } from '../models';
import { del, get, getModelSchemaRef, HttpErrors, param, patch, post, requestBody, response, put } from '@loopback/rest';
import { authenticate } from '@loopback/authentication';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';
import { FirebaseCloudMessaging } from '../models'
  
@authenticate('jwt')
export class SpotController {
  constructor(
  @repository(SpotRepository) protected spotRepo: SpotRepository,
  @repository(PermissionRepository) protected permissionRepo : PermissionRepository,
  @repository(UserPermissionsRepository) protected userPermissionRepo : UserPermissionsRepository,
  @repository(AdminLogsRepository) protected adminRepo : AdminLogsRepository,
  @repository(UserRepository) protected userRepo : UserRepository,
  @repository(ImageRepository) protected imgRepo : ImageRepository,
	) {}
	
  @get('/spots/{spotId}/image')
  async request_images(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('spotId') spotId: number,
  ) : Promise<Image[]> {
	return this.imgRepo.find({ where: { spot_id: spotId } });
  }
	
  //Upload picture to spot
  @put('/spots/image/{spotId}')
  async upload_image(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('spotId') spotId: number,
  @requestBody(
      {
        description: 'Required input for image upload',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
			  required: ['data', 'type'],
              properties: {
				data:{
                  type: 'string',
                },
                type: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) img: Image
  ) {
	  
	  
	  
	let log = new AdminLogs();
		log.execution_date = new Date().getTime().toString();
		log.by = parseInt(currentUserProfile[securityId]);
		log.action = "ASSIGN IMAGE {"+
		"'spot_id':'"+spotId+"',"+
		"'image_data':'"+img.data+"',"+
		"'image_type':'"+img.type+"',"+
		"}";
	await this.adminRepo.create(log);
	
	img.spot_id = spotId;
	
	await this.imgRepo.create(img);
	return {'SUCCES':'image_uploaded', 'spot_id':''+spotId+'', 'data': ''+img.data+'', 'type': ''+img.type+''};
  }
  
  
  
  //function to create a spot
  @post('/spot')
  @response(200, {
    description: 'Spot model instance',
    content: {'application/json': {schema: {
      type: 'object',
      properties: {
        title: {
          type: 'string',
        },
        coordinates: {
          type: 'string',
        },
        date_created: {
          type: 'string',
        }
      },
    }}},
  })
  async post_spot(
      @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
	  @requestBody(
      {
        description: 'Required input for posting a spot',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['title', 'coordinates', 'creator'],
              properties: {
                title: {
                  type: 'string',
                },
                coordinates: {
                  type: 'string',
                },
                category: {
                  type: 'string',
                },
                range: {
                  type: 'number',
                },
				tags: {
                  type: 'array',
				  items: {type: 'string'},
                }
              },
            },
          }
        },
      }) spot: Spot) {
		  
		//check if no other spot is closeby
		let spots = (await this.spotRepo.find()).filter(s=> s.coordinates != undefined && this.isInRange( spot.coordinates, s.coordinates, 0.2));
		if(spots.length != 0) return {"ERROR" : "spot_to_close"}
		
		//prepare spot object
        spot.date_created = new Date().getTime().toString();
		spot.date_modified = spot.date_created;
		spot.creator = parseInt(currentUserProfile[securityId]);
		spot.range = 0.5; 
		
	    //create spot
        spot.id = (await this.spotRepo.create(spot)).id;
		
		//log spot creation
		let log = new AdminLogs();
		log.execution_date = new Date().getTime().toString();
		log.by = parseInt(currentUserProfile[securityId]);
		log.action = "CREATE SPOT {'id':'"+spot.id+"','by':'"+log.by+"','date_created':'"+log.execution_date+"'}";
		await this.adminRepo.create(log);
		
		FirebaseCloudMessaging.getInstance().sendMessage();
		
        return {'SUCCES': 'spot_created', 'id' : spot.id, 'title' : spot.title, 'coordinates' : spot.coordinates, 'date_created' : spot.date_created};
  }


  @get('/spots')
  @response(200, {
    description: 'JSON Array of Spot instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Spot, {includeRelations: true}),
        },
      },
    },
  })
  async get_spot(): Promise<Spot[]> {
    return this.spotRepo.find();
  }

  
  //function to get spots by category
  @post('/spots_by_category')
  @response(200, {
    description: 'Array of spot instances filtered by category',
    content: {'application/json': {schema: {
      type: 'object',
      properties: {
        title: {
          type: 'string',
        },
		category: {
          type: 'string',
        },
        coordinates: {
          type: 'string',
        },
        date_created: {
          type: 'string',
        }
      },
    }}},
  })
  async get_spots_by_category(
  @requestBody(
      {
        description: 'Required input for filtering spots by category',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['category'],
              properties: {
                category: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) spot: Spot): Promise<Spot[]> {
   
   //create filter object
   const filterBuilder = new FilterBuilder();
   const filter = filterBuilder
  .where({category: spot.category})
  .build();
  
  //search and apply filter
  return this.spotRepo.find(filter);
  }
  
  //function to get spot by id
  @get('/spots/{spotId}')
  async getSpotById(
  @param.path.number('spotId') spotId: number,
  ): Promise<Spot[]> {  
	   //create filter object
	   const filterBuilder = new FilterBuilder();
	   const filter = filterBuilder
	  .where({id: spotId})
	  .build();
	  
	  //search and apply filter
	  return this.spotRepo.find(filter);
  }
  
  //function to get spot by id
  @post('/spot_by_id')
  @response(200, {
    description: 'Array of spot instances filtered by category',
    content: {'application/json': {schema: {
      type: 'object',
      properties: {
        title: {
          type: 'string',
        },
		category: {
          type: 'string',
        },
        coordinates: {
          type: 'string',
        },
        date_created: {
          type: 'string',
        }
      },
    }}},
  })
  async get_spot_by_id(
  @requestBody(
      {
        description: 'Required input for filtering spots by id',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['id'],
              properties: {
                id: {
                  type: 'number',
                }
              },
            },
          }
        },
      }) spot: Spot): Promise<Spot[]> {
   
   //create filter object
   const filterBuilder = new FilterBuilder();
   const filter = filterBuilder
  .where({id: spot.id})
  .build();
  
  //search and apply filter
  return this.spotRepo.find(filter);
  }
  
  
  //function to check if the distance between two coordinates is smaller than the provided range
  isInRange = function (pivotCoordinates: string, actualCoordinates: string, range: number): boolean {
    const pivotCoordArray = pivotCoordinates.split(",");
	const pivotCoordX = parseFloat(pivotCoordArray[0]);
	const pivotCoordY = parseFloat(pivotCoordArray[1]);
		  
	if( isNaN(pivotCoordX) || isNaN(pivotCoordY) ) return false;
	
	const actualCoordArray = actualCoordinates.split(",");
	const actualCoordX = parseFloat(actualCoordArray[0]);
	const actualCoordY = parseFloat(actualCoordArray[1]);
		  
	if( isNaN(actualCoordX) || isNaN(actualCoordY) ) return false;
	
	const deltaX = Math.abs(pivotCoordX - actualCoordX);
	const deltaY = Math.abs(pivotCoordY - actualCoordY);
		  
    if( Math.sqrt(deltaX*deltaX + deltaY*deltaY) > (0.01*range) ) return false;
	
	return true;
  };
  
  
  //function to get spots by coordinates
  @post('/spots_by_coordinates')
  @response(200, {
    description: 'Array of spot instances in a certain range',
    content: {'application/json': {schema: {
      type: 'object',
      properties: {
        coordinates: {
          type: 'string',
        }
      },
    }}},
  })
  async get_spots_by_coordinates(@requestBody(
      {
        description: 'Required input for filtering spots by coordinates',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['coordinates'],
              properties: {
                coordinates: {
                  type: 'string',
                }, 
				range: {
                  type: 'number',
				  default: 3
                },
				category: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) pivotSpot: Spot): Promise<Spot[]> {
		  
    //prepare filter
    const filterBuilder = new FilterBuilder();
	const filter = filterBuilder
    .where({category: pivotSpot.category})
    .build();
	
	//search and apply filter
    return (await this.spotRepo.find(filter)).filter(spot => spot.coordinates != undefined && this.isInRange( pivotSpot.coordinates, spot.coordinates, pivotSpot.range) );
  }
  
  
  //function to get all spots nearby the user
  @post('/spots_nearby_user')
  @response(200, {
    description: 'Array of spot instances for which the user is in range',
  })
  async get_spots_nearby_user(@requestBody(
      {
        description: 'Required input for finding spots nearby user',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['coordinates'],
              properties: {
                coordinates: {
                  type: 'string',
                },
				category: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) pivotSpot: Spot): Promise<Spot[]> {
		  
	//prepare filter
    const filterBuilder = new FilterBuilder();
	const filter = filterBuilder
    .where({category: pivotSpot.category})
    .build();
	
	//search and apply filter
    return (await this.spotRepo.find(filter)).filter(spot => spot.coordinates != undefined && this.isInRange( pivotSpot.coordinates, spot.coordinates, spot.range) );
  }


  //function to delete a spot
  @del('/spots/{id}')
  async deleteById(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('id') spotId: number,
  ) {
	  
	//check if spot exists 
	let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
    if(!oldSpot) return {"ERROR" : "spot_not_found", "id": spotId};
	
	//check if user is onwer of spot and delete spot
		let hasPermission = await this.has_permission('delete_spot', parseInt(currentUserProfile[securityId]));

    if(!hasPermission && parseInt(currentUserProfile[securityId]) !== oldSpot.creator) return {'ERROR': 'not_creator', "id" : spotId};
	
	//log spot deletion
	let log = new AdminLogs();
	log.execution_date = new Date().getTime().toString();
	log.by = parseInt(currentUserProfile[securityId]);
	log.action = "DELETE SPOT {'id':'"+oldSpot.id+"','by':'"+log.by+"','date_created':'"+log.execution_date+"'}";
	await this.adminRepo.create(log);
	
	//Delete spot
	await this.spotRepo.deleteById(spotId)
		
    return {'SUCCES': 'spot_deleted', "id" : spotId};
  }
  
  
  //function to upvote a spot
  @patch('/spots/{spotId}/upvote')
  async upvoteById(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('spotId') spotId: number,
  ) {
	
	//check if spot exists 
	let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
    if(!oldSpot) return {'ERROR': 'spot_not_found', 'id': spotId};
	
	//check if user has already rated this spot
	let str = '%VOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
    let rating = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: str} } }));	
	if(rating) return {'ERROR': 'already_up_voted', "id" : spotId};
	
	//check if user is onwer of spot
    if(parseInt(currentUserProfile[securityId]) === oldSpot.creator) return {'ERROR': 'spot_owner_rate_not_allowed', "id" : spotId};
	
	//log spot upvote
	let log = new AdminLogs();
	log.execution_date = new Date().getTime().toString();
	log.by = parseInt(currentUserProfile[securityId]);
	log.action = "UPVOTE SPOT {'id':'"+oldSpot.id+"','by':'"+log.by+"','date_created':'"+log.execution_date+"'}";
	await this.adminRepo.create(log);
	
	//update positive rating value
	oldSpot.positive_rating = oldSpot.positive_rating + 1; 
	await this.spotRepo.updateById(spotId, oldSpot);
	
	return {'SUCCES': 'spot_up_voted', "id" : spotId};
  }


  //function to downvote a spot
  @patch('/spots/{spotId}/downvote')
  async downvoteById(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('spotId') spotId: number,
  ) {
	  
	//check if spot exists 
	let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
    if(!oldSpot) return {"ERROR": "spot_not_found", "id" : spotId};
	
	//check if user has already rated this spot
	let str = '%VOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
    let rating = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: str} } }));	
	if(rating) return {'ERROR': 'already_up_voted', "id" : spotId};
	
	//check if user is onwer of spot
    if(parseInt(currentUserProfile[securityId]) === oldSpot.creator) return {'ERROR': 'spot_owner_rate_not_allowed', "id" : spotId};
	
	//log spot downvote
	let log = new AdminLogs();
	log.execution_date = new Date().getTime().toString();
	log.by = parseInt(currentUserProfile[securityId]);
	log.action = "DOWNVOTE SPOT {'id':'"+oldSpot.id+"','by':'"+log.by+"','date_created':'"+log.execution_date+"'}";
	await this.adminRepo.create(log);
	
	//update negative rating value
	oldSpot.negative_rating = oldSpot.negative_rating + 1; 
	await this.spotRepo.updateById(spotId, oldSpot);
	  
	return {"SUCCES" : "spot_down_voted", "id" : spotId};
  }
  
  @get('/spots/{spotId}/rateable')
  async canUserRateSpot(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('spotId') spotId: number,
  ) {	
	//check if spot exists 
	let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
    if(!oldSpot) return {"response" : false};
	
	//check if user has already rated this spot
	let str = '%VOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
	let strU = '%UPVOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
    let rating = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: str} } }));		
	let ratingU = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: strU} } }));
	
	if(rating && ratingU) return {"response" : false, "is_creator": false, "is_upvote": true};
	else if(rating) return {"response" : false, "is_creator": false, "is_upvote": false};
	
	//check if user is onwer of spot
    if(parseInt(currentUserProfile[securityId]) === oldSpot.creator) return {"response" : false, "is_creator": true};
    
	return {"response" : true, "is_creator": false, "is_upvote": false};
  }
  
  //function to check if spot can be rated by current user
  @post('/can_rate_spot')
  async canBeRated(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @requestBody(
      {
        description: 'Required input for filtering spots by id',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['id'],
              properties: {
                id: {
                  type: 'number',
                }
              },
            },
          }
        },
      }) spot: Spot) {
	  
	let spotId = spot.id;
	
	//check if spot exists 
	let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
    if(!oldSpot) return {"response" : false};
	
	//check if user has already rated this spot
	let str = '%VOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
	let strU = '%UPVOTE SPOT {\'id\':\'' + oldSpot.id + '\'%';
    let rating = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: str} } }));		
	let ratingU = (await this.adminRepo.findOne({ where: { by: parseInt(currentUserProfile[securityId]), action: {like: strU} } }));
	
	if(rating && ratingU) return {"response" : false, "is_upvote": true};
	else if(rating) return {"response" : false, "is_upvote": false};
	
	//check if user is onwer of spot
    if(parseInt(currentUserProfile[securityId]) === oldSpot.creator) return {"response" : false, "is_upvote": true};
    
	return {"response" : true, "is_upvote": false};
  }
  

  //function to update a spot
  @patch('/spots')
  @response(204, {
    description: 'Spot PATCH success',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            message: {
              type: 'string',
            }
          }
        }
      }
    }
  })
  async patchById(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
	@requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['id'],
            properties: {
              id: {
                type: 'number',
              },
              title: {
                type: 'string',
              },
              coordinates: {
                type: 'string',
              },
              category: {
                type: 'string',
              },
              range: {
                type: 'number',
              },
			  tags: {
                type: 'array',
				items: {type: 'string'},
              }
            },
          },
        }
      },
    })
    spot: Spot,
  ): Promise<object> {
	  
	  //check if spot exists
	  let oldSpot = await this.spotRepo.findOne({ where: { id: spot.id } });  
	  if(!oldSpot) return {"ERROR" : "spot_not_found"};
	  
	  //update last modified date
	  spot.date_modified = new Date().getTime().toString();
	  
	  //check if user is owner of spot and update spot
	  let hasPermission = await this.has_permission('delete_spot', parseInt(currentUserProfile[securityId]));
      if(hasPermission || parseInt(currentUserProfile[securityId]) === oldSpot.creator) await this.spotRepo.updateById(spot.id, spot);
	  else return {"ERROR": "no_permission", "permission": 'delete_spot'};
	  
	  //log spot update
	  let log = new AdminLogs();
	  log.execution_date = new Date().getTime().toString();
	  log.by = parseInt(currentUserProfile[securityId]);
	  log.action = "PATCH SPOT {'id':'"+oldSpot.id+"','by':'"+log.by+"','date_created':'"+log.execution_date+"'}";
	  await this.adminRepo.create(log);
	
      return {"SUCCES": "spot_updated", "message" : "Spot with id = " + spot.id + " PATCH success", "modified_on": spot.date_modified};
  }
  
  
  //function to get all spots created by the user.
  @get('/created_spots_history')
  @response(200, {
    description: 'JSON Array of Spot instances created by the user',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Spot, {includeRelations: true}),
        },
      },
    },
  })
  async get_created_spots_history(@inject(SecurityBindings.USER) currentUserProfile: UserProfile,): Promise<Spot[]> {
    return this.spotRepo.find({ where: { creator: parseInt(currentUserProfile[securityId]) } });
  }
  
  //function to check if user has permission to edit/delete spot
  @get('/spots/{spotId}/editable')
  async canEditSpot(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
	@param.path.number('spotId') spotId: number,
  ): Promise<object> {
	  
	  //check if spot exists
	  let oldSpot = await this.spotRepo.findOne({ where: { id: spotId } });  
	  if(!oldSpot) return {"ERROR" : "spot_not_found", "allow_edit": false};
	  
	  //check if user has permission to edit spot
	  let hasPermission = await this.has_permission('delete_spot', parseInt(currentUserProfile[securityId]));
	  if(!hasPermission && parseInt(currentUserProfile[securityId]) != oldSpot.creator) return {"ERROR" : "not_spot_owner", "allow_edit": false};
	  
	  //check if user is owner of spot
	  return {"ERROR" : "spot_owner", "allow_edit": true};
  }
  
  
  //function to get spot rating
  @get('/spots/{spotId}/rating')
  async get_rating(
  @param.path.number('spotId') spotId: number,
  ): Promise<object> {
    let s = await (this.spotRepo.findOne({ where: { id: spotId } }));
	if(s){
	let pos = s.positive_rating;
	let neg = s.negative_rating;
	let sum = pos - neg;
	if(sum != null && sum != undefined) return {"rating":  sum };
	}
	return {"rating":  0 }
  }
  
  
  //function to check if user has certain permissions
  async has_permission(permission:string,sessionId:number): Promise<UserPermissions | null> {
	  
	  let perm = await this.permissionRepo.findOne({ where: { permission_name: permission.toLowerCase() } });  
	  if(!perm) return null;
	  
	  const hasPermission = await this.userPermissionRepo.findOne({ where: { user_id: sessionId, permission_id: perm.id}});
	  return hasPermission;
	  
  }
  
}
