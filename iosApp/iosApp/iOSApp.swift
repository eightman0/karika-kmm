import SwiftUI

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView(
                backDispatcher: appDelegate.backDispatcher,
                component: appDelegate.component
            )
        }
    }
}
