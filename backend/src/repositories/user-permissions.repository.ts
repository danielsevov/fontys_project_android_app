import {inject} from '@loopback/core';
import {DefaultCrudRepository} from '@loopback/repository';
import {MySqlDataSource} from '../datasources';
import {UserPermissions, UserPermissionsRelations} from '../models';

export class UserPermissionsRepository extends DefaultCrudRepository<
  UserPermissions,
  typeof UserPermissions.prototype.id,
  UserPermissionsRelations
> {
  constructor(
    @inject('datasources.MySQL') dataSource: MySqlDataSource,
  ) {
    super(UserPermissions, dataSource);
  }
}
