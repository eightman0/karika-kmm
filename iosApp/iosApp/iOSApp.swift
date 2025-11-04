import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView(
                backDispatcher: appDelegate.backDispatcher,
                component: appDelegate.component
            ).onOpenURL { url in
                handleDeepLink(url: url)
            }
        }
    }
    
    func handleDeepLink(url: URL) {
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: true) {
            let token = components.queryItems?.first(where: { $0.name == "token" })?.value
            let email = components.queryItems?.first(where: { $0.name == "email" })?.value
            if let email = email, let token = token {
                appDelegate.component.handleDeepLink(emailToken: email, token: token)
            }
        }
    }
}
