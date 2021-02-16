package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior

/**
 * Adapter interface to wrap `NativeHyperLinkBehaviorBuilder`--which is JS only--and make it available to
 * common code.
 */
interface NativeLinkStyler {
    /**
     * Gets a native [HyperLink] [Behavior] that "wraps" [behavior]. The end result is a fully
     * customized look-feel controlled by [behavior], with native hyperlink traversal provided
     * by the native wrapper.
     */
    operator fun invoke(link: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink>
}
