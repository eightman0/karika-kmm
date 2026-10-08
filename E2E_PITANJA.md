# Pitanja za E2E testove

Otvorena pitanja koja su se pojavila dok su pisani E2E testovi. Uz svako je zapisano šta je test za sada pretpostavio. Odgovor napiši ispod pitanja.

## Okruženje

1. **DNS emulatora povremeno ne radi.** Prijava ponekad dobije "Nema internet konekcije", a u istom trenutku i Googleove aplikacije na emulatoru dobijaju `ERR_NAME_NOT_RESOLVED`. To se dešava i kad se emulator pokrene s `-dns-server 8.8.8.8,1.1.1.1`. Na Macu je DNS na `127.0.0.1` uz više `utun` interfejsa, što liči na VPN ili sigurnosni alat (FortiClient, AnyConnect?). Testovi zato kod mrežne greške pokušaju prijavu do 4 puta. Ako možeš, pokreni set bez tog alata, ili mi reci koji je to alat.
2. **Stage zaključava nalog nakon neuspjelih prijava.** Magento (`RequestThrottler`) tada i za tačnu šifru vraća "The account sign-in was incorrect or your account is disabled temporarily". Svaki run ima po jedan test s pogrešnom šifrom za kupca i dobavljača. Kad se dva runa sudare ili se set pokreće više puta zaredom, neuspjesi se nakupe i nalog bude zaključan neko vrijeme. Test to sad prepozna i ne pokušava ponovo, jer bi svaki pokušaj produžio zaključavanje. Može li se na stage-u povećati broj dozvoljenih neuspjeha ili isključiti zaključavanje za test naloge? Druga opcija je poseban nalog samo za testove pogrešne šifre.

3. **Stage povremeno ne radi kako treba** (8.10.): greške 500 (npr. `carts/mine/shipping-information`), stari podaci odmah poslije slanja (komentar ili poruka se pojavi na stage-u tek par sekundi kasnije) i red push poruka koji zna kasniti do minute ili zastati. Testovi zato čekaju na podatke na stage-u, a push testovi čekaju do 60 s. Ako padne više push testova odjednom, prvo provjeri red na stage-u.
4. **Polovična narudžba `3000001022`** ostala je od runa koji je prekinuo pad ADB-a. U listi komercijaliste je `pending`, ali je stage ne može prikazati ni odbiti (`vendor/order` vraća 404 "Requested entity does not exist"). Treba je počistiti na backendu. Testovi komercijaliste sada biraju narudžbu koju stage može prikazati.

## Aplikacija

1. **Snackbar s greškom prekriva strelicu "Nazad".** Snackbar se crta na vrhu (`KarikaScaffold`, `Alignment.TopCenter`), preko gornje trake. Greške idu u red, pa kad stigne nekoliko grešaka zaredom (npr. detalji narudžbe i komentari ne mogu da se učitaju), strelica "Nazad" je prekrivena više sekundi, a klik na nju samo zatvori snackbar. Reproducirano kod komercijaliste na narudžbi `3000001022`. Odluka 8.10.: zasad ostaje ovako.
2. **"Dodaj u korpu" s količinom većom od minimalne šalje dva zahtjeva:** prvo doda artikal s količinom 1 (`POST carts/mine/items`), pa je promijeni (`PUT`). Na kraju je količina tačna, ali korpa kratko ima pogrešnu količinu, a za isto se troše dva zahtjeva.
3. **Ispravljeno 8.10.:** tekst korisnika u URL-u se sada enkodira (komentar sa `%` se ranije uopšte nije slao, a `&`, `#` i `+` su ga kvarili), dobavljač osvježava narudžbe na svaki push, a vrijeme sa servera se prikazuje u sarajevskoj zoni.


## Kupac

1. **Pokriveni ekrani kupca:** landing, prijava, zaboravljena šifra, registracija, Home, tab Dobavljači, korpa, pretraga, detalji proizvoda, stranica dobavljača, kategorija sa sortiranjem i filterom, Meni i Profil. Od 7.10. testovi i mijenjaju podatke, jer je test nalog slobodan:
   - **Moj nalog:** svako polje profila, naplate i zadane adrese za dostavu, obavijesti, brisanje dodatne adrese, promjena šifre (test je odmah vrati na staru).
   - **Narudžbe:** "Završi narudžbu" sa zadanom i novom adresom, otkazivanje, komentar, "Naruči ponovo", filter po statusu.
   - **Ostalo:** poruke adminu i dobavljaču, odgovor u razgovoru, "Označi sve kao pročitano", prava registracija novog kupca sa `@example.com` emailom.
