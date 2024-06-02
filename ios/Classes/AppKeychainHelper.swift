//
//  AppKeychainHelper.swift
//  sfa_utils
//
//  Created by Shivam Kumar on 02/06/24.
//

import Foundation


final class AppKeychainHelper {
    
    let serviceName = "access-token"
    let accountName = "SFA-DEVICE-AUTH"
    
//    static let standard = AppKeychainHelper()
//    private init() {}
    
     func saveSfaDevicetoken(uniqueToken:String) {
        let data = Data(uniqueToken.utf8)
        AppKeychainHelper().saveToKeychain(data, service: self.serviceName, account: self.accountName)
    }
    
    
     func getSfaDevicetoken()->String {
       
        let data = AppKeychainHelper().readFromKeychain(service: self.serviceName, account: self.accountName)
        let uniqueToken = data == nil ?  "" : String(data: (data!), encoding: .utf8)!
        print(uniqueToken)
        return uniqueToken;
    }
    
    func saveToKeychain(_ data: Data, service: String, account: String) {
        
        // Create query
        let query = [
            kSecValueData: data,
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
        ] as CFDictionary
        
        // Add data in query to keychain
        let status = SecItemAdd(query, nil)
        
        if status != errSecSuccess {
            // Print out the error
            print("Error: \(status)")
        }
    }
    
    func readFromKeychain(service: String, account: String) -> Data? {
        
        let query = [
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecClass: kSecClassGenericPassword,
            kSecReturnData: true
        ] as CFDictionary
        
        var result: AnyObject?
        SecItemCopyMatching(query, &result)
        
        return (result as? Data)
    }
}
