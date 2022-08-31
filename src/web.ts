import { WebPlugin } from '@capacitor/core';

import type { GrayGeolocationPlugin } from './definitions';

export class GrayGeolocationWeb
  extends WebPlugin
  implements GrayGeolocationPlugin
{
  getCurrentPosition(): Promise<{ latitude: number; longitude: number }> {
    return new Promise((resolve, reject) => {
      window.navigator.geolocation.getCurrentPosition(
        pos =>
          resolve({
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          }),
        err => reject(err),
      );
    });
  }

  async turnLocationOn(): Promise<{ res: boolean }> {
    return { res: true };
  }
}
