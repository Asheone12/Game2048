package com.muen.game2048.util


import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.muen.game2048.R

/**
 * @author ry
 * @date 2019-05-17
 */


class WindowUtils private constructor() {
    companion object {
        /**
         * 设置全屏且状态栏透明
         */
        @JvmStatic
        fun setScreenFull(window: Window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else { // 全屏显示，隐藏状态栏
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        /**
         * 设置decorView全屏，黑色状态栏文字
         */
        @JvmStatic
        fun setLightStatusBar(window: Window) {
            //亮色背景，黑色状态栏文字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.decorView.systemUiVisibility = flag
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        @JvmStatic
        fun setLightStatusBar(window: Window,color:Int) {
            //亮色背景，黑色状态栏文字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.decorView.systemUiVisibility = flag
                window.statusBarColor = color
            }
        }

        @JvmStatic
        fun setLightStatusBar2(window: Window,color:Int) {
            //亮色背景，黑色状态栏文字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.decorView.systemUiVisibility = flag
                window.statusBarColor = color
            }
        }

        /**
         * 设置decorView全屏，白色状态栏文字和图标
         */
        @JvmStatic
        fun setDarkStatusBar(window: Window) {
            //暗色背景，白色状态栏文字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.decorView.systemUiVisibility = flag
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        /**
         * 设置状态栏颜色
         */
        @JvmStatic
        fun setStatusBarColor(window: Window, @ColorInt color: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = color
            }
        }


        /**
         * 获取状态栏高度
         */
        @JvmStatic
        fun getStatusBarHeight(context: Context): Int {
            var height = 0
            val resourceId = context.applicationContext.resources
                .getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                height =
                    context.applicationContext.resources.getDimensionPixelSize(resourceId)
            }
            return height
        }

        /**
         * 给View设置状态栏高度的PaddingTop
         */
        @JvmStatic
        fun setStatusBarHeightPadding(
            context: Context,
            view: View
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val height = getStatusBarHeight(context)
                if (height > 0) {
                    view.setPadding(
                        view.paddingLeft,
                        height,
                        view.paddingRight,
                        view.paddingBottom
                    )
                }
            }
        }

        /**
         * 设置Toolbar和状态栏上的图标为深色。
         */
        @JvmStatic
        fun setToolbarAndStatusBarIconIntoDark(activity: AppCompatActivity, toolbar: Toolbar) {
            setLightStatusBar(activity.window)
            setToolbarIconColor(activity, toolbar, true)
        }

        /**
         * 设置Toolbar和状态栏上的图标颜色为浅色。
         */
        @JvmStatic
        fun setToolbarAndStatusBarIconIntoLight(activity: AppCompatActivity, toolbar: Toolbar) {
            setDarkStatusBar(activity.window)
            setToolbarIconColor(activity, toolbar, false)
        }


        @JvmStatic
        fun setToolbarIconColor(activity: AppCompatActivity, toolbar: Toolbar, isDark: Boolean) {
            val color = if (isDark) {
                ContextCompat.getColor(activity, R.color.black_text)
            } else {
                ContextCompat.getColor(activity, R.color.white_text)
            }
            //修改文字颜色
            toolbar.setTitleTextColor(color)
            //修改返回键颜色
            toolbar.navigationIcon?.setTint(color)
        }

        @JvmStatic
        fun setEditWithClearIcon(editText: EditText, clearView: View) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrEmpty()) {
                        clearView.visibility = View.GONE
                    } else {
                        clearView.visibility = View.VISIBLE
                    }
                }
            })

            editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editText.text.toString().isNotEmpty()) {
                    clearView.visibility = View.VISIBLE
                } else {
                    clearView.visibility = View.GONE
                }
            }

            clearView.setOnClickListener {
                editText.setText("")
            }
        }

        @JvmStatic
        fun setPasswordShowOrNot(isShow: Boolean, editText: EditText) {
            if (isShow) {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            editText.setSelection(editText.text.length)
        }
    }

}

