import {Model, model, property} from '@loopback/repository';

@model()
export class Weatherspot extends Model {
  @property({
    type: 'string',
    required: true,
  })
  query: string;
  
  @property({
    type: 'string',
  })
  forecast?: string;
  
  @property({
    type: 'string',
  })
  astro?: string;
  
  @property({
    type: 'string',
  })
  current_state?: string;


  constructor(data?: Partial<Weatherspot>) {
    super(data);
  }
}

export interface WeatherspotRelations {
  // describe navigational properties here
}

export type WeatherspotWithRelations = Weatherspot & WeatherspotRelations;
