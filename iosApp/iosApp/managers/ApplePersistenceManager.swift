//
//  ApplePersistenceManager.swift
//  iosApp
//
//  Created by Osman Rešidović on 3. 9. 2025..
//
import Karika

class ApplePersistenceManager : NSObject, PersistenceManager {
    let userDefaults = UserDefaults(suiteName: "Karika")!
    
    func get(key: String) -> String {
        userDefaults.string(forKey: key) ?? ""
    }
    
    func save(key: String, value: String) {
        userDefaults.set(value, forKey: key)
    }
    
    func clear(){
        userDefaults.removePersistentDomain(forName: "Karika")
    }
}
