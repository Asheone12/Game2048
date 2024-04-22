package com.muen.game2048.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.muen.game2048.R

class Card @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var textView: TextView
    var num = 0

    val backgroundColorIdMap: Map<Int, Int> = mapOf(
        0 to R.color.backgroundColor0,
        2 to R.color.backgroundColor2,
        4 to R.color.backgroundColor4,
        8 to R.color.backgroundColor8,
        16 to R.color.backgroundColor16,
        32 to R.color.backgroundColor32,
        64 to R.color.backgroundColor64,
        128 to R.color.backgroundColor128,
        256 to R.color.backgroundColor256,
        512 to R.color.backgroundColor512,
        1024 to R.color.backgroundColor1024,
        2048 to R.color.backgroundColor2048
    )

    val textColorIdMap: Map<Int, Int> = mapOf(
        0 to R.color.textColor0,
        2 to R.color.textColor2,
        4 to R.color.textColor4
    )


    init {
        textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.text = getNumber().toString()
        textView.textSize = 50F
        setNumber(0)

        val lp = LayoutParams(-1, -1)
        lp.setMargins(10, 10, 10, 10)

        addView(textView, lp)
    }

    fun getNumber(): Int {
        return num
    }

    fun setNumber(num: Int) {
        this.num = num
        textView.text = num.toString()
        changeColor(num)
        changeSize(num)
    }

    private fun changeSize(num: Int) {
        if (num >= 1024) {
            textView.textSize = 20f
        } else if (num >= 128) {
            textView.textSize = 25f
        } else if (num >= 16) {
            textView.textSize = 35f
        } else {
            textView.textSize = 40f
        }
    }

    private fun changeColor(num: Int) {
        if (num >= 8) {
            textView.setTextColor(resources.getColor(R.color.textColorCommon))
        } else {
            textView.setTextColor(resources.getColor(textColorIdMap[num]!!))
        }

        if (num >= 2048) {
            textView.setBackgroundColor(resources.getColor(R.color.backgroundColorBiggerThan2048))
        } else {
            textView.setBackgroundColor(resources.getColor(backgroundColorIdMap[num]!!))
        }

    }

}