// Uncomment these imports to begin using these cool features!

import {inject} from '@loopback/core';
import { Filter, repository, FilterBuilder } from '@loopback/repository';
import { SpotvisitedRepository, SpotRepository } from '../repositories';
import { Spot, Check, Spotvisited } from '../models';
import { del, get, getModelSchemaRef, HttpErrors, param, patch, post, requestBody, response } from '@loopback/rest';
import {authenticate} from '@loopback/authentication';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';
import { FirebaseCloudMessaging } from '../models'

@authenticate('jwt')
export class SpotVisitedController {
  constructor(@repository(SpotRepository) protected spotRepo: SpotRepository, @repository(SpotvisitedRepository) protected spotVisitedRepo: SpotvisitedRepository) {}
  

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
  
  
  //function to check if user is in the range of a certain spot
  //coordinates have to be in DD format (ex. '-40.2234,70.1933')
  @post('/is_spot_nearby_user')
  @response(200, {
    description: 'Boolean indicating whether the user is in range of a specific location.',
  })
  async is_spot_nearby_user(@requestBody(
      {
        description: 'Required input for checking whether the user is in range of a specific location.',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['user_coordinates', 'spot_id'],
              properties: {
                user_coordinates: {
                  type: 'string',
                },
				spot_id: {
                  type: 'number',
                }
              },
            },
          }
        },
      }) requestBody: Check): Promise<object> {
		  
    //create filter object
    const filterBuilder = new FilterBuilder();
	const filter = filterBuilder
    .where({id: requestBody.spot_id})
    .build();
	
	//check if user is in range of spot
	let check = false;
    (await this.spotRepo.find(filter)).forEach(spot => {
		if(spot.coordinates != undefined && this.isInRange( requestBody.user_coordinates, spot.coordinates, spot.range)){
			check = true;
		}
	});
	return {"response" : check};
  }
  
  @authenticate.skip()
  @post('/is_user_nearby_spot')
  @response(200, {
    description: 'Boolean indicating whether the user is in range of a location.',
  })
  async is_user_nearby_spot(@requestBody(
      {
        description: 'Required input for checking whether the user is in range of a location.',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['user_token','user_coordinates'],
              properties: {
                user_coordinates: {
                  type: 'string',
                },
				user_token: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) requestBody: Check): Promise<object> {
		  
    //create filter object
    const filterBuilder = new FilterBuilder();
	const filter = filterBuilder
    .where({id: requestBody.spot_id})
    .build();
	
	//check if user is in range of spot
	let check = false;
	let spot = new Spot();
	spot.title= "UNKNOWN_SPOT_93829204";
	
    (await this.spotRepo.find()).forEach(s => {
		if(s.coordinates != undefined && this.isInRange( requestBody.user_coordinates, s.coordinates, s.range)){
			check = true;
			spot = s;
		}
	});

	if(check) {
		FirebaseCloudMessaging.getInstance().sendMessageVisited(requestBody.user_token, spot.title, spot.id);
	}
	
	return {};
	
  }
  
  
  //function to mark spot as visited
  @post('/visit_spot/{id}')
  async visit_spot(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
	@param.path.number('id') spotId: number) {
	
    let visited_spot = new Spotvisited();
	console.log("Spot visit received!");
	
	//check if spot exists 
    let spot = await this.spotRepo.findOne({ where: { id: spotId } });  
	if(!spot) return visited_spot;
	
	console.log("Spot exisist!");
	
	//prepare visit object 
	visited_spot.spot_id = spotId;
    visited_spot.last_visit_date = new Date().getTime().toString();
	visited_spot.user_identifier = parseInt(currentUserProfile[securityId]);
	
	console.log("Spot prepared!");
	
	//create visit 
	visited_spot = await this.spotVisitedRepo.create(visited_spot);
	
	console.log("Returning the thing");
	return visited_spot;
  }
  
  
  //function to get all spot visits made by a certain user for a certain spot
  @get('/visits_history/{id}')
  @response(200, {
    description: 'JSON Array of Visit instances made by a certain user for a certain spot',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Spotvisited, {includeRelations: true}),
        },
      },
    },
  })
  async get_visit_history_for_spot(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.path.number('id') spotId: number): Promise<Spotvisited[]> {
    return this.spotVisitedRepo.find({ order: ['last_visit_date DESC'] , where: { spot_id: spotId, user_identifier: parseInt(currentUserProfile[securityId]) } });
  }
  
  
  //function to get all spot visits
  @get('/visits')
  @response(200, {
    description: 'JSON Array of Visit instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Spotvisited, {includeRelations: true}),
        },
      },
    },
  })
  async get_spot(): Promise<Spotvisited[]> {
    return this.spotVisitedRepo.find();
  }
  
  
  //function to get all spot visits made by a certain user
  @get('/visits_history')
  @response(200, {
    description: 'JSON Array of Visit instances made by a certain user',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Spotvisited, {includeRelations: true}),
        },
      },
    },
  })
  async get_visit_history(@inject(SecurityBindings.USER) currentUserProfile: UserProfile,): Promise<Spotvisited[]> {
    return this.spotVisitedRepo.find({ where: { user_identifier: parseInt(currentUserProfile[securityId]) } });
  }
}
