import {Entity, model, property} from '@loopback/repository';

@model()
export class Credentials extends Entity {
  @property({
    type: 'string',
    required: true,
  })
  email: string;

  @property({
    type: 'string',
    required: true,
  })
  password: string;

  @property({
    type: 'string',
    required: true,
  })
  ip: string;


  constructor(data?: Partial<Credentials>) {
    super(data);
  }
}

export interface CredentialsRelations {
  // describe navigational properties here
}

export type credentialsWithRelations = Credentials & CredentialsRelations;