1c. **Bug sa obavijestima je popravljen** (7.10.). `AccountComponent.updateAddress` je pri spremanju adrese slao zadane vrijednosti prekidača obavijesti (sve uključeno) i tako ponovo uključivao obavijesti koje je kupac isključio. Sad spremanje adrese ne dira postavke obavijesti; test `savingAnAddressKeepsTheNotificationSettings` prolazi.
1d. **Svaki run pravi nove podatke na stage-u:** 4–6 narudžbi (dio otkazan), komentare, poruke adminu i dobavljaču, novu adresu i jednog novog kupca na čekanju za odobrenje. Je li to u redu za admina stage-a, posebno novi kupci za odobrenje?
1e. **Partnerstva i "Pošalji uplatnicu" zavise od dobavljača.** Zahtjev za partnerstvo i status "Čekanje na uplatu" pravi dobavljač ili komercijalista. Test nalog trenutno nema otvoren zahtjev, pa se ti testovi preskaču. Mogu li testovi komercijaliste poslati poziv test kupcu ("Pozovi kupca"), i dobavljača poslati predračun, da bi kupac imao šta prihvatiti i platiti?
1a. **"Prikaži rasprodate"** na stranici dobavljača se preskače, jer dobavljač prvog preporučenog proizvoda nema rasprodatih artikala. Ako znaš dobavljača koji ih ima, napiši ga i test će koristiti njega.
2. **Zaključavanje naloga.** 29.9. je stage odbijao prijavu kupca i s ispravnom šifrom: "Prijava na račun je bila pogrešna ili je Vaš račun privremeno onemogućen". Tome su prethodile dvije pogrešne prijave po pokretanju (kupac i dobavljač) i oko 80 prijava za 40 minuta. Ako nema limita na prijavu, da li je zaključavanje došlo od pogrešnih šifri? Treba li testove s pogrešnom šifrom premjestiti na poseban nalog?
3. **Bug s "Odustani" u filteru regija** (tab Dobavljači) i dalje postoji. Test je isključen s `@Ignore`.

## Dobavljač

Pokriveno je: navigacija kroz fioku, analitika (tabovi, filteri, kupci u riziku, proizvodi), narudžbe (lista, pretraga, filter po broju i iznosu, detalji, meni Akcije, dijalog za minimalnu narudžbu), rabati (lista, editor bez čuvanja), poruke (liste, razgovor, nova poruka bez slanja), obavijesti, profil (bez čuvanja) i odjava.

Od 7.10. `VendorOrderActionsE2ETest` i mijenja podatke. Svaki test prvo kao test kupac naruči jedan artikal dobavljača, pa na toj narudžbi u aplikaciji: odobri (bez dostave i sa A2B dostavom), odbije s razlogom, pošalje komentar, izmijeni količinu i rabat stavke, pošalje predračun i promijeni minimalnu narudžbu (pa je vrati). Starije narudžbe na čekanju, koje zaključavaju novu, test odbije.

1. **Bug: dijalog "Obriši nalog" se pojavi dvaput.** U `ProfileView.kt` je `DeleteAccountConfirmation` nacrtan dvaput, pa se dva dijaloga slože jedan na drugi. Test je isključen s `@Ignore`. Da li da ga popravim?
2. **Šta testovi smiju upisati na stage?** Sada ništa ne odobravaju, ne odbijaju, ne šalju (poruke, komentare, predračune) i ne čuvaju (pravila rabata, profil, minimalnu narudžbu). Smijem li napraviti pravilo rabata pa ga odmah obrisati? A minimalnu narudžbu promijeniti pa vratiti?
3. **Otvaranje razgovora ga označi kao pročitan** (`POST chat/conversations/{id}/read`). Test otvori prvi razgovor s kupcem. Je li to u redu za test nalog dobavljača?
4. **"Upravljanje artiklima" je zakomentarisan** u fioci, pa ekran artikala nije testiran. Da li je to namjerno?
5. **Pravilo rabata bez kupca važi za sve kupce** ("Svi kupci"). Test to uzima kao namjerno ponašanje, a ne kao grešku. Tačno?
6. **Bug s filterom narudžbi je popravljen** (7.10.). `OrdersComponent.loadNextPage` je preskakao filter i "Očisti" dok je bio uključen loader, koji je zajednički za cijelu aplikaciju. Sad novi filter prekine učitavanje koje je u toku, a sljedeća stranica čeka samo na vlastito učitavanje.
7. Ranije sam mislio da filter narudžbi po iznosu ne radi. Ispostavilo se da je test promašivao dugme "Filtriraj" zbog otvorene tastature, a **filter radi**.
7. **Komercijalisti i historija lokacija su pokriveni** (`VendorEmployeesE2ETest`): stavka u fioci, lista tima, filter po statusu, pretraga po emailu, historija lokacija za "Danas" i "Zadnjih 30 dana" i povratak na listu. Mapa se ne provjerava, samo podaci ispod nje.
8. **Magic link (`/magic-links/<token>`) nije testiran.** Za to treba pravi token iz emaila ili Vibera, a ne znam da li link poslije otvaranja prestaje važiti. Postoji li test token koji smijem otvarati više puta, ili da preskočimo?

