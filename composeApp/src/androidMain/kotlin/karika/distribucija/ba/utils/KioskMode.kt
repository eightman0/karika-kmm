package karika.distribucija.ba.utils

interface KioskMode {
    fun enter()
    fun exit()
    fun isAdmin(): Boolean
}