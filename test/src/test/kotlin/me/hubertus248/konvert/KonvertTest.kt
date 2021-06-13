package me.hubertus248.konvert

import me.hubertus248.konvert.model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class KonvertTest {

    @Test
    fun `should generate konverter for fully compatible classes`() {
        val source = FooCompatible(1, "qwe")

        val result: Foo = source.konvert(Foo::class)

        assertEquals(source.field1, result.field1)
        assertEquals(source.field2, result.field2)
    }

    @Test
    fun `should convert with computed values`() {
        val source = FooCompatible(1, "qwe")
        val computedField1 = 2

        val result: Foo = source.konvert(Foo::class) {
            field1(computedField1)
            build()
        }

        assertEquals(computedField1, result.field1)
        assertEquals(source.field2, result.field2)
    }

    @Test
    fun `should allow access to source fields`() {
        val source = FooCompatible(1, "qwe")

        val result: Foo = source.konvert(Foo::class) {
            field1(field1 * 2)
            build()
        }

        assertEquals(source.field1 * 2, result.field1)
        assertEquals(source.field2, result.field2)
    }

    @Test
    fun `should generate konverter for class with additional fields`() {
        val source = FooWithAdditionalFields(1, "qwe", 2L)

        val result: Foo = source.konvert(Foo::class)

        assertEquals(source.field1, result.field1)
        assertEquals(source.field2, result.field2)
    }

    @Test
    fun `should generate konverter for class with missing fields`() {
        val source = FooWithMissingFields(1)
        val computedField2 = "computed"

        val result: Foo = source.konvert(Foo::class) {
            field2(computedField2)
            build()
        }

        assertEquals(source.field1, result.field1)
        assertEquals(computedField2, result.field2)
    }

    @Test
    fun `should allow nullable target field`() {
        val source = Foo(1, "qwe")

        val result: FooWithNullableField = source.konvert(FooWithNullableField::class)

        assertEquals(source.field1, result.field1)
        assertEquals(source.field2, result.field2)
    }

    @Test
    fun `should convert nested properties`() {
        val source = FooWithNestedCompatible(FooCompatible(123, "qwe"))

        val result: FooWithNested = source.konvert(FooWithNested::class)

        assertEquals(source.field1.field1, result.field1.field1)
    }
}
