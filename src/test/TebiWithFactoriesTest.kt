package tebi.json

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.io.File
import kotlin.test.fail

class TebiWithFactoriesTest {
    @TestFactory
    fun mustFactory(): List<DynamicTest> {
        return dynamicTest('y') {
            DynamicTest.dynamicTest(it.name) {
                try {
                    TebiValue.parse(it.readText())
                } catch (e: NotImplementedError) {
                    println(it.name)
                    fail(it.name)
                }
            }
        }
    }

    @TestFactory
    fun mayFactory(): List<DynamicTest> {
        return dynamicTest('i') {
            DynamicTest.dynamicTest(it.name) {
                try {
                    TebiValue.parse(it.readText())
                } catch (e: NotImplementedException) {
                    println(it.name)
                    fail(it.name)
                }
            }
        }
    }

    @TestFactory
    fun musntFactory(): List<DynamicTest> {
        return dynamicTest('n') {
            val name = it.name
            DynamicTest.dynamicTest(name) {
                try {
                    TebiValue.parse(it.readText())
                    println(it.name)
                    fail(it.name)
                } catch (e: NotImplementedException) {
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