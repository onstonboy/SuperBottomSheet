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

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SuperBottomSheetDialog : AppCompatDialog {

    private lateinit var mBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var mCoordinatorLayout: CoordinatorLayout
    private lateinit var mCornerRadiusLayout: FrameLayout
    private lateinit var mBottomSheetView: ConstraintLayout
    private lateinit var mTouchOutsideLayout: View
    private lateinit var mContainer: View

    private var mIsCancelable = true

    constructor(context: Context) : this(context, 0) {
        // Hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    constructor(context: Context, theme: Int) : super(context, theme) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        mIsCancelable = cancelable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.run {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun setContentView(@LayoutRes layoutResId: Int) =
        super.setContentView(wrapInBottomSheet(layoutResId, null, null))

    override fun setContentView(view: View) = super.setContentView(wrapInBottomSheet(0, view, null))

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) =
        super.setContentView(wrapInBottomSheet(0, view, params))

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        if (mIsCancelable != cancelable) {
            mIsCancelable = cancelable
            if (::mBehavior.isInitialized) {
                mBehavior.isHideable = cancelable
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::mBehavior.isInitialized) {
            mBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !mIsCancelable) {
            mIsCancelable = true
        }
    }

    private fun wrapInBottomSheet(
        layoutResId: Int,
        view: View?,
        params: ViewGroup.LayoutParams?
    ): View {
        var mainView = view
        initViews()

        if (layoutResId != 0) {
            mainView = layoutInflater.inflate(layoutResId, mCoordinatorLayout, false)
        }

        mBehavior = BottomSheetBehavior.from(mBottomSheetView)
        mBehavior.setBottomSheetCallback(getBottomSheetCallback())
        mBehavior.isHideable = mIsCancelable

        if (params == null) {
            mCornerRadiusLayout.addView(mainView)
        } else {
            mCornerRadiusLayout.addView(mainView, params)
        }

        handleEvents()
        return mContainer
    }

    private fun initViews() {
        mContainer = View.inflate(context, R.layout.super_bottom_sheet_dialog, null)
        mCoordinatorLayout = mContainer.findViewById(R.id.coordinatorLayout)
        mCornerRadiusLayout = mContainer.findViewById(R.id.cornerRadiusLayout)
        mBottomSheetView = mContainer.findViewById(R.id.bottomSheetView)
        mTouchOutsideLayout = mContainer.findViewById<View>(R.id.touchOutside)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleEvents() {
        mTouchOutsideLayout.setOnClickListener { onTouchOutSideClick() }
        ViewCompat.setAccessibilityDelegate(mCornerRadiusLayout, getAccessibilityDelegate())
        mCornerRadiusLayout.setOnTouchListener { _, _ -> true }
    }

    private fun getAccessibilityDelegate(): AccessibilityDelegateCompat? {
        return object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                when {
                    mIsCancelable -> {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS)
                        info.isDismissable = true
                    }
                    else -> info.isDismissable = false
                }
            }

            override fun performAccessibilityAction(
                host: View,
                action: Int,
                args: Bundle
            ): Boolean {
                if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && mIsCancelable) {
                    cancel()
                    return true
                }
                return super.performAccessibilityAction(host, action, args)
            }
        }
    }

    private fun onTouchOutSideClick() {
        if (mIsCancelable && isShowing && shouldWindowCloseOnTouchOutside()) {
            cancel()
        }
    }

    private fun shouldWindowCloseOnTouchOutside(): Boolean {
        return true
    }

    private fun getBottomSheetCallback(): BottomSheetBehavior.BottomSheetCallback? {
        return object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) cancel()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
    }
}