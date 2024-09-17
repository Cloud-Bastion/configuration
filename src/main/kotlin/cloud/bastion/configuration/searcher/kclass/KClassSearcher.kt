package cloud.bastion.configuration.searcher.kclass

import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.name.Named
import kotlin.reflect.KClass

/**
 * `KClassSearcher` is responsible for searching and filtering KClass instances based on various criteria,
 * such as class loaders, package names and class filters. It uses the Guava ClassPath library to scan
 * available classes dynamically.
 * */
class KClassSearcher {
    // Injector for dependency injection, used to get instances of other required classes.
    @Inject
    private lateinit var injector: Injector

    // Custom ClassLoader injected by Guice, used to search for classes.
    @Named("SearcherClassLoader")
    @Inject
    private lateinit var searcherClassLoader: ClassLoader

    // List of ClassLoaders to use for searching classes.
    private var classLoaderList: MutableList<ClassLoader> = mutableListOf()

    // List of package names to limit the search scope to specific packages.
    private var packageNames: MutableList<String> = mutableListOf()

    // List of `KClassFilter` to apply during the class search.
    private var klassFilters: MutableList<KClassFilter> = mutableListOf()

    /**
     * Adds the given class loaders to the list used for searching.
     *
     * @param classLoader vararg of `ClassLoader` to be added.
     * @return The current `KClassSearcher` instance for method chaining.
     * */
    fun filter(vararg classLoader: ClassLoader): KClassSearcher {
        classLoader.forEach { this.classLoaderList.add(it) }
        return this
    }

    /**
     * Adds the given package names to the list used for limiting the search to specific packages.
     *
     * @param packageNames vararg of package names to be added.
     * @return The current `KClassSearcher` instance for method chaining.
     * */
    fun filter(vararg packageNames: String): KClassSearcher {
        packageNames.forEach { this.packageNames.add(it) }
        return this
    }

    /**
     * Adds the given `KClassFilter` to the list used for filtering classes during the search.
     *
     * @param klassFilters vararg of `KClassFilter` to be added.
     * @return The current `KClassSearcher` instance for method chaining.
     * */
    fun filter(vararg klassFilters: KClassFilter): KClassSearcher {
        klassFilters.forEach { this.klassFilters.add(it) }
        return this;
    }

    /**
     * Finds and returns a sequence of KClass objects that match all the filters applied
     *
     * The search is performed across all the class loaders and packages specified, and only
     * the classes that pass all the filters are included in the result.
     *
     * @return A sequence of KClass objects that match the search criteria.
     * @throws NoSuchElementException if no package names were specified.
     * */
    fun find(): Sequence<KClass<*>> {
        // Add the injected class loader to the list of class loaders for searching
        this.classLoaderList.add(searcherClassLoader)

        // If no package names were specified, throw an exception
        if (this.packageNames.isEmpty()) throw NoSuchElementException("No package names were specified!")

        return classLoaderList.asSequence().flatMap { classLoader ->
            // If no packages are specified, search all available classes.
            // Otherwise, limit the search to the specified package names.
            val classPath = ClassPath.from(classLoader)
            (packageNames.asSequence().flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                .toList()).asSequence()
                // Convert Java classes to Kotlin KClass objects .
                .mapNotNull { classInfo -> classInfo.load().kotlin }
        }.filter { klass ->
            // Apply all `KClassFilter` to each found class.
            klassFilters.all { it.test(klass) }
        }
    }
}