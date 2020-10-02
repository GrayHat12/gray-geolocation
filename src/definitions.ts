/* eslint-disable */
declare module '@capacitor/core' {
  interface PluginRegistry {
    GrayGeolocation: GrayGeolocationPlugin;
  }
}

export interface GrayGeolocationPlugin {
  turnLocationOn(): Promise<{ res: boolean }>;
  getCurrentPosition(): Promise<{ latitude: number, longitude: number }>;
}