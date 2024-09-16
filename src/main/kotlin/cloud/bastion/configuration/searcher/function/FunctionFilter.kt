package cloud.bastion.configuration.searcher.function

import java.util.function.Predicate
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

fun interface FunctionFilter: Predicate<KFunction<*>> {
    companion object {
        fun annotatedWith(annotationClass: KClass<out Annotation>): FunctionFilter {
            return FunctionFilter { function ->
                function.annotations.any {
                    it.annotationClass == annotationClass
                }
            }
        }
    }
}
