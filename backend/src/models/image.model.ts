import {Entity, model, property} from '@loopback/repository';

@model()
export class Image extends Entity {
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
  spot_id: number;

  @property({
    type: 'string',
    required: true,
	mysql: {
        dataType: "TEXT"
    },
  })
  data: string;
  
  @property({
    type: 'string',
    required: true,
  })
  type: string;

  constructor(data?: Partial<Image>) {
    super(data);
  }
}

export interface ImageRelations {
  // describe navigational properties here
}

export type ImageWithRelations = Image & ImageRelations;
