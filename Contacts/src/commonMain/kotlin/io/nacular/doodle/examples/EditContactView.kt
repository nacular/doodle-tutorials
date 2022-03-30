package io.nacular.doodle.examples

import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.then
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * Handles Contact editing.
 *
 * @param assets containing fonts, colors, etc.
 * @param modals to show modal windows
 * @param contact being edited
 * @param contacts model
 * @param buttons containing styled buttons
 * @param appScope to launch coroutines
 * @param navigator to show various screens
 * @param pathMetrics to measure Paths
 * @param textMetrics to measure text
 * @param uiDispatcher to run coroutines on the UI thread
 * @param textFieldStyler to apply custom behavior to TextFields
 */
class EditContactView(
    assets         : AppConfig,
    modals         : Modals,
    contact        : Contact,
    contacts       : ContactsModel,
    buttons        : AppButtons,
    appScope       : CoroutineScope,
    navigator      : Navigator,
    pathMetrics    : PathMetrics,
    textMetrics    : TextMetrics,
    uiDispatcher   : CoroutineDispatcher,
    textFieldStyler: NativeTextFieldStyler,
): ContactCommon(
    assets       = assets,
    modals       = modals,
    contact      = contact,
    buttons      = buttons,
    contacts     = contacts,
    appScope     = appScope,
    navigator    = navigator,
    textMetrics  = textMetrics,
    uiDispatcher = uiDispatcher
) {
    init {
        lateinit var newName       : String
        lateinit var newPhoneNumber: String

        edit.apply {
            enabled = false
            fired += {
                contacts.edit(super.contact) {
                    name        = newName
                    phoneNumber = newPhoneNumber
                }.onSuccess {
                    super.contact = it
                }

                enabled = false
            }
        }

        val form = Form {
            this(
                super.contact.name        to formTextField(assets, textFieldStyler, pathMetrics, "Name",         assets.nameIcon,  Regex(".+")        ),
                super.contact.phoneNumber to formTextField(assets, textFieldStyler, pathMetrics, "Phone Number", assets.phoneIcon, Regex("[\\s,0-9]+")),
                onInvalid = { edit.enabled = false }
            ) { name_, phone_ ->
                newName        = name_
                newPhoneNumber = phone_
                edit.enabled = name_ != super.contact.name || phone_ != super.contact.phoneNumber
            }
        }.apply {
            font   = assets.small
            layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
        }

        setDetail(form)

        layout = simpleLayout {
            layoutCommonItems()

            edit.position = Point(form.x, form.bounds.bottom + 2 * INSET)
        }.then {
            idealSize = Size(spacer.width + 2 * INSET, edit.bounds.bottom + INSET)
        }
    }
}