//
//  CboUtils.swift
//  sfa_utils
//
//  Created by Shivam Kumar on 02/06/24.
//

import Foundation
import DeviceCheck
import CoreLocation

class CboUtils {
    
    func getDeviceUniqueId()->String{
        
        var deviceid = UIDevice.current.identifierForVendor?.uuidString ?? ""
        if(deviceid == ""){
            if #available(iOS 11.0, *) {
                if DCDevice.current.isSupported {
                    DCDevice.current.generateToken { token, error in
                        if(error != nil){
                            
                            // hanndle errors
                            
                        }else{
                        
                            deviceid = token!.base64EncodedString()
                        }
                    }
                }
            } else {
                
                // Fallback on earlier versions
            }
        }
        print("DEVICE ID: \(deviceid)")
        return deviceid

    }
    
    func getLocationPermission(completion: @escaping (String) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            var permissionStatus = ""

            if CLLocationManager.locationServicesEnabled() {
                switch CLLocationManager.authorizationStatus() {
                case .notDetermined, .denied:
                    permissionStatus = "request"
                case .restricted:
                    permissionStatus = "open_setting"
                case .authorizedAlways:
                    permissionStatus = "authorized_always"
                case .authorizedWhenInUse:
                    permissionStatus = "authorized_when_in_use"
                default:
                    permissionStatus = ""
                }
            } else {
                print("Location services are not enabled")
                // Set a default value if location services are not enabled
                permissionStatus = ""
            }

            DispatchQueue.main.async {
                // Send the result back to the main thread
                completion(permissionStatus)
            }
        }
    }

    
    
    func locationAuthorisationStatus()->String{
        switch(CLLocationManager.authorizationStatus()) {
            case .notDetermined, .denied:
            return "request"
            case .restricted:
            return "open_setting"
            case .authorizedAlways:
            return "authorized_always"
            case .authorizedWhenInUse:
            return "authorized_when_in_use"
            default:
            return ""
        }
    }
    

}

