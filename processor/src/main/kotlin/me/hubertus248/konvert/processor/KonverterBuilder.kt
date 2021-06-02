package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmConstructor
import java.io.File
import javax.lang.model.element.*

object KonverterBuilder {

    private val konverters: MutableMap<Key, Konverter> = mutableMapOf()

    fun registerKonverter(sourceElement: TypeElement, targetElement: TypeElement, pack: String) {
        val source = sourceElement.kmClass()
        val target = targetElement.kmClass()

        val filename: Filename = "${targetElement.simpleName}From${sourceElement.simpleName}Builder"

        val targetConstructor = target.constructors.find { !Flag.Constructor.IS_SECONDARY(it.flags) }
            ?: throw IllegalStateException("${targetElement.simpleName} has no primary constructor")

        //TODO abbreviatedType support
        val targetProperties = getTargetProperties(targetConstructor)
        val sourceProperties = getSourceProperties(source)

        //TODO missing source fields should be allowed when corresponding target field is nullable
        val (commonProperties, missingProperties) = filterProperties(targetProperties, sourceProperties)

        konverters[Key(source, target)] =
            Konverter(source, target, filename, sourceProperties, commonProperties, missingProperties, pack, false)
    }

    fun generate(generatedDir: String) {

        konverters.values.forEach { konverter ->
            val fileBuilder = FileSpec.builder(konverter.pack, konverter.filename)
            val generator = CodeGenerator(konverter)

            fileBuilder
                .addType(generator.generateBuilder())

            generator.generateMissingPropertyExtensions().forEach { fileBuilder.addFunction(it) }

            fileBuilder.addFunction(generator.generateBuildFunction())
                .addFunction(generator.generateKonvertFunction())
                .build()
            fileBuilder.build().writeTo(File(generatedDir))
        }

    }

    private fun getTargetProperties(targetConstructor: KmConstructor) = targetConstructor.valueParameters
        .filter { it.type != null }
        .map { KotlinProperty(it.name, it.type!!) }

    private fun getSourceProperties(source: KmClass) = source.properties
        .filter { Flag.Property.HAS_GETTER(it.flags) }
        .map { KotlinProperty(it.name, it.returnType) }

    private fun filterProperties(
        targetProperties: List<KotlinProperty>,
        sourceProperties: List<KotlinProperty>
    ): Pair<List<KotlinProperty>, List<KotlinProperty>> {
        val commonProperties = mutableListOf<KotlinProperty>()
        val missingProperties = mutableListOf<KotlinProperty>()

        targetProperties.forEach { targetProperty ->

            if (sourceProperties.find { it.name == targetProperty.name }
                    ?.let { targetProperty.type.isAssignableFrom(it.type) } == true) {
                commonProperties.add(targetProperty)
            } else {
                missingProperties.add(targetProperty)
            }
        }
        return Pair(commonProperties, missingProperties)
    }
}
