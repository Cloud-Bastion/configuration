package cloud.bastion.configuration.searcher.kclass

import java.util.function.Predicate
import kotlin.reflect.KClass

fun interface KClassFilter: Predicate<KClass<*>> {
    companion object {
        fun annotatedWith(annotationClass: KClass<out Annotation>): KClassFilter {
            return KClassFilter {
                type -> type.annotations.any {
                    it.annotationClass == annotationClass
                }
            }
        }
    }
}