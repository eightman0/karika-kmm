//
//  AppleKarikaFilePicker.swift
//  iosApp
//
//  Created by Osman Rešidović on 3. 9. 2025..
//
import Karika
import UIKit
import UniformTypeIdentifiers
import FirebaseMessaging
import FirebaseInstallations

class AppleKarikaHandler : KarikaHandler {
    func takePhoto(callback: @escaping (String, KotlinByteArray) -> Void) {
        
    }
    
    func downloadFile(fileName: String, fileType: String, fileUrl: String) {
        
    }
    
    func getPushHandle(callback: @escaping (String, String) -> Void) {
        var iid: String?
        var token: String?
        let group = DispatchGroup()
        
        group.enter()
        Installations.installations().installationID { id, _ in
            iid = id
            group.leave()
        }
        
        group.enter()
        Messaging.messaging().token { tkn, _ in
            token = tkn
            group.leave()
        }
        
        group.notify(queue: .main) {
            if let iid = iid, let token = token {
                callback(iid, token)
            }
        }
    }
    
    func pickFile(mediaTypes: KotlinArray<NSString>, callback: @escaping (String, KotlinByteArray) -> Void) {
        // 1) MIME -> UTType
        let mimeTypes: [String] = (0..<mediaTypes.size).compactMap { i in
            (mediaTypes.get(index: i) as NSString?) as String?
        }
        let utis: [UTType] = mimeTypes.compactMap { UTType(mimeType: $0) }

        // 2) Tražimo KOPIJU (asCopy: true) – iOS 14+
        let documentPicker: UIDocumentPickerViewController
        if #available(iOS 14.0, *) {
            documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: utis, asCopy: true)
        } else {
            // Fallback: .import kopira u sandbox i starijim API-jem
            documentPicker = UIDocumentPickerViewController(documentTypes: utis.compactMap { $0.identifier }, in: .import)
        }
        documentPicker.allowsMultipleSelection = false

        final class Coordinator: NSObject, UIDocumentPickerDelegate {
            let callback: (String, KotlinByteArray) -> Void
            init(callback: @escaping (String, KotlinByteArray) -> Void) { self.callback = callback }

            func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
                guard let pickedURL = urls.first else { return }

                // Ako smo dobili kopiju u sandboxu – super; ako ne, probaj security-scope + koordinaciju.
                func readData(from url: URL) throws -> Data {
                    // Pokušaj direktno (radiće ako je već u našem sandboxu – npr. zbog asCopy/import)
                    do { return try Data(contentsOf: url) } catch { /* nastavi niže */ }

                    var dataOut: Data?
                    var coordError: NSError?

                    let _ = pickedURL.startAccessingSecurityScopedResource()
                    defer { pickedURL.stopAccessingSecurityScopedResource() }

                    let coordinator = NSFileCoordinator()
                    coordinator.coordinate(readingItemAt: pickedURL, options: [], error: &coordError) { secureURL in
                        dataOut = try? Data(contentsOf: secureURL)
                    }
                    if let d = dataOut { return d }

                    // Fallback: eksplicitno kopiraj u /tmp pa čitaj
                    let tmp = FileManager.default.temporaryDirectory.appendingPathComponent(pickedURL.lastPathComponent)
                    // ukloni staro ako postoji
                    try? FileManager.default.removeItem(at: tmp)
                    try FileManager.default.copyItem(at: pickedURL, to: tmp)
                    return try Data(contentsOf: tmp)
                }

                do {
                    let data = try readData(from: pickedURL)

                    // Pretvori u KotlinByteArray bez unsafe čitanja iz pointera
                    var bytes = [UInt8](repeating: 0, count: data.count)
                    data.copyBytes(to: &bytes, count: data.count)

                    let kArray = KotlinByteArray(size: Int32(bytes.count)) { i in
                        KotlinByte(value: Int8(bitPattern: bytes[Int(truncating: i)]))
                    }

                    callback(pickedURL.lastPathComponent, kArray)
                } catch {
                    print("Greška pri čitanju fajla: \(error)")
                }
            }

            func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
                print("Picker otkazan")
            }
        }

        let coordinator = Coordinator(callback: callback)
        documentPicker.delegate = coordinator
        // zadrži delegata u životu
        objc_setAssociatedObject(documentPicker, "coordinator", coordinator, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)

        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
            root.present(documentPicker, animated: true, completion: nil)
        }
    }
    
    func exitKiosk() {
        
    }
    
    func checkForUpdate() {
        
    }
    
    func openWifi() {
        
    }
    
}
