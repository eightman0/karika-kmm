import UIKit
import SwiftUI
import Karika

struct ComposeView: UIViewControllerRepresentable {
    let component: AppComponent
    let backDispatcher: BackDispatcher
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            component: component,
            backDispatcher: backDispatcher
        )
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let backDispatcher: BackDispatcher
    let component: AppComponent
    @Environment(\.scenePhase) private var scenePhase
    
    var body: some View {
        ComposeView(component: component, backDispatcher: backDispatcher)
            .ignoresSafeArea(.all)
    }
}



