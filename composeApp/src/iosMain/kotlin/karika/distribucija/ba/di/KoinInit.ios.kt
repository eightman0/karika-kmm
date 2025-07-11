package karika.distribucija.ba.di

import org.koin.dsl.module

actual fun platformModule() = module {
}

fun initKoinIos(
    manager: PersistenceManager
) {
    initKoin(
        listOf(
            module {
                single<PersistenceManager> { manager }
            }
        )
    )
}