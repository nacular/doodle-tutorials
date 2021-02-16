package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior
import io.nacular.doodle.theme.native.NativeHyperLinkBehaviorBuilder

/**
 * Simple wrapper around [NativeHyperLinkBehaviorBuilder].
 */
class NativeLinkStylerImpl(private val delegate: NativeHyperLinkBehaviorBuilder): NativeLinkStyler {
    override fun invoke(link: HyperLink, behavior: Behavior<HyperLink>) = delegate(link, behavior)
}