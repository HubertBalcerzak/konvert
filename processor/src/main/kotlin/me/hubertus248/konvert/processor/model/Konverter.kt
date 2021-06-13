package me.hubertus248.konvert.processor.model

import kotlinx.metadata.KmClass

class Konverter(
    val key: Key,
    val source: KmClass,
    val target: KmClass,
    val pack: String,
    val filename: Filename,
    val sourceProperties: Set<KotlinProperty>,
    val commonProperties: Set<KotlinProperty>,
    val requiredProperties: Set<KotlinProperty>,
    val convertibleProperties: Set<ConvertibleProperty>
)
