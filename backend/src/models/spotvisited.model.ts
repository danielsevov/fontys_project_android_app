import {Entity, model, property} from '@loopback/repository';

@model()
export class Spotvisited extends Entity {
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
  user_identifier: number;

  @property({
    type: 'number',
    required: true,
  })
  spot_id: number;

  @property({
    type: 'string', 
  })
  last_visit_date: string;


  constructor(data?: Partial<Spotvisited>) {
    super(data);
  }
}

export interface SpotvisitedRelations {
  // describe navigational properties here
}

export type SpotvisitedWithRelations = Spotvisited & SpotvisitedRelations;
