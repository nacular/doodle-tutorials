package io.nacular.examples

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
    override fun invoke(number: Int   ) = number.toString()
    override fun invoke(number: Long  ) = number.toString()
    override fun invoke(number: Float ) = number.toString()
    override fun invoke(number: Double) = number.toString()
}