package cloud.bastion.configuration.config

import com.google.inject.Inject
import com.google.inject.Injector
import kotlin.reflect.full.findAnnotation

open class Config {
    @Inject private lateinit var injector: Injector

    fun save() {
        this::class.findAnnotation<Configuration>()?.let {
            injector.getInstance(it.type.java).onSave(this::class)
        }
    }
}