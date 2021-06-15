# Konvert

![](https://github.com/HubertBalcerzak/konvert/actions/workflows/CI.yml/badge.svg?branch=master)
[![](https://jitpack.io/v/HubertBalcerzak/konvert.svg)](https://jitpack.io/#HubertBalcerzak/konvert)

Kotlin library for boilerplate-free data transformations.

Often we have to convert an object of one type to an object of another type with similar fields.

```kotlin
data class User(val username: String, val email: String, val passwordHash: String)

data class UserView(val username: String, val email: String, val timestamp: Instant)
```

Usually we do that by rewriting fields one by one.

```kotlin
val user = User("username", "email@example.com", "passwordHash")
val userView = UserView(
    username = user.username,
    email = user.email,
    timestamp = Instant.now()
)
```

While this approach works, it creates a lot of boilerplate code. Konvert tries to address that by providing generated `konvert()` methods.

```kotlin
val userView = user.konvert {
    timestamp(Instant.now())
    build()
}
```

## Using with Gradle

Add the following lines to your `build.gradle.kts` file.
```kotlin
repositories {
    maven("https://jitpack.io")
    ...
}

dependencies {
    implementation("com.github.HubertBalcerzak.konvert:konvert-api:0.1")
    kapt("com.github.HubertBalcerzak.konvert:konvert-processor:0.1")
    ...
}
```


## Examples
To generate the `konvert()` method, annotate the target class with `@Konvert`.

```kotlin
data class A(val a: Int, val b: List<B>)
data class B(val int: Int)

@Konvert(from = [A::class])
data class C(val a: Int, val b: List<D>)

@Konvert(from = [B::class])
data class D(val int: Int)

fun main() {
    val a = A(123, listOf(B(123)))
    val c: C = a.konvert(C::class)
}

```
Properties can be overwritten by supplying a builder lambda. This way you can also provide any missing fields, if source and target classes don't have identical layouts.


```kotlin
data class E(val a: Int, val b: String)

@Konvert(from = [E::class])
data class F(val a: Int, val b: Int)

fun main() {
    val e = E(123, "123")
    val f: F = e.konvert(F::class) {
        b(source().b.toInt())
        build()
    }
}
```

Omitting any non-nullable properties from target class will result in a compilation error.

```kotlin
val f: F = e.konvert(F::class) { build() } //compilation error
```
