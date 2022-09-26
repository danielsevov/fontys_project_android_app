import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {MySqlDataSource} from '../datasources';
import {Spotvisited, SpotvisitedRelations} from '../models';

export class SpotvisitedRepository extends DefaultCrudRepository<
  Spotvisited,
  typeof Spotvisited.prototype.id,
  SpotvisitedRelations
> {
  constructor(
    @inject('datasources.MySQL') dataSource: MySqlDataSource,
  ) {
    super(Spotvisited, dataSource);
  }
}
