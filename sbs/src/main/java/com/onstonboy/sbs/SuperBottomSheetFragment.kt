/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 André Sousa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Refactored to adapt with my project
 * Le Van Chuong
 */
package com.onstonboy.sbs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.max

abstract class SuperBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var mContext: Context
    private lateinit var mTouchOutsideView: View
    private lateinit var mCornerRadiusLayout: CornerRadiusFrameLayout
    private lateinit var mAnchorView: CornerRadiusFrameLayout
    private lateinit var mBottomSheetLayout: ConstraintLayout
    private lateinit var mBehavior: BottomSheetBehavior<*>

    private var mDim = 0f
    private var mCornerRadius = 0f
    private var mStatusBarColor = 0
    private var mIsAlwaysExpanded = false
    private var mIsSheetCancelableOnTouchOutside = true
    private var mIsSheetCancelable = true
    private var mIsAnimateCornerRadius = true
    private var mIsCanSetStatusBarColor = false
    private var mIsAnimateDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = context ?: return
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mIsAnimateDialog = animateDialog()
        if (mIsAnimateDialog) {
            return SuperBottomSheetDialog(mContext, R.style.superBottomSheetDialog)
        }
        return SuperBottomSheetDialog(mContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mIsCanSetStatusBarColor = isCanChangeStatusBarColor()
        mDim = getDim()
        mCornerRadius = getCornerRadius()
        mStatusBarColor = getStatusBarColor()
        mIsAlwaysExpanded = isSheetAlwaysExpanded()
        mIsSheetCancelable = isSheetCancelable()
        mIsSheetCancelableOnTouchOutside = isSheetCancelableOnTouchOutside()
        mIsAnimateCornerRadius = animateCornerRadius()

        setupSheetDialog(dialog)
        setupSheetWindow(dialog)
        return null
    }

    override fun onStart() {
        super.onStart()
        iniBottomSheetUiComponents()
    }

    private fun setupSheetDialog(dialog: Dialog?) {
        dialog?.run {
            setCancelable(mIsSheetCancelable)

            val isCancelableOnTouchOutside =
                mIsSheetCancelable && mIsSheetCancelableOnTouchOutside
            setCanceledOnTouchOutside(isCancelableOnTouchOutside)
        }
    }

    private fun setupSheetWindow(dialog: Dialog?) {
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(mDim)

            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setStatusBarColor(1f)

            if (context.isTablet() && !context.isInPortrait()) {
                setGravity(Gravity.CENTER_HORIZONTAL)
                setLayout(
                    resources.getDimensionPixelSize(R.dimen.super_bottom_sheet_width),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    @UiThread
    private fun iniBottomSheetUiComponents() {
        val dialog = dialog ?: return
        initViews(dialog)

        mCornerRadiusLayout.setBackgroundColor(getBackgroundColor())
        mCornerRadiusLayout.setTopCornerRadius(mCornerRadius)

        handleIfIsTabletAndInLandscape()
        handleIfHavePeekLayout()
        handleIfInLandscape()
        handleEvents()

        val layoutParams = mBottomSheetLayout.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomSheetLayout.layoutParams = layoutParams
    }

    private fun handleIfInLandscape() {
        // Only skip the collapse state when the device is in landscape or the sheet is always expanded
        val isDeviceInLandscape =
            (!context.isTablet() && !context.isInPortrait()) || mIsAlwaysExpanded
        mBehavior.skipCollapsed = isDeviceInLandscape

        if (isDeviceInLandscape) {
            mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            setStatusBarColor(1f)

            // Load content container height
            mBottomSheetLayout.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (mBottomSheetLayout.height <= 0) return true
                    mBottomSheetLayout.viewTreeObserver.removeOnPreDrawListener(this)
                    // If the content sheet is expanded set the background and status bar properties

                    if (mBottomSheetLayout.height != mTouchOutsideView.height) return true
                    setStatusBarColor(mStatusBarColor.toFloat())
                    if (mIsAnimateCornerRadius) mCornerRadiusLayout.setTopCornerRadius(0f)
                    return true
                }
            })
        }
    }

    private fun handleEvents() {
        mBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    setStatusBarColor(1f)
                    dialog?.cancel()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                setRoundedCornersOnScroll(bottomSheet, slideOffset)
                setStatusBarColorOnScroll(bottomSheet, slideOffset)
            }
        })
    }

    private fun handleIfHavePeekLayout() {
        if (mIsAlwaysExpanded) return
        mBehavior.peekHeight = getPeekHeight()
        mBottomSheetLayout.run {
            minimumHeight = mBehavior.peekHeight
        }
    }

    private fun handleIfIsTabletAndInLandscape() {
        if (context.isTablet() && !context.isInPortrait()) {
            val layoutParams = mBottomSheetLayout.layoutParams
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.super_bottom_sheet_width)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mBottomSheetLayout.layoutParams = layoutParams
        }
    }

    private fun initViews(dialog: Dialog) {
        mCornerRadiusLayout = dialog.findViewById(R.id.cornerRadiusLayout)
        mAnchorView = dialog.findViewById(R.id.anchorView)
        mBottomSheetLayout = dialog.findViewById(R.id.bottomSheetView)
        mTouchOutsideView = dialog.findViewById(R.id.touchOutside)
        mBehavior = BottomSheetBehavior.from(mBottomSheetLayout)
    }

    @UiThread
    private fun setStatusBarColorOnScroll(bottomSheet: View, slideOffset: Float) {
        if (!mIsCanSetStatusBarColor) return

        if (bottomSheet.height != mTouchOutsideView.height) {
            mIsCanSetStatusBarColor = false
            return
        }

        if (slideOffset.isNaN() || slideOffset <= 0) {
            setStatusBarColor(1f)
            return
        }

        val invertOffset = 1 - (1 * slideOffset)
        setStatusBarColor(invertOffset)
    }

    @UiThread
    private fun setStatusBarColor(dim: Float) {
        if (!mIsCanSetStatusBarColor) {
            dialog?.window?.statusBarColor = Color.TRANSPARENT
            return
        }
        val color = calculateColor(mStatusBarColor, dim)
        dialog?.window?.statusBarColor = color
    }

    @UiThread
    private fun setRoundedCornersOnScroll(bottomSheet: View, slideOffset: Float) {
        if (!mIsAnimateCornerRadius) {
            return
        }

        if (bottomSheet.height != mTouchOutsideView.height) {
            mIsAnimateCornerRadius = false
            return
        }

        if (slideOffset.isNaN() || slideOffset <= 0) {
            mCornerRadiusLayout.setTopCornerRadius(mCornerRadius)
            return
        }

        if (mIsAnimateCornerRadius) {
            val radius = mCornerRadius - (mCornerRadius * slideOffset)
            mCornerRadiusLayout.setTopCornerRadius(radius)
        }
    }

    @Dimension
    open fun getPeekHeight(): Int {
        val peekHeightMin =
            when (val peekHeight = mContext.getAttrId(R.attr.superBottomSheet_peekHeight)) {
                Constant.INVALID_RESOURCE_ID -> resources.getDimensionPixelSize(R.dimen.super_bottom_sheet_peek_height)
                else -> resources.getDimensionPixelSize(peekHeight)
            }
        // 16:9 ratio
        return with(resources.displayMetrics) {
            max(peekHeightMin, heightPixels - heightPixels * 9 / 16)
        }
    }

    @Dimension
    open fun getDim(): Float {
        return when (val dim = mContext.getAttrId(R.attr.superBottomSheet_dim)) {
            Constant.INVALID_RESOURCE_ID -> TypedValue().let { typeValue ->
                resources.getValue(R.dimen.super_bottom_sheet_dim, typeValue, true)
                typeValue.float
            }
            else -> TypedValue().let { typeValue ->
                resources.getValue(dim, typeValue, true)
                typeValue.float
            }
        }
    }

    @ColorInt
    open fun getBackgroundColor(): Int {
        return when (val backgroundColor =
            mContext.getAttrId(R.attr.superBottomSheet_backgroundColor)) {
            Constant.INVALID_RESOURCE_ID -> Color.WHITE
            else -> ContextCompat.getColor(mContext, backgroundColor)
        }
    }

    @ColorInt
    open fun getStatusBarColor(): Int {
        return when (val statusBarColor =
            mContext.getAttrId(R.attr.superBottomSheet_statusBarColor)) {
            Constant.INVALID_RESOURCE_ID -> ContextCompat.getColor(
                mContext,
                mContext.getAttrId(R.attr.colorPrimaryDark)
            )
            else -> ContextCompat.getColor(mContext, statusBarColor)
        }
    }

    @Dimension
    open fun getCornerRadius(): Float {
        return when (val cornerRadius = mContext.getAttrId(R.attr.superBottomSheet_cornerRadius)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getDimension(R.dimen.super_bottom_sheet_radius)
            else -> resources.getDimension(cornerRadius)
        }
    }

    open fun isSheetAlwaysExpanded(): Boolean {
        return when (val isAlwaysExpanded =
            mContext.getAttrId(R.attr.superBottomSheet_alwaysExpanded)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_isAlwaysExpanded)
            else -> resources.getBoolean(isAlwaysExpanded)
        }
    }

    open fun isSheetCancelableOnTouchOutside(): Boolean {
        return when (val isSheetCancelableOnTouchOutside =
            mContext.getAttrId(R.attr.superBottomSheet_cancelableOnTouchOutside)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_cancelableOnTouchOutside)
            else -> resources.getBoolean(isSheetCancelableOnTouchOutside)
        }
    }

    open fun isSheetCancelable(): Boolean {
        return when (val isSheetCancelable =
            mContext.getAttrId(R.attr.superBottomSheet_cancelable)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_cancelable)
            else -> resources.getBoolean(isSheetCancelable)
        }
    }

    open fun isCanChangeStatusBarColor(): Boolean {
        return when (val isCanChangeStatusBarColor =
            mContext.getAttrId(R.attr.superBottomSheet_changeStatusBarColor)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_cancelable)
            else -> resources.getBoolean(isCanChangeStatusBarColor)
        }
    }

    open fun animateCornerRadius(): Boolean {
        return when (val animate =
            mContext.getAttrId(R.attr.superBottomSheet_animateCornerRadius)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_animate_corner_radius)
            else -> resources.getBoolean(animate)
        }
    }

    open fun animateStatusBar(): Boolean {
        return when (val animate = mContext.getAttrId(R.attr.superBottomSheet_animateStatusBar)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_animate_status_bar)
            else -> resources.getBoolean(animate)
        }
    }

    open fun animateDialog(): Boolean {
        return when (val animate = mContext.getAttrId(R.attr.superBottomSheet_animateDialog)) {
            Constant.INVALID_RESOURCE_ID -> mContext.resources.getBoolean(R.bool.super_bottom_sheet_animate_status_bar)
            else -> resources.getBoolean(animate)
        }
    }
}