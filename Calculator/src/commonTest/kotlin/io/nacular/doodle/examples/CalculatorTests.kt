package io.nacular.doodle.examples

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontDetector
import kotlin.js.JsName
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.expect

/**
 * Created by Nicholas Eddy on 11/14/20.
 */
class CalculatorTests {
    @Test @JsName("test1")
    fun `1 + 4 5 = 46`() {
        expect(46.0) { compute { listOf(`1`, `+`, `4`, `5`, `=`) } }
    }

    @Test @JsName("test2")
    fun `1 - + 4 5 = 46`() {
        expect(46.0) { compute { listOf(`1`, `-`, `+`, `4`, `5`, `=`) } }
    }

    @Test @JsName("test3")
    fun `- 1 = -1`() {
        expect(-1.0) { compute { listOf(negate, `1`, `=`) } }
    }

    private fun compute(block: Calculator.() -> List<Button>): Double {
        val calculator = Calculator(fontDetector(), mockk(relaxed = true), mockk(relaxed = true))

        block(calculator).forEach {
            spyk(it).apply { every { displayed } returns true }.click()
        }

        return calculator.result
    }

    private fun fontDetector() = mockk<FontDetector>(relaxed = true)/*.also {
        coEvery { it.invoke(any()       ) } returns mockk()
        coEvery { it.invoke(any(), any()) } returns mockk()
    }*/
}