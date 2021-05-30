package me.hubertus248.konvert.processor.codegen

import com.squareup.kotlinpoet.*
import me.hubertus248.konvert.processor.KotlinClass
import me.hubertus248.konvert.processor.KotlinProperty
import me.hubertus248.konvert.processor.typeName

class BuilderGenerator(
    private val packageName: String,
    private val fileName: String,
    private val source: KotlinClass,
    private val target: KotlinClass,
    private val commonProperties: List<KotlinProperty>,
    private val missingProperties: List<KotlinProperty>
) {
    fun build(): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(KModifier.SEALED)
            .generateBuilderConstructor()
            .generateSourceProperty()
            .generateCommonProperties()
            .generateMissingPropertyInterfaces()
            .generateImpl()
        return classBuilder.build()
    }

    private fun TypeSpec.Builder.generateBuilderConstructor() = primaryConstructor(FunSpec.constructorBuilder()
        .addParameter(
            ParameterSpec
                .builder("_source", source.element.asType().asTypeName())
                .build()
        )
        .addParameters(
            commonProperties.map { ParameterSpec.builder("_${it.name}", it.type.typeName()).build() }
        )
        .build())

    private fun TypeSpec.Builder.generateSourceProperty() = addProperty(
        PropertySpec.builder("_source", source.element.asType().asTypeName())
            .addModifiers(KModifier.PRIVATE)
            .initializer("_source")
            .build()
    )
        .addFunction(
            FunSpec.builder("source")
                .returns(source.element.asClassName())
                .addStatement("return _source")
                .build()
        )

    private fun TypeSpec.Builder.generateCommonProperties() = addProperties(commonProperties.map { property ->
        PropertySpec.builder(property.name, property.type.typeName())
            .mutable(true)
            .setter(
                FunSpec.builder("set()")
                    .addModifiers(KModifier.PRIVATE)
                    .addParameter(ParameterSpec.builder("value", property.type.typeName()).build())
                    .addStatement("${property.name} = value")
                    .build()
            )
            .initializer("_${property.name}")
            .build()
    })
        .addFunctions(commonProperties.map { property ->
            FunSpec.builder(property.name)
                .addParameter(property.name, property.type.typeName())
                .addStatement("this.${property.name} = ${property.name}")
                .build()
        })

    private fun TypeSpec.Builder.generateMissingPropertyInterfaces() = addTypes(missingProperties.map { property ->
        TypeSpec.interfaceBuilder(getMissingPropertyInterfaceName(property))
            .addProperty(
                PropertySpec.builder(property.name, property.type.typeName())
                    .mutable(true)
                    .build()
            )
            .build()
    })

    private fun TypeSpec.Builder.generateImpl() = addType(
        TypeSpec.classBuilder("Impl")
            .addModifiers(KModifier.PRIVATE)
            .superclass(ClassName(packageName, fileName))
            .addSuperinterfaces(missingProperties.map { property ->
                ClassName(packageName, "$fileName.${getMissingPropertyInterfaceName(property)}")
            })
            .build()
    )


    private fun getMissingPropertyInterfaceName(property: KotlinProperty): String = "Has${property.name.capitalize()}"
}
