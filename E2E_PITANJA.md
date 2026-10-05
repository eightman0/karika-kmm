# Pitanja za E2E testove

Otvorena pitanja koja su se pojavila dok su pisani E2E testovi. Uz svako je zapisano šta je test za sada pretpostavio. Odgovor napiši ispod pitanja.

## Okruženje

1. **DNS emulatora povremeno ne radi.** Prijava ponekad dobije "Nema internet konekcije", a u istom trenutku i Googleove aplikacije na emulatoru dobijaju `ERR_NAME_NOT_RESOLVED`. To se dešava i kad se emulator pokrene s `-dns-server 8.8.8.8,1.1.1.1`. Na Macu je DNS na `127.0.0.1` uz više `utun` interfejsa, što liči na VPN ili sigurnosni alat (FortiClient, AnyConnect?). Testovi zato kod mrežne greške pokušaju prijavu do 4 puta. Ako možeš, pokreni set bez tog alata, ili mi reci koji je to alat.

## Kupac

1. **Pokriveni ekrani kupca:** landing, prijava, zaboravljena šifra, Home, tab Dobavljači, korpa, pretraga, detalji proizvoda, stranica dobavljača, kategorija sa sortiranjem i filterom, Meni (kategorije, blog, Samo na Kariki, FAQ, kontakt) i Profil (Moj nalog, narudžbe sa filterom i detaljima, bodovi, notifikacije, poruke, zahtjevi za partnerstvo, odjava). **Nisu pokriveni** ekran dostave sa "Završi narudžbu" i registracija, jer prave narudžbu ili nalog na stage-u. Smijem li ih raditi, s odmah otkazanom narudžbom i test nalogom za registraciju?
1a. **"Prikaži rasprodate"** na stranici dobavljača se preskače, jer dobavljač prvog preporučenog proizvoda nema rasprodatih artikala. Ako znaš dobavljača koji ih ima, napiši ga i test će koristiti njega.
1b. **Profil se samo otvara, ništa se ne čuva:** adrese, promjena šifre, notifikacije, otkazivanje narudžbe, "Naruči ponovo", komentari, prihvatanje i odbijanje partnerstva. Šta od toga smije raditi na test nalogu kupca? Npr. "Naruči ponovo" briše trenutnu korpu.
2. **Zaključavanje naloga.** 29.9. je stage odbijao prijavu kupca i s ispravnom šifrom: "Prijava na račun je bila pogrešna ili je Vaš račun privremeno onemogućen". Tome su prethodile dvije pogrešne prijave po pokretanju (kupac i dobavljač) i oko 80 prijava za 40 minuta. Ako nema limita na prijavu, da li je zaključavanje došlo od pogrešnih šifri? Treba li testove s pogrešnom šifrom premjestiti na poseban nalog?
3. **Bug s "Odustani" u filteru regija** (tab Dobavljači) i dalje postoji. Test je isključen s `@Ignore`.

## Dobavljač

Pokriveno je: navigacija kroz fioku, analitika (tabovi, filteri, kupci u riziku, proizvodi), narudžbe (lista, pretraga, filter po broju i iznosu, detalji, meni Akcije, dijalog za minimalnu narudžbu), rabati (lista, editor bez čuvanja), poruke (liste, razgovor, nova poruka bez slanja), obavijesti, profil (bez čuvanja) i odjava.

1. **Bug: dijalog "Obriši nalog" se pojavi dvaput.** U `ProfileView.kt` je `DeleteAccountConfirmation` nacrtan dvaput, pa se dva dijaloga slože jedan na drugi. Test je isključen s `@Ignore`. Da li da ga popravim?
2. **Šta testovi smiju upisati na stage?** Sada ništa ne odobravaju, ne odbijaju, ne šalju (poruke, komentare, predračune) i ne čuvaju (pravila rabata, profil, minimalnu narudžbu). Smijem li napraviti pravilo rabata pa ga odmah obrisati? A minimalnu narudžbu promijeniti pa vratiti?
3. **Otvaranje razgovora ga označi kao pročitan** (`POST chat/conversations/{id}/read`). Test otvori prvi razgovor s kupcem. Je li to u redu za test nalog dobavljača?
4. **"Upravljanje artiklima" je zakomentarisan** u fioci, pa ekran artikala nije testiran. Da li je to namjerno?
5. **Pravilo rabata bez kupca važi za sve kupce** ("Svi kupci"). Test to uzima kao namjerno ponašanje, a ne kao grešku. Tačno?
6. **Mogući bug: filter narudžbi se tiho preskoči.** `OrdersComponent.loadNextPage` ne radi ništa dok je `loader.value` uključen. Ako korisnik tapne "Filtriraj" ili "Očisti" dok se lista osvježava (npr. kad stigne nova obavijest), filter se ne primijeni, a "Očisti" se ipak pojavi. U testu se to desilo jednom od tri puta. Da li da to popravim?
7. Ranije sam mislio da filter narudžbi po iznosu ne radi. Ispostavilo se da je test promašivao dugme "Filtriraj" zbog otvorene tastature, a **filter radi**.

## Komercijalista

1. **Lokacija se šalje na stage.** Testovi sad unaprijed odobre dozvolu za lokaciju, jer bez nje sistemski dijalog prekrije aplikaciju i svi testovi komercijaliste padnu. Zato aplikacija, kao i kod pravog korisnika, poslije prijave šalje lokaciju emulatora na stage (`EmployeeLocationRepository.submit`). To je jedini upis kod komercijaliste. Je li to u redu, ili da testovi odbiju dozvolu i zatvore dijalog?
2. **Šta testovi komercijaliste ne rade:** ne dodaju artikle u korpu kupca, jer se to upisuje u pravu korpu kupca na serveru. Ne potvrđuju narudžbe, ne šalju poruke, komentare ni predračune, ne prave kupce i ne šalju poziv za partnerstvo. Forme se samo otvore i zatvore s "Odustani". Smijem li ići dalje, npr. dodati artikal u korpu nekog test kupca pa ga ukloniti?
3. **Postoji li test kupac** kod kojeg komercijalista smije praviti korpu i narudžbu? Narudžba bi se odmah otkazala.
4. **Prema mapi koda, poziv kupca kaže "unesite najmanje 2 znaka", a kod traži 3** (`SalesInviteCustomerView`). Nije provjereno na stage-u.
