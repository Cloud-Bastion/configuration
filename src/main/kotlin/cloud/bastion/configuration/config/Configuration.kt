package cloud.bastion.configuration.config

import cloud.bastion.configuration.config.type.BastionConfigType
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Configuration(
    val path: String = "",
    val filename: String,
    val type: KClass<out ConfigType> = BastionConfigType::class
)
