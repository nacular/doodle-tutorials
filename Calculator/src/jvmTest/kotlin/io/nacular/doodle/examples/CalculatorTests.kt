package io.nacular.doodle.examples

import JsName
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.FontLoader
import kotlinx.coroutines.GlobalScope
import kotlin.test.Test
import kotlin.test.expect

/**
 * Illustrating how you could test [Calculator].
 */
class CalculatorTests {
    @Test @JsName("test1")
    fun `1 + 4 5 = 46`() {
        expect(46.0) { compute { listOf(`1`, plusButton, `4`, `5`, equalButton) } }
    }

    @Test @JsName("test2")
    fun `1 - + 4 5 = 46`() {
        expect(46.0) { compute { listOf(`1`, minusButton, plusButton, `4`, `5`, equalButton) } }
    }

    @Test @JsName("simpleNegation")
    fun `- 1 = -1`() {
        expect(-1.0) { compute { listOf(negate, `1`, equalButton) } }
    }

    @Test @JsName("negationToggles")
    fun `- - 2 3 4 = 234`() {
        expect(234.0) { compute { listOf(negate, negate, `2`, `3`, `4`, equalButton) } }
    }

    @Test @JsName("decimals")
    fun `decimal 123 = _123`() {
        expect(0.123) { compute { listOf(decimal, `1`, `2`, `3`, equalButton) } }
    }

    @Test @JsName("decimalIdempotent")
    fun `decimal decimal 123 = _123`() {
        expect(0.123) { compute { listOf(decimal, decimal, `1`, `2`, `3`, equalButton) } }
    }

    private fun compute(block: Calculator.() -> List<Button>): Double {
        val calculator = Calculator(fontLoader(), GlobalScope, mockk(relaxed = true), mockk(relaxed = true))

        block(calculator).forEach {
            spyk(it).apply { every { displayed } returns true }.click()
        }

        return calculator.result
    }

    private fun fontLoader() = mockk<FontLoader>(relaxed = true).also {
        coEvery { it(any()             ) } returns mockk()
        coEvery { it(any<Font>(), any()) } returns mockk()
    }
}