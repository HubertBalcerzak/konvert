package me.hubertus248.konvert.processor

class TypeMap {
    companion object {
        val TYPES = listOf(
            Iterable::class,
            Collection::class,
            MutableCollection::class,
            List::class,
            MutableList::class,
            Set::class,
            MutableSet::class
        ).associate { it.qualifiedName to it.java.canonicalName }
    }
}
