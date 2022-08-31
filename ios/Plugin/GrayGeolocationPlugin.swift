import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GrayGeolocationPlugin)
public class GrayGeolocationPlugin: CAPPlugin {

    @objc func turnLocationOn(_ call: CAPPluginCall) {
        call.resolve({
            "res": false
        })
    }

    @objc func turnLocationOn(_ call: CAPPluginCall) {
        call.resolve([
            latitude: 0
            longitude: 0
        ])
    }
}
