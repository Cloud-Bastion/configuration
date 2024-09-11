package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parsers.generated.BastionYMLBaseVisitor
import cloud.bastion.configuration.parsers.generated.BastionYMLParser
import kotlin.reflect.KParameter

class SimplePropertyVisitor : BastionYMLBaseVisitor<Collection<ConfigProperty>>() {

    // []                  list
    // [1]                 list -> [ element ]
    // [1, 2]              list -> [ 1, element ]
    // [1, 2, 3, 4]

    override fun visitFile(ctx: BastionYMLParser.FileContext): Collection<ConfigProperty> {
        return super.visitFile(ctx)
    }

    override fun visitLine(ctx: BastionYMLParser.LineContext): Collection<ConfigProperty> {


        return super.visitLine(ctx)
    }

    override fun visitKeyValuePair(ctx: BastionYMLParser.KeyValuePairContext): Collection<ConfigProperty> {
        return listOf(ConfigProperty(ctx.ID().text, ctx.value().text))
    }

    private fun aggregateList(originalList: ArrayList<ConfigProperty>, newElement: ConfigProperty): Collection<ConfigProperty> {
        originalList.add(newElement)
        return originalList
    }

    override fun defaultResult(): Collection<ConfigProperty> {
        return emptyList()
    }


}