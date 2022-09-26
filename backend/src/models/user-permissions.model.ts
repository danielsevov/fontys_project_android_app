import {Entity, model, property} from '@loopback/repository';

@model()
export class UserPermissions extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'number',
    required: true,
  })
  user_id: number;

  @property({
    type: 'number',
    required: true,
  })
  permission_id: number;


  constructor(data?: Partial<UserPermissions>) {
    super(data);
  }
}

export interface UserPermissionsRelations {
  // describe navigational properties here
}

export type UserPermissionsWithRelations = UserPermissions & UserPermissionsRelations;
