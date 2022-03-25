package io.nacular.doodle.examples

import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineScope

class EditContactView(
    navigator      : Navigator,
    textFieldStyler: NativeTextFieldStyler,
    pathMetrics    : PathMetrics,
    appScope       : CoroutineScope,
    textMetrics    : TextMetrics,
    contact        : Contact,
    fonts          : AppFonts,
    buttons        : ButtonFactory,
    modals         : Modals): ContactCommon(textMetrics, navigator, appScope, contact, fonts, buttons, modals) {
    init {
        lateinit var newName       : String
        lateinit var newPhoneNumber: String

        edit.apply {
            enabled = false
            fired += {
                navigator.editContact(contact, newName, newPhoneNumber)
            }
        }

        val form = Form { this(
                contact.name        to customTextField(textFieldStyler, pathMetrics, "Name",         NAME_ICON_PATH,  Regex(".+"        )),
                contact.phoneNumber to customTextField(textFieldStyler, pathMetrics, "Phone Number", PHONE_ICON_PATH, Regex("[\\s,0-9]+")),
                onInvalid = { edit.enabled = false }
            ) { name_, phone_ ->
                newName        = name_
                newPhoneNumber = phone_
                edit.enabled   = name_ != contact.name || phone_ != contact.phoneNumber
            }
        }.apply {
            font   = fonts.small
            layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
        }

        setDetail(form)

        layout = simpleLayout {
            layoutCommonItems()

            edit.position = Point(form.x, form.bounds.bottom + 2 * INSET)
        }
    }
}