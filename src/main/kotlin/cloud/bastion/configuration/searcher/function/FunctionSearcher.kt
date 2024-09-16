package cloud.bastion.configuration.searcher.function

import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.name.Named
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions

class FunctionSearcher {
    @Inject
    private lateinit var injector: Injector

    @Named("SearcherClassLoader")
    @Inject
    private lateinit var searcherClassLoader: ClassLoader

    private var classLoaderList: MutableList<ClassLoader> = mutableListOf()
    private var packageNames: MutableList<String> = mutableListOf()
    private var functionFilters: MutableList<FunctionFilter> = mutableListOf()

    fun filter(vararg classLoader: ClassLoader): FunctionSearcher {
        classLoader.forEach { this.classLoaderList.add(it) }
        return this
    }

    fun filter(vararg packageNames: String): FunctionSearcher {
        packageNames.forEach { this.packageNames.add(it) }
        return this
    }

    fun filter(vararg functionFilters: FunctionFilter): FunctionSearcher {
        functionFilters.forEach { this.functionFilters.add(it) }
        return this;
    }

    fun find(): Sequence<KFunction<*>> {
        this.classLoaderList.add(searcherClassLoader)
        return classLoaderList
            .asSequence()
            .flatMap { classLoader ->
                val classPath = ClassPath.from(classLoader)
                (
                        if (packageNames.isEmpty()) classPath.allClasses
                        else packageNames
                            .asSequence()
                            .flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                            .toList()
                        )
                    .asSequence()
                    .mapNotNull { classInfo -> classInfo.load().kotlin }
                    .flatMap { clazz -> clazz.declaredFunctions.asSequence() }
            }
            .filter { function -> functionFilters.all { it.test(function) } }
    }

    private fun filterFunction(classPath: ClassPath): Sequence<KFunction<*>> {
        return (
                if (packageNames.isEmpty()) classPath.allClasses
                else packageNames
                    .asSequence()
                    .flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                    .toList())
            .asSequence()
            .mapNotNull { classInfo -> classInfo.load().kotlin }
            .flatMap { clazz -> clazz.declaredFunctions.asSequence() }
            .filter { function -> functionFilters.all { it.test(function) } }
    }
}