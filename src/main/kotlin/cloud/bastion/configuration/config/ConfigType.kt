package cloud.bastion.configuration.config

import kotlin.reflect.KClass

interface ConfigType {
    fun onSave(clazz: KClass<out Config>)

    fun fileExtension(): String
}