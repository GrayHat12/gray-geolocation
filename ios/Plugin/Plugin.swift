import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GrayGeolocation)
public class GrayGeolocation: CAPPlugin {

    @objc func echo(_ call: CAPPluginCall) {
        call.success([
            res: true
        ])
    }

    @objc func echo(_ call: CAPPluginCall) {
        call.success([
            latitude: 0
            longitude: 0
        ])
    }
}
