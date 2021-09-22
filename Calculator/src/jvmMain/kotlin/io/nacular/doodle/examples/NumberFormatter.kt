package io.nacular.doodle.examples

import java.text.NumberFormat
import java.util.Locale

/**
 * Created by Nicholas Eddy on 8/19/20.
 */
actual interface NumberFormatter {
    actual operator fun invoke(number: Int   ): String
    actual operator fun invoke(number: Long  ): String
    actual operator fun invoke(number: Float ): String
    actual operator fun invoke(number: Double): String
}

/**
 * Simple impl since we are only using jvm for testing.
 */
class NumberFormatterImpl: NumberFormatter {
    private val formatter = NumberFormat.getInstance(Locale.ENGLISH)

    override fun invoke(number: Int   ): String = formatter.format(number)
    override fun invoke(number: Long  ): String = formatter.format(number)
    override fun invoke(number: Float ): String = formatter.format(number)
    override fun invoke(number: Double): String = formatter.format(number)
}