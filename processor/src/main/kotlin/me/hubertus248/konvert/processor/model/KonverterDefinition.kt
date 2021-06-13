package me.hubertus248.konvert.processor.model

import kotlinx.metadata.KmClass

typealias PropertyName = String
typealias Filename = String




data class KonverterDefinition(
    val key: Key,
    val source: KmClass,
    val target: KmClass,
    val filename: Filename,
    val sourceProperties: Set<KotlinProperty>,
    val commonProperties: Set<KotlinProperty>,
    val missingProperties: Set<KotlinProperty>,
    val pack: String,
    val pure: Boolean
)
