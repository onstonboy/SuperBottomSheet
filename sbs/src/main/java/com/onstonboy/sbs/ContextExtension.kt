package com.onstonboy.sbs

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue

fun Context?.isTablet() = this?.resources?.getBoolean(R.bool.super_bottom_sheet_isTablet) ?: false

fun Context?.isInPortrait() = this?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.getAttrId(attrId: Int): Int {
    TypedValue().run {
        return when {
            !theme.resolveAttribute(attrId, this, true) -> Constant.INVALID_RESOURCE_ID
            else -> resourceId
        }
    }
}