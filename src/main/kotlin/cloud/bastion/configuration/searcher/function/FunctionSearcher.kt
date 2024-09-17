package cloud.bastion.configuration.searcher.function

import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.name.Named
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

/**
 * The `FunctionSearcher` class is responsible for searching and filtering Kotlin functions (KFunction)
 * within specific classes. It uses the Guava ClassPath library to dynamically load classes and for
 * functions based on the applied filters.
 * */
class FunctionSearcher {
    // Injector for dependency injection, used to get instances of other required classes.
    @Inject
    private lateinit var injector: Injector

    // Custom ClassLoader injected by Guice, used to search for classes.
    @Named("SearcherClassLoader")
    @Inject
    private lateinit var searcherClassLoader: ClassLoader

    // List of ClassLoaders to use for searching functions within classes..
    private var classLoaderList: MutableList<ClassLoader> = mutableListOf()

    // List of package names to limit the search scope to specific packages.
    private var packageNames: MutableList<String> = mutableListOf()

    // List of `FunctionFilter` to apply during the function search.
    private var functionFilters: MutableList<FunctionFilter> = mutableListOf()

    /**
     * Adds the given class loaders to the list used for searching functions within classes.
     *
     * @param classLoader Vararg of ClassLoader to be added to the search context.
     * @return The current `FunctionSearcher` instance for method chaining.
     */
    fun filter(vararg classLoader: ClassLoader): FunctionSearcher {
        classLoader.forEach { this.classLoaderList.add(it) }
        return this
    }

    /**
     * Adds the given package names to the list used for restricting the search to specific packages.
     *
     * @param packageNames Vararg of package names to be added to the search context.
     * @return The current `FunctionSearcher` instance for method chaining.
     */
    fun filter(vararg packageNames: String): FunctionSearcher {
        packageNames.forEach { this.packageNames.add(it) }
        return this
    }

    /**
     * Adds the given function filters to the list used for filtering the functions during the search.
     *
     * @param functionFilters Vararg of FunctionFilter to be added.
     * @return The current `FunctionSearcher` instance for method chaining.
     */
    fun filter(vararg functionFilters: FunctionFilter): FunctionSearcher {
        functionFilters.forEach { this.functionFilters.add(it) }
        return this;
    }

    /**
     * Finds and returns a sequence of KFunction objects that match all the filters applied.
     *
     * The search is conducted across all the class loaders and package names specified, and only the
     * functions that pass all the filters are included in the result.
     *
     * @return A sequence of KFunction objects that match the search criteria.
     * @throws NoSuchElementException if no package names were specified.
     */
    fun find(): Sequence<KFunction<*>> {
        // Add the injected class loader to the list of class loaders for searching
        this.classLoaderList.add(searcherClassLoader)

        // If no package names were specified, throw an exception
        if (this.packageNames.isEmpty()) throw NoSuchElementException("No package names were specified!")

        // Search through all class loaders and apply the filters.
        return classLoaderList.asSequence().flatMap { classLoader ->
            val classPath = ClassPath.from(classLoader)
            (packageNames.asSequence().flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                .toList()).asSequence().mapNotNull { classInfo -> classInfo.load().kotlin }.flatMap { clazz ->
                    // Extract declared functions from each class.
                    clazz.declaredFunctions.asSequence()
                }
        }.filter { function ->
            // Apply all function filters to each found function.
            functionFilters.all { it.test(function) }
        }
    }
}