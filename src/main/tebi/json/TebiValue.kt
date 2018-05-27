package tebi.json

import sun.reflect.generics.reflectiveObjects.NotImplementedException
import kotlin.String

sealed class TebiValue {
    companion object {
        fun parse(str: String) : Any? {
            val parsed = parseTebiValue(str.trim())
            return when(parsed.remainder) {
                "" -> parsed.value
                else -> throw NotImplementedException()
            }
        }
        internal fun parseTebiValue(str: String) : TebiResult {
            return when (true) {
                TebiNull.match(str) -> TebiNull.parse(str)
                TebiBoolean.match(str) -> TebiBoolean.parse(str)
                TebiNumber.match(str) -> TebiNumber.parse(str)
                TebiString.match(str) -> TebiString.parse(str)
                TebiArray.match(str) -> TebiArray.parse(str)
                TebiObject.match(str) -> TebiObject.parse(str)
                else -> throw NotImplementedException()
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
                val nextKey = TebiString.parse(toParse)
                if (nextKey.value !is String) throw NotImplementedException()
                toParse = nextKey.remainder.trimStart()
                if (toParse[0] != ':') throw NotImplementedException()
                toParse = toParse.drop(1).trimStart()
                val nextValue = parseTebiValue(toParse)
                map[nextKey.value] = nextValue.value
                toParse = nextValue.remainder.trimStart()
            } while (toParse[0] == ',')
            return when (toParse[0] == '}') {
                true -> TebiResult(map.toMap(), toParse.drop(1))
                else -> throw NotImplementedException()
            }
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
            } while (toParse[0] in arrayOf(','))
            return when (toParse[0] == ']') {
                true -> TebiResult(list.toList(), toParse.drop(1))
                else -> throw NotImplementedException()
            }
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
            val value = when(true) {
                "true" == str -> true
                "false" == str -> false
                else -> throw NotImplementedException()
            }
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
