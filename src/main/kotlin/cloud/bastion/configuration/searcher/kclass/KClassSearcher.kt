package cloud.bastion.configuration.searcher.kclass

import com.google.common.base.Preconditions
import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Singleton
import com.google.inject.name.Named
import kotlin.reflect.KClass

@Singleton
class KClassSearcher {

    @Inject
    private lateinit var injector: Injector

    @Inject
    @Named("SearcherClassLoader")
    private lateinit var classLoader: ClassLoader

    fun filter(packageNames: Array<String>, vararg typeFilters: KClassFilter): Sequence<KClass<*>> {
        Preconditions.checkNotNull(packageNames)
        println("TRYING TO RUN TEST with classloader: ${classLoader.name}")
        return try {
            val classPath = ClassPath.from(classLoader)
            (if (packageNames.isEmpty()) classPath.allClasses
            else packageNames.asSequence().flatMap { name -> classPath.getTopLevelClassesRecursive(name).asSequence() }
                .toList()).asSequence().mapNotNull { classInfo -> classInfo.load().kotlin }
                .filter { type -> typeFilters.all { it.test(type) } }
        } catch (e: Exception) {
            e.printStackTrace()
            emptySequence()
        }
    }

}