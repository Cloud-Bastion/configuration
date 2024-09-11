package cloud.bastion.configuration.searcher.function

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface FunctionFilter {
    fun filter(function: KFunction<*>): Boolean

    companion object {
        fun annotatedWith(annotationClass: KClass<out Annotation>): FunctionFilter {
            return object : FunctionFilter {
                override fun filter(function: KFunction<*>): Boolean {
                    return function.annotations.any {
                        it.annotationClass == annotationClass
                    }
                }
            }
        }
    }
}
