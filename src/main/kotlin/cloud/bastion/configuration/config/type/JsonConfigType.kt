package cloud.bastion.configuration.config.type

import cloud.bastion.configuration.config.Config
import cloud.bastion.configuration.config.ConfigType
import cloud.bastion.configuration.config.Configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Injector
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class JsonConfigType : ConfigType {
    @Inject
    private lateinit var injector: Injector

    @Inject
    private lateinit var jacksonObjectMapper: ObjectMapper

    override fun onSave(clazz: KClass<out Config>) {
        val annotation = clazz.findAnnotation<Configuration>()
        val file = if (!annotation?.path.isNullOrEmpty()) {
            File("./${annotation?.path}", "${annotation?.filename}.${fileExtension()}")
        } else {
            File(".", "${annotation?.filename}.${fileExtension()}")
        }

        if (!file.exists() && !file.mkdirs()) {
            System.err.println("Failed to create file or directory: ${file.absolutePath}")
            return
        }

        if (!file.canRead()) {
            System.err.println("Failed to read file: ${file.absolutePath}")
            return
        }

        if (!file.exists()) {
            if (!file.parentFile.exists() && !file.parentFile.mkdirs()) {
                System.err.println("Failed to create directory ${file.parentFile}")
                return
            }

            if (!file.createNewFile()) {
                System.err.println("Failed to create file: ${file.absolutePath}")
                return
            }
            file.writeText(jacksonObjectMapper.writeValueAsString(injector.getInstance(clazz.java)))
        } else {
            file.writeText(
                jacksonObjectMapper.writeValueAsString(
                    jacksonObjectMapper.readValue(
                        file.readText(), clazz.java
                    )
                )
            )
        }
    }

    override fun fileExtension(): String {
        return "json"
    }
}