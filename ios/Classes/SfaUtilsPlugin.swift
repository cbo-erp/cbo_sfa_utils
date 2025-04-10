import Flutter
import CoreLocation
import UIKit

public class SfaUtilsPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let taskQueue = registrar.messenger().makeBackgroundTaskQueue?()
        
        let channel = FlutterMethodChannel(
            name: "com.cbo.sfa.utils.native",
            binaryMessenger: registrar.messenger(),
            codec: FlutterStandardMethodCodec.sharedInstance(),
            taskQueue: taskQueue
        )
        
        let instance = SfaUtilsPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "launchTurnByTurn":
            launchTurnByTurn(methodCall: call, flutterResult: result)
        
        case "getMobileIMEI":
            getMobileIMEI(methodCall: call, flutterResult: result)
        
        case "getOsDetail":
            getOsDetail(methodCall: call, flutterResult: result)
            
        case "setMobileIMEI":
            setMobileIMEI(methodCall: call, flutterResult: result)
            
        case "getBatteryPercentage":
            getBatteryPercentage(methodCall: call, flutterResult: result)
            
        case "hasLocationPermission":
            hasLocationPermission(methodCall: call, flutterResult: result)
            
        case "getLocation":
            result(FlutterMethodNotImplemented)
            
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    // MARK: - Native Method Implementations
    
    private func getOsDetail(methodCall: FlutterMethodCall, flutterResult: @escaping FlutterResult) {
        let osDetails: [String: Any] = [
            "platform": "ios",
            "manufacturer": DeviceInfo.manufacturer,
            "os_version": DeviceInfo.osVersion,
            "device_model": DeviceInfo.model
        ]
        
        flutterResult(osDetails)
    }
    
    private func getMobileIMEI(methodCall: FlutterMethodCall, flutterResult: @escaping FlutterResult) {
        // If you want to switch between CboUtils and AppKeychainHelper
        // let _deviceId = CboUtils().getDeviceUniqueId()
        let deviceId = AppKeychainHelper().getSfaDevicetoken()
        flutterResult(deviceId)
    }
    
    private func setMobileIMEI(methodCall: FlutterMethodCall, flutterResult: @escaping FlutterResult) {
        if let args = methodCall.arguments as? [String: Any],
           let uniqueToken = args["uniqueToken"] as? String {
            AppKeychainHelper().saveSfaDevicetoken(uniqueToken: uniqueToken)
            flutterResult(true)
        } else {
            flutterResult(false)
        }
    }
    
    private func getBatteryPercentage(methodCall: FlutterMethodCall, flutterResult: @escaping FlutterResult) {
        UIDevice.current.isBatteryMonitoringEnabled = true
        let level = UIDevice.current.batteryLevel
        let batteryPercentage = Int(level * 100)
        flutterResult(batteryPercentage)
    }
    
    private func hasLocationPermission(methodCall: FlutterMethodCall, flutterResult: @escaping FlutterResult) {
        DispatchQueue.global().async {
            let locationServicesEnabled = CLLocationManager.locationServicesEnabled()
            
            DispatchQueue.main.async {
                guard locationServicesEnabled else {
                    flutterResult(FlutterError(
                        code: "LOCATION_DISABLED",
                        message: "Location services are not enabled",
                        details: nil
                    ))
                    return
                }
                
                let authorizationStatus = CboUtils().locationAuthorisationStatus()
                flutterResult(authorizationStatus)
            }
        }
        
        // MARK: - Optional: Use for iOS 14+ advanced permission check
        /*
        if #available(iOS 14.0, *) {
            CboLocationManager().checkLocationPermission { status in
                let result: [String: Any] = ["status": "1", "data": status]
                flutterResult(result)
            }
        } else {
            CboUtils().getLocationPermission { result in
                let resultDict: [String: Any] = ["status": "1", "data": result]
                flutterResult(resultDict)
            }
        }
        */
    }
    
    private func launchTurnByTurn(methodCall: FlutterMethodCall,
                                  flutterResult : @escaping FlutterResult) {
            if let args = methodCall.arguments as? [String:Any]{
                let mLat = args["mLat"] as! Double
                let mLon = args["mLon"] as! Double
                let googleMapUrl = URL(string: "comgooglemaps://")!
                if UIApplication.shared.canOpenURL(googleMapUrl) {
                    let directionsRequest = "comgooglemaps://?saddr=\(mLat),\(mLon)" + "&x-success=REPCO://?resume=true&x-source=REPCO"
                    let directionsURL = URL(string: directionsRequest)!
                    UIApplication.shared.openURL(directionsURL)
                } else {
                    NSLog("Can't use comgooglemaps:// on this device.")
                }
            }else{
                let _dictResult : [String:String] = ["status":"0", "data":"Cannot launch map"]
                flutterResult(_dictResult);
            }
        }
}
