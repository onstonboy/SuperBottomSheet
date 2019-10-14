package com.onstonboy.superbottomsheet

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.onstonboy.sbs.SuperBottomSheetFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        show_sheet.setOnClickListener {
            val sheet = DemoBottomSheetFragment()
            sheet.show(supportFragmentManager, "DemoBottomSheetFragment")
        }
    }
}

class DemoBottomSheetFragment : SuperBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_demo_sheet, container, false)
    }

    override fun getCornerRadius() = context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)

    override fun getStatusBarColor() = Color.TRANSPARENT

    override fun isCanChangeStatusBarColor() = false

    override fun getBackgroundColor() = Color.WHITE

    override fun isSheetAlwaysExpanded() = true

    override fun animateCornerRadius() = false

    override fun animateStatusBar() = false

    override fun animateDialog() = true
}
