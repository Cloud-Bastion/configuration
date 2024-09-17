package cloud.bastion.configuration.compose

import cloud.bastion.configuration.parser.Comment
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class BastionComposer {

    private object BastionFileRules {
        const val SINGLE_INDENT: Int = 3
    }

    fun generateConfig(config: Any, recursionDepth: Int = 0): String {

        val output: StringBuilder = StringBuilder()
        val configClass: KClass<*> = config::class

        fun generateIndent(additionalIndent: Int = 0) {
            for (recursionLayer in 0 until (recursionDepth + additionalIndent)) {
                for (spaces in 0 until BastionFileRules.SINGLE_INDENT) {
                    output.append(" ")
                }
            }
        }

        fun formatList(list: List<*>, listRecursionDepth: Int = 0) {
            list.forEach {
                // We need an additional layer of indentation as
                // list elements shall be indented by one level to
                // separate them from other properties.
                generateIndent(additionalIndent = 1 + listRecursionDepth)
                when (it) {
                    is String -> output.append("\"$it\",\n")
                    is Int -> output.append("$it,\n")
                    is List<*> -> {
                        output.append("[\n")
                        formatList(it, listRecursionDepth + 1)
                        generateIndent(additionalIndent = listRecursionDepth + 1)
                        output.append("],\n")
                    }
                    is Any -> {
                        output.append("{\n")
                        output.append(generateConfig(it, listRecursionDepth + 2))
                        generateIndent(additionalIndent = 1)
                        output.append("{,\n")
                    }
                }
            }
        }

        /*

            Todo for tomorrow:
            - Implement Map composition with the following pattern:
                map: (
                    key = value,
                    key2 = value2,
                    key4 = [listE1, listE2, ....]
                )
            - the best approach would be to use the same structure as the top-level function
            - however, the property iteration has to be replaced with a more universal operation
              as we cannot simply iterate over class-level properties in a map here.
            - So we have to iterate the properties separately in the top level function,
              which we have to do anyway to implement @Order and @Group annotations for
              property order

         */

        configClass.memberProperties.forEach { property ->
            val docs: Comment? = property.findAnnotation<Comment>()

            // if the current member is annotated with @Documentation
            // add the comment given in the documentation to the output
            if (docs != null) {
                // add an empty line before each comment block to separate it from
                // previous members.
                output.append("\n")
                docs.value.split("\n").forEach {
                    // it.trimIndent() automatically removes any white spaces
                    // made in the source code for better readability.
                    generateIndent()
                    output.append("# ").append(it.trimIndent()).append("\n")
                }
            }
            generateIndent()

            when (val value = property.getter.call(config)) {
                is String -> output.append("${property.name} = \"$value\"\n")
                is Int -> output.append("${property.name} = $value\n")
                is List<*> -> {
                    output.append("${property.name} = [\n")
                    formatList(value)
                    generateIndent()
                    output.append("]\n")
                }
                is Map<*,*> -> {

                }
//                is Sequence<*> -> {
//
//                }
                // ...
                is Any -> {
                    // if it's a nested object, recursively generate the values for it.
                    output.append("${property.name} = {\n")
                    output.append(generateConfig(value, recursionDepth + 1))
                    generateIndent()
                    output.append("}\n")
                }
                else -> {
                    println("Error: No composition rule for configuration value ${property.name} found! Skipping..")
                }
            }
        }

        return output.toString()
    }

    fun serializeMember() {

    }

}