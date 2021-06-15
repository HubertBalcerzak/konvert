package me.hubertus248.konvert.processor


fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean {
    return find { predicate(it) } != null
}
