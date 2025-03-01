package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.invoke
import io.nacular.doodle.animation.transition.easeOutBack
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.Photo
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.itemVisualizer
import io.nacular.doodle.controls.range.CircularSlider
import io.nacular.doodle.controls.spinbutton.MutableIntSpinButtonModel
import io.nacular.doodle.controls.spinbutton.MutableSpinButton
import io.nacular.doodle.controls.spinbutton.SpinButton
import io.nacular.doodle.controls.spinbutton.SpinButtonModel
import io.nacular.doodle.controls.spinbutton.spinButtonEditor
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.core.Container
import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.View.SizeAuditor.Companion.preserveAspect
import io.nacular.doodle.core.center
import io.nacular.doodle.core.container
import io.nacular.doodle.core.height
import io.nacular.doodle.core.width
import io.nacular.doodle.datatransport.Files
import io.nacular.doodle.datatransport.dragdrop.DropEvent
import io.nacular.doodle.datatransport.dragdrop.DropReceiver
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.AffineTransform2D
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Darkgray
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.computeAngle
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.rect
import io.nacular.doodle.event.PointerListener.Companion.pressed
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.times
import io.nacular.doodle.image.Image
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.image.aspectRatio
import io.nacular.doodle.image.height
import io.nacular.doodle.image.width
import io.nacular.doodle.layout.Insets
import io.nacular.doodle.layout.ListLayout
import io.nacular.doodle.layout.WidthSource.Parent
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.system.Cursor.Companion.Text
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.basic.range.BasicCircularSliderBehavior
import io.nacular.doodle.theme.basic.spinbutton.SpinButtonTextEditOperation
import io.nacular.doodle.utils.PropertyObserver
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.Resizer
import io.nacular.doodle.utils.SetPool
import io.nacular.doodle.utils.ToStringIntEncoder
import io.nacular.doodle.utils.lerp
import io.nacular.measured.units.Angle
import io.nacular.measured.units.Angle.Companion.atan2
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time.Companion.milliseconds
import io.nacular.measured.units.normalize
import io.nacular.measured.units.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.properties.Delegates.observable
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import io.nacular.doodle.datatransport.Image as ImageType

/**
 * Panel used to display editable properties of an image.
 */
private class PropertyPanel(private val focusManager: FocusManager): Container() {
    private val spinButtonHeight = 24.0

    private interface Settable<T> {
        val name: String
        fun get(): T
        fun set(value: T)
    }

    /**
     * Simple View with a [MutableSpinButton] and [Label] that displays a numeric property.
     */
    private inner class Property(
        private val property  : Settable<Double>,
        private val updateWhen: PropertyObservers<Any, Any>,
                    suffix    : String = ""
    ): View() {
        constructor(
            property: KMutableProperty0<Double>,
            updateWhen: PropertyObservers<Any, Any>,
            suffix    : String = ""
        ): this(
            object : Settable<Double> {
                override val name get() = property.name

                override fun get(             ) = property.get()
                override fun set(value: Double) { property.set(value) }
            },
            updateWhen,
            suffix
        )

        private fun <T, M: SpinButtonModel<T>> spinButtonVisualizer(suffix: String = "") = itemVisualizer { item: Int, previous: View?, context: SpinButton<T, M> ->
            when (previous) {
                is Label -> previous.also { it.text = "$item$suffix" }
                else     -> Label("$item$suffix").apply {
                    font            = previous?.font
                    cursor          = Text
                    foregroundColor = previous?.foregroundColor
                    backgroundColor = previous?.backgroundColor ?: Transparent

                    (context as? MutableSpinButton<*,*>)?.let { spinButton ->
                        pointerFilter += pressed { event ->
                            spinButton.startEditing()
                            event.consume()
                        }
                    }
                }
            }
        }

        private val spinButton = MutableSpinButton(
            MutableIntSpinButtonModel(Int.MIN_VALUE .. Int.MAX_VALUE, property.get().toInt()),
            spinButtonVisualizer(suffix)
        ).apply {
            cellAlignment = fill

            // Make the spinButton editable
            editor = spinButtonEditor { button, value, current ->
                object: SpinButtonTextEditOperation<Int>(focusManager, ToStringIntEncoder, button, value, current) {
                    init {
                        textField.selectionBackgroundColor = Darkgray
                    }
                }
            }

            changed += {
                it.value.onSuccess {
                    if (property.get().toInt() != it) {
                        property.set(it.toDouble())
                    }
                }
            }
        }

        private val callBack: PropertyObserver<Any, Any> = { _,_,_ -> spinButton.set(property.get().toInt()) }

        init {
            updateWhen += callBack

            children += listOf(spinButton, Label(property.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }))

            // Label is centered below spinButton, which is stretched to fit its parent's width
            layout = constrain(children[0], children[1]) { spinButton, label ->
                spinButton.top    eq 0
                spinButton.left   eq 0
                spinButton.right  eq parent.right
                spinButton.height eq spinButtonHeight
                label.top         eq spinButton.bottom + 2
                label.centerX     eq parent.centerX
                label.height      eq label.height.readOnly
            }
        }

