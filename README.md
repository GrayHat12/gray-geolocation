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

This Plugin also has method calls to turn on location.

--------------------

## Supported platforms

- Android
- Web

--------------------

## Install

```bash
npm install capacitor-gray-geolocation
npx cap sync
```

--------------------

## API

<docgen-index>

* [`turnLocationOn()`](#turnlocationon)
* [`getCurrentPosition()`](#getcurrentposition)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### turnLocationOn()

```typescript
turnLocationOn() => Promise<{ res: boolean; }>
```

This prompts the user on android to turn location on.
Firtstly checks for location permissions and if they are not granted, it prompts the user to grant permissions.
Always returns `{res: true}` for web.
Not implemented on iOS.

**Returns:** <code>Promise&lt;{ res: boolean; }&gt;</code>

--------------------


### getCurrentPosition()

```typescript
getCurrentPosition() => Promise<{ latitude: number; longitude: number; }>
```

Returns current position of the device.
Firtstly checks for location permissions and if they are not granted, it prompts the user to grant permissions.
Not implemented on iOS.

**Returns:** <code>Promise&lt;{ latitude: number; longitude: number; }&gt;</code>

--------------------

</docgen-api>


## Example

```js
// Inside the Ionic code

import { Device } from "@capacitor/device";
import { GrayGeolocation } from "capacitor-gray-geolocation";

// ...code

//function to get location (handle errors yourself) 
getLocation = async() => {
    let deviceInfo = await Device.getInfo();
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

```java
// Inside the Android code (MainActivity.java)

// ... imports
import com.gray.plugins.capacitor.GrayGeolocationPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ... register other plugins if any
        registerPlugin(GrayGeolocationPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
```

--------------------

## Testing

Manually tested against the following platforms:

* Android device 11.0 (API Level 30)