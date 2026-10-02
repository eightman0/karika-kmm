### Kiosk / provisioning tableta

Ni salesrep ni shop (kiosk flavor `karika.distribucija.ba.kiosk`) više nisu Device Owner niti se same ažuriraju.
Device Owner je `launcher` - on instalira, zaključava (lock task), ponovo pokreće i ažurira aplikaciju koju uređaj vrti.

1. Admin dashboard (Karika Ops) -> **Provisioning**: izaberi aplikaciju (`app=salesrep|shop`), po želji kupca/lokaciju i WiFi, pa **Generiši QR**.
2. Factory-resetovan tablet: 6x tap na welcome ekran -> skeniraj QR.
3. Launcher se instalira, pročita `app` iz `PROVISIONING_ADMIN_EXTRAS_BUNDLE` i povuče tu aplikaciju sa njenog taba na **Verzije** (Salesrep / Shop).

Nove verzije: **Verzije** -> tab aplikacije -> upload APK-a -> izabrani uređaji ili "Pošalji svima".
Uređaji su na listi podijeljeni na tabove **Komercijalisti** i **Kupci**.
Izlaz iz kiosk moda ide samo preko dashboard-a (debug unlock / maintenance), u aplikaciji nema skrivenog izlaza.
