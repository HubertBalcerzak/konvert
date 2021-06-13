package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmConstructor
import me.hubertus248.konvert.processor.model.Filename
import me.hubertus248.konvert.processor.model.Key
import me.hubertus248.konvert.processor.model.KonverterDefinition
import me.hubertus248.konvert.processor.model.KotlinProperty
import me.hubertus248.konvert.processor.tree.KonverterTree
import java.io.File
import javax.lang.model.element.*

object KonverterBuilder {

    private val konverters: MutableMap<Key, KonverterDefinition> = mutableMapOf()

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

        val key = Key(source.name, target.name)
        konverters[key] =
            KonverterDefinition(
                key,
                source,
                target,
                filename,
                sourceProperties,
                commonProperties,
                missingProperties,
                pack,
                pure = missingProperties.isEmpty()
            )
    }

    fun generate(generatedDir: String) {

        KonverterTree.create(konverters)
            .getKonverters()
            .forEach { konverter ->
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
        .map { KotlinProperty(it.name, it.type!!) }.toSet()

    private fun getSourceProperties(source: KmClass) = source.properties
        .filter { Flag.Property.HAS_GETTER(it.flags) }
        .map { KotlinProperty(it.name, it.returnType) }.toSet()

    private fun filterProperties(
        targetProperties: Set<KotlinProperty>,
        sourceProperties: Set<KotlinProperty>
    ): Pair<Set<KotlinProperty>, Set<KotlinProperty>> {
        val commonProperties = mutableSetOf<KotlinProperty>()
        val missingProperties = mutableSetOf<KotlinProperty>()

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
