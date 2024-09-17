package cloud.bastion.configuration.searcher.function

import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * `FunctionFilter` is a functional interface that implements Predicate for KFunction objects.
 * It provides a utility function to filter functions based on annotations or functions
 * that match certain criteria.
 * */
fun interface FunctionFilter : Predicate<KFunction<*>> {
    companion object {
        /**
         * Creates a `FunctionFilter` that filters functions annotated with the given annotation.
         *
         * @param annotationClass The annotation class to filter by.
         * @return A `FunctionFilter` that returns true for functions annotated with annotationClass.
         * */
        fun annotatedWith(annotationClass: KClass<out Annotation>): FunctionFilter {
            return FunctionFilter { function ->
                // Check if the function has the specified annotation.
                function.annotations.any {
                    it.annotationClass == annotationClass
                }
            }
        }
    }
}
