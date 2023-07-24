package io.github.xn32.json5k

import kotlin.jvm.JvmInline
import kotlin.native.concurrent.SharedImmutable


interface Json5Object : Json5Element, Map<String, Json5Element>

interface Json5MutableObject : Json5Object, MutableMap<String, Json5Element>

//region constructors
fun Json5Object(content: Map<String, Json5Element> = emptyMap()): Json5Object =
    Json5ObjectImpl(content)

fun json5ObjectOf(vararg elements: Pair<String, Json5Element>): Json5Object =
    Json5ObjectImpl(mapOf(*elements))

fun Json5MutableObject(content: MutableMap<String, Json5Element> = mutableMapOf()): Json5MutableObject =
    Json5MutableObjectImpl(content)

fun json5MutableObjectOf(vararg elements: Pair<String, Json5Element>): Json5MutableObject =
    Json5MutableObjectImpl(mutableMapOf(*elements))
//endregion

//region builders
fun buildJson5Object(builder: Json5MutableObject.() -> Unit): Json5Object =
    Json5MutableObject().apply(builder)

fun buildJson5MutableObject(builder: Json5MutableObject.() -> Unit): Json5MutableObject =
    Json5MutableObject().apply(builder)
//endregion


@JvmInline
private value class Json5ObjectImpl(
    private val content: Map<String, Json5Element>
) : Json5Object, Map<String, Json5Element> by content {
    override fun toString(): String = content.toJsonString()
}

@JvmInline
private value class Json5MutableObjectImpl(
    private val content: MutableMap<String, Json5Element>
) : Json5MutableObject, MutableMap<String, Json5Element> by content {
    override fun toString(): String = content.toJsonString()
}

private fun Map<String, Json5Element>.toJsonString(): String =
    entries.joinToString(
        separator = ",",
        prefix = "{",
        postfix = "}",
        transform = { (k, v) ->
            buildString {
                printQuoted(k)
                append(':')
                append(v)
            }
        }
    )

private fun StringBuilder.printQuoted(value: String) {
    append("\"")
    var lastPos = 0
    for (i in value.indices) {
        val c = value[i].code
        if (c < ESCAPE_STRINGS.size && ESCAPE_STRINGS[c] != null) {
            append(value, lastPos, i) // flush prev
            append(ESCAPE_STRINGS[c])
            lastPos = i + 1
        }
    }
    if (lastPos != 0) append(value, lastPos, value.length)
    else append(value)
    append("\"")
}


@SharedImmutable
private val ESCAPE_STRINGS: Array<String?> = arrayOfNulls<String>(93).apply {
    for (c in 0..0x1f) {
        val c1 = toHexChar(c shr 12)
        val c2 = toHexChar(c shr 8)
        val c3 = toHexChar(c shr 4)
        val c4 = toHexChar(c)
        this[c] = "\\u$c1$c2$c3$c4"
    }
    this['"'.code] = "\\\""
    this['\\'.code] = "\\\\"
    this['\t'.code] = "\\t"
    this['\b'.code] = "\\b"
    this['\n'.code] = "\\n"
    this['\r'.code] = "\\r"
    this[0x0c] = "\\f"
}


private fun toHexChar(i: Int): Char {
    val d = i and 0xf
    return when {
        d < 10 -> (d + '0'.code).toChar()
        else -> (d - 10 + 'a'.code).toChar()
    }
}
