import {Entity, model, property} from '@loopback/repository';

@model()
export class AdminLogs extends Entity {
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
  execution_date: string;

  @property({
    type: 'number',
    required: true,
  })
  by: number;

  @property({
    type: 'string',
    required: true,
	mysql: {
        dataType: "TEXT"
    },
  })
  action: string;


  constructor(data?: Partial<AdminLogs>) {
    super(data);
  }
}

export interface AdminLogsRelations {
  // describe navigational properties here
}

export type AdminLogsWithRelations = AdminLogs & AdminLogsRelations;
