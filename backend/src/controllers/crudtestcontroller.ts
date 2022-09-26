import {
  Count,
  CountSchema,
  Filter,
  FilterExcludingWhere,
  repository,
  Where,
} from '@loopback/repository';
import {
  post,
  param,
  get,
  getModelSchemaRef,
  patch,
  put,
  del,
  requestBody,
  response,
} from '@loopback/rest';
import {AdminLogs} from '../models';
import {AdminLogsRepository} from '../repositories';

export class CrudtestController {
  constructor(
    @repository(AdminLogsRepository)
    public adminLogsRepository : AdminLogsRepository,
  ) {}

  @post('/admin-logs')
  @response(200, {
    description: 'AdminLogs model instance',
    content: {'application/json': {schema: getModelSchemaRef(AdminLogs)}},
  })
  async create(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(AdminLogs, {
            title: 'NewAdminLogs',
            
          }),
        },
      },
    })
    adminLogs: AdminLogs,
  ): Promise<AdminLogs> {
    return this.adminLogsRepository.create(adminLogs);
  }

  @get('/admin-logs/count')
  @response(200, {
    description: 'AdminLogs model count',
    content: {'application/json': {schema: CountSchema}},
  })
  async count(
    @param.where(AdminLogs) where?: Where<AdminLogs>,
  ): Promise<Count> {
    return this.adminLogsRepository.count(where);
  }

  @get('/admin-logs')
  @response(200, {
    description: 'Array of AdminLogs model instances',
    content: {
      'application/json': {
        schema: {
          type: 'array',
          items: getModelSchemaRef(AdminLogs, {includeRelations: true}),
        },
      },
    },
  })
  async find(
    @param.filter(AdminLogs) filter?: Filter<AdminLogs>,
  ): Promise<AdminLogs[]> {
    return this.adminLogsRepository.find(filter);
  }

  @patch('/admin-logs')
  @response(200, {
    description: 'AdminLogs PATCH success count',
    content: {'application/json': {schema: CountSchema}},
  })
  async updateAll(
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(AdminLogs, {partial: true}),
        },
      },
    })
    adminLogs: AdminLogs,
    @param.where(AdminLogs) where?: Where<AdminLogs>,
  ): Promise<Count> {
    return this.adminLogsRepository.updateAll(adminLogs, where);
  }

  @get('/admin-logs/{id}')
  @response(200, {
    description: 'AdminLogs model instance',
    content: {
      'application/json': {
        schema: getModelSchemaRef(AdminLogs, {includeRelations: true}),
      },
    },
  })
  async findById(
    @param.path.number('id') id: number,
    @param.filter(AdminLogs, {exclude: 'where'}) filter?: FilterExcludingWhere<AdminLogs>
  ): Promise<AdminLogs> {
    return this.adminLogsRepository.findById(id, filter);
  }

  @patch('/admin-logs/{id}')
  @response(204, {
    description: 'AdminLogs PATCH success',
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody({
      content: {
        'application/json': {
          schema: getModelSchemaRef(AdminLogs, {partial: true}),
        },
      },
    })
    adminLogs: AdminLogs,
  ): Promise<void> {
    await this.adminLogsRepository.updateById(id, adminLogs);
  }

  @put('/admin-logs/{id}')
  @response(204, {
    description: 'AdminLogs PUT success',
  })
  async replaceById(
    @param.path.number('id') id: number,
    @requestBody() adminLogs: AdminLogs,
  ): Promise<void> {
    await this.adminLogsRepository.replaceById(id, adminLogs);
  }

  @del('/admin-logs/{id}')
  @response(204, {
    description: 'AdminLogs DELETE success',
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.adminLogsRepository.deleteById(id);
  }
}
