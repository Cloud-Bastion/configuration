package cloud.bastion.configuration.config

import cloud.bastion.configuration.config.type.BastionConfigType
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configuration(
    val path: String = "",
    val filename: String,
    val type: KClass<out ConfigType> = BastionConfigType::class
)
