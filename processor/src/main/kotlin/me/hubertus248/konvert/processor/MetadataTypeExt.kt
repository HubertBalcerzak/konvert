package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.*

@OptIn(KotlinPoetMetadataPreview::class)
fun KmType.typeName(): TypeName {
    //TODO support abbreviated types
    val rawType: ClassName = ClassInspectorUtil.createClassName((classifier as KmClassifier.Class).name)
        .let { if (isNullable()) it.copy(nullable = true) else it } as ClassName

    return if (arguments.isEmpty()) rawType
    else rawType.parameterizedBy(arguments.map { projection ->
        projection.type?.typeName() ?: ANY.copy(nullable = true)
    })
}

@OptIn(KotlinPoetMetadataPreview::class)
fun KmClass.className(): ClassName = ClassInspectorUtil.createClassName(name)

fun KmType.isEqual(other: KmType): Boolean {
    return (classifier as KmClassifier.Class) == (other.classifier as KmClassifier.Class)
            && arguments.size == other.arguments.size
            && arguments.zip(other.arguments).all { (first, second) -> first.isEqual(second) }
}

fun KmTypeProjection.isEqual(other: KmTypeProjection): Boolean {
    if (type == null && other.type == null) {
        return true
    }
    if (type == null || other.type == null) {
        return false
    }
    //TODO get rid of assertions
    return type!!.isEqual(other.type!!) && type!!.isNullable() == other.type!!.isNullable()
}

fun KmType.isNullable() = Flag.Type.IS_NULLABLE(flags)

fun KmType.isAssignableFrom(other: KmType): Boolean {
    //TODO implement inheritance support
    return isEqual(other) && (isNullable() || !other.isNullable())
}
