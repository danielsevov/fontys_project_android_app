import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {MySqlDataSource} from '../datasources';
import {AdminLogs, AdminLogsRelations} from '../models';

export class AdminLogsRepository extends DefaultCrudRepository<
  AdminLogs,
  typeof AdminLogs.prototype.id,
  AdminLogsRelations
> {
  constructor(
    @inject('datasources.MySQL') dataSource: MySqlDataSource,
  ) {
    super(AdminLogs, dataSource);
  }
}
