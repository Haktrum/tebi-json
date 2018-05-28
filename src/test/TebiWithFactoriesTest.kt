package tebi.json

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.text.ParseException
import kotlin.test.fail

class TebiWithFactoriesTest {

    @TestFactory
    fun must(): List<DynamicTest> {
        return dynamicTest('y') {
            val name = it.name
            val text = it.readText()
            DynamicTest.dynamicTest(name) {
                try {
                    TebiValue.parse(text)
                } catch (e: ParseException) {
                    fail(name)
                }
            }
        }
    }

    @TestFactory
    fun may(): List<DynamicTest> {
        return dynamicTest('i') {
            val name = it.name
            val text = it.readText()
            DynamicTest.dynamicTest(name) {
                try {
                    TebiValue.parse(text)
                } catch (e: ParseException) {
                    fail(name)
                }
            }
        }
    }

    @TestFactory
    fun musnt(): List<DynamicTest> {
        return dynamicTest('n') {
            val name = it.name
            val text = it.readText()
            DynamicTest.dynamicTest(name) {
                try {
                    TebiValue.parse(text)
                    fail(name)
                } catch (e: ParseException) {
                    // pass
                }
            }
        }
    }

    fun dynamicTest(init: Char, factory: (File) -> DynamicTest) = File(TebiValue::class.java.getResource("/parse").toURI())
            .walk().asIterable()
            .filter { !it.isDirectory && it.name[0] == init }
            .map {
                factory(it)
            }
}