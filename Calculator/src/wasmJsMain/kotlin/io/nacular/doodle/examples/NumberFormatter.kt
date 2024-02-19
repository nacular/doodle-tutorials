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

external class NumberFormat

@JsFun("() => ( new Intl.NumberFormat('en-US', {maximumFractionDigits: 10}) )")
external fun numberFormat(): NumberFormat

@JsFun("(obj, value) => ( obj.format(value) )") private external fun format(obj: NumberFormat, value: Int   ): String
@JsFun("(obj, value) => ( obj.format(value) )") private external fun format(obj: NumberFormat, value: Long  ): String
@JsFun("(obj, value) => ( obj.format(value) )") private external fun format(obj: NumberFormat, value: Float ): String
@JsFun("(obj, value) => ( obj.format(value) )") private external fun format(obj: NumberFormat, value: Double): String

/**
 * NumberFormatter based on native JS impl.
 */
class NumberFormatterImpl: NumberFormatter {
    private val formatter = numberFormat()

    override fun invoke(number: Int   ) = format(formatter, number)
    override fun invoke(number: Long  ) = format(formatter, number)
    override fun invoke(number: Float ) = format(formatter, number)
    override fun invoke(number: Double) = format(formatter, number)
}