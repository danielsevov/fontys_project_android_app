// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { repository, Filter, } from '@loopback/repository';
import { BansRepository, AdminLogsRepository, PermissionRepository, UserPermissionsRepository } from '../repositories';
import { Bans, AdminLogs, UserPermissions, Permission } from '../models';
import { genSalt, hash, compare} from 'bcryptjs';
import { HttpErrors, post, get, del, patch, requestBody, param, getModelSchemaRef, response } from '@loopback/rest';
import { TokenService } from '@loopback/authentication';
import { TokenServiceBindings, UserServiceBindings, MyUserService, Credentials, } from '@loopback/authentication-jwt';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';
import { authenticate } from '@loopback/authentication';

@authenticate('jwt')
export class BansController {
  constructor(
	@repository(BansRepository) protected repos: BansRepository,
	@repository(AdminLogsRepository) protected adminRepo : AdminLogsRepository,
	@repository(PermissionRepository) protected permissionRepo : PermissionRepository,
	@repository(UserPermissionsRepository) protected userPermissionRepo : UserPermissionsRepository,
  ) {}
  
  @authenticate.skip()
  @get('/bans')
  @response(200, {
    description: 'Array of Bans model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Bans, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @param.filter(Bans) filter?: Filter<Bans>,
  ): Promise<Bans[]> {
    return this.repos.find(filter);
  }
  
  @authenticate.skip()
  @get('/bans/{id}')
  async getBanById(
  @param.path.number('id') banId: number
  ) {
	let ban = await this.repos.findOne({ where: { id: banId } });  
	if(!ban) return {"ERROR": "ban_not_found"};
	
	return ban;
  }
  
  @del('/bans/{id}')
  async deleteBan(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('id') banId: number
  ) {
	let hasPermission = await this.has_permission('delete_ban', parseInt(currentUserProfile[securityId]));
	if(!hasPermission) return {"ERROR": "no_permission", "permission": 'delete_ban'};
	
	let ban = await this.repos.findOne({ where: { id: banId } });  
	if(!ban) return {"ERROR": "ban_not_found"};
	
	let log = new AdminLogs();
	log.execution_date = new Date().getTime().toString();
	log.by = parseInt(currentUserProfile[securityId]);
	log.action = "DELETE BAN {'id':'"+banId+"','identifier':'"+ban.identifier+"','reason':'"+ban.reason+"','by':'"+ban.by+"','date_created':'"+ban.date_created+"','date_expire':'"+ban.date_expire+"'}";
	await this.adminRepo.create(log);
	
	await this.repos.deleteById(banId);
	return {'SUCCES':'ban_removed_'+ banId + ''};
  }
  
  @patch('/bans/{id}')
  async updateBanById(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('id') banId: number,
  @requestBody(
		{
        description: 'Required input for updating a ban',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              properties: {
				date_expire:{
                  type: 'string',
                },
				reason:{
                  type: 'string',
                }
              },
            },
          }
        },
      }) new_ban: Bans,
  ) {
	  let ban = await this.repos.findOne({ where: { id: banId } });  
	  if(!ban) return {"ERROR": "ban_not_found"};
	  
	  let hasPermission = await this.has_permission('ban_' + ban.type, parseInt(currentUserProfile[securityId]));
	  if(!hasPermission) return {"ERROR": "no_permission", "permission": 'ban_' + ban.type};
	  
	  let log = new AdminLogs();
	  log.execution_date = new Date().getTime().toString();
	  log.by = parseInt(currentUserProfile[securityId]);
	  log.action = "UPDATE BAN {"+
		  "'id':'"+banId+"',"+
		  "'identifier':'"+ban.identifier+"',"+
		  "'reason_old':'"+ban.reason+"',"+
		  "'reason':'"+new_ban.reason+"',"+
		  "'by':'"+ban.by+"',"+
		  "'date_created':'"+ban.date_created+"',"+
		  "'date_expire_old':'"+ban.date_expire+"',"+
		  "'date_expire':'"+new_ban.date_expire+"',"+
		  "}";
	  await this.adminRepo.create(log);
	  
	  if(new_ban.date_expire) ban.date_expire = new_ban.date_expire;
	  if(new_ban.reason) ban.reason = new_ban.reason;
	  
	  await this.repos.updateById(banId, ban);
	  return {"SUCCES": "ban_updated"};
  }
  
  @post('/bans')
  async registerBan(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @requestBody(
		{
        description: 'Required input for registering a ban',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['identifier', 'type', 'date_expire', 'reason'],
              properties: {
                identifier: {
                  type: 'string',
                },
                type: {
                  type: 'string',
                },
				date_expire:{
                  type: 'string',
                },
				reason:{
                  type: 'string',
                }
              },
            },
          }
        },
      }) ban: Bans) {
		  ban.date_created = new Date().getTime().toString();
		  ban.by = parseInt(currentUserProfile[securityId]);
		  ban.type = ban.type.toUpperCase();
		  
		  let hasPermission = await this.has_permission('ban_' + ban.type, parseInt(currentUserProfile[securityId]));
		  if(!hasPermission) return {"ERROR": "no_permission", "permission": 'ban_' + ban.type};
		  
		  if(ban.type != "IP" && ban.type != "USER") return {"ERROR": "ban_type_incorrect"};
		  await this.repos.create(ban);
		  
		  let log = new AdminLogs();
		  log.execution_date = new Date().getTime().toString();
		  log.by = parseInt(currentUserProfile[securityId]);
		  log.action = "CREATE BAN {'id':'"+ban.id+"','identifier':'"+ban.identifier+"','reason':'"+ban.reason+"','by':'"+ban.by+"','date_created':'"+ban.date_created+"','date_expire':'"+ban.date_expire+"'}";
		  await this.adminRepo.create(log);

		  return {"SUCCES": "ban_" + ban.type + "_succes_" + ban.id +""};
  }  
  
  @get('/admin_logs')
  @response(200, {
    description: 'JSON Array of Spot instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(AdminLogs, {includeRelations: true}),
        },
      },
    },
  })
  async get_logs(): Promise<AdminLogs[]> {
    return this.adminRepo.find();
  }
  
  async has_permission(permission:string,sessionId:number): Promise<UserPermissions | null> {
	  
	  let perm = await this.permissionRepo.findOne({ where: { permission_name: permission.toLowerCase() } });  
	  if(!perm) return null;
	  
	  const hasPermission = await this.userPermissionRepo.findOne({ where: { user_id: sessionId, permission_id: perm.id}});
	  return hasPermission;
	  
  }
  
}
