package cloud.bastion.configuration.config.procedure

import cloud.bastion.configuration.config.Config
import cloud.bastion.configuration.config.ConfigType
import cloud.bastion.configuration.config.Configuration
import cloud.bastion.configuration.searcher.kclass.KClassFilter
import cloud.bastion.configuration.searcher.kclass.KClassSearcher
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import kotlin.reflect.full.findAnnotation

@Singleton
/**
 * `ConfigRegistrationProcedure` class is responsible for initializing and loading config classes.
 *
 * It scans the specified package names for classes that meet certain filter criteria, such as inheriting from
 * `Config` class and being annotated with @Configuration. For each found configuration class, the corresponding
 * configuration is loaded and saved.
 * */
class ConfigRegistrationProcedure {
    // Injector for dependency injection, used to get instances of other required classes.
    @Inject
    private lateinit var injector: Injector

    /**
     * Initializes the registration process for configuration classes.
     *
     * This method scans all specified package names for classes that meet certain filter criteria. For each
     * found class, the corresponding configuration is loaded and saved.
     *
     * @param packageNames The package names to search for configuration classes.
     * */
    fun initialize(vararg packageNames: String) {
        // Get an instance of KClassSearcher from the injector.
        // This class is used to search for classes based on the specified packages and filters.
        injector.getInstance(KClassSearcher::class.java)
            // Filters classes based on the specified package names.
            .filter(*packageNames)
            // Filters classes that are inheritances from `Config` class.
            .filter(KClassFilter.subClassOf(Config::class))
            // Filters classes that annotated with @Configuration.
            .filter(KClassFilter.annotatedWith(Configuration::class))
            // Finds the classes that meet all filter criteria.
            .find().forEach { klass ->
                // Retrieves the @Configuration annotation from the class.
                val annotation: Configuration = klass.findAnnotation<Configuration>()!!

                // Gets an instance of the class that extends Config.
                val config: Config = (injector.getInstance(klass.java) as Config?)!!

                // Gets the ConfigType instance based on the type specified the annotation.
                val type: ConfigType = injector.getInstance(annotation.type.java)

                // Calls onSave method of the ConfigType instance and passes configuration class.
                // This method create (when not exists) and save changes of the configuration.
                type.onSave(config::class)
            }
    }
}