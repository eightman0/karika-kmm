package karika.distribucija.ba.salesrep.util

/** Ported verbatim from composeApp's util/KarikaConstants.kt (BiH administrative divisions). */
object KarikaConstants {

    class Entity(val name: String, val id: Int, val cantons: List<Canton>)
    class Canton(val name: String, val id: Int, val cities: List<City>)
    class City(val name: String, val id: Int)

    val entries: List<Entity> = listOf(
        Entity(
            name = "Federacija",
            id = 1,
            cantons = listOf(
                Canton("Kanton Sarajevo", 1, listOf(
                    City("Hadžići", 0), City("Ilijaš", 0), City("Centar", 0), City("Ilidža", 0),
                    City("Novo Sarajevo", 0), City("Vogošća", 0), City("Novi Grad", 0),
                    City("Stari Grad", 0), City("Trnovo (FBiH)", 0)
                )),
                Canton("Tuzlanski Kanton", 2, listOf(
                    City("Banovići", 0), City("Gračanica", 0), City("Gradačac", 0), City("Kalesija", 0),
                    City("Kladanj", 0), City("Čelić", 0), City("Lukavac", 0), City("Srebrenik", 0),
                    City("Tuzla", 0), City("Živinice", 0), City("Doboj - Istok", 0), City("Sapna", 0),
                    City("Teočak", 0)
                )),
                Canton("Unsko-Sanski Kanton", 3, listOf(
                    City("Bihać", 0), City("Bosanska Krupa", 0), City("Bosanski Petrovac", 0),
                    City("Cazin", 0), City("Ključ", 0), City("Sanski Most", 0),
                    City("Velika Kladuša", 0), City("Bužim", 0)
                )),
                Canton("Zeničko-Dobojski Kanton", 4, listOf(
                    City("Breza", 0), City("Kakanj", 0), City("Maglaj", 0), City("Olovo", 0),
                    City("Tešanj", 0), City("Vareš", 0), City("Visoko", 0), City("Zavidovići", 0),
                    City("Zenica", 0), City("Žepče", 0), City("Doboj - Jug", 0), City("Usora", 0)
                )),
                Canton("Hercegovačko-Neretvanski Kanton", 5, listOf(
                    City("Čapljina", 0), City("Čitluk", 0), City("Grad Mostar", 0), City("Jablanica", 0),
                    City("Konjic", 0), City("Neum", 0), City("Prozor-Rama", 0), City("Ravno", 0),
                    City("Stolac", 0)
                )),
                Canton("Bosansko-Podrinjski Kanton", 6, listOf(
                    City("Goražde", 0), City("Foča - Ustikolina", 0), City("Pale - Prača", 0)
                )),
                Canton("Srednjebosanski Kanton", 7, listOf(
                    City("Bugojno", 0), City("Busovača", 0), City("Donji Vakuf", 0), City("Dobretići", 0),
                    City("Fojnica", 0), City("Gornji Vakuf", 0), City("Jajce", 0), City("Kiseljak", 0),
                    City("Kreševo", 0), City("Novi Travnik", 0), City("Travnik", 0), City("Vitez", 0)
                )),
                Canton("Livanjski Kanton", 8, listOf(
                    City("Livno", 0), City("Tomislavgrad", 0), City("Kupres", 0), City("Glamoč", 0),
                    City("Bosansko Grahovo", 0), City("Drvar", 0)
                )),
                Canton("Posavski Kanton", 9, listOf(
                    City("Orašje", 0), City("Odžak", 0), City("Domaljevac-Šamac", 0)
                )),
                Canton("Zapadno-Hercegovački Kanton", 10, listOf(
                    City("Široki Brijeg", 0), City("Grude", 0), City("Ljubuški", 0), City("Posušje", 0)
                ))
            )
        ),
        Entity(
            name = "Republika Srpska",
            id = 2,
            cantons = listOf(
                Canton("Banja Luka", 0, emptyList()), Canton("Čelinac", 0, emptyList()),
                Canton("Gradiška", 0, emptyList()), Canton("Kozarska Dubica", 0, emptyList()),
                Canton("Kotor Varoš", 0, emptyList()), Canton("Laktaši", 0, emptyList()),
                Canton("Prnjavor", 0, emptyList()), Canton("Srbac", 0, emptyList()),
                Canton("Prijedor", 0, emptyList()), Canton("Oštra Luka", 0, emptyList()),
                Canton("Novi Grad (Bosanski Novi)", 0, emptyList()), Canton("Kostajnica", 0, emptyList()),
                Canton("Krupa na Uni", 0, emptyList()), Canton("Doboj", 0, emptyList()),
                Canton("Derventa", 0, emptyList()), Canton("Modriča", 0, emptyList()),
                Canton("Šamac", 0, emptyList()), Canton("Brod", 0, emptyList()),
                Canton("Vukosavlje", 0, emptyList()), Canton("Pelagićevo", 0, emptyList()),
                Canton("Petrovo", 0, emptyList()), Canton("Donji Žabar", 0, emptyList()),
                Canton("Bijeljina", 0, emptyList()), Canton("Lopare", 0, emptyList()),
                Canton("Ugljevik", 0, emptyList()), Canton("Zvornik", 0, emptyList()),
                Canton("Bratunac", 0, emptyList()), Canton("Milići", 0, emptyList()),
                Canton("Šekovići", 0, emptyList()), Canton("Vlasenica", 0, emptyList()),
                Canton("Istočno Sarajevo", 0, emptyList()), Canton("Sokolac", 0, emptyList()),
                Canton("Pale", 0, emptyList()), Canton("Trnovo (RS)", 0, emptyList()),
                Canton("Rudo", 0, emptyList()), Canton("Rogatica", 0, emptyList()),
                Canton("Han Pijesak", 0, emptyList()), Canton("Foča", 0, emptyList()),
                Canton("Čajniče", 0, emptyList()), Canton("Kalinovik", 0, emptyList()),
                Canton("Trebinje", 0, emptyList()), Canton("Bileća", 0, emptyList()),
                Canton("Ljubinje", 0, emptyList()), Canton("Gacko", 0, emptyList()),
                Canton("Nevesinje", 0, emptyList()), Canton("Berkovići", 0, emptyList()),
                Canton("Jezero", 0, emptyList()), Canton("Šipovo", 0, emptyList())
            )
        ),
        Entity(
            name = "Distrikt Brčko",
            id = 3,
            cantons = listOf(Canton("Brčko Grad", 0, emptyList()))
        )
    )

    fun cantons(entityName: String): List<String> =
        entries.find { it.name == entityName }?.cantons?.map { it.name } ?: emptyList()

    /** Only Federacija cantons have a further city level. */
    fun cities(canton: String): List<String> =
        entries[0].cantons.find { it.name == canton }?.cities?.map { it.name } ?: emptyList()

    val companyTypes: List<String> = listOf(
        "Trgovina na veliko", "Trgovina na malo", "Hotel", "Restoran", "Caffe",
        "Benzinska pumpa", "Frizerski salon", "Fitnes centar", "Ostalo"
    )

    val companySizes: List<String> = listOf("do 50 m2", "do 200 m2", "do 500 m2", "preko 500 m2")
}
