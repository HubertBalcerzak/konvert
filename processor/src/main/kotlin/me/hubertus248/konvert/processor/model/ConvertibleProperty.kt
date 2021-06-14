package me.hubertus248.konvert.processor.model

import com.squareup.kotlinpoet.CodeBlock

interface ConvertibleProperty {
    val property: KotlinProperty

    fun getBlock(): CodeBlock
}

data class SimpleConvertibleProperty(
    override val property: KotlinProperty,
    val konverter: KonverterDefinition.Generated
) : ConvertibleProperty {

    override fun getBlock(): CodeBlock = konverter.getBlock(property.name)
}

data class ConvertibleIterableProperty(
    override val property: KotlinProperty,
    val konverter: KonverterDefinition.Predefined
) : ConvertibleProperty {

    override fun getBlock(): CodeBlock = konverter.getBlock(property.name)

}
