package cloud.bastion.configuration.config.procedure

import cloud.bastion.configuration.config.Config
import cloud.bastion.configuration.config.ConfigType
import cloud.bastion.configuration.config.Configuration
import cloud.bastion.configuration.searcher.kclass.KClassFilter
import cloud.bastion.configuration.searcher.kclass.KClassSearcher
import cloud.bastion.configuration.searcher.klass.ClassFilter
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Singleton
class ConfigRegistrationProcedure {
    @Inject
    private lateinit var injector: Injector
    @Inject
    private lateinit var klassSearcher: KClassSearcher

//    fun intialize(packageNames: Array<String>) {
//        this.klassSearcher
//            .filter(
//                ClassLoader.getSystemClassLoader(),
//                packageNames,
//                ClassFilter.subClassOf(Config::class),
//                ClassFilter.annotatedWith(Configuration::class))
//            .forEach { klass -> {
//                var annotation: Configuration = klass.findAnnotation<Configuration::class>()
//                var type: ConfigType = injector.getInstance(annotation.type.java)
//                var config: Config = injector.getInstance(klass.java)
//
//                type.onLoad(config::class)
//            } }
//    }

    fun loadConfigurationFiles(packageNames: Array<String>) {
        val configurationInstances = loadConfigurationInstances(packageNames)
        configurationInstances.forEach({
            val configClass: KClass<Config> = it::class as KClass<Config>
            val configAnnotation: Configuration = getConfigAnnotation(configClass)
            injector.getInstance(configAnnotation.type.java).onLoad(configClass)
        })
    }

    private fun loadConfigurationInstances(packageNames: Array<String>): Collection<Config> {
        val instances: ArrayList<Config> = arrayListOf()
        klassSearcher.filter(packageNames, KClassFilter.annotatedWith(Configuration::class)).forEach {
            instances.add(injector.getInstance(it.java) as Config)
        }
        return instances
    }

    private fun getConfigAnnotation(configClass: KClass<Config>): Configuration {
        return configClass.findAnnotation<Configuration>()!!
    }

}