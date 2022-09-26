import {Entity, model, property} from '@loopback/repository';

@model()
export class Bans extends Entity {
  @property({
    type: 'number',
    id: true,
    generated: true,
  })
  id?: number;

  @property({
    type: 'string',
    required: true,
  })
  identifier: string;

  @property({
    type: 'string',
    required: true,
  })
  type: string;

  @property({
    type: 'string',
    required: true,
  })
  date_created: string;
  
  @property({
    type: 'number',
    required: true,
  })
  by: number;
  
  @property({
    type: 'string',
    required: true,
  })
  date_expire: string;
  
  @property({
    type: 'string',
    required: true,
  })
  reason: string;


  constructor(data?: Partial<Bans>) {
    super(data);
  }
}

export interface BansRelations {
  // describe navigational properties here
}

export type BansWithRelations = Bans & BansRelations;
