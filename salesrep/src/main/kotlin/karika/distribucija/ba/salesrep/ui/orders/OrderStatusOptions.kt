package karika.distribucija.ba.salesrep.ui.orders

/** Statuses an order can be filtered by - same list and order as orderStatusOptions in
 * composeApp's SalesOrdersView.kt. */
internal val orderStatusOptions = listOf(
    "pending" to "Na čekanju",
    "processing" to "U obradi",
    "approved" to "Odobreno",
    "bill-sent" to "Uplaćeno",
    "estimate-sent" to "Čekanje na uplatu",
    "rejected" to "Odbijeno",
    "cancelled" to "Otkazano"
)
