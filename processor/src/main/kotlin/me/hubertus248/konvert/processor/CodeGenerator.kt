package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.properties.Delegates

class CodeGenerator(
    private val konverter: Konverter
) {

    private val source = konverter.source
    private val target = konverter.target
    private val filename = konverter.filename
    private val missingProperties = konverter.missingProperties
    private val commonProperties = konverter.commonProperties

    private val builderClassName = ClassName(konverter.pack, filename)

    fun generateBuilder(): TypeSpec = TypeSpec.classBuilder(filename)
        .addModifiers(KModifier.SEALED)
        .generateBuilderConstructor()
        .generateSourceProperty()
        .generateCommonProperties()
        .generateMissingPropertyInterfaces()
        .generateImpl()
        .generateCompanionObject()
        .build()

    fun generateBuildFunction() = FunSpec.builder("build")
        .addTypeVariable(
            TypeVariableName(
                "S",
                builderClassName,
                *missingProperties.map { property ->
                    ClassName(konverter.pack, "${filename}.${getMissingPropertyInterfaceName(property)}")
                }.toTypedArray()
            )
        )
        .receiver(TypeVariableName("S"))
        .returns(target.className())
        .apply {
            val args = (commonProperties + missingProperties).joinToString(separator = ",\n    ") { property ->
                "${property.name} = ${property.name}"
            }
            addCode(CodeBlock.of("return %T(\n    $args)", target.className()))
        }
        .build()

    fun generateMissingPropertyExtensions(): List<FunSpec> = missingProperties.map { property ->
        FunSpec.builder(property.name)
            .receiver(builderClassName)
            .addAnnotation(
                AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                    .addMember(CodeBlock.of("%T::class", ClassName("kotlin.contracts", "ExperimentalContracts")))
                    .build()
            )
            .addParameter(property.name, property.type.typeName())
            .addCode(
                CodeBlock.of(
                    """
                %M {
                    returns() implies (this@${property.name} is ${filename}.${getMissingPropertyInterfaceName(property)})
                }
                (this as $filename.${getMissingPropertyInterfaceName(property)}).${property.name} = ${property.name}
                
            """.trimIndent(), MemberName("kotlin.contracts", "contract")
                )
            )
            .build()
    }

    fun generateKonvertFunction() = FunSpec.builder("konvert")
        .receiver(source.className())
        .returns(target.className())
        .addParameter(
            ParameterSpec.builder(
                "target",
                ClassName("kotlin.reflect", "KClass").parameterizedBy(target.className())
            )
                .defaultValue(CodeBlock.of("%T::class", target.className()))
                .build()
        )
        .addParameter(
            ParameterSpec.builder(
                "block",
                LambdaTypeName.get(builderClassName, returnType = target.className())
            )
                .apply {
                    if (missingProperties.isEmpty()) {
                        defaultValue(CodeBlock.of("{ build() }"))
                    }
                }
                .build()
        )
        .apply {
            val args = commonProperties.joinToString(separator = ",\n${indent(2)}") { property ->
                if (konverter.sourceProperties.find { it.name == property.name } != null) {
                    "_${property.name} = ${property.name}"
                } else {
                    "_${property.name} = null"
                }
            }
            addCode(
                CodeBlock.of(
                    "return %T(\n${indent(2)}_source = this,\n${indent(2)}$args\n${indent()}).block()",
                    builderClassName
                )
            )
        }
        .build()

    private fun TypeSpec.Builder.generateBuilderConstructor() = primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter(getSourceParameter())
            .addParameters(getConstructorParams())
            .build()
    )

    private fun TypeSpec.Builder.generateSourceProperty() = addProperty(
        PropertySpec.builder("_source", source.className())
            .addModifiers(KModifier.PRIVATE)
            .initializer("_source")
            .build()
    )
        .addFunction(
            FunSpec.builder("source")
                .returns(source.className())
                .addStatement("return _source")
                .build()
        )

    private fun TypeSpec.Builder.generateCommonProperties() = addProperties(commonProperties.map { property ->
        PropertySpec.builder(property.name, property.type.typeName())
            .mutable(true)
            .setter(
                FunSpec.builder("set()")
                    .addModifiers(KModifier.PRIVATE)
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
            .superclass(builderClassName)
            .addSuperinterfaces(missingProperties.map { property ->
                ClassName(konverter.pack, "$filename.${getMissingPropertyInterfaceName(property)}")
            })
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(getSourceParameter())
                    .addParameters(getConstructorParams())
                    .build()
            ).addSuperclassConstructorParameter(getConstructorParamsInvocation())
            .addProperties(missingProperties.map { property ->
                PropertySpec.builder(property.name, property.type.typeName(), KModifier.OVERRIDE)
                    .mutable(true)
                    .apply {
                        if (property.type.isNullable()) {
                            initializer("null")
                        } else {
                            delegate(CodeBlock.of("%T.notNull<%T>()", Delegates::class, property.type.typeName()))
                        }
                    }
                    .build()
            })
            .build()
    )

    private fun TypeSpec.Builder.generateCompanionObject() = addType(
        TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter(getSourceParameter())
                    .addParameters(getConstructorParams())
                    .returns(builderClassName)
                    .addCode(CodeBlock.of("return Impl(${getConstructorParamsInvocation()})\n"))
                    .build()
            )
            .build()
    )

    private fun getMissingPropertyInterfaceName(property: KotlinProperty): String = "Has${property.name.capitalize()}"

    private fun getSourceParameter() = ParameterSpec
        .builder("_source", source.className())
        .build()

    private fun getConstructorParams() =
        commonProperties.map { ParameterSpec.builder("_${it.name}", it.type.typeName()).build() }

    private fun getConstructorParamsInvocation() = StringBuilder().apply {
        append("_source = _source, ")
        commonProperties.map { property ->
            append("_${property.name} = _${property.name}, ")
        }
    }.toString()

    private fun indent(i: Int = 1) = "    ".repeat(i)
}
