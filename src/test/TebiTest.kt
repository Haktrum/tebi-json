package tebi.json

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TebiTest {

    @Test
    fun number() {
        assertEquals(TebiValue.parse("1.2e3"), 1.2e3)
        assertFails { TebiValue.parse("1.2e+-3") }
        assertFails { TebiValue.parse(".2e3") }
    }

    @Test
    fun string() {
        assertEquals(TebiValue.parse("\"asd\""), "asd")            // "asd"
        assertEquals(TebiValue.parse("\"asd\\\"\""), "asd\\\"")    // "asd\""
        assertFails { TebiValue.parse("\"asd\\\"") }
        assertFails { TebiValue.parse("\"\\uD800\\\"") }
    }

    @Test
    fun boolean() {
        assertEquals(TebiValue.parse("true"), true)
        assertEquals(TebiValue.parse("false"), false)
        assertFails { TebiValue.parse("truea") }
    }

    @Test
    fun nully() {
        assertEquals(TebiValue.parse("null"), null)
        assertFails { TebiValue.parse("nulla") }
    }

    @Test
    fun array() {
        assertArrayEquals((TebiValue.parse("[]") as? List<Any?>)?.toTypedArray(), listOf<Any>().toTypedArray())
        assertArrayEquals(
                (TebiValue.parse("[1.2e3,true,null,\"la wea\",[123]]") as? List<Any?>)?.toTypedArray(),
                listOf(1.2e3, true, null, "la wea", listOf(123e0)).toTypedArray()
        )
    }

    @Test
    fun obj() {
        assertEquals(TebiValue.parse("{\"a\":1.2e3}"), mapOf("a" to 1.2e3))
        assertEquals(TebiValue.parse(
                "{\"a\":1200,\"arry\":[\"asd\",null,false,{\"123\":[1,2,3]}]}"),
                mapOf(
                        "a" to 1.2e3,
                        "arry" to listOf("asd", null, false, mapOf("123" to listOf(1e0, 2e0, 3e0)))
                )
        )
        assertEquals(((((TebiValue.parse(
                "{\"a\":1200,\"arry\":[\"asd\",null,false,{\"123\":[1,2,3]}]}") as? Map<*,*>)!!["arry"] as? List<*>)!![3] as? Map<*,*>)!!["123"] as? List<*>)!![0],
                1e0
        )
    }

    data class Result constructor(val parsed: Int, val notParsed: Int) {
        val total = parsed + notParsed
    }

    fun parse(initial: Char): Result {
        var parsed = 0
        var notParsed = 0

        File(TebiValue::class.java.getResource("/parse").toURI()).walk().forEach {
            if (!it.isDirectory) {
                val str = it.readText()
                if (it.name[0] != initial) return@forEach
                try {
                    TebiValue.parse(str)
                    parsed++
                } catch (ex: Throwable) {
                    notParsed++
                }
            }
        }
        return Result(parsed, notParsed)
    }

    @Test
    fun must() {
        val res = parse('y')
        println("MUST: ${res.parsed}/${res.total}")
        assertEquals(res.total, res.parsed)
    }

    @Test
    fun may() {
        val res = parse('i')
        println("MAY: ${res.parsed}/${res.total}")
        //assertEquals(res.total, res.parsed)
    }

    @Test
    fun musnt() {
        val res = parse('n')
        println("MUSN'T: ${res.parsed}/${res.total}")
        assertEquals(0, res.parsed)
    }
}