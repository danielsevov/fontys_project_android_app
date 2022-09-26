// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { repository, Filter, } from '@loopback/repository';
import { AdminLogsRepository, UserRepository } from '../repositories';
import { AdminLogs, User } from '../models';
import { genSalt, hash, compare} from 'bcryptjs';
import { HttpErrors, post, get, del, patch, requestBody, param, getModelSchemaRef, response } from '@loopback/rest';
import { TokenService } from '@loopback/authentication';
import { TokenServiceBindings, UserServiceBindings, MyUserService, Credentials, } from '@loopback/authentication-jwt';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';
import { authenticate } from '@loopback/authentication';

@authenticate('jwt')
export class UserController {
  constructor(
  @repository(AdminLogsRepository) protected adminRepo : AdminLogsRepository,
  @repository(UserRepository) protected userRepo : UserRepository,
  ) {}
  
  @get('/users/{userId}')
  async getPermissionsForUser(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  @param.path.number('userId') userId: number,
  ) {
  
	let userData = await this.userRepo.findOne({ where: { id: userId } });  
	if(!userData) return {"ERROR": "user_not_found"};
	
	return userData;
  }
  
  @get('/user')
  async getCurrentUser(
  @inject(SecurityBindings.USER) currentUserProfile: UserProfile,
  ) {
	return this.userRepo.findOne({ where: { id: parseInt(currentUserProfile[securityId]) } });
  }
  
  @post('/username')
  async get_username(@requestBody(
      {
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
      }) user: User): Promise<object> {
    let u = await (this.userRepo.findOne({ where: { id: user.id } }));
	let username = (u) ? u.username : "username not found";
	return {"response": username};
  }
  
}