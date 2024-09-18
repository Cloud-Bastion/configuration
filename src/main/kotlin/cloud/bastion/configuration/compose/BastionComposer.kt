package cloud.bastion.configuration.compose

import cloud.bastion.configuration.parser.Comment
import cloud.bastion.configuration.parser.Group
import cloud.bastion.configuration.parser.Order
import com.google.common.collect.ImmutableList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

class BastionComposer {

    private object BastionFileRules {
        const val SINGLE_INDENT: Int = 3
    }

    fun <T> MutableList<T?>.safeAdd(index: Int, element: T?) {
        while (this.size <= index) {
            this.add(null)
        }
        this[index] = element
    }

    class ConfigGroup(groupName: String, groupOrder: Int = -1) {
        val properties: MutableList<KProperty1<*, *>?> = mutableListOf()
        val addLast: MutableList<KProperty1<*, *>?> = mutableListOf()
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

        val finalOrder: MutableMap<Int, Any> = mutableMapOf()
        val groups: MutableMap<String, ConfigGroup> = mutableMapOf()
        val addLast: MutableList<KProperty1<*, *>> = mutableListOf()

        configClass.memberProperties.forEach { property ->
            val group: Group? = property.findAnnotation<Group>()
            val order: Order? = property.findAnnotation<Order>()

            if (group == null && order != null) {
                finalOrder[order.position] = property
                return@forEach
            }

            if (group != null) {
                if (!groups.containsKey(group.groupName)) {
                    val configGroup: ConfigGroup = ConfigGroup(group.groupName, group.groupOrder)
                    groups[group.groupName] = configGroup
                    finalOrder[group.groupOrder] = configGroup
                }
                val currentGroup: ConfigGroup = groups[group.groupName]!!

                if (order == null) {
                    currentGroup.addLast.add(property)
                } else {
                    currentGroup.properties.safeAdd(order.position, property)
                }

                return@forEach
            }

            addLast.add(property)

        }

        val properties: MutableList<KProperty1<*, *>> = mutableListOf()
        finalOrder.entries.sortedBy { it.key }.forEach {(position, entry) ->
            run {
                when (entry) {
                    is ConfigGroup -> {
                        entry.properties.forEach {
                            if (it != null) {
                                properties.add(it)
                            }
                        }
                    }

                    is KProperty1<*, *> -> {
                        properties.add(entry)
                    }

                    else -> {
                        TODO("Throw Exception, unknown property type")
                    }
                }
            }
        }

        addLast.forEach { properties.add(it) }

        properties.forEach { property ->
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
                    val map: Map<String, Int> = mapOf()
                    //map.entries.forEach()
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