package io.nacular.doodle.examples

import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.transition.easeInOutCubic
import io.nacular.doodle.animation.tweenFloat
import io.nacular.doodle.controls.buttons.Button
import io.nacular.doodle.controls.buttons.PushButton
import io.nacular.doodle.controls.buttons.ToggleButton
import io.nacular.doodle.controls.form.Form
import io.nacular.doodle.controls.form.Form.Companion.FormBuildContext
import io.nacular.doodle.controls.form.textField
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.controls.text.TextField.Purpose
import io.nacular.doodle.controls.text.TextField.Purpose.Password
import io.nacular.doodle.controls.text.TextField.Purpose.Text
import io.nacular.doodle.controls.theme.simpleButtonRenderer
import io.nacular.doodle.controls.theme.simpleTextButtonRenderer
import io.nacular.doodle.core.Layout.Companion.simpleLayout
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.core.view
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Gray
import io.nacular.doodle.drawing.Color.Companion.Lightgray
import io.nacular.doodle.drawing.Color.Companion.Transparent
import io.nacular.doodle.drawing.Color.Companion.White
import io.nacular.doodle.drawing.LinearGradientPaint
import io.nacular.doodle.drawing.Paint
import io.nacular.doodle.drawing.Stroke
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.height
import io.nacular.doodle.drawing.lighter
import io.nacular.doodle.drawing.opacity
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.width
import io.nacular.doodle.examples.AnimatingFormApp.AppFonts
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.geometry.Circle.Companion.Unit
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Point.Companion.Origin
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.geometry.inscribed
import io.nacular.doodle.geometry.inscribedCircle
import io.nacular.doodle.geometry.inset
import io.nacular.doodle.geometry.lerp
import io.nacular.doodle.geometry.path
import io.nacular.doodle.geometry.rounded
import io.nacular.doodle.geometry.toPath
import io.nacular.doodle.layout.HorizontalFlowLayout
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.system.Cursor.Companion.Pointer
import io.nacular.doodle.text.invoke
import io.nacular.doodle.theme.native.NativeTextFieldBehaviorModifier
import io.nacular.doodle.theme.native.NativeTextFieldStyler
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.lerp
import io.nacular.doodle.utils.observable
import io.nacular.measured.units.Angle.Companion.degrees
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times

