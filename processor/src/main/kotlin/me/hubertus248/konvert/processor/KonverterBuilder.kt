package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmType
import java.io.File
import javax.lang.model.element.*

data class KotlinProperty(val name: String, val type: KmType)

data class KotlinClass(val element: TypeElement, val kotlinClass: KmClass)

object KonverterBuilder {

    fun generate(sourceElement: TypeElement, targetElement: TypeElement, pack: String, generatedDir: String) {
        val source = KotlinClass(sourceElement, sourceElement.kmClass())
        val target = KotlinClass(targetElement, targetElement.kmClass())

        val fileName = "${targetElement.simpleName}From${source.element.simpleName}Builder"
        val fileBuilder = FileSpec.builder(pack, fileName)

        val targetConstructor = target.kotlinClass.constructors.find { !Flag.Constructor.IS_SECONDARY(it.flags) }
            ?: throw IllegalStateException("${targetElement.simpleName} has no primary constructor")

        //TODO abbreviatedType support
        val targetProperties = getTargetProperties(targetConstructor)
        val sourceProperties = getSourceProperties(source)

        val (commonProperties, missingProperties) = filterProperties(targetProperties, sourceProperties)

        val generator = CodeGenerator(pack, fileName, source, target, commonProperties, missingProperties)

        fileBuilder
            .addType(generator.generateBuilder())

        generator.generateMissingPropertyExtensions().forEach { fileBuilder.addFunction(it) }

        fileBuilder.addFunction(generator.generateBuildFunction())
            .addFunction(generator.generateKonvertFunction(sourceProperties))
            .build()
        fileBuilder.build().writeTo(File(generatedDir))
    }


    private fun getTargetProperties(targetConstructor: KmConstructor) = targetConstructor.valueParameters
        .filter { it.type != null }
        .map { KotlinProperty(it.name, it.type!!) }

    private fun getSourceProperties(source: KotlinClass) = source.kotlinClass.properties
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
