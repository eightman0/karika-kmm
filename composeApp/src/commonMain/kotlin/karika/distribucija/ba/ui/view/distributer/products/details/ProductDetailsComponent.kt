package karika.distribucija.ba.ui.view.distributer.products.details

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.MediaGallery
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

class ProductDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    product: VendorProduct
) :
    CommonComponent(componentContext, stateHolder) {

    private val repository = DashRepository()
    private val _product = MutableStateFlow(product)
    val product = _product.asStateFlow()

    val approveProduct = mutableStateOf(product.approved())
    val name = mutableStateOf(product.name ?: "")
    val barcode = mutableStateOf("")
    val sku = mutableStateOf(product.sku ?: "")
    val price = mutableStateOf(product.price ?: "")
    val specialPrice = mutableStateOf(product.specialPrice ?: "")
    val specialPriceFrom = mutableStateOf(product.specialPriceFrom?.toDate1() ?: "")
    val specialPriceTo = mutableStateOf(product.specialPriceTo?.toDate1() ?: "")
    val minQty = mutableStateOf(product.minQty ?: "")
    val minQtyUnit = mutableStateOf(minQtyUnit(product))
    val availableQty = mutableStateOf(product.salableQty ?: "")
    val categories = mutableStateOf(categories(product))
    val onlyKarika = mutableStateOf(product.isExclusive == "1")
    val availableMessages = mutableStateOf(product.enabledMessages == "1")
    val shortDesc = mutableStateOf(product.shortDescription ?: "")
    val longDesc = mutableStateOf(product.description ?: "")
    val aiImages = mutableStateOf<List<String>>(emptyList())
    val aiImagesSelected = mutableStateOf<List<String>>(emptyList())
    val showAISheet = mutableStateOf(false)

    init {
        getProduct()
    }

    private fun getProduct() {
        iOScope.launch {
            repository.getProduct(product.value.productId ?: return@launch)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            _product.update { result.data }

                            minQty.value = result.data.minQty ?: ""
                            minQtyUnit.value = minQtyUnit(result.data)

                            availableQty.value = result.data.qty ?: ""
                            onlyKarika.value = result.data.isExclusive == "1"
                            availableMessages.value = result.data.enabledMessages == "1"
                            shortDesc.value = result.data.shortDescription ?: ""
                            longDesc.value = result.data.description ?: ""
                            categories.value = categories(result.data)
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun pickImage() {
        stateHolder.filePicker.pickFile { name, data ->
            if (product.value.mediaGallery.any { it.filename == name }) {
                showMessage("Slika već dodana!")
                return@pickFile
            }

            _product.update {
                it.copy(
                    media = it.mediaGallery.plus(
                        MediaGallery(
                            filename = name,
                            data = data
                        )
                    )
                )
            }
        }
    }

    fun save() {
        if (name.value.isBlank()) {
            showMessage("Naziv proizvoda je obavezno polje")
            return
        }
        if (price.value.isBlank()) {
            showMessage("Cijana ne može biti manja ili jednaka nuli.")
            return
        }

        if (minQty.value.isBlank()) {
            showMessage("Minimalna količina je obavezno polje")
            return
        }

        iOScope.launch {
            val newImages = mutableListOf<String>()
            async {
                product.value.media?.filter { it.data != null }?.forEach {
                    repository.saveProductImage(
                        attachment = it.data,
                        filename = it.filename ?: ""
                    ).collect { result ->
                        when (result) {
                            is ResultState.Loading -> showLoader()
                            is ResultState.Success -> {
                                newImages.add(result.data.replace("\"", ""))
                            }

                            is ResultState.Error -> {
                                hideLoader()
                            }
                        }
                    }
                }

            }.await()

            repository.saveProduct(
                product = VendorProduct(
                    productId = product.value.productId,
                    status = approveProduct.value.toStatus(),
                    name = name.value,
                    minQty = minQty.value,
                    minQtyUnit = stateHolder.config.value.unitOptions.find { it.label == minQtyUnit.value }
                        ?.unit(),
                    qty = availableQty.value,
                    manageStock = "1",
                    //useConfigManageStock = makeDefaultStock.value.toInt(),
                    price = price.value,
                    sku = sku.value,
                    specialPrice = specialPrice.value,
                    specialPriceFrom = specialPriceFrom.value,
                    specialPriceTo = specialPriceTo.value,
                    newsFrom = null,
                    newsTo = null,
                    shortDescription = shortDesc.value,
                    description = longDesc.value,
                    //isInStock = qtyStatus.value.toInStock(),
                    categories = categories.value.map { it.id.toString() },
                    enabledMessages = availableMessages.value.toInt(),
                    isExclusive = onlyKarika.value.toInt(),
                    media = product.value.media,
                    thumbnail = product.value.thumbnail,
                    smallImage = product.value.smallImage,
                    swatchImage = product.value.swatchImage
                ),
                removeImages = product.value.media?.filter { it.markAsDeleted } ?: emptyList(),
                newImages = newImages,
                //primaryImage = primaryImage
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        _product.update { it.copy(productId = result.data) }
                        getProduct()
                        showMessage("Artikal je uspješno sačuvan.")
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun fillWithAi() {
        iOScope.launch {
            repository.productData(name = name.value)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            shortDesc.value = result.data.shortDescription
                            longDesc.value = result.data.description
                            aiImages.value = result.data.images
                            showAISheet.negate()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    private fun minQtyUnit(product: VendorProduct): String {
        return stateHolder.config.value.unitOptions.find {
            it.unit() == product.minQtyUnit
        }?.label() ?: "kom"
    }

    @OptIn(ExperimentalUuidApi::class)
    fun acceptAIInput() {
        showAISheet.negate()
        aiImagesSelected.value
            .forEach {
                iOScope.launch {
                    repository.getBytesFromImage(it)
                        .collect { result ->
                            if (result is ResultState.Success) {
                                _product.update {
                                    it.copy(
                                        media = it.mediaGallery.plus(
                                            MediaGallery(
                                                filename = "image.png",
                                                data = result.data
                                            )
                                        )
                                    )
                                }
                            }
                        }
                }
            }
    }

    fun categories(product: VendorProduct): List<Category> {
        val categories = ArrayDeque<Category>()
        val tmp = stateHolder.categories.value
        tmp.forEach { d1 ->
            categories.add(d1)
            d1.childrenData.forEach { d2 ->
                categories.add(d2)
                d2.childrenData.forEach { d3 ->
                    categories.add(d3)
                    d3.childrenData.forEach { d4 ->
                        categories.add(d4)
                    }
                }
            }

        }
        return categories.filter {
            product.categories.contains(it.id.toString())
        }
    }
}

private fun Boolean.toStatus(): String {
    return when (this) {
        true -> "1"
        false -> "2"
    }
}

private fun Boolean.toInt(): String {
    return when (this) {
        true -> "1"
        false -> "0"
    }
}