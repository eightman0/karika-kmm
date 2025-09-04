//
//  Untitled.swift
//  Karika
//
//  Created by Selma Suvalija on 19. 12. 2024..
//

import UIKit
import Alamofire

class AppVersionManager {
    
    static let shared = AppVersionManager()
    let appId = "6692625868"
    
    private init() {}
    
    func fetchLatestAppStoreVersion(completion: @escaping (String?) -> Void) {
        guard let url = URL(string: "https://itunes.apple.com/lookup?id=\(appId)") else {
            return
        }
        
        AF.request(url, method: .get).responseJSON { response in
            switch response.result {
            case .success(let data):
                if let json = data as? [String: Any],
                   let results = json["results"] as? [[String: Any]],
                   let appStoreVersion = results.first?["version"] as? String {
                    completion(appStoreVersion)
                } else {
                    completion(nil)
                }
            case .failure:
                completion(nil)
            }
        }
    }
    
    func isUpdateRequired(currentVersion: String, appStoreVersion: String) -> Bool {
        return currentVersion.compare(appStoreVersion, options: .numeric) == .orderedAscending
    }
    
    func checkForUpdates() {
        let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        
        fetchLatestAppStoreVersion { appStoreVersion in
            guard let appStoreVersion = appStoreVersion else { return }
            
            let requiresUpdate = self.isUpdateRequired(currentVersion: currentVersion, appStoreVersion: appStoreVersion)
            
            DispatchQueue.main.async {
                if requiresUpdate {
                    self.showUpdateDialog()
                }
            }
        }
    }
    
    private func showUpdateDialog() {
        let alert = UIAlertController(
            title: "Nova verzija aplikacije",
            message: "Dostupna je nova verzija aplikacije. Da biste nastavili sa korištenjem aplikacije, molimo Vas da je ažurirate na najnoviju verziju.",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "Instaliraj novu verziju", style: .default) { _ in
            if let url = URL(string: "https://apps.apple.com/app/id\(self.appId)") {
                UIApplication.shared.open(url)
            }
        })
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let window = windowScene.windows.first(where: { $0.isKeyWindow }),
           let rootViewController = window.rootViewController {
            rootViewController.present(alert, animated: true)
        }
    }
}
