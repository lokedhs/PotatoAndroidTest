package com.dhsdevelopments.potato.tester

class Foo(val value: Int) {
    override fun toString(): String {
        return "Foo(value=$value)"
    }
}

class FooResult(val value: Int) {
    override fun toString(): String {
        return "FooResult(value=$value)"
    }
}

infix fun (() -> Foo).bar(fn: () -> Foo): FooResult {
    val v1 = this()
    val v2 = fn()
    return FooResult(v1.value + v2.value)
}

class KtTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val v1 = Foo(1)
            val v2 = Foo(100)
            val v3 = { v1 } bar { v2 }
            println("v3=$v3")

            var i = 0
            val l = generateSequence { i++ }

            var i2 = 1000
            val l2 = generateSequence { if (i2 < 1010) i2++ else null }.plus(l.takeWhile { it < 10 })
            println("content=${l2.toList()}")

            val seq = generateSequence({ 5 }, { it + 4 })
            println("seq value=${seq.takeWhile { it < 100 }.toList()}")
        }
    }
}
