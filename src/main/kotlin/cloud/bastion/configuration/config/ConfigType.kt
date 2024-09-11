package cloud.bastion.configuration.config

import kotlin.reflect.KClass

interface ConfigType {
    fun onLoad(clazz: KClass<out Config>)

    fun onSave(clazz: KClass<out Config>)

    fun fileExtension(): String
}