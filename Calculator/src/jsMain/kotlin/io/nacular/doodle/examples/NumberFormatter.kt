package io.nacular.doodle.examples

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
 * NumberFormatter based on native JS impl.
 */
class NumberFormatterImpl: NumberFormatter {
    private val formatter = js("new Intl.NumberFormat('en-US', {maximumFractionDigits: 10})")

    override fun invoke(number: Int   ) = formatter.format(number)
    override fun invoke(number: Long  ) = formatter.format(number)
    override fun invoke(number: Float ) = formatter.format(number)
    override fun invoke(number: Double) = formatter.format(number)
}