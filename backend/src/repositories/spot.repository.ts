import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {MySqlDataSource} from '../datasources';
import {Spot, SpotRelations} from '../models';

export class SpotRepository extends DefaultCrudRepository<
  Spot,
  typeof Spot.prototype.id,
  SpotRelations
> {
  constructor(
    @inject('datasources.MySQL') dataSource: MySqlDataSource,
  ) {
    super(Spot, dataSource);
  }
}
