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
        val testFile: File = File("C:/Development/CLOUD-BASTION/configuration/config/", "test.bastion")

        System.err.println(testFile.absolutePath)
        if (!testFile.exists()) {
            System.err.println("File doesn't exist ${testFile.name}")
            System.exit(1);
            return
        }

        val inputStream = CharStreams.fromString(testFile.readText())
        val lexer = BastionYMLLexer(inputStream)
        val toketStream = CommonTokenStream(lexer)
        val parser = BastionYMLParser(toketStream)
        val tree = parser.file()

        println(tree.toStringTree())
    }
}