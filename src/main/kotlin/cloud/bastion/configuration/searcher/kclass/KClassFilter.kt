package cloud.bastion.configuration.searcher.kclass

import cloud.bastion.configuration.searcher.function.FunctionFilter
import java.util.function.Predicate
import kotlin.reflect.KClass

fun interface KClassFilter: Predicate<KClass<*>> {
    companion object {
        fun subClassOf(vararg classes: KClass<*>) {
        }

        fun matchFunctions(vararg functionFilter: FunctionFilter) {}

        fun annotatedWith(annotationClass: KClass<out Annotation>): KClassFilter {
            return KClassFilter {
                type -> type.annotations.any {
                    it.annotationClass == annotationClass
                }
            }
        }
    }
}