package cloud.bastion.configuration.config.type

import cloud.bastion.configuration.config.Config
import cloud.bastion.configuration.config.ConfigType
import com.google.inject.Inject
import com.google.inject.Injector
import kotlin.reflect.KClass

open class BastionConfigType : ConfigType {
    @Inject
    private lateinit var injector: Injector

    override fun onSave(clazz: KClass<out Config>) {
        TODO("Not yet implemented")
    }

    override fun fileExtension(): String {
        return "bastion"
    }
}