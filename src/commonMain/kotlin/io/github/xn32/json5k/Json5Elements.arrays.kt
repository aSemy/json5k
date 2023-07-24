package io.github.xn32.json5k

import kotlin.jvm.JvmInline


interface Json5Array : Json5Element, List<Json5Element>

interface Json5MutableArray : Json5Array, MutableList<Json5Element>

//region Constructors
fun Json5Array(content: List<Json5Element> = emptyList()): Json5Array = Json5ArrayImpl(content)

fun json5ArrayOf(vararg elements: Json5Element): Json5Array = Json5ArrayImpl(elements.toList())


fun Json5MutableArray(content: List<Json5Element> = emptyList()): Json5MutableArray =
    Json5MutableArrayImpl(content.toMutableList())

fun json5MutableArrayOf(vararg elements: Json5Element): Json5MutableArray =
    Json5MutableArrayImpl(elements.toMutableList())
//endregion

//region builders
fun buildJson5Array(builder: Json5MutableArray.() -> Unit): Json5Array =
    Json5MutableArray().apply(builder)

fun buildJson5MutableArray(builder: Json5MutableArray.() -> Unit): Json5MutableArray =
    Json5MutableArray().apply(builder)
//endregion


@JvmInline
private value class Json5ArrayImpl(
    private val content: List<Json5Element>
) : Json5Array, List<Json5Element> by content {
    override fun toString(): String = content.toJson5String()
}

@JvmInline
private value class Json5MutableArrayImpl(
    private val content: MutableList<Json5Element>
) : Json5MutableArray, MutableList<Json5Element> by content {
    override fun toString(): String = content.toJson5String()
}

private fun List<Json5Element>.toJson5String(): String =
    joinToString(prefix = "[", postfix = "]", separator = ",")
