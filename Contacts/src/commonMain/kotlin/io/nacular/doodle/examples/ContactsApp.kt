package io.nacular.doodle.examples

import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.MutableListModel
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.icons.ImageIcon
import io.nacular.doodle.controls.theme.CommonTextButtonBehavior
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.plusAssign
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.OuterShadow
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Button use to create a new Contact
 */
private class CreateContactButton(router: Router, textMetrics: TextMetrics, addImage: Image): PushButton("Create Contact", ImageIcon(addImage)) {
    init {
        fired += {
            router.goTo("/add")
        }

        val iconSize = icon!!.size(this)
        acceptsThemes   = false
        iconTextSpacing = 10.0
        behavior        = object: CommonTextButtonBehavior<Button>(textMetrics) {
            override fun clipCanvasToBounds(view: Button) = false

            override fun render(view: Button, canvas: Canvas) {
                val draw     = { canvas.rect(bounds.atOrigin, radius = view.height / 2, color = White) }
                val showText = view.width > view.height

                val shadow = when {
                    showText -> OuterShadow(horizontal = 0.0, vertical = 0.0, color = Black.opacity(0.1f), blurRadius = 3.0)
                    else     -> OuterShadow(horizontal = 0.0, vertical = 7.0, color = Black.opacity(0.1f), blurRadius = 5.0)
                }

                canvas.shadow(shadow) {
                    when {
                        view.model.pointerOver && !view.model.pressed -> canvas.outerShadow(horizontal = 0.0, vertical = 3.0, color = Black.opacity(0.1f), blurRadius = 3.0) {
                            draw()
                        }
                        else -> draw()
                    }
                }

                when {
                    showText -> {
                        icon!!.render(view, canvas, at = iconPosition(view, icon = icon!!) + Point(x = 10))
                        canvas.text(view.text, at = textPosition(view, icon = icon) + Point(x = 8), font = view.font, color = Black)
                    }
                    else     -> icon!!.render(view, canvas, at = Point((view.width - iconSize.width) / 2, (view.height - iconSize.height) / 2))
                }
            }
        }
    }
}

class ContactsApp<M>(
    private val display        : Display,
                uiDispatcher   : CoroutineDispatcher,
                navigator      : Navigator,
                fonts          : FontLoader,
                images         : ImageLoader,
                linkStyler     : NativeHyperLinkStyler,
                themeManager   : ThemeManager,
                theme          : DynamicTheme,
                modals         : Modals,
                textMetrics    : TextMetrics,
                pathMetrics    : PathMetrics,
                textFieldStyler: NativeTextFieldStyler,
                buttonFactory  : ButtonFactory,
                router         : Router,
                model          : M
): Application where M: ContactsModel, M: MutableListModel<Contact> {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        appScope.launch(uiDispatcher) {

            themeManager.selected = theme

            val largeFont = fonts("dmsans.woff2") {
                size     = 20
                weight   = 100
                families = listOf("DM Sans")
            }!!

            val appFonts = AppFonts(
                small  = fonts(largeFont) { size = 16 }!!,
                medium = fonts(largeFont) { size = 18 }!!,
                large  = largeFont,
                xLarge = fonts(largeFont) { size = 30 }!!
            )

            val boldSmallFont = fonts(appFonts.small) { weight = 500 }

            val creatImage = images.load("create.png")!!

            display += Header(
                fonts                  = appFonts,
                logoImage              = images.load("logo.png")!!,
                navigator              = navigator,
                textMetrics            = textMetrics,
                pathMetrics            = pathMetrics,
                contactsModel          = model,
                naturalHeight          = PAGE_HEADER_HEIGHT,
                filterCenterAboveWidth = MEDIUM_WIDTH - 2 * INSET,
                filterRightAboveWidth  = SMALL_WIDTH  - 2 * INSET,
            ).apply { font = largeFont }

            val contactList = ContactList(
                fonts       = appFonts,
                modals      = modals,
                appScope    = appScope,
                contacts    = model,
                navigator   = navigator,
                textMetrics = textMetrics,
                pathMetrics = pathMetrics
            ).apply { font = appFonts.small }

            router[""    ] = { _,_ -> setMainView(contactList) }
            router["/add"] = { _,_ ->
                setMainView(CreateContactView(
                    textFieldStyler = textFieldStyler,
                    actions         = navigator,
                    pathMetrics     = pathMetrics,
                    textMetrics     = textMetrics,
                    fonts           = appFonts,
                    image           = creatImage,
                    buttons         = buttonFactory
                ))
            }
            router["/contact/([0-9]+)"] = { _,matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { model.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(ContactView(
                        navigator   = navigator,
                        appScope    = appScope,
                        textMetrics = textMetrics,
                        pathMetrics = pathMetrics,
                        fonts       = appFonts,
                        contact     = contact,
                        buttons     = buttonFactory,
                        modals      = modals,
                        linkStyler  = linkStyler,
                    ))
                }
            }
            router["/contact/([0-9]+)/edit"] = { _,matches ->
                when (val contact = matches.firstOrNull()?.toInt()?.let { model.find(it) }) {
                    null -> navigator.showContactList()
                    else -> setMainView(EditContactView(
                        pathMetrics     = pathMetrics,
                        textFieldStyler = textFieldStyler,
                        navigator       = navigator,
                        appScope        = appScope,
                        textMetrics     = textMetrics,
                        fonts           = appFonts,
                        contact         = contact,
                        buttons         = buttonFactory,
                        modals          = modals,
                    ))
                }
            }

            router.fireAction()

            display += CreateContactButton(router, textMetrics, images.load("add.png")!!).apply { font = boldSmallFont }

            display.layout = simpleLayout { container ->
                val header   = container.children[0]
                val mainView = container.children[1]
                val button   = container.children[2]

                header.size     = Size(container.width - 2 * INSET, if (container.width > SMALL_WIDTH) PAGE_HEADER_HEIGHT else PAGE_HEADER_HEIGHT_COMPACT)
                mainView.bounds = Rectangle(INSET, header.height, header.width, container.height - header.height)

                button.bounds = when {
                    container.width > MEDIUM_WIDTH -> {
                        val size = Size(186, 45)
                        Rectangle(container.width - size.width - 20, (PAGE_HEADER_HEIGHT - size.height) / 2, size.width, size.height)
                    }
                    else           -> {
                        val size = Size(68, 68)
                        Rectangle(container.width - size.width - 20, container.height - size.height - 40, size.width, size.height)
                    }
                }
            }

            display.fill(White.paint)
        }
    }

    private fun setMainView(view: View) {
        when {
            display.children.size < 3 -> display             += view
            else                      -> display.children[1]  = view
        }
    }

    override fun shutdown() { /* no-op */ }
}

private const val MEDIUM_WIDTH               = 768.0 // Moves create button down below this width
private const val SMALL_WIDTH                = 640.0 // Moves filter down below this width
private const val PAGE_HEADER_HEIGHT         =  64.0
private const val PAGE_HEADER_HEIGHT_COMPACT = 116.0 // Height of header for small viewport