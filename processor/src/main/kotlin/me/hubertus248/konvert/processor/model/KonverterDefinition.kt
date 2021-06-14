package me.hubertus248.konvert.processor.model

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import kotlinx.metadata.KmClass
import me.hubertus248.konvert.processor.className

typealias PropertyName = String
typealias Filename = String


sealed class KonverterDefinition {

    abstract fun getBlock(propertyName: String): CodeBlock

    data class Generated(
        val key: Key,
        val source: KmClass,
        val target: KmClass,
        val filename: Filename,
        val sourceProperties: Set<KotlinProperty>,
        val commonProperties: Set<KotlinProperty>,
        val missingProperties: Set<KotlinProperty>,
        val pack: String,
        val pure: Boolean
    ) : KonverterDefinition() {
        override fun getBlock(propertyName: String): CodeBlock = CodeBlock.of(
            "$propertyName.%M(%T::class)",
            MemberName(pack, "konvert"),
            target.className()
        )
    }

    data class Predefined(
        val konvertFunction: MemberName,
        val elementKonverter: KonverterDefinition
    ) : KonverterDefinition() {
        override fun getBlock(propertyName: String): CodeBlock = CodeBlock.of(
            "%M($propertyName) { element -> %L }",
            konvertFunction,
            elementKonverter.getBlock("element")
        )
    }
}

