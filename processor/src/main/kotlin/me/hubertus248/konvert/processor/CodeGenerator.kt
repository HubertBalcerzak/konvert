package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmType
import me.hubertus248.konvert.processor.codegen.BuilderGenerator
import java.io.File
import javax.lang.model.element.*

data class KotlinProperty(val name: String, val type: KmType)

data class KotlinClass(val element: TypeElement, val kotlinClass: KmClass)

object CodeGenerator {

    fun generate(sourceElement: TypeElement, targetElement: TypeElement, pack: String, generatedDir: String) {
        val source = KotlinClass(sourceElement, sourceElement.kmClass())
        val target = KotlinClass(targetElement, targetElement.kmClass())

        val fileName = "${targetElement.simpleName}From${source.element.simpleName}Builder"
        val fileBuilder = FileSpec.builder(pack, fileName)

        val targetConstructor = target.kotlinClass.constructors.find { !Flag.Constructor.IS_SECONDARY(it.flags) }
            ?: throw IllegalStateException("${targetElement.simpleName} has no primary constructor")

        //TODO abbreviatedType support
        val targetProperties = targetConstructor.valueParameters
            .filter { it.type != null }
            .map { KotlinProperty(it.name, it.type!!) }

        val sourceProperties = source.kotlinClass.properties
            .filter { Flag.Property.HAS_GETTER(it.flags) }
            .map { KotlinProperty(it.name, it.returnType) }


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

        val builderClass = BuilderGenerator(pack, fileName, source, target, commonProperties, missingProperties)
            .build()

        val file = fileBuilder.addType(builderClass).build()
        file.writeTo(File(generatedDir))
    }

    private fun generateBuildFunction(): FunSpec {
        TODO()
    }

    private fun generateTransformFunction(): FunSpec = TODO()

//    private fun generateBuilderClass(
//        fileName: String,
//        source: KotlinClass,
//        target: KotlinClass,
//        commonProperties: List<KotlinProperty>,
//        missingProperties: List<KotlinProperty>
//    ): TypeSpec {
//
//    }
}
