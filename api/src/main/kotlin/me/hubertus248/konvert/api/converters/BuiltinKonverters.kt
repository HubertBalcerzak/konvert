package me.hubertus248.konvert.api.converters

fun <T, R> convertToList(input: Iterable<T>, converter: (T) -> R) = input.map(converter)

fun <T, R> convertToMutableList(input: Iterable<T>, converter: (T) -> R) = input.map(converter).toMutableList()

fun <T, R> convertToSet(input: Iterable<T>, converter: (T) -> R) = input.map(converter).toSet()

fun <T, R> convertToMutableSet(input: Iterable<T>, converter: (T) -> R) = input.map(converter).toMutableSet()
