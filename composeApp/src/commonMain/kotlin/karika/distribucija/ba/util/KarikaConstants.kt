package karika.distribucija.ba.util

object KarikaConstants {
    val numbers = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val capitalLetters = listOf(
        "A",
        "B",
        "C",
        "Č",
        "Ć",
        "D",
        "Đ",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "Š",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z",
        "Ž"
    )
    private val nonCapitalLetters = listOf(
        "a",
        "b",
        "c",
        "č",
        "ć",
        "d",
        "đ",
        "e",
        "f",
        "g",
        "h",
        "i",
        "j",
        "k",
        "l",
        "m",
        "n",
        "o",
        "p",
        "q",
        "r",
        "s",
        "š",
        "t",
        "u",
        "v",
        "w",
        "x",
        "y",
        "z",
        "ž"
    )
    val numbersAndLetters = numbers + capitalLetters + nonCapitalLetters
    val numbersAndLettersSpace = numbers + capitalLetters + nonCapitalLetters + " "
    val letters = capitalLetters + nonCapitalLetters
    val lettersSpace = capitalLetters + nonCapitalLetters + " "

    val entries: List<Entity> = mutableListOf(
        Entity(
            name = "Federacija",
            id = 1,
            cantons = mutableListOf(
                Canton(
                    name = "Kanton Sarajevo",
                    id = 1,
                    cities = mutableListOf(
                        City("Hadžići", 0),
                        City("Ilijaš", 0),
                        City("Centar", 0),
                        City("Ilidža", 0),
                        City("Novo Sarajevo", 0),
                        City("Vogošća", 0),
                        City("Novi Grad", 0),
                        City("Stari Grad", 0),
                        City("Trnovo (FBiH)", 0)
                    )
                ),
                Canton(
                    name = "Tuzlanski Kanton",
                    id = 2,
                    cities = mutableListOf(
                        City("Banovići", 0),
                        City("Gračanica", 0),
                        City("Gradačac", 0),
                        City("Kalesija", 0),
                        City("Kladanj", 0),
                        City("Čelić", 0),
                        City("Lukavac", 0),
                        City("Srebrenik", 0),
                        City("Tuzla", 0),
                        City("Živinice", 0),
                        City("Doboj - Istok", 0),
                        City("Sapna", 0),
                        City("Teočak", 0)
                    )
                ),
                Canton(
                    name = "Unsko-Sanski Kanton",
                    id = 3,
                    cities = mutableListOf(
                        City("Bihać", 0),
                        City("Bosanska Krupa", 0),
                        City("Bosanski Petrovac", 0),
                        City("Cazin", 0),
                        City("Ključ", 0),
                        City("Sanski Most", 0),
                        City("Velika Kladuša", 0),
                        City("Bužim", 0),
                    )
                ),
                Canton(
                    name = "Zeničko-Dobojski Kanton",
                    id = 4,
                    cities = mutableListOf(
                        City("Breza", 0),
                        City("Kakanj", 0),
                        City("Maglaj", 0),
                        City("Olovo", 0),
                        City("Tešanj", 0),
                        City("Vareš", 0),
                        City("Visoko", 0),
                        City("Zavidovići", 0),
                        City("Zenica", 0),
                        City("Žepče", 0),
                        City("Doboj - Jug", 0),
                        City("Usora", 0),
                    )
                ),
                Canton(
                    name = "Hercegovačko-Neretvanski Kanton",
                    id = 5,
                    cities = mutableListOf(
                        City("Čapljina", 0),
                        City("Čitluk", 0),
                        City("Grad Mostar", 0),
                        City("Jablanica", 0),
                        City("Konjic", 0),
                        City("Neum", 0),
                        City("Prozor-Rama", 0),
                        City("Ravno", 0),
                        City("Stolac", 0),
                    )
                ),
                Canton(
                    name = "Bosansko-Podrinjski Kanton",
                    id = 6,
                    cities = mutableListOf(
                        City("Goražde", 0),
                        City("Foča - Ustikolina", 0),
                        City("Pale - Prača", 0)
                    )
                ),
                Canton(
                    name = "Srednjebosanski Kanton",
                    id = 7,
                    cities = mutableListOf(
                        City("Bugojno", 0),
                        City("Busovača", 0),
                        City("Donji Vakuf", 0),
                        City("Dobretići", 0),
                        City("Fojnica", 0),
                        City("Gornji Vakuf", 0),
                        City("Jajce", 0),
                        City("Kiseljak", 0),
                        City("Kreševo", 0),
                        City("Novi Travnik", 0),
                        City("Travnik", 0),
                        City("Vitez", 0)
                    )
                ),
                Canton(
                    name = "Livanjski Kanton",
                    id = 8,
                    cities = mutableListOf(
                        City("Livno", 0),
                        City("Tomislavgrad", 0),
                        City("Kupres", 0),
                        City("Glamoč", 0),
                        City("Bosansko Grahovo", 0),
                        City("Drvar", 0)
                    )
                ),
                Canton(
                    name = "Posavski Kanton",
                    id = 9,
                    cities = mutableListOf(
                        City("Orašje", 0),
                        City("Odžak", 0),
                        City("Domaljevac-Šamac", 0)
                    )
                ),
                Canton(
                    name = "Zapadno-Hercegovački Kanton",
                    id = 10,
                    cities = mutableListOf(
                        City("Široki Brijeg", 0),
                        City("Grude", 0),
                        City("Ljubuški", 0),
                        City("Posušje", 0)
                    )
                )
            )
        ),
        Entity(
            name = "Republika Srpska",
            id = 2,
            cantons = mutableListOf(
                Canton("Banja Luka", 0, listOf()),
                Canton("Čelinac", 0, listOf()),
                Canton("Gradiška", 0, listOf()),
                Canton("Kozarska Dubica", 0, listOf()),
                Canton("Kotor Varoš", 0, listOf()),
                Canton("Laktaši", 0, listOf()),
                Canton("Prnjavor", 0, listOf()),
                Canton("Srbac", 0, listOf()),
                Canton("Prijedor", 0, listOf()),
                Canton("Oštra Luka", 0, listOf()),
                Canton("Novi Grad (Bosanski Novi)", 0, listOf()),
                Canton("Kostajnica", 0, listOf()),
                Canton("Krupa na Uni", 0, listOf()),
                Canton("Doboj", 0, listOf()),
                Canton("Derventa", 0, listOf()),
                Canton("Modriča", 0, listOf()),
                Canton("Šamac", 0, listOf()),
                Canton("Brod", 0, listOf()),
                Canton("Vukosavlje", 0, listOf()),
                Canton("Pelagićevo", 0, listOf()),
                Canton("Petrovo", 0, listOf()),
                Canton("Donji Žabar", 0, listOf()),
                Canton("Bijeljina", 0, listOf()),
                Canton("Lopare", 0, listOf()),
                Canton("Ugljevik", 0, listOf()),
                Canton("Zvornik", 0, listOf()),
                Canton("Bratunac", 0, listOf()),
                Canton("Milići", 0, listOf()),
                Canton("Šekovići", 0, listOf()),
                Canton("Vlasenica", 0, listOf()),
                Canton("Istočno Sarajevo", 0, listOf()),
                Canton("Sokolac", 0, listOf()),
                Canton("Pale", 0, listOf()),
                Canton("Trnovo (RS)", 0, listOf()),
                Canton("Rudo", 0, listOf()),
                Canton("Rogatica", 0, listOf()),
                Canton("Han Pijesak", 0, listOf()),
                Canton("Foča", 0, listOf()),
                Canton("Čajniče", 0, listOf()),
                Canton("Kalinovik", 0, listOf()),
                Canton("Trebinje", 0, listOf()),
                Canton("Bileća", 0, listOf()),
                Canton("Ljubinje", 0, listOf()),
                Canton("Gacko", 0, listOf()),
                Canton("Nevesinje", 0, listOf()),
                Canton("Berkovići", 0, listOf()),
                Canton("Jezero", 0, listOf()),
                Canton("Šipovo", 0, listOf())
            )
        ),
        Entity(
            name = "Distrikt Brčko",
            id = 3,
            cantons = mutableListOf(
                Canton("Brčko Grad", 0, emptyList()),
            )
        ),
    )

    fun cantons(name: String): List<String> {
        return entries.find { it.name == name }
            ?.cantons
            ?.map { it.name }
            ?: emptyList()
    }

    fun cities(canton: String): List<String> {
        return entries[0].cantons.find { it.name == canton }?.cities?.map { it.name } ?: emptyList()
    }

    val companyTypes: List<String> = listOf(
        "Trgovina na veliko",
        "Trgovina na malo",
        "Hotel",
        "Restoran",
        "Caffe",
        "Benzinska pumpa",
        "Frizerski salon",
        "Fitnes centar",
        "Ostalo"
    )

    val companySizes: List<String> = listOf(
        "do 50 m2",
        "do 200 m2",
        "do 500 m2",
        "preko 500 m2"
    )
}

class Entity(
    val name: String,
    val id: Int,
    val cantons: List<Canton>
)

class Canton(
    val name: String,
    val id: Int,
    val cities: List<City>
)

class City(
    val name: String,
    val id: Int
)

class ObjectSize(
    id: Int,
    name: String
)