        override fun removedFromDisplay() {
            updateWhen -= callBack
        }
    }

    /**
     * Simple container created from a [Label] and two property views.
     */
    private fun propertyGroup(name: String, property1: View, property2: View) = container {
        this += listOf(Label(name), property1, property2)

        val spacing = 10.0

        layout = constrain(children[0], property1, property2) { label, first, second ->
            label.top     eq second.top
            label.left    eq spacing
            label.height  eq spinButtonHeight
            first.centerY eq parent.centerY
            first.right   eq second.left - spacing
            first.width   eq second.width
            first.height  eq 50
            second.height eq 50
            second.top    eq first.top.readOnly
            second.right  eq parent.right - spacing
            second.width  eq (parent.width - spacing * 3) / 3

            parent.height greaterEq first.bottom
            parent.height greaterEq second.bottom
        }
    }

    // Used to cache listeners, so they can be cleaned up when photo changes
    private val photoBoundsChanged   : PropertyObservers<View, Rectangle>       = SetPool()
    private val photoTransformChanged: PropertyObservers<View, AffineTransform> = SetPool()

    var photo: View? by io.nacular.doodle.utils.observable(null) {old, new ->
        old?.let { photo ->
            (photoBoundsChanged    as SetPool).forEach { photo.boundsChanged    -= it }
            (photoTransformChanged as SetPool).forEach { photo.transformChanged -= it }
        }

        children.clear()

        val computeAngle: (View) -> Measure<Angle> = { (it.transform as? AffineTransform2D)?.computeAngle() ?: (0 * degrees) }

        new?.also { photo ->
            val photoAngle = object: Any() {
                var angle by observable(computeAngle(photo) `in` degrees) { _,old,new ->
                    if ((new * degrees).normalize() != computeAngle(photo).normalize()) {
                        photo.transform = photo.transform.rotate(around = photo.center, (new - old) * degrees)
                    }
                }
            }

            val rotationSlider = CircularSlider(0 * degrees .. 360 * degrees).apply {
                suggestSize(Size(50))
                value    = photoAngle.angle * degrees
                behavior = BasicCircularSliderBehavior(thickness = 18.0)
                changed += { _,_,new -> photoAngle.angle = new `in` degrees }
            }

            photo.transformChanged += { _,_,_ ->
                photoAngle.angle     = computeAngle(photo) `in` degrees
                rotationSlider.value = photoAngle.angle * degrees
            }

            children += propertyGroup("Size",     Property(prop(photo::width, photo::suggestWidth), photoBoundsChanged), Property(prop(photo::height, photo::suggestHeight), photoBoundsChanged                 ))
            children += propertyGroup("Position", Property(prop(photo::x,     photo::suggestX    ), photoBoundsChanged), Property(prop(photo::y,      photo::suggestY     ), photoBoundsChanged                 ))
            children += propertyGroup("Rotation", rotationSlider,                                                        Property(photoAngle::angle,                         photoTransformChanged, suffix = "°"))

            (photoBoundsChanged    as SetPool).forEach { photo.boundsChanged    += it }
            (photoTransformChanged as SetPool).forEach { photo.transformChanged += it }
        }
    }

    init {
        clipCanvasToBounds = false

        insets = Insets(top = 48.0, left = 20.0, right = 20.0)
        layout = ListLayout(widthSource = Parent)

        suggestWidth(300.0 + insets.left)
    }

    override fun render(canvas: Canvas) {
        canvas.outerShadow(blurRadius = 8.0, color = Black opacity 0.3f) {
            rect(bounds.atOrigin.inset(Insets(top = 20.0)), radius = 20.0, color = White)
        }
    }

    private fun prop(prop: KProperty0<Double>, set: (Double) -> Unit) = object: Settable<Double> {
        override val name get() = prop.name
        override fun get() = prop.get()
        override fun set(value: Double) { set(value) }
    }
}

/**
 * Simple View that displays an image and ensures its aspect ratio.
 */
private fun fixedAspectPhoto(image: Image) = Photo(image).apply {
    sizeAuditor = preserveAspect(image.width, image.height)
}

/**
 * Simple app for displaying images.
 */