## Komercijalista

Od 7.10. `SalesRepActionsE2ETest` i mijenja podatke, uvijek za test kupca (mora biti aktivni kupac komercijaliste, inače se testovi preskaču):
   - **Popusti:** "Novi popust" za sve artikle i za jedan artikal, "Izmijeni", "Obriši" (prvo "Odustani"), i da se bez rabata ništa ne sačuva. Testovi koriste samo rabate 13, 17, 19, 23 i 29 % i na kraju ih obrišu.
   - **Korpa kupca:** "Dodaj" iz kataloga, stepper "+", "Ukloni" i "Isprazni korpu". Korpa se isprazni prije i poslije svakog testa.
   - **Narudžba za kupca:** katalog → korpa → "Pregledaj narudžbu" → "Potvrdi narudžbu", i komentar na takvoj narudžbi. Dobavljač poslije testa odbije narudžbu.
   - **Poruke** (`SalesRepMessagingE2ETest`): nova poruka test kupcu (test provjeri da je kupac dobio), odgovor u razgovoru s njim, poruka adminu i interna poruka vlasniku dobavljača. Poruke ostaju na stage-u.
   - **Dodaj kupca** (`SalesRepNewCustomerE2ETest`): provjera obaveznih polja i formata emaila, pravi novi kupac (`e2e.komercijalista…@example.com`, "E2E Komercijalista …"), dijalog "Kupac već postoji" → "Pozovi", pretraga u "Pozovi kupca" (najmanje 3 znaka, "Nema korisnika…") i pravi zahtjev za partnerstvo.
   Pitanja 2 i 3 ispod su time odgovorena; ostavljena su radi historije.

1. **Lokacija se šalje na stage.** Testovi sad unaprijed odobre dozvolu za lokaciju, jer bez nje sistemski dijalog prekrije aplikaciju i svi testovi komercijaliste padnu. Zato aplikacija, kao i kod pravog korisnika, poslije prijave šalje lokaciju emulatora na stage (`EmployeeLocationRepository.submit`). To je jedini upis kod komercijaliste. Je li to u redu, ili da testovi odbiju dozvolu i zatvore dijalog?
2. **Šta testovi komercijaliste ne rade:** ne dodaju artikle u korpu kupca, jer se to upisuje u pravu korpu kupca na serveru. Ne potvrđuju narudžbe, ne šalju poruke, komentare ni predračune, ne prave kupce i ne šalju poziv za partnerstvo. Forme se samo otvore i zatvore s "Odustani". Smijem li ići dalje, npr. dodati artikal u korpu nekog test kupca pa ga ukloniti?
3. **Postoji li test kupac** kod kojeg komercijalista smije praviti korpu i narudžbu? Narudžba bi se odmah otkazala.
4. **Prema mapi koda, poziv kupca kaže "unesite najmanje 2 znaka", a kod traži 3** (`SalesInviteCustomerView`). Nije provjereno na stage-u.
5. **Svaki run pravi i kod komercijaliste podatke koji se ne mogu obrisati iz aplikacije:** jednog novog kupca dodijeljenog komercijalisti i jedan zahtjev za partnerstvo. Zahtjev ide samo kupcu kojeg je napravio test registracije ("E2E Firma …", `@example.com`). Ako takvog nema (npr. kad se test registracije ne pokrene), test poziva se preskače. Je li to u redu, ili da se ti kupci povremeno čiste na stage-u?
6. **Test kupac ne može dobiti poziv od komercijaliste** jer je već aktivni partner, pa pitanje 1e kod kupca ostaje otvoreno: za "Prihvati/Odbij" kupcu treba dobavljač s kojim još nema partnerstvo.
7. **Razgovor u testu ponekad padne pri otvaranju** ("performMeasureAndLayout called during measure layout"). Ekran razgovora odmah skroluje na zadnju poruku (`LaunchedEffect` + `scrollToItem`). U testu se to pokrene usred mjerenja ekrana, a u pravoj aplikaciji tek u sljedećem frame-u, pa korisnik to ne vidi. Test u tom slučaju ponovo pokrene aplikaciju i otvori razgovor.
8. **`4.` iznad je popravljeno:** poziv kupca sad piše "unesite najmanje 3 znaka", kao što kod i traži; test to provjerava.
