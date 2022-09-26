// Uncomment these imports to begin using these cool features!

import { inject } from '@loopback/core';
import { repository } from '@loopback/repository';
import { UserRepository, BansRepository } from '../repositories';
import { User, Bans } from '../models';
import { genSalt, hash } from 'bcryptjs';
import { HttpErrors, post, requestBody } from '@loopback/rest';


export class SignupController {
  reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
  constructor( 
  @repository(UserRepository) protected userRepo: UserRepository, 
  @repository(BansRepository) protected bansRepo: BansRepository,
  ) {}

@post('/signup')
async signup(@requestBody(
      {
        description: 'Required input for registration',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['email', 'password', 'username', 'ip_reg'],
              properties: {
                username: {
                  type: 'string',
                },
                password: {
                  type: 'string',
                },
		email:{
                  type: 'string',
                },
                ip_reg: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) user: User) {
if (!user.email) {
      return { 'ERROR': 'email_required' };
    } else if (!this.reg.test(user.email)) {
      return { 'ERROR': 'invalid_email' };
    }
    if (!user.password) {
      return { 'ERROR': 'password_required' };
    }	
    if (!user.ip_reg) {
	  return { 'ERROR': 'reg_ip_required' };
    }
	user.ip_reg = await hash(user.ip_reg, await genSalt());
    user.ip_last = user.ip_reg;

    const password = await hash(user.password, await genSalt());
    user.password = password;    

    if (!user.username) {
      return { 'ERROR': 'name_required' };
    }

    const checkEmail = await this.userRepo.findOne({ where: { email: user.email } });
    if (!checkEmail) {
      await this.userRepo.create(user);
      return { 'SUCCES': 'account_created' };
    } else {
      return { 'ERROR': 'email_already_exists' };
    }
  }


}
