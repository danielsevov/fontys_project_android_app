import {Model, model, property} from '@loopback/repository';

@model()
export class Check extends Model {
  @property({
    type: 'string',
    required: true,
  })
  user_coordinates: string;

  @property({
    type: 'number',
    required: true,
  })
  spot_id: number;
  
  @property({
    type: 'string',
    required: true,
  })
  user_token: string;


  constructor(data?: Partial<Check>) {
    super(data);
  }
}

export interface CheckRelations {
  // describe navigational properties here
}

export type CheckWithRelations = Check & CheckRelations;
