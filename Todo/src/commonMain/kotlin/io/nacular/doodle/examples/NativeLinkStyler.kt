package io.nacular.doodle.examples

import io.nacular.doodle.controls.buttons.HyperLink
import io.nacular.doodle.core.Behavior

/**
 * Created by Nicholas Eddy on 1/28/21.
 */
interface NativeLinkStyler {
    operator fun invoke(link: HyperLink, behavior: Behavior<HyperLink>): Behavior<HyperLink>
}
