package me.hubertus248.konvert.processor

import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.TypeElement

fun TypeElement.kmClass(): KmClass = getAnnotation(Metadata::class.java)?.let {
    KotlinClassMetadata.read(
        KotlinClassHeader(
            it.kind,
            it.metadataVersion,
            it.data1,
            it.data2,
            it.extraString,
            it.packageName,
            it.extraInt
        )
    )
}?.let { it as KotlinClassMetadata.Class }
    ?.toKmClass() ?: throw IllegalArgumentException("$simpleName is not a Kotlin class")
