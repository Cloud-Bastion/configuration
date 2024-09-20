package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parser.generated.BastionYMLLexer
import cloud.bastion.configuration.parser.generated.BastionYMLParser
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.antlr.v4.kotlinruntime.CharStream
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import java.io.File

fun main() {
    ConfigurationParserService().parse()
}

class SimpleModule: AbstractModule() {
    override fun configure() {

    }
}

class ConfigurationParserService {
    fun parse() {

        val injector: Injector = Guice.createInjector(SimpleModule())

        // DO NOT COMMIT THIS LINE
        val testFile: File = File("F:/CloudBastion/configuration/config/", "test.bastion")

        System.err.println(testFile.absolutePath)
        if (!testFile.exists()) {
            System.err.println("File doesn't exist ${testFile.name}")
            System.exit(1);
            return
        }


        val runs: MutableList<Long> = mutableListOf()
        val inputStream: CharStream = CharStreams.fromFileName(testFile.absolutePath)

        val lexer = BastionYMLLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)

        val parser = BastionYMLParser(tokenStream)

        //val tree: BastionYMLParser.FileContext = parser.file()

        val ov = injector.getInstance(ObjectVisitor::class.java)
        ov.visit(parser.file(), TestConfig::class)

        println(injector.getInstance(TestConfig::class.java).host)
        println(injector.getInstance(TestConfig::class.java).port)
        println(injector.getInstance(TestConfig::class.java).blockedPorts)
        println(injector.getInstance(TestConfig::class.java).player.name)
        println(injector.getInstance(TestConfig::class.java).player.age)
        println(injector.getInstance(TestConfig::class.java).player.status?.message)
        println(injector.getInstance(TestConfig::class.java).player.status?.lastChanged)

        // var s = FileVisitor<TestConfig>(testConfig).visitFile(parser.file())
        // println(s)
        // println(tree.line().toString()) // List of top level elements, in this case words ('lines' in the grammar)
        //println(tree.toStringTree())
    }
}