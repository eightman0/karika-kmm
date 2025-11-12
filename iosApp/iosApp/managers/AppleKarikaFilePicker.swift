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
import PhotosUI

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
    
    func pickPhoto(callback: @escaping (String, KotlinByteArray) -> Void) {
        
        if #available(iOS 14.0, *) {
            // iOS 14+ - PHPickerViewController (moderno rješenje)
            var config = PHPickerConfiguration()
            config.selectionLimit = 1
            config.filter = .images // samo slike, ili .any(of: [.images, .videos]) za video
            
            let picker = PHPickerViewController(configuration: config)
            
            final class Coordinator: NSObject, PHPickerViewControllerDelegate {
                let callback: (String, KotlinByteArray) -> Void
                init(callback: @escaping (String, KotlinByteArray) -> Void) {
                    self.callback = callback
                }
                
                func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
                    picker.dismiss(animated: true)
                    
                    guard let result = results.first else { return }
                    
                    // Dohvati sliku kao Data
                    result.itemProvider.loadDataRepresentation(forTypeIdentifier: UTType.image.identifier) { data, error in
                        guard let data = data else {
                            print("Greška pri učitavanju slike: \(error?.localizedDescription ?? "nepoznato")")
                            return
                        }
                        
                        // Dohvati filename (ili generiši default)
                        let filename = result.itemProvider.suggestedName ?? "image_\(Date().timeIntervalSince1970).jpg"
                        
                        // Pretvori u KotlinByteArray
                        var bytes = [UInt8](repeating: 0, count: data.count)
                        data.copyBytes(to: &bytes, count: data.count)
                        
                        let kArray = KotlinByteArray(size: Int32(bytes.count)) { i in
                            KotlinByte(value: Int8(bitPattern: bytes[Int(truncating: i)]))
                        }
                        
                        // Pozovi callback na main thread-u
                        DispatchQueue.main.async {
                            self.callback(filename, kArray)
                        }
                    }
                }
            }
            
            let coordinator = Coordinator(callback: callback)
            picker.delegate = coordinator
            objc_setAssociatedObject(picker, "coordinator", coordinator, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
            
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
                root.present(picker, animated: true)
            }
            
        } else {
            // iOS 13 i starije - UIImagePickerController
            let picker = UIImagePickerController()
            picker.sourceType = .photoLibrary
            picker.mediaTypes = ["public.image"] // za video dodaj "public.movie"
            
            final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
                let callback: (String, KotlinByteArray) -> Void
                init(callback: @escaping (String, KotlinByteArray) -> Void) {
                    self.callback = callback
                }
                
                func imagePickerController(_ picker: UIImagePickerController,
                                         didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
                    picker.dismiss(animated: true)
                    
                    guard let image = info[.originalImage] as? UIImage else { return }
                    
                    // Konvertuj u JPEG (možeš i PNG)
                    guard let data = image.jpegData(compressionQuality: 0.9) else { return }
                    
                    // Generiši filename
                    let filename = "image_\(Date().timeIntervalSince1970).jpg"
                    
                    // Pretvori u KotlinByteArray
                    var bytes = [UInt8](repeating: 0, count: data.count)
                    data.copyBytes(to: &bytes, count: data.count)
                    
                    let kArray = KotlinByteArray(size: Int32(bytes.count)) { i in
                        KotlinByte(value: Int8(bitPattern: bytes[Int(truncating: i)]))
                    }
                    
                    self.callback(filename, kArray)
                }
                
                func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
                    picker.dismiss(animated: true)
                    print("Picker otkazan")
                }
            }
            
            let coordinator = Coordinator(callback: callback)
            picker.delegate = coordinator
            objc_setAssociatedObject(picker, "coordinator", coordinator, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
            
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController {
                root.present(picker, animated: true)
            }
        }
    }
    
    func exitKiosk() {
        
    }
    
    func checkForUpdate() {
        
    }
    
    func openWifi() {
        
    }
    
}
