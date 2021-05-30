package me.hubertus248.konvert.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class Transform(
    val from: Array<KClass<*>>
)
