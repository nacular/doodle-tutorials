package io.nacular.doodle.examples

/**
 * Provides number formatting
 */
expect interface NumberFormatter {
    operator fun invoke(number: Int   ): String
    operator fun invoke(number: Long  ): String
    operator fun invoke(number: Float ): String
    operator fun invoke(number: Double): String
}