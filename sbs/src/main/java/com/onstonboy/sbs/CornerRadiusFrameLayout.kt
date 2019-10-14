package com.onstonboy.sbs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout

class CornerRadiusFrameLayout : FrameLayout {

    private var mIsNoCornerRadius = false
    private val mPath = Path()
    private val mRect = RectF()
    private val mCornerRadiusArray = floatArrayOf(
        // Top left corner
        0f, 0f,
        // Top right corner
        0f, 0f,
        // Bottom right corner
        0f, 0f,
        // Bottom left corner
        0f, 0f
    )

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.CornerRadiusFrameLayout, 0, 0)
        try {
            // Top left corner
            mCornerRadiusArray[0] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_leftTopCorner, 0f)
            mCornerRadiusArray[1] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_leftTopCorner, 0f)
            // Top right corner
            mCornerRadiusArray[2] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_rightTopCorner, 0f)
            mCornerRadiusArray[3] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_rightTopCorner, 0f)
            // Bottom right corner
            mCornerRadiusArray[4] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_leftBottomCorner, 0f)
            mCornerRadiusArray[5] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_leftBottomCorner, 0f)
            // Bottom left corner
            mCornerRadiusArray[6] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_rightBottomCorner, 0f)
            mCornerRadiusArray[7] = typeArray.getDimension(R.styleable.CornerRadiusFrameLayout_rightBottomCorner, 0f)
        } finally {
            typeArray.recycle()
        }
        resetPath()
        invalidate()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mRect.set(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        when {
            mIsNoCornerRadius -> super.draw(canvas)
            else -> with(canvas) {
                val save = save()
                clipPath(mPath)
                super.draw(this)
                restoreToCount(save)
            }
        }
    }

    fun setTopCornerRadius(radius: Float) {
        // Top left corner
        mCornerRadiusArray[0] = radius
        mCornerRadiusArray[1] = radius

        // Top right corner
        mCornerRadiusArray[2] = radius
        mCornerRadiusArray[3] = radius

        handleValidateLayout(radius)
    }

    fun setBottomCornerRadius(radius: Float) {
        // Bottom left corner
        mCornerRadiusArray[4] = radius
        mCornerRadiusArray[5] = radius

        // Bottom right corner
        mCornerRadiusArray[6] = radius
        mCornerRadiusArray[7] = radius

        handleValidateLayout(radius)
    }

    fun setCornerRadius(radius: Float) {
        // Top left corner
        mCornerRadiusArray[0] = radius
        mCornerRadiusArray[1] = radius

        // Top right corner
        mCornerRadiusArray[2] = radius
        mCornerRadiusArray[3] = radius

        // Bottom left corner
        mCornerRadiusArray[4] = radius
        mCornerRadiusArray[5] = radius

        // Bottom right corner
        mCornerRadiusArray[6] = radius
        mCornerRadiusArray[7] = radius

        handleValidateLayout(radius)
    }

    private fun handleValidateLayout(radius: Float) {
        mIsNoCornerRadius = (radius == 0f)
        if (width == 0 || height == 0) {
            // Discard invalid events
            return
        }
        resetPath()
        invalidate()
    }

    private fun resetPath() {
        mPath.run {
            reset()
            addRoundRect(mRect, mCornerRadiusArray, Path.Direction.CW)
            close()
        }
    }
}
