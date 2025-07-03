package karika.distribucija.ba

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform