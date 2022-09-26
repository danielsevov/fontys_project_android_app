import {inject, lifeCycleObserver, LifeCycleObserver} from '@loopback/core';
import {juggler} from '@loopback/repository';

const config = {
  name: 'MySQL',
  connector: 'mysql',
  url: 'mysql://prj4-loopback4:xXc6C8AFCatp4k3r@213.34.128.195:8891/prj4-loopback4',
  host: '213.34.128.195',
  port: 8891,
  user: 'prj4-loopback4',
  password: 'xXc6C8AFCatp4k3r',
  database: 'prj4-loopback4',
  connectTimeout: 200000,
  acquireTimeout: 200000 
};

// Observe application's life cycle to disconnect the datasource when
// application is stopped. This allows the application to be shut down
// gracefully. The `stop()` method is inherited from `juggler.DataSource`.
// Learn more at https://loopback.io/doc/en/lb4/Life-cycle.html
@lifeCycleObserver('datasource')
export class MySqlDataSource extends juggler.DataSource
  implements LifeCycleObserver {
  static dataSourceName = 'MySQL';
  static readonly defaultConfig = config;

  constructor(
    @inject('datasources.config.MySQL', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
