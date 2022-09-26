import {inject, Provider} from '@loopback/core';
import {getService} from '@loopback/service-proxy';
import {WeatherDataSource} from '../datasources';

export interface Weather {
  // this is where you define the Node.js methods that will be
  // mapped to REST/SOAP/gRPC operations as stated in the datasource
  // json file.
  getForecast(query: string): Promise<string>;
  getCurrentState(query: string): Promise<string>;
}

export class WeatherProvider implements Provider<Weather> {
  constructor(
    // weather must match the name property in the datasource json file
    @inject('datasources.weather')
    protected dataSource: WeatherDataSource = new WeatherDataSource(),
  ) {}

  value(): Promise<Weather> {
    return getService(this.dataSource);
  }
}
