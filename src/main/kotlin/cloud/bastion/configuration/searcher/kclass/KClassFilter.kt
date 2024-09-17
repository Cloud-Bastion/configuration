package cloud.bastion.configuration.searcher.kclass

import cloud.bastion.configuration.searcher.function.FunctionFilter
import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSubclassOf

/**
 * `KClassFilter` class is a functional interface that implements Predicate for KClass objects.
 * It provides utility functions to filter classes based on their inheritance, annotations, or
 * functions that match certain criteria.
 * */
fun interface KClassFilter : Predicate<KClass<*>> {
    companion object {
        /**
         * Creates a `KClassFilter` that filters classes which are subclasses of the given superclass.
         *
         * @param superKlass The superclass to filter by.
         * @return A KClassFilter that returns true for classes that are subclasses of superKlass.
         * */
        fun subClassOf(superKlass: KClass<*>): KClassFilter {
            return KClassFilter { klass ->
                klass.isSubclassOf(superKlass)
            }
        }

        /**
         * Creates a `KClassFilter` that filters classes annotated with the given annotation.
         *
         * @param annotationClass The annotation class to filter by.
         * @return A KClassFilter that returns true for classes that are annotated with annotationClass.
         * */
        fun annotatedWith(annotationClass: KClass<out Annotation>): KClassFilter {
            return KClassFilter { klass ->
                klass.annotations.any {
                    it.annotationClass == annotationClass
                }
            }
        }

        /**
         * Creates a `KClassFilter` that filters classes based on whether any of their functions match
         * any of the provided function filters.
         *
         * @param functionFilters Vararg of FunctionFilter to apply on the class's functions.
         * @return A KClassFilter that returns true if any function in the class matches a `FunctionFilter`.
         * */
        fun matchesAnyFunctionFilter(vararg functionFilters: FunctionFilter): KClassFilter {
            return KClassFilter { klass ->
                klass.declaredFunctions.any { function ->
                    functionFilters.any { filter ->
                        filter.test(function)
                    }
                }
            }
        }
    }
}