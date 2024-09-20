package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parser.generated.BastionYMLBaseVisitor
import cloud.bastion.configuration.parser.generated.BastionYMLParser

class ListVisitor: BastionYMLBaseVisitor<List<Any>>() {

    override fun visitList(ctx: BastionYMLParser.ListContext): List<Any> {
        val output: MutableList<Any> = mutableListOf()
        ctx.value().forEach {
            if (it is BastionYMLParser.ListValueContext) {
                visitList(it.list())
            }
            output.add(it.text)
        }
        return output
    }

    override fun defaultResult(): List<Any> {
        return emptyList<Any>()
    }

}