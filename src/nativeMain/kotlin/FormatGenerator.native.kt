package io.github.xn32.json5k.generation

import io.github.xn32.json5k.format.Token


internal actual fun Token.FloatingPoint.format(): String = number.toString()
