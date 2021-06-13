package me.hubertus248.konvert.model

import me.hubertus248.konvert.api.Konvert

@Konvert(
    from = [FooCompatible::class,
        FooWithAdditionalFields::class,
        FooWithMissingFields::class,
        FooWithNullableField::class]
)
data class Foo(val field1: Int, val field2: String)

data class FooCompatible(val field1: Int, val field2: String)

data class FooWithAdditionalFields(val field1: Int, val field2: String, val field3: Long)

data class FooWithMissingFields(val field1: Int)

@Konvert(from = [Foo::class])
data class FooWithNullableField(val field1: Int, val field2: String?)

@Konvert(from = [FooWithNestedCompatible::class])
data class FooWithNested(val field1: Foo)

data class FooWithNestedCompatible(val field1: FooCompatible)
