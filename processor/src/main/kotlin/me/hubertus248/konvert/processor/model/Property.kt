package me.hubertus248.konvert.processor.model

import kotlinx.metadata.KmType

data class KotlinProperty(val name: PropertyName, val type: KmType)

data class ConvertibleProperty(val property: KotlinProperty, val konverter: KonverterDefinition)
