package cloud.bastion.configuration.searcher.function

import com.google.common.base.Preconditions
import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Singleton
import com.google.inject.name.Named
import com.google.inject.name.Names
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

@Singleton
class FunctionSearcher {
    @Inject
    private lateinit var injector: Injector

    @Inject
    @Named("SearcherClassLoader")
    private lateinit var classLoader: ClassLoader

    fun filter(packageNames: Array<String>, vararg functionFilters: FunctionFilter): Sequence<KFunction<*>> {
        Preconditions.checkNotNull(packageNames)
        println("TRYING TO RUN TEST with classloader: ${classLoader.name}")
        return try {
            val classPath = ClassPath.from(classLoader)
            (if (packageNames.isEmpty()) classPath.allClasses
            else packageNames.asSequence().flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                .toList()).asSequence().mapNotNull { classInfo -> classInfo.load().kotlin }
                .flatMap { clazz -> clazz.declaredFunctions.asSequence() }
                .filter { function -> functionFilters.all { it.filter(function) } }
        } catch (e: Exception) {
            e.printStackTrace()
            emptySequence()
        }
    }

    fun filterDebug(packageNames: Array<String>, vararg functionFilters: FunctionFilter): Sequence<KFunction<*>> {
        Preconditions.checkNotNull(packageNames)
        println("Starting filter operation with packages: ${packageNames.joinToString()}")

        return try {
            println("ClassLoader acquired: ${classLoader::class.java.name}")

            val classPath = ClassPath.from(classLoader)
            println("ClassPath loaded with ${classPath.allClasses.size} classes.")

            val classes = if (packageNames.isEmpty()) {
                println("No package names provided. Searching all classes.")
                classPath.allClasses
            } else {
                println("Searching classes in packages: ${packageNames.joinToString()}")
                packageNames.asSequence()
                    .flatMap { name ->
                        println("Fetching top-level classes for package: $name")
                        classPath.getTopLevelClassesRecursive(name).asSequence()
                    }.toList()
            }

            println("Total classes found: ${classes.size}")

            classes.asSequence()
                .mapNotNull { classInfo ->
                    try {
                        val kotlinClass = classInfo.load().kotlin
                        println("Loaded Kotlin class: ${kotlinClass.simpleName}")
                        kotlinClass
                    } catch (throwable: Throwable) {
                        println("Failed to load class: ${classInfo.name}")
                        null
                    }
                }
                .flatMap { clazz ->
                    println("Found ${clazz.declaredFunctions.size} functions in class: ${clazz.simpleName}")
                    clazz.declaredFunctions.asSequence()
                }
                .filter { function ->
                    val matches = functionFilters.all { it.filter(function) }
                    println("Function ${function.name} matches all filters: $matches")
                    matches
                }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            e.printStackTrace()
            emptySequence()
        }
    }

}
