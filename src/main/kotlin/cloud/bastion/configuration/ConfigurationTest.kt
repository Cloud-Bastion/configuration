package cloud.bastion.configuration

import cloud.bastion.configuration.searcher.function.FunctionFilter
import cloud.bastion.configuration.searcher.function.FunctionSearcher
import cloud.bastion.configuration.test.TestAnnotation
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.name.Names
import java.time.Duration

class TestModule : AbstractModule() {
    override fun configure() {
        bind(ClassLoader::class.java)
            .annotatedWith(Names.named("SearcherClassLoader"))
            .toInstance(ClassLoader.getSystemClassLoader())
    }
}

class Blob {
    fun main123() {
            println("jan stinkt")
            Thread.sleep(Duration.ofMillis(100))
    }
}

fun main() {
    while (true) {
        Blob().main123()
    }
//    val injector = Guice.createInjector(TestModule())
//    println("RUNNING ALL FILES")
//    injector.getInstance(FunctionSearcher::class.java)
//        .filter(arrayOf("cloud.bastion.configuration"), FunctionFilter.annotatedWith(TestAnnotation::class))
//        .forEach { function ->
//            println("Found function ${function.name} with annotation: ${function.annotations.get(0)}")
//        }
}

class testclass {
    @TestAnnotation
    fun thisIsATestFunction() {
        println("WORKED!")
    }
}