// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { repository, Filter, } from '@loopback/repository';
import { AdminLogsRepository, PermissionRepository, UserPermissionsRepository } from '../repositories';
import { AdminLogs, UserPermissions, Permission } from '../models';
import { genSalt, hash, compare} from 'bcryptjs';
import { HttpErrors, post, get, del, patch, requestBody, param, getModelSchemaRef, response } from '@loopback/rest';
import { TokenService } from '@loopback/authentication';
import { TokenServiceBindings, UserServiceBindings, MyUserService, Credentials, } from '@loopback/authentication-jwt';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';
import { authenticate } from '@loopback/authentication';

@authenticate('jwt')
export class PermissionControllerController {
  constructor(
  @repository(AdminLogsRepository) protected adminRepo : AdminLogsRepository,
  @repository(PermissionRepository) protected permissionRepo : PermissionRepository,
  @repository(UserPermissionsRepository) protected userPermissionRepo : UserPermissionsRepository,
  ) {}
  
  @post('/permissions/{userId}/{permissionName}')
  async registerPermission(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('userId') userId: number,
  @param.path.string('permissionName') permissionName: string,
  ) {
	  
	let hasPermission = await this.has_permission('assign_permission', parseInt(currentUserProfile[securityId]));
    if(!hasPermission) return {"ERROR": "no_permission", "permission": 'assign_permission'};
	  
	const perm = await this.permissionRepo.findOne({ where: { permission_name: permissionName.toLowerCase() } });  
	if(!perm) return {'ERROR': 'permission_not_found', 'permission':''+permissionName.toLowerCase()+''};
	  
	//TODO: Test if userID is valid!
	  
	let log = new AdminLogs();
		log.execution_date = new Date().getTime().toString();
		log.by = parseInt(currentUserProfile[securityId]);
		log.action = "ASSIGN PERMISSIONS {"+
		"'id':'"+userId+"',"+
		"'permission':'"+permissionName+"',"+
		"}";
	await this.adminRepo.create(log);
	
	let userPermission = new UserPermissions();
	userPermission.user_id = userId;
	userPermission.permission_id = perm.id || 0;
	
	await this.userPermissionRepo.create(userPermission);
	return {"SUCCES": "permission_added_succes", "for":"" + userId +"", "permission": ""+permissionName+""};
  }
  
  @del('/permissions/{userId}/{permissionName}')
  async deletePermission(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('userId') userId: number,
  @param.path.string('permissionName') permissionName: string,
  ) {
	let hasPermission = await this.has_permission('delete_permission', parseInt(currentUserProfile[securityId]));
    if(!hasPermission) return {"ERROR": "no_permission", "permission": 'delete_permission'};
	
	let perm = await this.permissionRepo.findOne({ where: { permission_name: permissionName.toLowerCase() } });  
	if(!perm) return {'ERROR': 'permission_not_found', 'permission':''+permissionName.toLowerCase()+''};
	  
	let usersPerms = await this.userPermissionRepo.findOne({ where: { user_id: userId, permission_id: perm.id } });  
	if(!usersPerms) return {"ERROR": "user_does_not_have_permission"};
	
	let log = new AdminLogs();
	  log.execution_date = new Date().getTime().toString();
	  log.by = parseInt(currentUserProfile[securityId]);
	  log.action = "DELETE PERMISSIONS {"+
		  "'id':'"+userId+"',"+
		  "'permission':'"+permissionName+"',"+
		  "}";
	  await this.adminRepo.create(log);
	
	await this.userPermissionRepo.deleteById(usersPerms.id);
	return {'SUCCES':'permission_removed', 'permission':''+permissionName+'', 'for': ''+userId+''};
  }
  
  @get('/permissions/{userId}')
  async getPermissionsForUser(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('userId') userId: number,
  ) {
	let hasPermission = await this.has_permission('view_permissions', parseInt(currentUserProfile[securityId]));
    if(!hasPermission) return {'ERROR': 'no_permission', 'permission': 'view_permissions'};
	
	let log = new AdminLogs();
	  log.execution_date = new Date().getTime().toString();
	  log.by = parseInt(currentUserProfile[securityId]);
	  log.action = "VIEW PERMISSIONS {"+
		  "'for':'"+userId+"',"+
		  "}";
	  await this.adminRepo.create(log);
  
	let userPermissions = await this.userPermissionRepo.find({ where: { user_id: userId } });  
	if(!userPermissions) return {"ERROR": "user_no_permissions"};
	
	var permissions = new Array();
	for(var userPerm of userPermissions) {
		const permCheck = await this.permissionRepo.findOne({ where: { id: userPerm.permission_id }});
		if(!permCheck) continue;
		
		permissions.push(permCheck);
	}
	
	return permissions;
  }
  
  @get('/permissions')
  @response(200, {
    description: 'Array of Permission model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(Permission, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
    @param.filter(Permission) filter?: Filter<Permission>,
  ): Promise<Permission[]> {
    return this.permissionRepo.find(filter);
  }
  
  async has_permission(permission:string,sessionId:number): Promise<UserPermissions | null> {
	  
	  let perm = await this.permissionRepo.findOne({ where: { permission_name: permission.toLowerCase() } });  
	  if(!perm) return null;
	  
	  const hasPermission = await this.userPermissionRepo.findOne({ where: { user_id: sessionId, permission_id: perm.id}});
	  return hasPermission;
	  
  }
  
}
