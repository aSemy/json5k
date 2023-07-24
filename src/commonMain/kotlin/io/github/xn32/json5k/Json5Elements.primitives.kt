package io.github.xn32.json5k

import io.github.xn32.json5k.serialization.Json5NullSerializer
import io.github.xn32.json5k.serialization.Json5PrimitiveSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.InlinePrimitiveDescriptor
import kotlin.native.concurrent.SharedImmutable

/**
 * Class representing JSON5 primitive value.
 * JSON5 primitives include numbers, strings, booleans and special null value [Json5Null].
 */
@Serializable(Json5PrimitiveSerializer::class)
sealed interface Json5Primitive : Json5Element {

    /**
     * Indicates whether the primitive was explicitly constructed from [String] and
     * whether it should be serialized as one. E.g. `JsonPrimitive("42")` is represented
     * by a string, while `JsonPrimitive(42)` is not.
     * These primitives will be serialized as `42` and `"42"` respectively.
     */
    val isString: Boolean

    /**
     * Content of given element without quotes. For [Json5Null] this method returns `null`.
     */
    val content: String
}

/** internal primitive implementation, containing metadata about the content and how to encode/decode it */
internal class Json5Literal(
    override val content: String,
    override val isString: Boolean = false,
    internal val coerceToInlineType: SerialDescriptor? = null,
) : Json5Primitive {
    init {
        if (coerceToInlineType != null) require(coerceToInlineType.isInline)
    }

    override fun toString(): String = content
}

@Serializable(Json5NullSerializer::class)
object Json5Null : Json5Primitive {
    override val isString: Boolean = false
    override val content: String = "null"
    override fun toString(): String = content
}


//region primitive constructors
fun Json5Primitive(value: String): Json5Primitive = Json5Literal(value, isString = true)

fun Json5Primitive(value: Byte): Json5Primitive = Json5Literal(value.toString())
fun Json5Primitive(value: Short): Json5Primitive = Json5Literal(value.toString())
fun Json5Primitive(value: Int): Json5Primitive = Json5Literal(value.toString())
fun Json5Primitive(value: Long): Json5Primitive = Json5Literal(value.toString())

fun Json5Primitive(value: UByte): Json5Primitive = Json5Primitive(value.toULong())
fun Json5Primitive(value: UShort): Json5Primitive = Json5Primitive(value.toULong())
fun Json5Primitive(value: UInt): Json5Primitive = Json5Primitive(value.toULong())
fun Json5Primitive(value: ULong): Json5Primitive = Json5UnquotedLiteral(value.toString())

fun Json5Primitive(value: Float): Json5Primitive = Json5Primitive(value.toString())
fun Json5Primitive(value: Double): Json5Primitive = Json5Primitive(value.toString())

fun Json5Primitive(value: Boolean): Json5Primitive = Json5Primitive(value.toString())
//endregion

@Suppress("FunctionName", "UNUSED_PARAMETER")
fun Json5Primitive(value: Nothing?): Json5Null = Json5Null

@Suppress("FunctionName")
fun Json5UnquotedLiteral(value: String): Json5Primitive =
    Json5Literal(value, coerceToInlineType = jsonUnquotedLiteralDescriptor)

/** Used as a marker to indicate during encoding that the [Json5Encoder] should use `encodeInline()` */
@OptIn(InternalSerializationApi::class)
@SharedImmutable
internal val jsonUnquotedLiteralDescriptor: SerialDescriptor =
    InlinePrimitiveDescriptor("io.github.xn32.json5k.Json5UnquotedLiteral", String.serializer())
