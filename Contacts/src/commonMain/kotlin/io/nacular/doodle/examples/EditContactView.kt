package io.nacular.doodle.examples

import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.verticalLayout
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EditContactView(
    assets         : AppAssets,
    modals         : Modals,
    contact        : Contact,
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
    appScope     = appScope,
    navigator    = navigator,
    textMetrics  = textMetrics,
    uiDispatcher = uiDispatcher
) {
    init {
        lateinit var newName: String
        lateinit var newPhoneNumber: String

        edit.apply {
            enabled = false
            fired += {
                navigator.editContact(contact, newName, newPhoneNumber)
            }
        }

        val form = Form {
            this(
                contact.name        to formTextField(assets, textFieldStyler, pathMetrics, "Name",         assets.nameIcon,  Regex(".+")        ),
                contact.phoneNumber to formTextField(assets, textFieldStyler, pathMetrics, "Phone Number", assets.phoneIcon, Regex("[\\s,0-9]+")),
                onInvalid = { edit.enabled = false }
            ) { name_, phone_ ->
                newName = name_
                newPhoneNumber = phone_
                edit.enabled = name_ != contact.name || phone_ != contact.phoneNumber
            }
        }.apply {
            appScope.launch(uiDispatcher) {
                font = assets.small
            }
            layout = verticalLayout(this, spacing = 32.0, itemHeight = 33.0)
        }

        setDetail(form)

        layout = simpleLayout {
            layoutCommonItems()

            edit.position = Point(form.x, form.bounds.bottom + 2 * INSET)
        }
    }
}