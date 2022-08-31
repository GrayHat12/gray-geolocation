import { registerPlugin } from '@capacitor/core';

import type { GrayGeolocationPlugin } from './definitions';

const GrayGeolocation = registerPlugin<GrayGeolocationPlugin>(
  'GrayGeolocation',
  {
    web: () => import('./web').then(m => new m.GrayGeolocationWeb()),
  },
);

export * from './definitions';
export { GrayGeolocation };
