package tebi.json

import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.text.ParseException
import kotlin.String

sealed class TebiValue {
    companion object {
        fun parse(str: String) : Any? {
            val parsed = try {
                parseTebiValue(str.trimStart())
            } catch (e: ParseException) {
                throw ParseException(str, str.length - e.errorOffset)
            }
            if (parsed.remainder.isNotBlank()) throw ParseException(str, str.indexOf(parsed.remainder))
            return parsed.value
        }
        internal fun parseTebiValue(str: String) : TebiResult {
            return when (true) {
                TebiNull.match(str) -> TebiNull.parse(str)
                TebiBoolean.match(str) -> TebiBoolean.parse(str)
                TebiNumber.match(str) -> TebiNumber.parse(str)
                TebiString.match(str) -> TebiString.parse(str)
                TebiArray.match(str) -> TebiArray.parse(str)
                TebiObject.match(str) -> TebiObject.parse(str)
                else -> throw ParseException(str, str.length)
            }
        }
    }
    internal abstract class Parser {
        abstract val regex : Regex
        abstract val initialMatch: Regex
        internal abstract fun new(str: String, remainder: String) : TebiResult
        internal open fun match(str: String) : Boolean {
            return initialMatch.containsMatchIn(str)
        }
        internal open fun parse(str: String) : TebiResult {
            val matchResult = regex.find(str) ?: throw ParseException(str, str.length)
            val result = matchResult.value
            val remainder = str.drop(result.length)
            return new(result, remainder)
        }
    }
    internal data class TebiResult constructor(val value: Any?, val remainder: String)
}

private class TebiObject : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("^\\{")
        override val initialMatch = Regex("^\\{[^}]*}")
        private val empty = Regex("^\\{}")
        override fun new(str: String, remainder: String): TebiResult {
            val map = mutableMapOf<String, Any?>()
            var toParse = (str + remainder).trimStart()
            if (empty.containsMatchIn(toParse)) return TebiResult(map, remainder.drop(1))
            do {
                toParse = toParse.drop(1).trimStart()
                val nextKey = TebiValue.parseTebiValue(toParse)
                if (nextKey.value !is String) throw ParseException(toParse, toParse.length)
                toParse = nextKey.remainder.trimStart()
                if (toParse[0] != ':') throw ParseException(toParse, toParse.length)
                toParse = toParse.drop(1).trimStart()
                val nextValue = parseTebiValue(toParse)
                map[nextKey.value] = nextValue.value
                toParse = nextValue.remainder.trimStart()
            } while (toParse.startsWith(','))
            if (!toParse.startsWith('}')) throw ParseException(toParse, toParse.length)
            return TebiResult(map.toMap(), toParse.drop(1))
        }
        override fun parse(str: String): TebiResult {
            val matchResult = regex.find(str)
            return when (matchResult) {
                null -> throw NotImplementedException()
                else -> {
                    val result = matchResult.value
                    val remainder = str.drop(result.length)
                    new(result, remainder)
                }
            }
        }
    }
}

private class TebiArray : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("^\\[")
        override val initialMatch = Regex("^\\[[^]]*]")
        private val closing = Regex("^]")
        private val empty = Regex("^\\[]")
        override fun new(str: String, remainder: String): TebiResult {
            val list = mutableListOf<Any?>()
            var toParse = (str + remainder).trimStart()
            if (empty.containsMatchIn(toParse)) return TebiResult(list, remainder.drop(1))
            do {
                toParse = toParse.drop(1).trimStart()
                val nextValue = parseTebiValue(toParse)
                list.add(nextValue.value)
                toParse = nextValue.remainder.trimStart()
            } while (toParse.startsWith(','))
            if (!toParse.startsWith(']')) throw ParseException(toParse, toParse.length)
            return TebiResult(list.toList(), toParse.drop(1))
        }
        override fun parse(str: String): TebiResult {
            val matchResult = regex.find(str)
            return when (matchResult) {
                null -> throw NotImplementedException()
                else -> {
                    val result = matchResult.value
                    val remainder = str.drop(result.length)
                    new(result, remainder)
                }
            }
        }
    }
}

private class TebiNumber : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("(^-?(0|[1-9]\\d*)(\\.\\d+)?(e[-+]?\\d+)?)", RegexOption.IGNORE_CASE)
        override val initialMatch = Regex("^-?[0-9]")
        override fun new(str: String, remainder: String) : TebiResult {
            val value = str.toDouble()
            return TebiResult(value, remainder)
        }
    }
}

private class TebiString : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("^\"(\\\\([\"\\\\/bfnrt]|u[0-9a-fA-F]{4})|[ !#-\\[\\]-\uDBFF\uDFFF])*?\"")
        override val initialMatch = Regex("^\"")
        override fun new(str: String, remainder: String) : TebiResult {
            val value = str.substring(1, str.length - 1)
            return TebiResult(value, remainder)
        }
    }
}

private class TebiBoolean : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("^(true|false)")
        override val initialMatch = Regex("^[tf]")
        override fun new(str: String, remainder: String): TebiResult {
            val value = "true" == str
            return TebiResult(value, remainder)
        }
    }
}

private class TebiNull : TebiValue() {
    companion object : Parser() {
        override val regex = Regex("^null")
        override val initialMatch = Regex("^n")
        override fun new(str: String, remainder: String): TebiResult {
            return TebiResult(null, remainder)
        }
    }
}
