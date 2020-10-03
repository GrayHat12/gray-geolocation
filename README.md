# capacitor-gray-geolocation

<p align="center">
    <a href="https://www.npmjs.com/package/capacitor-gray-geolocation">
        <img src="https://badge.fury.io/js/capacitor-gray-geolocation.svg" alt="npm version" />
    </a>
        <a href="https://snyk.io/test/npm/capacitor-gray-geolocation">
            <img src="https://snyk.io/test/npm/capacitor-gray-geolocation/badge.svg" alt="Known Vulnerabilities" />
        </a>
    <a href="https://opensource.org/licenses/MIT">
        <img src="https://img.shields.io/badge/License-MIT-GREEN.svg" alt="License" />
    </a>
</p>

## Description

Geolocation plugin that uses the fused location service instead of the native API.

Getting a location under android is quite difficult. The standard API implemented now in capacitor returns the GPS provider which results in never getting a position indoors. This is not the case under iOS. A better way under Android is the [FusedLocationProvider](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) which already handles that.

This Plugin also has method calls to turn of location.


## Supported platforms

- Android

## Android setup

- `npm i capacitor-gray-geolocation`
- `ionic build`
- `npx cap copy`
- `npx cap sync`
- `npx cap open android`
- `[extra step]` in android case we need to tell Capacitor to initialise the plugin:

> on your `MainActivity.java` file add `import com.grayhat.geolocation.GrayGeolocation;` and then inside the init callback `add(GrayGeolocation.class);`

Now you should be set to go. Try to run your client using `npx cap open android`.

> Tip: every time you change a native code you may need to clean up the cache (Build > Clean Project | Build > Rebuild Project) and then run the app again.

## Example

```js
import { Plugins } from "@capacitor/core";
import "capacitor-gray-geolocation";

const { GrayGeolocation } = Plugins;

// ...code

//function to get location (handle errors yourself) 
getLocation = async() => {
    let deviceInfo = await Plugins.Device.getInfo();
    if(deviceInfo.platform === "ios") {
        //use the capacitor Plugins.Geolocation for geolocation
        return;
    }
    let locationOn = await GrayGeolocation.turnLocationOn();
    if(locationOn.res) {
        let coords = await GrayGeolocation.getCurrentPosition();
        console.log("latitude :",coords.latitude,"longitude :",coords.longitude);
        return coords;
    }
    else{
        //failed to turn on location : User declined
    }
}
```

## Testing

Manually tested against the following platforms:

- Android device 10.0 (API Level 29)
