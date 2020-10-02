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

  getCurrentPosition(): Promise<{ latitude: number, longitude: number }> {
    return new Promise((resolve, reject) => {
      window.navigator.geolocation.getCurrentPosition(pos => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }), err => reject(err));
    });
  }

  async turnLocationOn(): Promise<{ res: boolean }> {
    return { res: true };
  }

}
const GrayGeolocation = new GrayGeolocationWeb();

export { GrayGeolocation };

registerWebPlugin(GrayGeolocation);