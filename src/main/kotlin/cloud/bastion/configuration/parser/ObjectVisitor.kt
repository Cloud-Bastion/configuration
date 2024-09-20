package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parser.generated.BastionYMLBaseVisitor
import cloud.bastion.configuration.parser.generated.BastionYMLParser
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
    private var recursiveInstance: Any? = null

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

            is BastionYMLParser.ObjectValueContext -> {
                val result: Any? = injector.getInstance(ObjectVisitor::class.java).visitObjectRecursively(value.object_(), (this.property.returnType.classifier as KClass<*>))
                this.call(receiver, result!!)
            }

            else -> {
                TODO("Throw exception, unhandled datatype or object with an invalid definition")
            }
        }
    }

    fun visit(tree: ParseTree, objectClass: KClass<*>): Any? {
        this.objectClass = objectClass
        return super.visit(tree)
    }

    private fun visitObjectRecursively(ctx: BastionYMLParser.ObjectContext, objectClass: KClass<*>): Any? {
        this.objectClass = objectClass
        this.recursiveInstance = injector.getInstance(this.objectClass!!.java)
        ctx.property().forEach {
            this.visitProperty(it)
        }
        return recursiveInstance
    }

    override fun visitProperty(ctx: BastionYMLParser.PropertyContext): Any? {
        fun getInstance(): Any {
            if (this.recursiveInstance != null) {
                return this.recursiveInstance!!
            } else {
                return injector.getInstance(this.objectClass!!.java)
            }
        }

        checkNotNull(this.objectClass)

        ctx.ID().forEachIndexed { index, keyContext ->
            val key: String = keyContext.text
            val value: BastionYMLParser.ValueContext = ctx.value(index)
                ?: TODO("Throw exception that value is not set properly in config file")

            val instance = getInstance()
            val changeProperty = objectClass!!.memberProperties.find { it.name == key }
            if (changeProperty is KMutableProperty<*>) {
                changeProperty.setter.callSafe(instance, value)
            } else {
                // TODO("Exception: property does not exist or is declared as immutable (val). Try changing it to var.")
            }

        }
        return super.visitProperty(ctx)
    }

    override fun defaultResult(): Any? {
        return null
    }

}