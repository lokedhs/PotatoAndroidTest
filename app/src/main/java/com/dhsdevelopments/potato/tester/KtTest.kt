package com.dhsdevelopments.potato.tester

import com.dhsdevelopments.potato.makeRandomCharacterSequence

class KtTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val buf = StringBuilder()
            buf.append("koko")
            makeRandomCharacterSequence(buf, 5)
            println("buf = '$buf'")
        }
    }
}