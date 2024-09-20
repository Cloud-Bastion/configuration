package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parsers.generated.BastionYMLBaseVisitor
import cloud.bastion.configuration.parsers.generated.BastionYMLParser
import com.google.inject.Inject
import com.google.inject.Injector
import org.antlr.v4.kotlinruntime.tree.ParseTree
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class ObjectVisitor: BastionYMLBaseVisitor<Any?>() {

    @Inject
    private lateinit var injector: Injector

    private var objectClass: KClass<*>? = null

    fun <Receiver> KMutableProperty.Setter<Receiver>.callSafe(receiver: Any?, value: BastionYMLParser.ValueContext) {
        when (value) {
            is BastionYMLParser.StringValueContext -> {
                this.call(receiver, value.text)
            }

            is BastionYMLParser.IntegerValueContext -> {
                this.call(receiver, value.text.toInt())
            }

            is BastionYMLParser.BooleanValueContext -> {
                this.call(receiver, value.text.toBoolean())
            }

            is BastionYMLParser.FloatValueContext -> {
                this.call(receiver, value.text.toFloat())
            }

            is BastionYMLParser.ListValueContext -> {
                this.call(receiver, ListVisitor().visitList(value.list()))
            }

            else -> {
                TODO("Throw exception, unhandled data type")
            }
        }
    }

    fun visit(tree: ParseTree, objectClass: KClass<*>): Any? {
        this.objectClass = objectClass
        return super.visit(tree)
    }

    override fun visitProperty(ctx: BastionYMLParser.PropertyContext): Any? {
        checkNotNull(this.objectClass)

        ctx.ID().forEachIndexed { index, keyContext ->
            println("")
            val key: String = keyContext.text
            val value: BastionYMLParser.ValueContext = ctx.value(index)
                ?: TODO("Throw exception that value is not set properly in config file")

            val instance = injector.getInstance(this.objectClass!!.java)
            val changeProperty = objectClass!!.memberProperties.find { it.name == key }
            if (changeProperty is KMutableProperty<*>) {
                changeProperty.setter.callSafe(instance, value)
            } else {
                TODO("Exception: property does not exist or is declared as immutable (val). Try changing it to var.")
            }

        }
        return super.visitProperty(ctx)
    }

    override fun visitList(ctx: BastionYMLParser.ListContext): Any? {
        return super.visitList(ctx)
    }

    override fun visitObject(ctx: BastionYMLParser.ObjectContext): Any? {


        return super.visitObject(ctx)
    }

    override fun defaultResult(): Any? {
        return null
    }
}