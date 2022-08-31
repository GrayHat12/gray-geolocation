export interface GrayGeolocationPlugin {
  /**
   * This prompts the user on android to turn location on.
   * Firtstly checks for location permissions and if they are not granted, it prompts the user to grant permissions.
   * Always returns `{res: true}` for web.
   * Not implemented on iOS.
   */
  turnLocationOn(): Promise<{ res: boolean }>;
  
  /**
   * Returns current position of the device.
   * Firtstly checks for location permissions and if they are not granted, it prompts the user to grant permissions.
   * Not implemented on iOS.
   */
  getCurrentPosition(): Promise<{ latitude: number; longitude: number }>;
}
