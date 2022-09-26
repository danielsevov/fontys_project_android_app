import {Entity, model, property} from '@loopback/repository';

@model({settings: {strict: false}})
export class Spot extends Entity {
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
  title: string;

  @property({
    type: 'string',
    required: true,
  })
  coordinates: string;

  @property({
    type: 'string',
  })
  category?: string;
  
  @property({
    type: 'number'
  })
  range: number; 

  @property({
    type: 'string',
  })
  date_created?: string;

  @property({
    type: 'string',
  })
  date_modified?: string;

  @property({
    type: 'number',
    required: true,
  })
  creator: number;
  
  @property.array(String)
  tags?: string[];
  
  @property({
    type: 'number',
	default: 0
  })
  positive_rating: number;
  
  @property({
    type: 'number',
	default: 0
  })
  negative_rating: number;

  // Define well-known properties here

  // Indexer property to allow additional data
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [prop: string]: any;

  constructor(data?: Partial<Spot>) {
    super(data);
  }
}

export interface SpotRelations {
  // describe navigational properties here
}

export type SpotWithRelations = Spot & SpotRelations;
