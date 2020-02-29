package nl.joery.animatedbottombar

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.recyclerview.widget.RecyclerView


class TabIndicator(
    val bottomBar: AnimatedBottomBar,
    val parent: RecyclerView,
    val adapter: TabAdapter
) :
    RecyclerView.ItemDecoration() {
    private lateinit var paint: Paint
    private var corners: FloatArray? = null
    private var animator: ValueAnimator? = null

    private var currentWidth: Float = 0f
    private var currentLeft: Float = 0f

    private val shouldRender: Boolean
        get() = bottomBar.indicatorStyle.indicatorAppearance != AnimatedBottomBar.IndicatorAppearance.NONE

    init {
        applyStyle()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        if (animator?.isRunning == true) {
            currentLeft = animator!!.animatedValue as Float
        } else {
            val view = parent.getChildAt(adapter.getSelectedIndex())
            currentLeft = view.left.toFloat()
            currentWidth = view.width.toFloat()
        }

        if (shouldRender && currentWidth > 0) {
            val left = currentLeft + bottomBar.indicatorStyle.indicatorMargin.toFloat();
            val top = getTop();
            val right =
                currentLeft + currentWidth - bottomBar.indicatorStyle.indicatorMargin.toFloat();
            val bottom = getBottom();

            when (bottomBar.indicatorStyle.indicatorAppearance) {
                AnimatedBottomBar.IndicatorAppearance.SQUARE ->
                    c.drawRect(
                        left,
                        top,
                        right,
                        bottom, paint
                    )
                AnimatedBottomBar.IndicatorAppearance.ROUNDED -> {
                    val path = Path()
                    path.addRoundRect(left, top, right, bottom, getCorners()!!, Path.Direction.CW)
                    c.drawPath(path, paint)
                }
                else -> {
                }
            }
        }
    }

    private fun getTop(): Float {
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                0f
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                parent.height - bottomBar.indicatorStyle.indicatorHeight.toFloat()
            else ->
                0f
        }
    }

    private fun getCorners(): FloatArray? {
        val radius = bottomBar.indicatorStyle.indicatorHeight.toFloat()
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                floatArrayOf(
                    0f, 0f,
                    0f, 0f,
                    radius, radius,
                    radius, radius
                )
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                floatArrayOf(
                    radius, radius,
                    radius, radius,
                    0f, 0f,
                    0f, 0f
                )
            else ->
                null
        }
    }

    private fun getBottom(): Float {
        return when (bottomBar.indicatorStyle.indicatorLocation) {
            AnimatedBottomBar.IndicatorLocation.TOP ->
                bottomBar.indicatorStyle.indicatorHeight.toFloat()
            AnimatedBottomBar.IndicatorLocation.BOTTOM ->
                parent.height.toFloat()
            else ->
                0f
        }
    }

    fun setSelectedIndex(lastIndex: Int, newIndex: Int) {
        if (animator?.isRunning == true) {
            animator!!.cancel()
        }

        if (!shouldRender) {
            return
        }

        val lastView = parent.getChildAt(lastIndex)
        val newView = parent.getChildAt(newIndex)
        val lastWidth = lastView.width.toFloat()
        val newWidth = newView.width.toFloat()

        animator = ValueAnimator.ofFloat(currentLeft, newView.left.toFloat()).apply {
            duration = bottomBar.tabStyle.animationDuration
            interpolator = bottomBar.tabStyle.animationInterpolator
            addUpdateListener { animation ->
                currentWidth = (lastWidth + (newWidth - lastWidth) * animation.animatedFraction)
                parent.postInvalidate()
            }
            start()
        }
    }

    fun applyStyle() {
        paint = Paint().apply {
            color = bottomBar.indicatorStyle.indicatorColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        corners = getCorners()

        if (shouldRender) {
            parent.postInvalidate()
        }
    }
}