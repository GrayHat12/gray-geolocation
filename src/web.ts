/* eslint-disable */
import { WebPlugin } from '@capacitor/core';
import { registerWebPlugin } from '@capacitor/core';
import { GrayGeolocationPlugin } from './definitions';

export class GrayGeolocationWeb extends WebPlugin implements GrayGeolocationPlugin {
  constructor() {
    super({
      name: 'GrayGeolocation',
      platforms: ['web'],
    });
  }

  async turnLocationOn(): Promise<{res:boolean}> {
    return {res:true};
  }

}
const GrayGeolocation = new GrayGeolocationWeb();

export { GrayGeolocation };

registerWebPlugin(GrayGeolocation);