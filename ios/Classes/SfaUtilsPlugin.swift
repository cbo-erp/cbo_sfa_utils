import Flutter
import CoreLocation
import UIKit

public class SfaUtilsPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
      let taskQueue = registrar.messenger().makeBackgroundTaskQueue?()
      
    let channel = FlutterMethodChannel(name: "com.cbo.sfa.utils.native",
                                       binaryMessenger: registrar.messenger(),
                                       codec: FlutterStandardMethodCodec.sharedInstance(),
                                       taskQueue: taskQueue
    )
    let instance = SfaUtilsPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

      switch call.method  {
          case "getMobileIMEI":
              self.getMobileIMEI(methodCall:call,flutterResult: result)
              
          case "getOsDetail":
              self.getOsDetail(methodCall:call,flutterResult: result)
              
          case "setMobileIMEI":
              self.setMobileIMEI(methodCall:call,flutterResult: result)
              
          case "getBatteryPercentage":
              self.getBatteryPercentage(methodCall:call,flutterResult: result)
              
          case "hasLocationPermission":
              self.hasLocationPermission(methodCall:call,flutterResult: result)
              
           case "getLocation":
              //self.getLocation(flutterResult: result)
              let dictResult : [String:String] = ["status":"0", "data":"0.00,0.00"]
              result(dictResult);
          
          default:
              result(FlutterMethodNotImplemented)
      }
  }
    
    
    
    private func getOsDetail(methodCall: FlutterMethodCall,
                               flutterResult : @escaping FlutterResult) {
        
        let dictData : [String:Any] = [
            "device": "ios",
            "sdkVersionNumber": UIDevice.current.systemVersion,
            "sdkVersionRelease": "",
            "brand":"Apple"
        ]
    
        let _dictResult : [String:Any] = ["status":"1", "data": dictData]
        flutterResult(_dictResult);
    }
    
    private func getMobileIMEI(methodCall: FlutterMethodCall,
                               flutterResult : @escaping FlutterResult) {
        

//        let _deviceId = CboUtils().getDeviceUniqueId();
        
        let _deviceId = AppKeychainHelper().getSfaDevicetoken();
        
        let _dictResult : [String:String] = ["status":"1", "data":_deviceId]
        flutterResult(_dictResult);
    }
    
    
    private func setMobileIMEI(methodCall: FlutterMethodCall,
                               flutterResult : @escaping FlutterResult) {
        
        var _dictResult : [String:String]
        
        if let args = methodCall.arguments as? [String:Any]{
            
            let uniqueToken = args["uniqueToken"] as! String
            
            AppKeychainHelper().saveSfaDevicetoken(uniqueToken: uniqueToken);
            
            _dictResult = ["status":"1", "data":"Saved In Keychain"]
            
        }else{
            
            _dictResult = ["status":"0", "data":"Not Saved In Keychain"]
            
        }
        
        flutterResult(_dictResult);
    }

    private func getBatteryPercentage(methodCall: FlutterMethodCall, flutterResult : @escaping FlutterResult) {
        UIDevice.current.isBatteryMonitoringEnabled = true
        let level = UIDevice.current.batteryLevel
        let batteryPer = Int(level*100)
        
        let _dictResult : [String:String] = [
            "status" : "1", "data": String(batteryPer)
        ]
        flutterResult(_dictResult);
    }
    
    private func hasLocationPermission(methodCall: FlutterMethodCall, flutterResult : @escaping FlutterResult)  {

        DispatchQueue.global().async{
            
            var result = ""
                
            if CLLocationManager.locationServicesEnabled() {
                result = CboUtils().locationAuthorisationStatus()
                    
            } else {
                print("Location services are not enabled")
                result = ""
            }
            
            DispatchQueue.main.async {
                
                let dictResult: [String: Any] = ["status": "1", "data": result]
                flutterResult(dictResult)
            }
        }

//        if #available(iOS 14.0, *) {
//
//            CboLocationManager().checkLocationPermission { status in
//                let dictResult: [String: Any] = ["status": "1", "data": status]
//                // Handle the result as needed, for example, send it to Flutter
//                flutterResult(dictResult)
//            }
//        } else {
//
//            CboUtils().getLocationPermission { (result) in
//                let dictResult: [String: Any] = ["status": "1", "data": result]
//                flutterResult(dictResult)
//            }
//        }

    }
    
    
   
}
