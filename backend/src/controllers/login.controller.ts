// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { repository } from '@loopback/repository';
import { UserRepository, BansRepository } from '../repositories';
import { User, Bans, Credentials } from '../models';
import { genSalt, hash, compare } from 'bcryptjs';
import { HttpErrors, post, requestBody } from '@loopback/rest';
import { TokenService } from '@loopback/authentication';
import { TokenServiceBindings, UserServiceBindings, MyUserService, } from '@loopback/authentication-jwt';
import { SecurityBindings, securityId, UserProfile } from '@loopback/security';

export class LoginController {
  constructor(
		@repository(UserRepository) protected userRepo: UserRepository,
		@repository(BansRepository) protected bansRepo: BansRepository,
		@inject(TokenServiceBindings.TOKEN_SERVICE) public jwtService: TokenService,  
		@inject(SecurityBindings.USER, {optional: true}) public user: UserProfile,
		@inject(UserServiceBindings.USER_SERVICE) public userService: MyUserService,
	) {}


@post('/signin')
async login(@requestBody(
      {
        description: 'Required input for login',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['email', 'password', 'ip'],
              properties: {
                password: {
                  type: 'string',
                },
				email:{
                  type: 'string',
                },
				ip:{
                  type: 'string',
                }
              },
            },
          }
        },
      }) credentials: Credentials) {

	let findUser = await this.userRepo.findOne({
	  where: {
		email: credentials.email
	  },
	  order: ["id DESC"]
	});
	
	let findUserBan = await this.bansRepo.findOne({
	  where: {
		identifier: credentials.email,
		type: "USER"
	  },
	});
	let ipBans = await this.bansRepo.find({
	  where: {
		type: "IP"
	  },
	});
	if(findUserBan) return {"ERROR": "user_ban", "type": "USER", "expire": findUserBan.date_expire, "reason": findUserBan.reason};
	for (var ip_hashed of ipBans) {
		const ipCheck = await compare(credentials.ip, ip_hashed.identifier);
		if(ipCheck) return {"ERROR": "ip_ban", "type": "IP", "expire": ip_hashed.date_expire, "reason": ip_hashed.reason};
	}
	
	if(!findUser) return { 'ERROR': 'email_not_found' };
	
	const passCheck = await compare(credentials.password, findUser.password);
	if(!passCheck) return { 'ERROR': 'password_incorrect' };
	
	findUser.ip_last = await hash(credentials.ip, await genSalt());
	await this.userRepo.updateById(findUser.id, findUser);
	
	const token = await this.jwtService.generateToken(this.convertToUserProfile(findUser));
    return { 'jwt_token': token };

  }
  
convertToUserProfile(user: User): UserProfile {
	return {
			[securityId]: user.id.toString(),
			name: user.username,
			id: user.id,
			email: user.email,
		};
	}
}
