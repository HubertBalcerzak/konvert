package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.*
import kotlinx.metadata.Flag
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeProjection

fun KmType.typeName(): TypeName {
    //TODO support abbreviated types
    val rawType = ClassName.bestGuess(
        (classifier as KmClassifier.Class).name
            .replace("/", ".")
    ).let { if (isNullable()) it.asNullable() else it }

    return if (arguments.isEmpty()) rawType
    else ParameterizedTypeName.get(rawType, *arguments.map { projection ->
        projection.type?.typeName() ?: WildcardTypeName.subtypeOf(ANY.asNullable())
    }.toTypedArray())
}

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
