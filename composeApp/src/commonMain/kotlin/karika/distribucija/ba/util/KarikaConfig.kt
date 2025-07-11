package karika.distribucija.ba.util

import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.DeliveryPrice
import karika.distribucija.ba.domain.model.Faq
import karika.distribucija.ba.domain.model.KarikaUnit

class KarikaConfig {

    companion object {
        private var units: List<KarikaUnit> = listOf()
        private var customerRegionList: List<KarikaUnit> = listOf()
        private var customerGroupList: List<KarikaUnit> = listOf()
        private var a2bPriceList: List<DeliveryPrice> = emptyList()
        private var categories = HashMap<String, List<Int>>()
        private var karikaCategories: List<Category> = emptyList()
        var faq = listOf(
            Faq(
                "Šta je Karika?",
                "Karika.ba je prva B2B internet platforma za kupovinu i prodaju roba široke potrošnje. Karika je namijenjena isključivo pravnim subjektima gdje trgovine, hoteli, restorani, kafei, apoteke, pet shopovi i benzinske pumpe mogu naručiti robu široke potrošnje za svoje kupce. Također kancelarije, fitnes centri, kozmetički saloni…itd. Mogu naručiti robe široke potrošnje koje su im potrebne za poslovanje. S druge strane, distributeri i proizvođači dobivaju novi prodajni kanal i direktan pristup svim potencijalnim kupcima u cijeloj BiH. Distributeri i proizvođači mogu napraviti svoj webshop na Karika platformi i ponuditi robu velikom broju kupaca."
            ),
            Faq(
                "Kako naručujem proizvode na Karika platformi i kome plaćam?",
                "Proizvode naručujete kao na svakom web-shopu odabirući artikle koji su Vam potrebni. Narudžbu sistem šalje direktno dobavljaču koji je stavio te artikle u ponudu. Robu plaćete direktno tom dobavljaču u skladu sa dogovorom ili ugovorom koji napravite. Karika se ne miješa Vaš odnos sa dobavljačem nakon što vas je spojila."
            ),
            Faq(
                "Ko isporučuje robu naručenu putem Karike i ko plaća dostavu?",
                "Robu direktno kupcu isporučuje dobavljač od kojeg je roba naručena. Plaćanje dostave zavisi od dogovora između kupca i dobavljača."
            ),
            Faq(
                "Da li mogu naručiti proizvode od više različitih dobavljača i kome plaćam za robu?",
                "Da, kupac može naručiti robu u jednoj narudžbi od neograničenog broja dobavljača. Sistem će sam razdijeliti narudžbu na svakog dobavljača pojedinačno. Plaćanje robe se vrši svakom dobavljaču pojedinačno za njegovu robu iz narudžbe."
            ),
            Faq(
                "Imam vlastiti proizvodni obrt, kako da pristupim Karika B2B platformi?",
                "Za pristup Karika platfomi potrebno je samo da ste pravno lice ili obrtnik registrovano u BiH. Registracijom na našem web portalu ili putem mobilne aplikacije dovoljno je da unesete informacije o svojoj firmi i nakon naše provjere bit ćete pušteni u sistem da dodate Vaše proizvode i spremni ste da primate narudžbe."
            ),
            Faq(
                "Da li Karika platforma zarađuje od mene kao kupca?",
                "Karika platforma nema nikakvu zaradu od kupaca. Korištenje platforme za kupce je potpuno besplatno. Karika patforma ostvaruje prihod od procenta za posredovanje i od prodaje marketinškog prostora."
            ),
            Faq(
                "Da li mogu pristupiti Karika platformi ako nisam pravno lice?",
                "Ne, Karika je B2B (business to business) platforma, namjenjena isključivo pravnim licima ili obrtnicima."
            )
        )

        fun getUnit(id: Int): String {
            return units.find { it.unit == id.toString() }?.label ?: "kom"
        }

        fun getUnits(): List<Pair<String, Int>> {
            return units.map { Pair(it.label ?: "kom", it.unit?.toIntOrNull() ?: 1) }
        }

        fun getCustomerRegionList(): List<KarikaUnit> {
            return customerRegionList
        }

        /*   fun getCustomerGroupList(): List<KarikaUnit> {
               return customerGroupList
           }

           fun getConfig() {
               CoroutineScope(Dispatchers.IO).launch {
                   KarikaHttpClient.getInstance()
                       .getKarikaConfig { _, _, response ->
                           response?.let {
                               a2bPriceList = it.a2bPriceList
                               units = it.unitOptions
                               customerGroupList = it.customerGroupList
                               customerRegionList = it.customerRegionList
                           }
                       }
               }

               CoroutineScope(Dispatchers.IO).launch {
                   KarikaHttpClient.getInstance()
                       .categories { _, _, response ->
                           response?.let {
                               karikaCategories = response.childrenData
                           }
                       }
               }

           }

           fun getCategories(callback: (List<Category>) -> Unit) {
               if (karikaCategories.isEmpty()) {
                   CoroutineScope(Dispatchers.IO).launch {
                       KarikaHttpClient.getInstance()
                           .categories { _, _, response ->
                               response?.let {
                                   karikaCategories = response.childrenData
                                   callback.invoke(karikaCategories)
                               }
                           }
                   }
               } else {
                   callback.invoke(karikaCategories)
               }
           }

           fun getPackageVolume(width: Double, height: Double, depth: Double, weight: Double): Double {
               val volume = maxOf((width * height * depth) / 5000, weight)
               val price =
                   a2bPriceList.find { it.min() <= volume && it.max() >= volume }?.price()
                       ?: a2bPriceList.lastOrNull()?.price() ?: 0.0
               return price + (price * 0.1)
           }

           fun setupCategories(childrenData: List<Category>) {
               categories.clear()

               fun flattenCategories(category: Category): List<Int> {
                   return listOf(category.id) + category.childrenData.flatMap { flattenCategories(it) }
               }

               childrenData.forEach { category ->
                   categories[category.name.uppercase()] = flattenCategories(category).toMutableList()
                   category.childrenData.forEach { category1 ->
                       categories[category1.name.uppercase()] =
                           flattenCategories(category1).toMutableList()
                   }
               }
           }


           fun getCategoryIdsByName(name: String): String {
               return categories.getOrDefault(name.uppercase(), emptyList())
                   .joinToString(separator = ",") { it.toString() }
           }*/

    }
}