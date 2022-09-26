// Uncomment these imports to begin using these cool features!

 import {inject} from '@loopback/core';
 import { HttpErrors, post, get, del, patch, requestBody, param, getModelSchemaRef, response } from '@loopback/rest';
 import { Weather} from '../services';
 import { Weatherspot } from '../models';
 import {authenticate} from '@loopback/authentication';

@authenticate('jwt')
export class WeatherController {
  constructor(@inject('services.Weather') protected weatherService: Weather,) {}
  
  
  //function for getting the forecast information for a specific location ( query has to be either name, postcode or coordinates )
  //this includes date, timestamp, astrological details, and daily weather information such as temperatures, sun hours and snow cover.
  @post('/forecast')
  @response(200, {
    description: 'Forecast.',
  })
  async get_forecast(
    @requestBody(
      {
        description: 'Required input for getting forecast detials: location name or coordinates.',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['query'],
              properties: {
                query: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) requestBody: Weatherspot) {
	
	//get forecast response object
    let myobj = (Object) (await this.weatherService.getForecast(requestBody.query));
	if( myobj.forecast === undefined) return requestBody;
	
	//create date string
	var todayDate = new Date();
	todayDate.setDate(todayDate.getDate() - 1);
	var dateString = todayDate.toISOString().slice(0, 10);
	
	//attatch forecast object to response
	requestBody.forecast = myobj.forecast[dateString];
	return requestBody;
  }
  
  
  //function for getting the astrological information for a specific location ( query has to be either name, postcode or coordinates )
  //this includes information about sun- and moon- rises and sets and phases of the moon
  @post('/astro')
  @response(200, {
    description: 'Astrological details.',
  })
  async get_astro(
    @requestBody(
      {
        description: 'Required input for getting astrological detials: location name or coordinates.',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['query'],
              properties: {
                query: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) requestBody: Weatherspot) {
    
	//get forecast response object
    let myobj = (Object) (await this.weatherService.getForecast(requestBody.query));
	if( myobj.forecast === undefined) return requestBody;
	
	//create date string
	var todayDate = new Date();
	todayDate.setDate(todayDate.getDate() - 1);
	var dateString = todayDate.toISOString().slice(0, 10);
	
	//attatch astrological details object to response 
	requestBody.astro = myobj.forecast[dateString]['astro'];
	return requestBody;
  }
  
  
  //function for getting the current weather information for a specific location ( query has to be either name, postcode or coordinates )
  //this includes temperatures, humidity, wind speed, air pressure, visibility, cloud cover, etc.
  @post('/current')
  @response(200, {
    description: 'Current weather state details.',
  })
  async get_current_sttae(
    @requestBody(
      {
        description: 'Required input for getting current weather state detials: location name or coordinates.',
        required: true,
        content: {
          'application/json': {
            schema: {
              type: 'object',
              required: ['query'],
              properties: {
                query: {
                  type: 'string',
                }
              },
            },
          }
        },
      }) requestBody: Weatherspot) {
		  
	//get current weather state response object
    let myobj = (Object) (await this.weatherService.getCurrentState(requestBody.query));
	if( myobj.current === undefined) return requestBody;
	
	//attatch current state object to response 
	requestBody.current_state = myobj.current;
	return requestBody;
  }
}
