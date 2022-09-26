import {inject, lifeCycleObserver, LifeCycleObserver} from '@loopback/core';
import {juggler} from '@loopback/repository';

const config = {
  name: 'weather',
  connector: 'rest',
  baseURL: '',
  crud: false,
  options: {
    headers: {
      accept: 'application/json',
      'content-type': 'application/json',
    },
  },
  operations: [
    {
      template: {
        method: 'GET',
        url: 'http://api.weatherstack.com/forecast',
         query: {
           format: '{format=json}',
           access_key: '3df83dacf6244e4689a8dbf3f0ce124c',
           query: '{query}',
         },
      },
      functions: {
        getForecast: ['query'],
      },
    },
	
	{
      template: {
        method: 'GET',
        url: 'http://api.weatherstack.com/current',
         query: {
           format: '{format=json}',
           access_key: '3df83dacf6244e4689a8dbf3f0ce124c',
           query: '{query}',
         },
      },
      functions: {
        getCurrentState: ['query'],
      },
    },
	
  ],
}

// Observe application's life cycle to disconnect the datasource when
// application is stopped. This allows the application to be shut down
// gracefully. The `stop()` method is inherited from `juggler.DataSource`.
// Learn more at https://loopback.io/doc/en/lb4/Life-cycle.html
@lifeCycleObserver('datasource')
export class WeatherDataSource extends juggler.DataSource
  implements LifeCycleObserver {
  static dataSourceName = 'weather';
  static readonly defaultConfig = config;

  constructor(
    @inject('datasources.config.weather', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
