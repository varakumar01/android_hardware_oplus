/*
 * SPDX-FileCopyrightText: 2019 CypherOS
 * SPDX-FileCopyrightText: 2014-2020 Paranoid Android
 * SPDX-FileCopyrightText: 2023-2026 The LineageOS Project
 * SPDX-FileCopyrightText: 2023 Yet Another AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.android.internal.graphics.drawable.BackgroundBlurDrawable

class AlertSliderDialog(private val context: Context) :
    Dialog(context, R.style.alert_slider_theme) {
    private val dialogView by lazy { findViewById<ViewGroup>(R.id.alert_slider_dialog)!! }
    private val frameView by lazy { findViewById<ViewGroup>(R.id.alert_slider_view)!! }
    private val glowView by lazy { findViewById<View>(R.id.alert_slider_glow)!! }
    private val iconContainer by lazy { findViewById<FrameLayout>(R.id.alert_slider_icon_container)!! }
    private val textContainer by lazy { findViewById<FrameLayout>(R.id.alert_slider_text_container)!! }
    private val iconView by lazy { findViewById<ImageView>(R.id.alert_slider_icon)!! }
    private val textView by lazy { findViewById<TextView>(R.id.alert_slider_text)!! }
    private val emojiView by lazy { findViewById<TextView>(R.id.alert_slider_emoji_view)!! }

    private val rotation: Int = context.getDisplay().getRotation()
    private val isLandscape = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
    private val flip = context.resources.getBoolean(R.bool.alert_slider_dialog_left)

    private val length: Int
    private val xPos: Int
    private val yPos: Int

    private var isBlurEnabled = false
    private var blurDrawable: BackgroundBlurDrawable? = null

    private var isGlowEnabled = true
    private var glowSpreadDp = 8
    private var glowStrengthPercent = 80

    private var currentAnimatorSet: AnimatorSet? = null
    private var glowAnimatorSet: AnimatorSet? = null
    private var contentAnimatorSet: AnimatorSet? = null
    private var iconMorphAnimator: AnimatorSet? = null
    private var emojiAnimator: AnimatorSet? = null
    private var glowStartRunnable: Runnable? = null
    private var entranceRunnable: Runnable? = null
    private var isDismissing = false
    private var isLabelHidden = false

    init {
        window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.addFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            it.addPrivateFlags(WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY)
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            it.setType(WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY)
            it.attributes =
                it.attributes.apply {
                    format = PixelFormat.TRANSLUCENT
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    title = TAG
                }
            it.setElevation(0f)
            it.decorView.elevation = 0f
            it.decorView.clipToOutline = false
        }

        setCanceledOnTouchOutside(false)
        setContentView(R.layout.alert_slider_dialog)

        dialogView.elevation = 0f
        dialogView.background = null
        dialogView.clipToOutline = false

        frameView.elevation = 0f
        frameView.clipToOutline = true
        frameView.outlineProvider = ViewOutlineProvider.BACKGROUND

        glowView.clipToOutline = false

        frameView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                blurDrawable = null
                val nightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                updateBlurBackground(isBlurEnabled, nightMode)
            }
            override fun onViewDetachedFromWindow(v: View) {
                stopTransientWork()
                blurDrawable = null
            }
        })

        val res = context.resources
        val fraction = res.getFraction(R.fraction.alert_slider_dialog_y, 1, 1)
        val widthPixels = res.displayMetrics.widthPixels
        val heightPixels = res.displayMetrics.heightPixels
        val pads = dialogView.paddingTop * 2
        length =
            if (isLandscape) res.getDimension(R.dimen.alert_slider_dialog_width).toInt()
            else res.getDimension(R.dimen.alert_slider_dialog_height).toInt()
        val hv = (length + pads) * 0.5

        val marginPx = res.getDimensionPixelSize(R.dimen.alert_slider_container_padding)
        val padOffset = 8.toPx()
        xPos =
            if (isLandscape) (widthPixels * fraction - hv).toInt() - padOffset
            else marginPx - padOffset
        yPos =
            if (isLandscape) marginPx - padOffset
            else (heightPixels * fraction - hv).toInt() - padOffset

        window?.let {
            it.attributes =
                it.attributes.apply {
                    gravity =
                        when (rotation) {
                            Surface.ROTATION_0 ->
                                if (flip) Gravity.TOP or Gravity.LEFT
                                else Gravity.TOP or Gravity.RIGHT
                            Surface.ROTATION_90 ->
                                if (flip) Gravity.BOTTOM or Gravity.LEFT
                                else Gravity.TOP or Gravity.LEFT
                            Surface.ROTATION_270 ->
                                if (flip) Gravity.TOP or Gravity.RIGHT
                                else Gravity.BOTTOM or Gravity.RIGHT
                            else ->
                                if (flip) Gravity.BOTTOM or Gravity.LEFT
                                else Gravity.TOP or Gravity.LEFT
                        }
                    x = xPos
                    y = yPos
                }
        }
    }

    override fun show() {
        isDismissing = false
        currentAnimatorSet?.cancel()
        if (!isShowing) {
            super.show()
        }
        startIconAnimation()
        scheduleEntrance()
    }

    private fun scheduleEntrance() {
        entranceRunnable?.let { frameView.removeCallbacks(it) }
        val runnable = Runnable {
            entranceRunnable = null
            animateEntrance()
            triggerGlowBreathing()
        }
        entranceRunnable = runnable
        frameView.post(runnable)
    }

    override fun onStop() {
        stopTransientWork()
        super.onStop()
    }

    override fun dismiss() {
        if (isDismissing || !isShowing) {
            stopTransientWork()
            super.dismiss()
            return
        }
        stopIconAnimation()
        glowAnimatorSet?.cancel()
        animateExit {
            super.dismiss()
        }
    }

    private fun stopIconAnimation() {
        (iconView.drawable as? AnimatedVectorDrawable)?.let { avd ->
            avd.clearAnimationCallbacks()
            avd.stop()
        }
    }

    private fun startIconAnimation() {
        (iconView.drawable as? AnimatedVectorDrawable)?.start()
    }

    private fun stopTransientWork() {
        currentAnimatorSet?.cancel()
        glowAnimatorSet?.cancel()
        contentAnimatorSet?.cancel()
        iconMorphAnimator?.cancel()
        emojiAnimator?.cancel()
        currentAnimatorSet = null
        glowAnimatorSet = null
        contentAnimatorSet = null
        iconMorphAnimator = null
        emojiAnimator = null

        glowStartRunnable?.let { glowView.removeCallbacks(it) }
        glowStartRunnable = null
        entranceRunnable?.let { frameView.removeCallbacks(it) }
        entranceRunnable = null
        frameView.animate().cancel()
        iconContainer.animate().cancel()
        textContainer.animate().cancel()
        stopIconAnimation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            glowView.setRenderEffect(null)
        }

        blurDrawable?.setBlurRadius(0)
        window?.setBackgroundBlurRadius(0)
    }

    private fun getDynamicResourceColor(resName: String, fallbackColor: Int): Int {
        // Force a completely fresh SystemUI context to bypass stale KeyHandler service caches.
        // This guarantees we instantly fetch the exact live Monet overlays used by QS tiles.
        try {
            val freshContext = context.createPackageContext("com.android.systemui", 0)
            val freshThemeContext = ContextThemeWrapper(freshContext, android.R.style.Theme_DeviceDefault_DayNight)
            val resId = freshThemeContext.resources.getIdentifier(resName, "color", "android")
            if (resId != 0) {
                val color = freshThemeContext.resources.getColor(resId, freshThemeContext.theme)
                if (color != 0) return color
            }
        } catch (e: Throwable) {}

        // Fallback to Theme colorAccent with a fresh theme wrapper
        if (resName == "system_accent1_500") {
            try {
                val freshContext = context.createPackageContext("com.android.systemui", 0)
                val freshThemeContext = ContextThemeWrapper(freshContext, android.R.style.Theme_DeviceDefault_DayNight)
                val typedValue = TypedValue()
                if (freshThemeContext.theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)) {
                    if (typedValue.data != 0) return typedValue.data
                }
            } catch (e: Throwable) {}
        }

        return fallbackColor
    }

    private fun getDynamicTextColor(nightMode: Boolean): Int {
        // Text requires the highest contrast shade (equivalent to system_accent1_100 / 900)
        // Since native 100/900 overlays fail to update dynamically on this ROM, we mathematically derive it from 500.
        val baseAccent = getDynamicGlowColor()
        return if (nightMode) {
            blendColors(baseAccent, Color.WHITE, 0.85f) // Very bright pastel
        } else {
            blendColors(baseAccent, Color.BLACK, 0.85f) // Very deep dark
        }
    }

    private fun getDynamicIconColor(nightMode: Boolean): Int {
        // Icon requires a mid-contrast shade (equivalent to system_accent1_300 / 700)
        // Mathematically derived from 500 to ensure distinct shading from text.
        val baseAccent = getDynamicGlowColor()
        return if (nightMode) {
            blendColors(baseAccent, Color.WHITE, 0.40f) // Mid-bright
        } else {
            blendColors(baseAccent, Color.BLACK, 0.40f) // Mid-dark
        }
    }

    private fun getDynamicGlowColor(): Int {
        // Vibrant primary shade for ambient glow (system_accent1_500 updates reliably)
        val resName = "system_accent1_500"
        return getDynamicResourceColor(resName, context.getColor(R.color.alert_slider_icon_color))
    }

    private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val a = (Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio).toInt()
        val r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio).toInt()
        val g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio).toInt()
        val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio).toInt()
        return Color.argb(a, r, g, b)
    }

    private fun getSystemNeutralBackgroundColor(nightMode: Boolean): Int {
        val resName = if (nightMode) "system_neutral1_900" else "system_neutral1_100"
        try {
            val freshContext = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault_DayNight)
            val res = freshContext.resources
            val resId = res.getIdentifier(resName, "color", "android")
            if (resId != 0) {
                val color = res.getColor(resId, freshContext.theme)
                if (color != 0) return color
            }
        } catch (e: Throwable) {}

        try {
            val sysRes = Resources.getSystem()
            val resId = sysRes.getIdentifier(resName, "color", "android")
            if (resId != 0) {
                val color = sysRes.getColor(resId, null)
                if (color != 0) return color
            }
        } catch (e: Throwable) {}

        return if (nightMode) Color.parseColor("#1C1F26") else Color.parseColor("#F0F4F8")
    }

    private fun updateGlowShape(targetWidth: Int) {
        if (!isGlowEnabled) {
            glowView.visibility = View.GONE
            glowView.alpha = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                glowView.setRenderEffect(null)
            }
            return
        }
        glowView.visibility = View.VISIBLE

        val marginPx = glowSpreadDp.toPx()
        val glowWidth = targetWidth + marginPx
        val glowHeight = 48.toPx() + marginPx
        glowView.layoutParams.width = glowWidth
        glowView.layoutParams.height = glowHeight
        glowView.requestLayout()

        val radiusPx = glowHeight / 2f
        val nightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val accentColor = getDynamicGlowColor()

        val strengthFactor = (glowStrengthPercent / 100f).coerceIn(0.1f, 1.0f)
        val strokeAlpha = ((if (nightMode) 220 else 240) * strengthFactor).toInt()
        val fillAlpha = ((if (nightMode) 50 else 70) * strengthFactor).toInt()

        val strokeColor = Color.argb(
            strokeAlpha,
            Color.red(accentColor),
            Color.green(accentColor),
            Color.blue(accentColor)
        )

        val fillColor = Color.argb(
            fillAlpha,
            Color.red(accentColor),
            Color.green(accentColor),
            Color.blue(accentColor)
        )

        val glowDrawable = GradientDrawable().apply {
            cornerRadius = radiusPx
            setStroke((2.5f * strengthFactor).coerceAtLeast(1f).toPx(), strokeColor)
            setColor(fillColor)
        }

        glowView.background = glowDrawable
        glowView.outlineProvider = ViewOutlineProvider.BACKGROUND
        glowView.clipToOutline = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurRadius = (glowSpreadDp / 2f).coerceAtLeast(2f)
            glowView.setRenderEffect(
                RenderEffect.createBlurEffect(
                    blurRadius.toPx().toFloat(),
                    blurRadius.toPx().toFloat(),
                    Shader.TileMode.DECAL
                )
            )
        }
    }

    private fun triggerGlowBreathing() {
        glowAnimatorSet?.cancel()
        glowStartRunnable?.let { glowView.removeCallbacks(it) }
        if (!isGlowEnabled) {
            glowView.visibility = View.GONE
            glowView.alpha = 0f
            return
        }
        val startRunnable = Runnable {
            glowStartRunnable = null
            updateGlowShape(frameView.width)

            val strengthFactor = (glowStrengthPercent / 100f).coerceIn(0.1f, 1.0f)
            val baseAlpha = 0.45f * strengthFactor
            val maxAlpha = 0.70f * strengthFactor
            glowView.alpha = baseAlpha
            glowView.scaleX = 1.0f
            glowView.scaleY = 1.0f

            val fastOutSlowIn = PathInterpolator(0.4f, 0f, 0.2f, 1f)

            // Luminous ambient bloom breathing loop
            val alphaAnim = ObjectAnimator.ofFloat(glowView, View.ALPHA, baseAlpha, maxAlpha).apply {
                duration = 1400
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = fastOutSlowIn
            }

            val scaleXAnim = ObjectAnimator.ofFloat(glowView, View.SCALE_X, 1.0f, 1.025f).apply {
                duration = 1400
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = fastOutSlowIn
            }

            val scaleYAnim = ObjectAnimator.ofFloat(glowView, View.SCALE_Y, 1.0f, 1.025f).apply {
                duration = 1400
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = fastOutSlowIn
            }

            glowAnimatorSet = AnimatorSet().apply {
                playTogether(alphaAnim, scaleXAnim, scaleYAnim)
                start()
            }
        }
        glowStartRunnable = startRunnable
        glowView.post(startRunnable)
    }

    private fun animateVenomIconMorph() {
        iconContainer.animate().cancel()
        iconMorphAnimator?.cancel()
        iconContainer.scaleX = 1.0f
        iconContainer.scaleY = 1.0f
        iconContainer.rotation = 0f
        iconContainer.alpha = 1.0f

        val scaleX = ObjectAnimator.ofFloat(iconContainer, View.SCALE_X, 1.0f, 0.45f, 1.32f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(iconContainer, View.SCALE_Y, 1.0f, 0.45f, 1.32f, 1.0f)
        val rotation = ObjectAnimator.ofFloat(iconContainer, View.ROTATION, 0f, -45f, 25f, 0f)
        val alpha = ObjectAnimator.ofFloat(iconContainer, View.ALPHA, 1.0f, 0.3f, 1.0f)

        iconMorphAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, rotation, alpha)
            duration = 450
            interpolator = OvershootInterpolator(2.5f)
            start()
        }
    }

    private fun animateEntrance() {
        currentAnimatorSet?.cancel()

        textContainer.visibility = if (isLabelHidden) View.GONE else View.VISIBLE
        // Measure the desired content width without applying an intermediate
        // window resize; only the final full width is committed below.
        frameView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        frameView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val fullWidth = frameView.measuredWidth
        val collapsedWidth = 48.toPx()
        val collapsedScale = (collapsedWidth.toFloat() / fullWidth).coerceAtMost(1f)
        frameView.layoutParams.width = fullWidth
        frameView.requestLayout()
        frameView.pivotX = if (flip) fullWidth.toFloat() else 0f
        frameView.pivotY = frameView.height / 2f

        // Phase 1 Initial State (0 - 140ms)
        frameView.alpha = 1f
        if (isBlurEnabled) {
            blurDrawable?.setBlurRadius(BLUR_RADIUS)
        }
        iconContainer.alpha = 0f
        iconContainer.scaleX = 0.78f
        iconContainer.scaleY = 0.78f
        iconContainer.translationX = (-4).toPx().toFloat()

        frameView.scaleX = collapsedScale

        textContainer.alpha = 0f
        textContainer.translationX = 8.toPx().toFloat()

        val fastOutSlowIn = PathInterpolator(0.2f, 0f, 0f, 1f)

        // Phase 1: Icon Spawn (0 - 140ms)
        val iconAlpha = ObjectAnimator.ofFloat(iconContainer, View.ALPHA, 0f, 1f).apply {
            duration = 140
            interpolator = fastOutSlowIn
        }
        val iconScaleX = ObjectAnimator.ofFloat(iconContainer, View.SCALE_X, 0.78f, 1.0f).apply {
            duration = 140
            interpolator = fastOutSlowIn
        }
        val iconScaleY = ObjectAnimator.ofFloat(iconContainer, View.SCALE_Y, 0.78f, 1.0f).apply {
            duration = 140
            interpolator = fastOutSlowIn
        }
        val iconTransX = ObjectAnimator.ofFloat(iconContainer, View.TRANSLATION_X, (-4).toPx().toFloat(), 0f).apply {
            duration = 140
            interpolator = fastOutSlowIn
        }

        // Phase 2: Capsule Morph (140 - 300ms)
        val capsuleExpand = ValueAnimator.ofFloat(collapsedScale, 1f).apply {
            duration = 160
            startDelay = 140
            interpolator = fastOutSlowIn
            addUpdateListener { anim ->
                frameView.scaleX = anim.animatedValue as Float
            }
        }

        // Phase 4: Text Reveal (220 - 380ms)
        val textAlpha = ObjectAnimator.ofFloat(textContainer, View.ALPHA, 0f, 1f).apply {
            duration = 160
            startDelay = 220
            interpolator = fastOutSlowIn
        }
        val textTransX = ObjectAnimator.ofFloat(textContainer, View.TRANSLATION_X, 8.toPx().toFloat(), 0f).apply {
            duration = 160
            startDelay = 220
            interpolator = fastOutSlowIn
        }

        val animList = mutableListOf<Animator>(
            iconAlpha, iconScaleX, iconScaleY, iconTransX,
            capsuleExpand,
            textAlpha, textTransX
        )

        if (isGlowEnabled) {
            glowView.visibility = View.VISIBLE
            glowView.alpha = 0f
            glowView.scaleX = 0.94f
            glowView.scaleY = 0.94f
            updateGlowShape(fullWidth)

            glowView.pivotX = if (flip) fullWidth.toFloat() else 0f
            glowView.pivotY = glowView.height / 2f
            glowView.scaleX = collapsedScale
            val glowExpand = ValueAnimator.ofFloat(collapsedScale, 1f).apply {
                duration = 160
                startDelay = 175
                interpolator = fastOutSlowIn
                addUpdateListener { anim ->
                    glowView.scaleX = anim.animatedValue as Float
                }
            }

            val strengthFactor = (glowStrengthPercent / 100f).coerceIn(0.1f, 1.0f)
            val glowAlpha = ObjectAnimator.ofFloat(glowView, View.ALPHA, 0f, 0.55f * strengthFactor, 0.45f * strengthFactor).apply {
                duration = 160
                startDelay = 175
                interpolator = fastOutSlowIn
            }

            val glowScaleY = ObjectAnimator.ofFloat(glowView, View.SCALE_Y, 0.94f, 1.06f, 1.00f).apply {
                duration = 160
                startDelay = 175
                interpolator = fastOutSlowIn
            }

            animList.add(glowExpand)
            animList.add(glowAlpha)
            animList.add(glowScaleY)
        } else {
            glowView.visibility = View.GONE
            glowView.alpha = 0f
        }

        // Phase 5: Micro Inertia Settle (300 - 340ms) - 100% -> 101% -> 99.8% -> 100%
        val settleAnim = ValueAnimator.ofFloat(1f, 1.01f, 0.998f, 1f).apply {
            duration = 40
            startDelay = 300
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            addUpdateListener { anim ->
                frameView.scaleX = anim.animatedValue as Float
                if (isGlowEnabled) glowView.scaleX = frameView.scaleX
            }
        }

        animList.add(settleAnim)

        currentAnimatorSet = AnimatorSet().apply {
            playTogether(animList)
            start()
        }
    }

    private fun animateExit(onComplete: () -> Unit) {
        currentAnimatorSet?.cancel()
        glowAnimatorSet?.cancel()
        isDismissing = true

        val currentWidth = frameView.width
        val collapsedWidth = 48.toPx()
        val collapsedScale = (collapsedWidth.toFloat() / currentWidth).coerceAtMost(1f)
        val fastOutLinearIn = PathInterpolator(0.4f, 0f, 1f, 1f)

        // Exit Phase 1: Text Fade (0 - 120ms)
        val textAlpha = ObjectAnimator.ofFloat(textContainer, View.ALPHA, textContainer.alpha, 0f).apply {
            duration = 120
            interpolator = fastOutLinearIn
        }
        val textTransX = ObjectAnimator.ofFloat(textContainer, View.TRANSLATION_X, textContainer.translationX, 6.toPx().toFloat()).apply {
            duration = 120
            interpolator = fastOutLinearIn
        }

        // Exit Phase 2: Capsule Collapse (100 - 260ms)
        val capsuleCollapse = ValueAnimator.ofFloat(1f, collapsedScale).apply {
            duration = 160
            startDelay = 100
            interpolator = fastOutLinearIn
            addUpdateListener { anim ->
                frameView.scaleX = anim.animatedValue as Float
            }
        }

        // Exit Phase 3: Icon & Capsule Background Fade (220 - 340ms)
        val frameAlpha = ObjectAnimator.ofFloat(frameView, View.ALPHA, frameView.alpha, 0f).apply {
            duration = 120
            startDelay = 220
            interpolator = fastOutLinearIn
        }
        val iconAlpha = ObjectAnimator.ofFloat(iconContainer, View.ALPHA, iconContainer.alpha, 0f).apply {
            duration = 120
            startDelay = 220
            interpolator = fastOutLinearIn
        }
        val iconScaleX = ObjectAnimator.ofFloat(iconContainer, View.SCALE_X, iconContainer.scaleX, 0.82f).apply {
            duration = 120
            startDelay = 220
            interpolator = fastOutLinearIn
        }
        val iconScaleY = ObjectAnimator.ofFloat(iconContainer, View.SCALE_Y, iconContainer.scaleY, 0.82f).apply {
            duration = 120
            startDelay = 220
            interpolator = fastOutLinearIn
        }

        val animList = mutableListOf<Animator>(
            textAlpha, textTransX, capsuleCollapse, frameAlpha, iconAlpha, iconScaleX, iconScaleY
        )

        if (isBlurEnabled) {
            val blurCollapse = ValueAnimator.ofInt(BLUR_RADIUS, 0).apply {
                duration = 120
                startDelay = 220
                interpolator = fastOutLinearIn
                addUpdateListener { anim ->
                    val r = anim.animatedValue as Int
                    blurDrawable?.setBlurRadius(r)
                }
            }
            animList.add(blurCollapse)

            val glowCollapse = ValueAnimator.ofFloat(1f, collapsedScale).apply {
                duration = 160
                startDelay = 135
                interpolator = fastOutLinearIn
                addUpdateListener { anim ->
                    glowView.scaleX = anim.animatedValue as Float
                }
            }

            val glowAlpha = ObjectAnimator.ofFloat(glowView, View.ALPHA, glowView.alpha, 0f).apply {
                duration = 120
                startDelay = 220
                interpolator = fastOutLinearIn
            }

            animList.add(glowCollapse)
            animList.add(glowAlpha)
        } else {
            glowView.visibility = View.GONE
            glowView.alpha = 0f
        }

        val anim = AnimatorSet().apply {
            playTogether(animList)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isDismissing) {
                        blurDrawable?.setBlurRadius(0)
                        isDismissing = false
                        onComplete()
                    }
                }
            })
        }
        currentAnimatorSet = anim
        anim.start()
    }

    private fun updateWidthAnimated(targetWidth: Int) {
        // Resize the wrap-content overlay once, then animate only properties.
        // Resizing layoutParams on every frame makes BLAST reject buffers while
        // the TYPE_VOLUME_OVERLAY surface is being reconfigured.
        if (frameView.width != targetWidth) {
            frameView.layoutParams.width = targetWidth
            frameView.requestLayout()
            if (isGlowEnabled) updateGlowShape(targetWidth)
        }

        // Venom Liquid Squeeze on Capsule Container
        val scaleAnimator = ObjectAnimator.ofFloat(frameView, View.SCALE_Y, 1.0f, 0.88f, 1.06f, 1.0f).apply {
            duration = 320
            interpolator = OvershootInterpolator(2.0f)
        }

        contentAnimatorSet?.cancel()
        contentAnimatorSet = AnimatorSet().apply {
            playTogether(scaleAnimator)
            start()
        }

        textContainer.alpha = 0.1f
        textContainer.translationX = 14.toPx().toFloat()
        textContainer.animate()
            .alpha(1.0f)
            .translationX(0f)
            .setDuration(260)
            .setInterpolator(PathInterpolator(0.2f, 0f, 0f, 1f))
            .start()
    }

    fun refreshBlur() {
        window?.let {
            it.setBackgroundBlurRadius(0)
            it.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        }
    }

    private fun updateBlurBackground(blurPopup: Boolean, nightMode: Boolean) {
        blurDrawable?.setBlurRadius(0)
        blurDrawable = null
        dialogView.background = null
        dialogView.elevation = 0f
        dialogView.clipToOutline = false
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setElevation(0f)
        window?.decorView?.elevation = 0f
        window?.decorView?.clipToOutline = false
        window?.setBackgroundBlurRadius(0)

        val radiusPx = context.resources.getDimension(R.dimen.alert_slider_corner_radius)

        val dynamicSurfaceColor = getSystemNeutralBackgroundColor(nightMode)

        if (blurPopup) {
            val vri = frameView.viewRootImpl
            if (vri != null) {
                blurDrawable = vri.createBackgroundBlurDrawable()
                val drawable = blurDrawable!!
                val h = frameView.height.toFloat()
                val r = if (h > 0) h / 2f else radiusPx
                drawable.setCornerRadius(r)
                drawable.setBlurRadius(BLUR_RADIUS)
                drawable.setColor(if (nightMode) Color.argb(175, 24, 28, 34) else Color.argb(190, 230, 238, 245))
                frameView.background = drawable
                frameView.invalidate()
            } else {
                val bgDrawable = context.getDrawable(R.drawable.alert_slider_bg)?.mutate() as? GradientDrawable
                if (bgDrawable != null) {
                    bgDrawable.cornerRadius = radiusPx
                    bgDrawable.setColor(dynamicSurfaceColor)
                    frameView.background = bgDrawable
                }
            }
        } else {
            val bgDrawable = context.getDrawable(R.drawable.alert_slider_bg)?.mutate() as? GradientDrawable
            if (bgDrawable != null) {
                bgDrawable.cornerRadius = radiusPx
                bgDrawable.setColor(dynamicSurfaceColor)
                frameView.background = bgDrawable
            }
        }

        frameView.outlineProvider = ViewOutlineProvider.BACKGROUND
        frameView.clipToOutline = true
        frameView.elevation = 0f

        frameView.post {
            val h = frameView.height.toFloat()
            if (h > 0) {
                val capsuleRadius = h / 2f
                blurDrawable?.setCornerRadius(capsuleRadius)
                (frameView.background as? GradientDrawable)?.cornerRadius = capsuleRadius
                frameView.invalidateOutline()
            }
        }
    }

    @Synchronized
    fun setState(position: Int, ringerMode: Int) {
        val resolver = context.contentResolver
        val islandMode = Settings.System.getInt(resolver, "config_alert_slider_island", 0) != 0
        val blurPopup = Settings.System.getInt(resolver, "config_alert_slider_glass", 0) != 0
        val hideLabel = Settings.System.getInt(resolver, "config_alert_slider_hide_label", 0) != 0
        val glowOn = Settings.System.getInt(resolver, "config_alert_slider_glow", 1) != 0
        val glowSpread = Settings.System.getInt(resolver, "config_alert_slider_glow_spread", 8)
        val glowStrength = Settings.System.getInt(resolver, "config_alert_slider_glow_strength", 80)

        isBlurEnabled = blurPopup
        isLabelHidden = hideLabel
        isGlowEnabled = glowOn
        glowSpreadDp = glowSpread
        glowStrengthPercent = glowStrength

        if (!isGlowEnabled) {
            glowView.visibility = View.GONE
            glowView.alpha = 0f
        }

        refreshBlur()

        applyUiContent(position, ringerMode, hideLabel, blurPopup)

        val delta =
            length *
                when (position) {
                    KeyHandler.POSITION_TOP -> -1
                    KeyHandler.POSITION_BOTTOM -> 1
                    else -> 0
                }

        if (islandMode) {
            val statusBarHeight = run {
                val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resId > 0) context.resources.getDimensionPixelSize(resId) else 96
            }
            val islandMarginPx = context.resources.getDimensionPixelSize(R.dimen.alert_slider_container_padding)
            window?.let {
                it.attributes = it.attributes.apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    x = 0
                    y = statusBarHeight + (islandMarginPx / 2)
                }
            }
            dialogView.background = null
            frameView.background = null
        } else {
            val padOffset = 8.toPx()
            window?.let {
                it.attributes = it.attributes.apply {
                    gravity = when (rotation) {
                        Surface.ROTATION_0 -> if (flip) Gravity.TOP or Gravity.LEFT else Gravity.TOP or Gravity.RIGHT
                        Surface.ROTATION_90 -> if (flip) Gravity.BOTTOM or Gravity.LEFT else Gravity.TOP or Gravity.LEFT
                        Surface.ROTATION_270 -> if (flip) Gravity.TOP or Gravity.RIGHT else Gravity.BOTTOM or Gravity.RIGHT
                        else -> if (flip) Gravity.BOTTOM or Gravity.LEFT else Gravity.TOP or Gravity.LEFT
                    }
                    x = xPos + if (isLandscape) delta else 0
                    y = yPos + if (isLandscape) 0 else delta
                }
            }
        }

        val nightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        updateBlurBackground(blurPopup, nightMode)

        if (hideLabel) {
            textContainer.visibility = View.GONE
        } else {
            textContainer.visibility = View.VISIBLE
            textView.setText(
                when (ringerMode) {
                    AudioManager.RINGER_MODE_SILENT -> R.string.alert_slider_mode_silent
                    AudioManager.RINGER_MODE_VIBRATE -> R.string.alert_slider_mode_vibration
                    AudioManager.RINGER_MODE_NORMAL -> R.string.alert_slider_mode_normal
                    KeyHandler.ZEN_PRIORITY_ONLY -> R.string.alert_slider_mode_dnd_priority_only
                    KeyHandler.ZEN_TOTAL_SILENCE -> R.string.alert_slider_mode_dnd_total_silence
                    KeyHandler.ZEN_ALARMS_ONLY -> R.string.alert_slider_mode_dnd_alarms_only
                    KeyHandler.TORCH_ON -> R.string.alert_slider_mode_torch_on
                    KeyHandler.TORCH_OFF -> R.string.alert_slider_mode_torch_off
                    else -> R.string.alert_slider_mode_none
                }
            )
            val nightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            val textColor = getDynamicTextColor(nightMode)
            textView.setTextColor(textColor)
        }

        if (isDismissing) {
            isDismissing = false
            currentAnimatorSet?.cancel()
            if (!isShowing) {
                super.show()
            }
            scheduleEntrance()
        } else if (isShowing) {
            frameView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            updateWidthAnimated(frameView.measuredWidth)
            animateVenomIconMorph()
            triggerGlowBreathing()
        }
    }

    private fun applyUiContent(position: Int, ringerMode: Int, hideLabel: Boolean, blurActive: Boolean) {
        val resolver = context.contentResolver
        val posKey = when (position) {
            KeyHandler.POSITION_TOP -> "top"
            KeyHandler.POSITION_MIDDLE -> "middle"
            KeyHandler.POSITION_BOTTOM -> "bottom"
            else -> null
        }

        val rawEmoji = posKey?.let { Settings.System.getString(resolver, "config_emoji_$it") }
        val emoji = rawEmoji?.takeIf { it.isNotEmpty() }

        stopIconAnimation()

        when {
            emoji != null -> {
                iconView.visibility = View.GONE
                emojiView.visibility = View.VISIBLE
                emojiView.text = emoji
                animateEmoji(emojiView)
            }
            else -> {
                emojiView.visibility = View.GONE
                iconView.visibility = View.VISIBLE
                applyDefaultIcon(ringerMode, blurActive)
            }
        }
    }

    private fun applyDefaultIcon(ringerMode: Int, blurActive: Boolean) {
        val animDrawableRes = when (ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_volume_ringer_vibrate_anim
            AudioManager.RINGER_MODE_NORMAL -> R.drawable.ic_volume_ringer_anim
            AudioManager.RINGER_MODE_SILENT -> R.drawable.ic_volume_ringer_mute_anim
            KeyHandler.ZEN_PRIORITY_ONLY -> R.drawable.ic_notifications_alert_anim
            KeyHandler.ZEN_TOTAL_SILENCE -> R.drawable.ic_notifications_silence_anim
            KeyHandler.ZEN_ALARMS_ONLY -> R.drawable.ic_alarm_anim
            KeyHandler.TORCH_ON -> R.drawable.ic_torch_on_anim
            KeyHandler.TORCH_OFF -> R.drawable.ic_torch_off_anim
            else -> R.drawable.ic_snow_anim
        }
        val staticDrawableRes = when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> R.drawable.ic_volume_ringer_mute
            AudioManager.RINGER_MODE_VIBRATE -> R.drawable.ic_volume_ringer_vibrate
            AudioManager.RINGER_MODE_NORMAL -> R.drawable.ic_volume_ringer
            KeyHandler.ZEN_PRIORITY_ONLY -> R.drawable.ic_notifications_alert
            KeyHandler.ZEN_TOTAL_SILENCE -> R.drawable.ic_notifications_silence
            KeyHandler.ZEN_ALARMS_ONLY -> R.drawable.ic_alarm
            KeyHandler.TORCH_ON -> R.drawable.ic_torch_on
            KeyHandler.TORCH_OFF -> R.drawable.ic_torch_off
            else -> R.drawable.ic_snow
        }

        val nightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val iconColor = getDynamicIconColor(nightMode)

        if (animDrawableRes != 0) {
            iconView.setImageResource(animDrawableRes)
            iconView.setColorFilter(iconColor)
            (iconView.drawable as? AnimatedVectorDrawable)?.let { avd ->
                // The vector resources that are meant to loop declare their own
                // repeatCount. One-shot vectors must not be turned into persistent
                // loops by a callback, especially while this dialog is retained.
                if (isShowing) avd.start()
            }
        } else {
            iconView.setImageResource(staticDrawableRes)
            iconView.setColorFilter(iconColor)
        }
    }

    private fun animateEmoji(view: TextView) {
        view.scaleX = 0.4f
        view.scaleY = 0.4f
        view.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1.15f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1.15f, 0.95f, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)

        emojiAnimator?.cancel()
        emojiAnimator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 400
            interpolator = OvershootInterpolator(1.0f)
            start()
        }
    }

    private fun Int.toPx(): Int = (this * context.resources.displayMetrics.density).toInt()
    private fun Float.toPx(): Int = (this * context.resources.displayMetrics.density).toInt()
    private fun Double.toPx(): Int = (this * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val TAG = "AlertSliderDialog"
        private const val BLUR_RADIUS = 100
    }
}
