import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {MySqlDataSource} from '../datasources';
import {Bans, BansRelations} from '../models';

export class BansRepository extends DefaultCrudRepository<
  Bans,
  typeof Bans.prototype.id,
  BansRelations
> {
  constructor(
    @inject('datasources.MySQL') dataSource: MySqlDataSource,
  ) {
    super(Bans, dataSource);
  }
}
