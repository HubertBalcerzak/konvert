package me.hubertus248.konvert.processor

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
