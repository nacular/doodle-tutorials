package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.labeled
import io.nacular.doodle.controls.form.textField
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.container
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Darkgray
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constrain
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.BasicButtonBehavior
import io.nacular.doodle.utils.Dimension.Height
import io.nacular.doodle.utils.Resizer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Simple app showing how to create custom modals.
 */
class ModalApp(display     : Display,
               focusManager: FocusManager,
               themeManager: ThemeManager,
               theme       : DynamicTheme,
               Modal       : ModalFactory,
   private val textMetrics : TextMetrics): Application {

    private data class LoginInfo(val userName: String, val password: String)


    init {
        // Install theme
        themeManager.selected = theme

        // Create a simple modal that says "Hello"
        val modal = Modal { modal ->

            // Show a simple container with a label and 2 buttons
            container {
                this += Label("Hello").apply { fitText = setOf(Height) }
                this += cancelButton { modal.hide() }
                this += okButton     { modal.hide() }

                layout = constrain(children[0], children[1], children[2]) { label, cancel, ok ->
                    label.top    = parent.top
                    label.left   = parent.left
                    label.right  = parent.right
                    ok.top       = label.bottom + 20
                    ok.right     = label.right
                    cancel.top   = ok.top
                    cancel.right = ok.left - 10
                }.then {
                    idealSize = Size(300.0, children[2].bounds.bottom)
                }
            }
        }

        // Create a modal that will suspend until it gets the user's login info
        val suspendingModal = Modal<LoginInfo> { completed ->

            // Show a container with a form and button
            container {
                lateinit var loginInfo: LoginInfo

                val ok = okButton { completed(loginInfo) }.also { it.enabled = false }

                // Collect username and password
                this += Form {
                    this(
                        + labeled("Name"    ) { textField(Regex(".+"   )) { focusManager.requestFocus(textField) } },
                        + labeled("Password") { textField(Regex(".{8,}")) { textField.mask = '*' } },
                        onInvalid = { ok.enabled = false }
                    ) { name, password ->
                        loginInfo = LoginInfo(name, password)
                        ok.enabled = true
                    }
                }.apply {
                    layout           = verticalLayout(this, spacing = 12.0, itemHeight = 32.0)
                    focusable        = false
                    isFocusCycleRoot = true
                }

                this += ok

                layout = constrain(children[0], ok) { form, ok ->
                    form.top   = parent.top
                    form.left  = parent.left
                    form.right = parent.right
                    ok.top     = form.bottom + 20
                    ok.centerX = form.centerX
                }.then {
                    idealSize = Size(300.0, ok.bounds.bottom)
                }
            }
        }

        display += PushButton("Regular").apply {
            fired += {
                modal.show()
            }

            size = Size(100, 40)
        }

        display += PushButton("Suspending").apply {
            fired += {
                GlobalScope.launch {
                    println(suspendingModal.show())
                }
            }

            size = Size(100, 40)
        }

        display.layout = constrain(display.children[0], display.children[1]) { regular, suspend ->
            regular.right  = parent.centerX - 10
            regular.bottom = parent.bottom - 10
            suspend.bottom = regular.bottom
            suspend.left   = regular.right + 10
        }
    }

    private fun okButton(action: (Button) -> Unit) = PushButton("OK").apply {
        fired        += action
        size          = Size(50, 30)
        acceptsThemes = false
        behavior      = BasicButtonBehavior(
            textMetrics,
            backgroundColor = Color(0x1890ffu),
            foregroundColor = White
        ).apply { hoverColorMapper = { it.lighter(0.25f) } }
    }

    private fun cancelButton(action: (Button) -> Unit) = PushButton("Cancel").apply {
        fired        += action
        size          = Size(60, 30)
        acceptsThemes = false
        behavior      = BasicButtonBehavior(
            textMetrics,
            backgroundColor = White,
            foregroundColor = Black,
            borderWidth     = 0.5,
            borderColor     = Darkgray
        ).apply { hoverColorMapper = { it.lighter(0.5f) } }
    }

    override fun shutdown() { /* no-op */ }
}