class AnimatingForm(
    private val fonts          : AppFonts,
    private val animate        : Animator,
    private val textMetrics    : TextMetrics,
    private val pathMetrics    : PathMetrics,
    private val textFieldStyler: NativeTextFieldStyler
): View() {
    private inner class FormSwitcher: View() {
        private inner class SwitcherButton: ToggleButton() {
            var progress by renderProperty(0f)

            private val signUpText     = "SIGN UP"
            private val signInText     = "SIGN IN"
            private val signInTextSize = textMetrics.size(signInText, fonts.button)
            private val signUpTextSize = textMetrics.size(signUpText, fonts.button)

            init {
                font          = fonts.button
                cursor        = Pointer
                acceptsThemes = false
                behavior      = simpleTextButtonRenderer(textMetrics) { button, canvas ->
                    val scale = if (button.model.pressed) 0.9 else 1.0

                    canvas.scale(around = button.bounds.atOrigin.center, scale, scale) {
                        canvas.rect(
                            rectangle = button.bounds.atOrigin.inset(0.5),
                            radius    = button.height / 2,
                            stroke    = Stroke(White, if (button.model.pointerOver) 2.0 else 1.0)
                        )

                        // Draw sign-in text with parallax based on animation progress
                        canvas.text(
                            text = signInText,
                            at   = Point(lerp((width - signUpTextSize.width) / 2, 0.0, minOf(1f, progress * 2)), (height - signInTextSize.height) / 2),
                            font = fonts.button,
                            fill = (White opacity lerp(1f, 0f, minOf(1f, progress * 2))).paint
                        )

                        // Draw sign-up text with parallax based on animation progress
                        canvas.text(
                            text = signUpText,
                            at   = Point(lerp(width - signInTextSize.width, (width - signUpTextSize.width) / 2, maxOf(0f, (progress - 0.5f) * 2)), (height - signInTextSize.height) / 2),
                            font = fonts.button,
                            fill = (White opacity lerp(0f, 1f, maxOf(0f, (progress - 0.5f) * 2))).paint
                        )
                    }
                }

                selectedChanged += { _,_,_ ->
                    val start = animationProgress
                    val end   = if (selected) 1f else 0f

                    animate {
                        // animate using given easing for specific duration
                        start to end using (tweenFloat(easeInOutCubic, 1 * seconds)) {
                            animationProgress = it
                        }
                    }
                }
            }
        }

        var progress by observable(0f)  { _,new ->
            updateBounds()
            button.progress = new
        }

        private val button            = SwitcherButton()
        private val spacing           = 25
        private val lineSpacing       = 1.8f
        private val buttonHeight      = 50.0
        private val parentWidth get() = parent?.width ?: 0.0

        private val signupHeader          = "Welcome Back!"
        private val signupHeaderY  get()  = ((parent?.height ?: 0.0) - (signupHeaderSize.height + signupBodySize.height + buttonHeight + 4 * spacing)) / 2
        private val signupHeaderSize      = textMetrics.size(signupHeader, font = fonts.header)

        private val loginHeader           = "Hello, Friend!"
        private val loginHeaderY    get() = signupHeaderY
        private val loginHeaderSize       = textMetrics.size(loginHeader, font = fonts.header)

        private val signupBody            = "Please login to stay connected with us"
        private val signupBodyY     get() = signupHeaderY + signupHeaderSize.height + spacing
        private var signupBodySize        = bodyTextSize(signupBody)

        private val loginBody             = "Enter your personal details and start your journey"
        private val loginBodyY      get() = loginHeaderY + loginHeaderSize.height + spacing
        private var loginBodySize         = bodyTextSize(loginBody)

        init {
            children += button

            layout = constrain(button) {
                val indent = (minSwitcherWidth - loginHeaderSize.width * 0.75) / 2

                it.top     eq signupBodyY + signupBodySize.height + 2 * spacing
                it.left    eq indent
                it.height  eq buttonHeight
                it.right   eq parent.right - indent
            }
        }

        override fun render(canvas: Canvas) {
            with(canvas) {
                rect(bounds.atOrigin, LinearGradientPaint(gradientColor1, gradientColor2, Origin, Point(0.0, width)))

                translate(Point(x = -x)) { drawBrand (White.paint) }
                drawShapes(           )
                drawText  (           )
            }
        }

        fun updateBounds() {
            val maxWidth   = parentWidth * (1 - minSwitcherWidthFraction)
            val middleRect = Rectangle(maxWidth, size.height).at(x = (parentWidth - maxWidth) / 2)

            suggestBounds(when {
                progress < 0.5f -> lerp(Rectangle(minSwitcherWidth, size.height), middleRect, progress * 2)
                else            -> lerp(middleRect, Rectangle(minSwitcherWidth, size.height).at(x = parentWidth - minSwitcherWidth), (progress - 0.5f) * 2)
            })
        }

        private fun bodyTextSize(text: String) = Size(loginHeaderSize.width, textMetrics.height(
            text        = text,
            font        = fonts.body,
            width       = loginHeaderSize.width,
            lineSpacing = lineSpacing
        ))

        private fun Canvas.drawShapes() {
            translate(by = Point(x = x * shapeParallaxFactory - x)) {
                drawShape(           Identity.scale(height * 0.25).translate(0.0, height))
                drawShape(sides = 4, Identity.translate(350.0, height * 0.85).rotate(45 * degrees).scale(height * 0.05, height * 0.025).rotate(45 * degrees))
                drawShape(sides = 3, Identity.translate(450.0, height * 0.55).scale(height * 0.1, height * 0.1).rotate(-10 * degrees))
                drawShape(sides = 4, Identity.translate(350.0, height * 0.10).rotate(45 * degrees).scale(height * 0.05, height * 0.05).rotate(45 * degrees))
                drawShape(sides = 4, Identity.translate(parentWidth * 0.51, height * 0.35).rotate(45 * degrees).scale(height * 0.06, height * 0.06).rotate(45 * degrees))

                drawShape(sides = 3, Identity.translate(parentWidth * 1.05, 50.0).scale(height * 0.65, height * 0.5).rotate(15 * degrees))
                drawShape(           Identity.translate(parentWidth * 0.60, height * 0.80).scale(height * 0.03))
                drawShape(sides = 3, Identity.translate(parentWidth * 0.80, height * 0.93).scale(height * 0.08, height * 0.088).rotate(45 * degrees))
                drawShape(sides = 4, Identity.translate(parentWidth * 0.63, height * 0.37).rotate(15 * degrees).scale(height * 0.03, height * 0.015).rotate(45 * degrees))
                drawShape(sides = 4, Identity.translate(parentWidth * 0.90, height * 0.60).rotate(45 * degrees).scale(height * 0.03, height * 0.030).rotate(45 * degrees))
            }
        }

        private fun Canvas.drawText() {
            val loginOffset    = (1 + textParallaxFactor) * (parentWidth - minSwitcherWidth)

            translate(by = Point(x = -x * textParallaxFactor)) {
                drawHeader(signupHeader, xOffset = 0.0,         yOffset = signupHeaderY, size = signupHeaderSize)
                drawBody  (signupBody,   xOffset = 0.0,         yOffset = signupBodyY,   size = signupBodySize  )
                drawHeader(loginHeader,  xOffset = loginOffset, yOffset = loginHeaderY,  size = loginHeaderSize )
                drawBody  (loginBody,    xOffset = loginOffset, yOffset = loginBodyY,    size = loginBodySize   )
            }
        }

        private fun Canvas.drawHeader(text: String, xOffset: Double, yOffset: Double, size: Size) {
            text(
                text = text,
                at   = Point(xOffset + (minSwitcherWidth - size.width) / 2 - x, yOffset),
                fill = White.paint,
                font = fonts.header
            )
        }

        private fun Canvas.drawBody(text: String, xOffset: Double, yOffset: Double, size: Size) {
            wrapped(
                text        = text,
                at          = Point(xOffset + (minSwitcherWidth - size.width) / 2 - x, yOffset),
                fill        = White.paint,
                font        = fonts.body,
                width       = size.width,
                alignment   = Center,
                lineSpacing = lineSpacing
            )
        }
    }

    private val brandColor           = Color(0x28b498u)
    private val gradientColor1       = Color(0x3ba5b4u)
    private val gradientColor2       = Color(0x38ba8au)
    private val shapeFill            = (White opacity 0.08f).paint
    private val textParallaxFactor   = 0.5
    private val shapeParallaxFactory = 0.1

    private val brandPath  = path("M14.9186 72.0151C8.0832 70.1869 4.1362 66.4736 1.5661 59.4537-.3822 54.1322-.54 41.9167 1.2464 34.7119 3.0814 27.3115 7.0623 19.428 10.4087 16.5673c2.6707-2.283 7.8334-4.3614 10.7566-4.3304 2.1051.0225 6.8232 2.3728 8.5946 4.2818l1.5893 1.7127 1.8219-5.485c1.0021-3.0167 2.2537-6.1494 2.7815-6.9616 1.4171-2.1806 4.973-4.1444 7.524-4.1554 1.232-.005 3.4957-.4465 5.0304-.9805 1.644-.572 3.6594-.7974 4.9053-.5485 2.9932.5978 6.793 4.6894 8.5766 9.2348 1.3639 3.476 1.5252 5.0142 1.5511 14.7934.0304 11.4833-.5676 16.1673-4.2786 33.514-2.2054 10.3087-3.2323 12.5598-6.3168 13.8469-3.1648 1.3206-7.9445 1.9085-8.48 1.0431-.3044-.4919-1.0346-.4905-2.4571.005-2.6819.9337-4.8598-.0775-6.6334-3.0796l-1.3629-2.307-2.1029 2.2269c-3.4469 3.6501-9.6211 4.6086-16.9897 2.6377zM17.0126 68.6048C10.8556 67.0588 7.4171 63.7175 5.4258 57.3454 3.723 51.8965 3.853 41.4691 5.7119 34.4 8.6694 23.1532 14.0793 16.6119 20.4539 16.5749c2.9668-.0172 5.1509 1.1208 7.391 3.851 1.5129 1.8439 2.3685 2.4472 3.4706 2.4472 1.9517 0 2.9086-1.1742 4.517-5.5429 2.865-7.782 4.4769-10.6817 6.5553-11.7926 1.5025-.8031 4.0491.3357 5.3111 2.375 1.4049 2.2702 2.2449 9.8258 1.8095 16.275-1.0095 14.9501-3.9278 37.4147-5.2803 40.6476-2.4203 5.785-5.7535 4.7293-7.0701-2.2393-1.1336-5.9999-3.1481-5.9267-6.2119.2256-1.0744 2.1574-2.5833 4.3744-3.3531 4.9266-1.6333 1.1716-7.4501 1.6425-10.5803.8566zM23.2387 52.0405c.1917-1.0206.2415-5.6937.1107-10.3848-.2604-9.3377-.7797-11.4212-2.8468-11.4212-2.4995 0-2.8468 3.1427-1.4934 13.5127 1.0845 8.3102 1.6049 10.1488 2.8721 10.1488.7377 0 1.1025-.4987 1.3574-1.8555z")!! //path("M7.4593 36.0076C4.0416 35.0934 2.0681 33.2368.783 29.7268-.1911 27.0661-.27 20.9584.6232 17.356 1.5407 13.6558 3.5311 9.714 5.2043 8.2837c1.3353-1.1415 3.9167-2.1807 5.3783-2.1652 1.0525.0112 3.4116 1.1864 4.2973 2.1409l.7947.8564.911-2.7425c.501-1.5084 1.1269-3.0747 1.3908-3.4808.7085-1.0903 2.4865-2.0722 3.762-2.0777.616-.0025 1.7478-.2233 2.5152-.4903.822-.286 1.8297-.3987 2.4527-.2742 1.4966.2989 3.3965 2.3447 4.2883 4.6174.6819 1.738.7626 2.5071.7755 7.3967.0152 5.7417-.2838 8.0837-2.1393 16.757-1.1027 5.1544-1.6161 6.2799-3.1584 6.9234-1.5824.6603-3.9722.9543-4.24.5215-.1522-.246-.5173-.2453-1.2285.0025-1.3409.4668-2.4299-.0387-3.3167-1.5398l-.6815-1.1535-1.0515 1.1134c-1.7234 1.825-4.8105 2.3043-8.4949 1.3188zM28.3225 3.3129c0-.4963-1.5935-1.5012-2.3806-1.5012-1.0528 0-1.094.079-.5202.9967.2915.4662.7723.657 1.6558.657.6847 0 1.245-.0686 1.245-.1525zM30.0744 7.8475c-.3875-2.4845-.591-2.7285-2.2759-2.7285-1.2626 0-1.4983.0925-1.3328.5231.1106.2877.201 1.0969.201 1.7981 0 1.1905.0712 1.2837 1.0763 1.4083 2.6173.3244 2.5431.3563 2.3314-1.001zM30.3095 12.7321c0-1.4431-.0924-1.6661-.7451-1.7982-.4098-.083-1.1394-.2336-1.6213-.3347-1.0002-.2098-1.2764.2172-1.2764 1.9733 0 1.0317.0933 1.1311 1.2419 1.323.683.1141 1.3909.2579 1.573.3196.7415.2512.8279.0964.8279-1.483zM29.9453 17.4772c.0362-1.4987-.3395-1.7612-2.5335-1.7704-1.0229-.0037-1.0765.057-1.0808 1.2357-.0027.6821-.0908 1.5615-.1963 1.9543-.173.644-.0183.7421 1.5775 1.0004 2.0126.3257 2.1709.1542 2.2331-2.42zM28.8894 24.3298c.2357-1.1666.3786-2.1711.3175-2.232-.1362-.136-3.3415-.7447-3.4341-.6521-.0366.0365-.1671 1.0474-.2901 2.2464-.2235 2.1785-.223 2.1803.7285 2.5321 1.8452.6824 2.2097.4245 2.6782-1.8944zM27.5492 30.6678c.1435-.864.2644-1.7034.2687-1.8652.0043-.1618-.6132-.4773-1.3722-.7012-1.2972-.3825-1.3914-.3649-1.5685.2942-.1036.3856-.1905 1.1844-.193 1.7751-.0037.8966.1732 1.1515 1.0718 1.544 1.5304.6684 1.5061.6826 1.7933-1.047zM25.806 34.1226c.5387-.3934.4891-.4623-.5648-.7854-1.0613-.3253-1.1845-.2902-1.5136.4313-.3305.7244-.2867.7854.5648.7854.5077 0 1.1888-.1941 1.5136-.4313zM8.5063 34.3024C5.4278 33.5294 3.7086 31.8588 2.7129 28.6727 1.8615 25.9482 1.9265 20.7345 2.856 17.2 4.3347 11.5766 7.0396 8.3059 10.2269 8.2875c1.4834-.0086 2.5755.5604 3.6955 1.9255.7565.922 1.1842 1.2236 1.7353 1.2236.9758 0 1.4543-.5871 2.2585-2.7714 1.4325-3.891 2.2385-5.3409 3.2777-5.8963.7513-.4015 2.0246.1678 2.6556 1.1875.7024 1.1351 1.1225 4.9129.9047 8.1375-.5047 7.4751-1.9639 18.7073-2.6402 20.3238-1.2102 2.8925-2.8768 2.3647-3.5351-1.1197-.5668-2.9999-1.574-2.9634-3.1059.1128-.5372 1.0787-1.2916 2.1872-1.6766 2.4633-.8166.5858-3.725.8213-5.2901.4283zM11.6193 26.0202c.0959-.5103.1208-2.8469.0554-5.1924-.1302-4.6689-.3898-5.7106-1.4234-5.7106-1.2497 0-1.4234 1.5714-.7467 6.7564.5423 4.1551.8024 5.0744 1.4361 5.0744.3688 0 .5513-.2494.6787-.9278z")!!
    private val fbLogoPath = path("m6.68 21.87h-4.01v-11.14h-2.67v-3.7h2.67v-2.18c0-3.01.82-4.85 4.36-4.85h2.95v3.7h-1.84c-1.38 0-1.45.52-1.45 1.48l-.01 1.85h3.32l-.39 3.7h-2.92z")!!
    private val gLogoPath  = path("M9.32 9.85h4.16a5.3 5.3 0 01-5.02 3.39 5.2 5.2 0 01-5.23-4.87 5.16 5.16 0 015.27-5.27c1.36 0 2.61.5 3.54 1.32a.6.6 0 00.78-.01l1.53-1.38a.54.54 0 000-.79A8.67 8.67 0 008.67 0c-4.69-.09-8.64 3.61-8.67 8.11-.03 4.54 3.79 8.23 8.5 8.23 4.54 0 8.24-3.42 8.49-7.71l.01-1.88h-7.68a.57.57 0 00-.57.55v2.01c0 .3.26.54.57.54zM21 5a1 1 0 012 0v2h2a1 1 0 010 2h-2v2a1 1 0 01-2 0v-2h-2a1 1 0 010-2h2z")!!
    private val lLogoPath  = path("M18.96 11.01v6.95H14.9v-6.49c0-1.62-.59-2.74-2.06-2.74-1.12 0-1.79.75-2.09 1.48-.1.26-.13.62-.13.98v6.77h-4.07s.06-10.98 0-12.12h4.07v1.72a.14.14 0 00-.03.04h.03v-.04c.54-.83 1.5-2 3.66-2 2.68 0 4.68 1.73 4.68 5.45zm-14.39-8.92c0 1.17-.88 2.1-2.3 2.1h-.02c-1.37 0-2.25-.93-2.25-2.1 0-1.19.91-2.09 2.3-2.09 1.39 0 2.25.9 2.27 2.09zm-4.33 15.87h4.07V5.84h-4.07z")!!

    private val formButtonBehavior = simpleTextButtonRenderer<Button>(textMetrics) { button, canvas ->
        val scale = if (button.model.pressed) 0.9 else 1.0

        canvas.scale(around = button.bounds.atOrigin.center, scale, scale) {
            canvas.rect(
                rectangle = button.bounds.atOrigin.inset(0.5),
                radius    = button.height / 2,
                fill      = if (button.enabled) brandColor.paint else Lightgray.paint
            )

            canvas.text(
                text = button.text,
                at   = textPosition(button),
                font = fonts.button,
                fill = White.paint
            )
        }
    }

    private val signIn                   = formView(isSignIn = true )
    private val signUp                   = formView(isSignIn = false)
    private val formSwitcher             = FormSwitcher()
    private val minSwitcherWidthFraction = 0.4
    private val minSwitcherWidth get()   = width * minSwitcherWidthFraction

    private var animationProgress by observable(0f) { _,new ->
        signIn.suggestX(lerp(minSwitcherWidth, 0.0, new))
        signIn.visible        = new >= 0.5
        signUp.visible        = new  < 0.5
        formSwitcher.progress = new
    }

    init {
        children += signIn
        children += signUp
        children += formSwitcher
        layout    = constrain(signIn, signUp, formSwitcher) { signIn, signUp, switcher ->
            signIn.top      eq 0
            signIn.width    eq parent.width * (1 - minSwitcherWidthFraction)
            signIn.bottom   eq parent.bottom
            signUp.edges    eq signIn.edges
            switcher.top    eq 0
            switcher.bottom eq parent.bottom
        }

        clipCanvasToBounds = false // allow overflow shapes to show

        // Ensure children clip to rounded rect
        childrenClipPath = object: ClipPath() {
            override val path get() = bounds.atOrigin.rounded(10.0)
            override fun contains(point: Point) = point in bounds.atOrigin
        }

        boundsChanged += { _,_,new ->
            formSwitcher.updateBounds()
            signIn.suggestX(lerp(minSwitcherWidth, 0.0, animationProgress))
        }
    }

    override fun render(canvas: Canvas) {
        // Overflow shapes
        with(canvas) {
            drawShape(Identity.scale(height * 0.25).translate(0.0, height), fill = Color(0xfbcf46u).paint)
            drawShape(
                sides = 3, Identity
                    .translate(width * 1.05 + width * (1 - minSwitcherWidthFraction) * shapeParallaxFactory, 50.0)
                    .scale(height * 0.65, height * 0.5)
                    .rotate(15 * degrees),
                fill = Color(0xe35e6au).paint
            )
        }
        // Card
        canvas.outerShadow(vertical = 40.0, blurRadius = 40.0, color = Black opacity 0.125f) {
            path(childrenClipPath!!.path, fill = White.paint)
        }
        canvas.drawBrand()
    }

    private fun Canvas.drawBrand(fill: Paint = brandColor.paint) = translate(Point(20, 20)) {
        scale(0.5, 0.5) { path(brandPath, Stroke(fill, 2.0)) }
    }

    private fun Canvas.drawShape(transform: AffineTransform, within: Circle = Unit, fill: Paint = shapeFill) = drawShape(0, transform, within, fill)

    private fun Canvas.drawShape(sides: Int, transform: AffineTransform, within: Circle = Unit, fill: Paint = shapeFill) = when {
        sides >= 3 -> transform(within.inscribed(sides)!!).toPath()
        else       -> {
            val newCenter = transform(within.center)
            val newRadius = transform(Point(x = within.radius)) - newCenter

            Circle(newCenter.as2d(), newRadius.magnitude()).toPath()
        }
    }.let { path(it, fill) }

    private fun ssoButton(path: Path) = PushButton().apply {
        suggestSize(44.0, 44.0)

        val iconSize = pathMetrics.size(path)

        cursor = Pointer

        behavior = simpleButtonRenderer<Button> { button, canvas ->
            val scale           = if (button.model.pressed) 0.9 else 1.0
            val strokeThickness = 1.0

            canvas.scale(around = button.bounds.atOrigin.center, scale, scale) {
                canvas.circle(
                    circle = button.bounds.atOrigin.inscribedCircle().inset(strokeThickness / 2),
                    stroke = Stroke(Lightgray, strokeThickness)
                )
                translate(Point((button.width - iconSize.width) / 2, (button.height - iconSize.height) / 2)) {
                    path(path, Black.paint)
                }
            }
        }
    }

    private fun formActionButton(text: String) = PushButton(text).apply {
        suggestSize(190.0, 50.0)
        font     = fonts.button
        cursor   = Pointer
        behavior = formButtonBehavior
    }

    private fun textFieldBehavior(textField: TextField) = textFieldStyler(textField, object: NativeTextFieldBehaviorModifier {
        init {
            textField.acceptsThemes   = false
            textField.borderVisible   = false
            textField.backgroundColor = Transparent
        }

        override fun clipCanvasToBounds(view: TextField) = false

        override fun renderBackground(textField: TextField, canvas: Canvas) {
            canvas.rect(Rectangle(-5.0, 0.0, textField.width + 5.0, textField.height), radius = 3.0, Lightgray.lighter(0.75f).paint)
        }
    })

    private fun formLayout() = simpleLayout { items, min, current, max, insets ->
        val width  = 300.0
        var bounds = Rectangle((current.width - width) / 2, 0.0, width, 40.0)

        items.forEachIndexed { i, it ->
            it.updateBounds(bounds)

            bounds = bounds.at(y = bounds.bottom + 10)
        }

        Size(width + 10, bounds.y - insets.bottom - 10)
    }

    private fun formView(isSignIn: Boolean) = view {
        visible = !isSignIn
        val button = formActionButton(if (isSignIn) "SIGN IN" else "SIGN UP").apply { enabled = false }

        + Label(brandColor { if (isSignIn) "Sign in to Doodle" else "Create Account" }).apply { font = fonts.header }
        + view {
            + ssoButton(fbLogoPath)
            + ssoButton(gLogoPath )
            + ssoButton(lLogoPath )

            layout        = HorizontalFlowLayout(spacing = 10.0, justification = HorizontalAlignment.Center)
            preferredSize = fixed(Size(200, 44))
        }
        + Label(Gray { "or user your email ${ if (isSignIn) "account" else "for registration" }" }).apply { font = fonts.body }
        + when {
            isSignIn -> Form { this(
                +email   (),
                +password(),
                onInvalid = { button.enabled = false },
            ) { email, password ->
                button.enabled = true
            } }
            else -> Form { this(
                +textField("Name"),
                +email    (      ),
                +password (      ),
                onInvalid = { button.enabled = false },
            ) { name, email, password ->
                button.enabled = true
            } }
        }.apply {
            layout = formLayout()
        }
        + button

        layout = simpleLayout { items, min, current, max, insets ->
            var yOffset = 100.0

            items.forEachIndexed { index, item ->
                val idealSize = item.idealSize
                item.updateBounds((current.width - idealSize.width) / 2, yOffset, idealSize.width, idealSize.height)
                yOffset += idealSize.height + if (index < 2) 40.0 else 20.0
            }

            current
        }
    }

    // Helper for creating TextField form elements
    private fun FormBuildContext.textField(placeHolder: String, pattern: Regex = Regex(".+"), purpose: Purpose = Text) = textField(pattern = pattern) {
        textField.font        = fonts.textField
        textField.purpose     = purpose
        textField.behavior    = textFieldBehavior(textField)
        textField.placeHolder = placeHolder
    }

    private fun FormBuildContext.email   () = textField("Email",    pattern = Regex(".+@.+\\..+"))
    private fun FormBuildContext.password() = textField("Password", purpose = Password           )
}