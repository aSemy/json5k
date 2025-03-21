package io.github.xn32.json5k.generation

import io.github.xn32.json5k.OutputStrategy
import io.github.xn32.json5k.format.DocumentTracker
import io.github.xn32.json5k.format.DocumentTracker.TokenType
import io.github.xn32.json5k.format.Specification
import io.github.xn32.json5k.format.Token
import io.github.xn32.json5k.toHexString

private const val INDENT_CHAR = ' '
private const val LINE_TERMINATOR = "\n"
private val LINE_DELIMITERS = arrayOf("\r\n") + Specification.LINE_TERMINATORS.map(Char::toString)

internal interface OutputSink {
    fun write(char: Char)
    fun finalize() {}
}

internal class StringOutputSink : OutputSink {
    private val builder = StringBuilder()

    override fun write(char: Char) {
        builder.append(char)
    }

    override fun toString() = builder.toString()
}

internal class FormatGenerator(private val sink: OutputSink, private val outputStrategy: OutputStrategy) {
    private val tracker = DocumentTracker()

    fun put(token: Token) {
        handleToken(token)
        tracker.supply(token)
    }

    fun writeComment(comment: String) {
        check(tracker.inObjectStruct)
        if (tracker.nextTokenType == TokenType.COMMA) {
            writeComma()
        }

        check(tracker.nextTokenType == TokenType.NEXT_ITEM)
        if (outputStrategy !is OutputStrategy.HumanReadable) {
            return
        }

        @Suppress("SpreadOperator")
        for (line in comment.splitToSequence(*LINE_DELIMITERS)) {
            writeVisualSep()
            sink.write("// $line")
        }
    }

    private fun writeComma() {
        sink.write(',')
        tracker.supplyComma()
    }

    private fun writeQuoted(sequence: CharSequence) {
        sink.writeQuoted(outputStrategy.quoteCharacter, sequence)
    }

    private fun writeVisualSep(levelOffset: Int = 0) {
        if (outputStrategy !is OutputStrategy.HumanReadable) {
            return
        }

        sink.write(LINE_TERMINATOR)
        repeat(outputStrategy.indentationWith * (tracker.nestingLevel + levelOffset)) {
            sink.write(INDENT_CHAR)
        }
    }

    private fun handleToken(token: Token) {
        if (token is Token.EndToken) {
            if (tracker.nextTokenType == TokenType.COMMA) {
                writeVisualSep(-1)
            }

            handleEndToken(token)
            return
        }

        if (tracker.nextTokenType == TokenType.NEXT_ITEM && tracker.nestingLevel > 0) {
            writeVisualSep()
        }

        if (tracker.nextTokenType == TokenType.COMMA) {
            writeComma()

            if (token is Token.BeginToken && outputStrategy is OutputStrategy.HumanReadable) {
                sink.write(' ')
            } else if (token !is Token.BeginToken) {
                writeVisualSep()
            }
        }

        when (tracker.nextTokenType) {
            TokenType.END_OF_FILE -> {
                require(token is Token.EndOfFile)
            }

            TokenType.NEXT_ITEM, TokenType.COMMA -> {
                if (tracker.inObjectStruct) {
                    require(token is Token.MemberName)
                    handleMemberName(token)
                } else {
                    handleNextItem(token)
                }
            }

            TokenType.MEMBER_VALUE -> {
                handleNextItem(token)
            }
        }
    }

    private fun handleEndToken(token: Token.EndToken) {
        when (token) {
            Token.EndObject -> sink.write('}')
            Token.EndArray -> sink.write(']')
        }
    }

    private fun handleBeginToken(token: Token.BeginToken) {
        when (token) {
            Token.BeginObject -> sink.write('{')
            Token.BeginArray -> sink.write('[')
        }
    }

    private fun handleMemberName(token: Token.MemberName) {
        val name = token.name

        if (!Specification.isIdentifier(name) || outputStrategy.quoteMemberNames) {
            writeQuoted(name)
        } else {
            sink.write(token.name)
        }

        sink.write(':')
        if (outputStrategy is OutputStrategy.HumanReadable) {
            sink.write(' ')
        }
    }

    private fun handleNextItem(token: Token) {
        when (token) {
            is Token.BeginToken -> handleBeginToken(token)
            is Token.Value -> putValue(token)
            else -> throw IllegalArgumentException("unexpected token type")
        }
    }

    private fun putValue(token: Token.Value) {
        when (token) {
            is Token.Bool -> sink.write(token.bool.toString())
            Token.Null -> sink.write("null")
            is Token.FloatingPoint -> sink.write(token.format())
            is Token.SignedInteger -> sink.write(token.number.toString())
            is Token.UnsignedInteger -> sink.write(token.number.toString())
            is Token.Str -> writeQuoted(token.string)
        }
    }
}

/**
 * Format the floating point number as a string.
 *
 * Finite numbers must contain a decimal point, even if the number is an integer.
 * E.g. `1f` should be formatted as `0.0`.
 *
 * For large/small numbers, scientific notation is permitted, but this must use
 * `E` (without a `+` symbol) for positive values, and `E-` for negative values.
 */
internal expect fun Token.FloatingPoint.format(): String

private fun OutputSink.write(sequence: CharSequence) {
    for (char in sequence) {
        write(char)
    }
}

private fun OutputSink.writeEscaped(char: Char) {
    write("\\$char")
}

private fun OutputSink.writeQuoted(quoteChar: Char, sequence: CharSequence) {
    write(quoteChar)
    for (char in sequence) {
        when (char) {
            quoteChar, '\\' -> writeEscaped(char)
            in Specification.LINE_TERMINATORS -> {
                when (val ctrl = Specification.REVERSE_ESCAPE_CHAR_MAP[char]) {
                    is Char -> writeEscaped(ctrl)
                    else -> write("\\u${char.toHexString()}")
                }
            }

            else -> write(char)
        }
    }

    write(quoteChar)
}
