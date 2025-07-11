//
//  AppDelegate.swift
//  iosApp
//
//  Created by Osman Rešidović on 22. 7. 2025..
//

import Foundation
import UIKit
import Karika


class AppDelegate: NSObject, UIApplicationDelegate {
    var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()
    lazy var component: AppComponent = AppComponent(
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: nil,
            instanceKeeper: nil,
            backHandler: backDispatcher
        )
    )
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        
        KoinInit_iosKt.doInitKoinIos(manager: ApplePersistenceManager())
        return true
    }
}


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
