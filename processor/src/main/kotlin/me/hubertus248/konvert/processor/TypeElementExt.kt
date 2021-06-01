package me.hubertus248.konvert.processor

import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

fun TypeElement.annotationMirrorByType(clazz: Class<*>): AnnotationMirror? {
    val clazzName = clazz.name
    return this.annotationMirrors.find { it.annotationType.toString() == clazzName }
}

fun AnnotationMirror.annotationClassValuesByKey(key: String): List<AnnotationValue> {
    val annotationValues = this
        .elementValues
        .entries
        .find { it.key.simpleName.toString() == key }
        ?.value
        ?.value
    return if (annotationValues == null) {
        emptyList()
    } else {
        annotationValues as List<AnnotationValue>
    }
}

fun TypeMirror.asTypeElement(processingEnvironment: ProcessingEnvironment): TypeElement {
    val typeUtils = processingEnvironment.typeUtils
    return typeUtils.asElement(this) as TypeElement
}

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
