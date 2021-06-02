package me.hubertus248.konvert.processor

import kotlinx.metadata.KmClass
import kotlinx.metadata.KmType

typealias PropertyName = String
typealias Filename = String

data class KotlinProperty(val name: PropertyName, val type: KmType)

data class Key(val source: KmClass, val target: KmClass)

data class Konverter(
    val source: KmClass,
    val target: KmClass,
    val filename: Filename,
    val sourceProperties: List<KotlinProperty>,
    val commonProperties: List<KotlinProperty>,
    val missingProperties: List<KotlinProperty>,
    val pack: String,
    val pure: Boolean
)
