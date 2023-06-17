package io.github.xn32.json5k.generation

import io.github.xn32.json5k.format.Token

internal actual fun Token.FloatingPoint.format(): String {
    val str = number.toString()

    // return toString() for infinite and NaN values
    return when {
        !number.isFinite() -> str

        // update scientific notation, if necessary
        'e' in str -> str
            .replace("e+", "E")
            .replace("e-", "E-")

        // is it already a decimal? No need to adjust
        '.' in str -> str

        // suffix .0 to this finite integer
        else -> "$str.0"
    }
}
