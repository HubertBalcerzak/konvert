package me.hubertus248.konvert.processor

import com.squareup.kotlinpoet.MemberName

class BuiltinConverterMap {
    companion object {
        private const val BUILTIN_CONVERTER_PACKAGE = "me.hubertus248.konvert.api.converters"

        val CONVERTERS = listOf(
            List::class.qualifiedName to MemberName(BUILTIN_CONVERTER_PACKAGE, "convertToList"),
            MutableList::class.qualifiedName to MemberName(BUILTIN_CONVERTER_PACKAGE, "convertToMutableList"),
            Set::class.qualifiedName to MemberName(BUILTIN_CONVERTER_PACKAGE, "convertToSet"),
            MutableSet::class.qualifiedName to MemberName(BUILTIN_CONVERTER_PACKAGE, "convertToMutableSet")
        ).toMap()
    }
}