class PhotosApp(display     : Display,
                focusManager: FocusManager,
                themeManager: ThemeManager,
                theme       : DynamicTheme,
                animate     : Animator,
    private val random      : Random,
    private val images      : ImageLoader): Application {

    init {
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        themeManager.selected = theme

        val buttonInset          = 20.0
        var panelVisibleFraction = 0f

        // Contains controls to show/update photo properties
        val propertyPanel = PropertyPanel(focusManager).apply { opacity = 0f }

        // Used to show/hide panel
        val panelToggle = PushButton().apply {
            suggestY(display.height - buttonInset / 2)
            suggestHeight(50.0)
            behavior = simpleButtonRenderer { _, canvas ->
                val color = when {
                    model.pointerOver -> Gray
                    model.pressed     -> Darkgray
                    else              -> Lightgray
                }

                canvas.rect(Rectangle(width / 2 - 25, height - 35.0, 50.0, 10.0), radius = 5.0, color = color)
            }

            fired += {
                val start = panelVisibleFraction
                val end   = if (panelVisibleFraction > 0f) 0f else 1f

                // Animate property panel show/hide
                animate(start to end, tweenFloat(easeOutBack, 250 * milliseconds)) {
                    propertyPanel.suggestY(display.height - buttonInset * 2 - ((propertyPanel.height - 30) * it.toDouble()))
                    propertyPanel.opacity = it
                    panelVisibleFraction  = it
                }
            }
        }

        // Holds images and serves as drop-target
        val mainContainer = container {
            val import = { image: Image, location: Point ->
                // Create fixed-aspect Photo from image
                val photo  = fixedAspectPhoto(image)

                val width  = display.width * 0.5
                val height = width / image.aspectRatio

                photo.suggestBounds(Rectangle(location - Point(width, height) / 2, Size(width, height)))

                photo.transform = Identity.rotate(
                    around = location,
                    by     = lerp(-15 * degrees, 15 * degrees, random.nextFloat())
                )

                // Bring photo to foreground and update panel on pointer-down
                photo.pointerChanged += pressed {
                    children.move(photo, to = children.size - 1)
                    propertyPanel.photo = photo
                }

                // Register gesture recognizer to track multi-pointer interactions
                GestureRecognizer(photo).changed += object: GestureListener<GestureEvent> {
                    lateinit var originalSize    : Size
                    lateinit var originalCenter  : Point
                    lateinit var originalVector  : Point
                    lateinit var originalPosition: Point
                    lateinit var initialTransform: AffineTransform

                    override fun started(event: GestureEvent) {
                        // Capture initial state to apply deltas with in `changed`
                        originalSize     = photo.size
                        originalCenter   = this@container.toLocal(event.center, photo)
                        originalVector   = event.initial[1].inParent(photo) - event.initial[0].inParent(photo)
                        originalPosition = photo.position
                        initialTransform = photo.transform

                        event.consume() // ensure event is consumed from Resizer
                    }

                    override fun changed(event: GestureEvent) {
                        val currentVector = event.current[1].inParent(photo) - event.current[0].inParent(photo)

                        // Angle between initial set of points and their current locations
                        val transformAngle = atan2(
                            originalVector.x * currentVector.y - originalVector.y * currentVector.x,
                            originalVector.x * currentVector.x + originalVector.y * currentVector.y
                        )

                        // Use transform for rotation
                        photo.transform = initialTransform.rotate(around = originalCenter, by = transformAngle)

                        // Use bounds to keep panel updated
                        photo.suggestBounds(Rectangle(
                            position = originalPosition - ((originalPosition - originalCenter) * (1 - event.scale)),
                            size     = originalSize * event.scale
                        ))

                        event.consume() // ensure event is consumed from Resizer
                    }

                    override fun ended(event: GestureEvent) {
                        event.consume() // ensure event is consumed from Resizer
                    }
                }

                Resizer(photo) // Use to handle edge resizing and movement with single pointer

                children += photo

                photo
            }

            dropReceiver = object: DropReceiver {
                private  val allowedFileTypes                    = Files(ImageType("jpg"), ImageType("jpeg"), ImageType("png"))
                override val active                              = true
                private  fun allowed          (event: DropEvent) = allowedFileTypes in event.bundle
                override fun dropEnter        (event: DropEvent) = allowed(event)
                override fun dropOver         (event: DropEvent) = allowed(event)
                override fun dropActionChanged(event: DropEvent) = allowed(event)
                override fun drop             (event: DropEvent) = event.bundle[allowedFileTypes]?.let { files ->
                    val photos = files.map { appScope.async { images.load(it) } }

                    // Import images
                    appScope.launch {
                        photos.mapNotNull { it.await() }.forEach { import(it, event.location) }
                    }
                    true
                } ?: false
            }

            // Load default images
            appScope.launch {
                listOf("tetons.jpg", "earth.jpg").forEachIndexed { index, file ->
                    images.load(file)?.let {
                        propertyPanel.photo = import(it, display.center + Point(y = index * 50.0))
                    }
                }
            }
        }

        display += listOf(mainContainer, propertyPanel, panelToggle)

        display.layout = constrain(mainContainer, panelToggle, propertyPanel) { main, toggle, panel ->
            main.edges     eq parent.edges

            toggle.width   eq panel.width - 24
            toggle.height  eq 50
            toggle.bottom  eq panel.top + 22 + 40
            toggle.centerX eq panel.centerX

            panel.top      eq parent.bottom - buttonInset * 2 - panelVisibleFraction * (panel.height - buttonInset)
            panel.height   eq panel.idealHeight
            panel.centerX  eq parent.centerX
        }

        display.relayout()
    }

    override fun shutdown() {}
}