package cloud.bastion.configuration.parser

import cloud.bastion.configuration.parsers.generated.BastionYMLLexer
import cloud.bastion.configuration.parsers.generated.BastionYMLParser
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import java.io.File

fun main() {
    ConfigurationParserService().parse()
}

class ConfigurationParserService {
    fun parse() {
        // DO NOT COMMIT THIS LINE
        val testFile: File = File("F:/CloudBastion/configuration/config/", "test.bastion")

        System.err.println(testFile.absolutePath)
        if (!testFile.exists()) {
            System.err.println("File doesn't exist ${testFile.name}")
            System.exit(1);
            return
        }

        val inputStream = CharStreams.fromString(testFile.readText())
        val lexer = BastionYMLLexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = BastionYMLParser(tokenStream)

        //val tree: BastionYMLParser.FileContext = parser.file()

        var s = SimplePropertyVisitor().visit(parser.keyValuePair())
        println(s)

        // println(tree.line().toString()) // List of top level elements, in this case words ('lines' in the grammar)
        //println(tree.toStringTree())
    }
}