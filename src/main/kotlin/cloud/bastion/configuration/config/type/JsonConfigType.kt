package cloud.bastion.configuration.config.type

import cloud.bastion.configuration.config.Config
import cloud.bastion.configuration.config.ConfigType
import cloud.bastion.configuration.config.Configuration
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.inject.Inject
import com.google.inject.Injector
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class JsonConfigType : ConfigType {
    @Inject
    private lateinit var injector: Injector

    private var gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override fun onLoad(clazz: KClass<out Config>) {
        val annotation = clazz.findAnnotation<Configuration>()
        val file = if (!annotation?.path.isNullOrEmpty()) {
            File("./${annotation?.path}", "${annotation?.filename}.${fileExtension()}")
        } else {
            File(".", "${annotation?.filename}.${fileExtension()}")
        }

        val instance: Config = if (file.exists()) {
            val bufferedReader = BufferedReader(file.bufferedReader())
            val existingConfig = gson.fromJson(bufferedReader, clazz.java)
            bufferedReader.close()

            val newInstance = injector.getInstance(clazz.java)
            clazz.memberProperties.forEach { property ->
                property.isAccessible = true
                val existingValue = property.call(existingConfig)
                if (existingValue == null) {
                    val dummyValue = getDummyValue(property.returnType.classifier as KClass<*>)
                    (property as KMutableProperty<*>).setter.call(newInstance, dummyValue)
                } else {
                    (property as KMutableProperty<*>).setter.call(newInstance, existingValue)
                }
            }
            newInstance
        } else {
            if (!file.parentFile.exists() && !file.parentFile.mkdirs()) {
                //TODO("THROW SOME ERROR BY CREATION, DONT HAVE ACCESS TO CREATE DIRECTORY")
                return
            }

            if (!file.createNewFile()) {
                //TODO("THROW SOME ERROR BY CREATION, DONT HAVE ACCESS TO CREATE FILE")
                return
            }

            val newInstance = injector.getInstance(clazz.java)
            onSave(clazz)
            newInstance
        }

        injector.getInstance(clazz.java)
    }

    override fun onSave(clazz: KClass<out Config>) {
        val annotation = clazz.findAnnotation<Configuration>()
        val file = if (!annotation?.path.isNullOrEmpty()) {
            File("./${annotation?.path}", "${annotation?.filename}.${fileExtension()}")
        } else {
            File(".", "${annotation?.filename}.${fileExtension()}")
        }

        if (!file.exists() && !file.mkdirs()) {
            //TODO("THROW SOME ERROR BY CREATION, DONT HAVE ACCESS TO CREATE DIRECTORY")
            return
        }

        if (!file.canRead()) {
            //TODO("THROW SOME ERROR BY READING ACCESS IS DENIED")
            return
        }

        val instance = injector.getInstance(clazz.java)


        if (file.exists()) {
            val bufferedReader = BufferedReader(file.bufferedReader())
            val existingConfig = gson.fromJson(bufferedReader, clazz.java)
            bufferedReader.close()

            clazz.memberProperties.forEach { property ->
                property.isAccessible = true
                val existingValue = property.call(existingConfig)
                val currentValue = property.call(instance)

                if (existingValue != currentValue && currentValue != null) {
                    (property as KMutableProperty<*>).setter.call(existingConfig, currentValue)
                }
            }

            FileWriter(file).use { writer ->
                gson.toJson(existingConfig, writer)
            }
        } else {
            FileWriter(file).use { writer ->
                gson.toJson(injector.getInstance(clazz.java), writer)
            }
        }
    }

    private fun getDummyValue(type: KClass<*>): Any? {
        return when {
            type.isSubclassOf(String::class) -> "dummy"
            type.isSubclassOf(Boolean::class) -> false
            type.isSubclassOf(Int::class) -> 0
            type.isSubclassOf(Double::class) -> 0.0
            type.isSubclassOf(Float::class) -> 0.0f

            type.isSubclassOf(List::class) -> {
                val elementType =
                    (type.supertypes.find { it.classifier == List::class }?.arguments?.firstOrNull()?.type?.classifier as? KClass<*>)
                if (elementType != null) {
                    listOf(getDummyValue(elementType))
                } else {
                    emptyList<Any>()
                }
            }

            type.isSubclassOf(Map::class) -> {
                val keyType =
                    (type.supertypes.find { it.classifier == Map::class }?.arguments?.firstOrNull()?.type?.classifier as? KClass<*>)
                val valueType =
                    (type.supertypes.find { it.classifier == Map::class }?.arguments?.get(1)?.type?.classifier as? KClass<*>)
                if (keyType != null && valueType != null) {
                    mapOf(getDummyValue(keyType) to getDummyValue(valueType))
                } else {
                    emptyMap<Any, Any>()
                }
            }

            type.isAbstract -> null
            else -> {
                val primaryConstructor = type.primaryConstructor
                primaryConstructor?.call() ?: "unknown"
            }
        }
    }

    override fun fileExtension(): String {
        return "json"
    }
}