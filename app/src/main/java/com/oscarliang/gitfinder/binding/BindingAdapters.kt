package com.oscarliang.gitfinder.binding

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("showHide")
    fun showHide(view: View, show: Boolean) {
        view.isVisible = show
    }

    @JvmStatic
    @BindingAdapter("formatText")
    fun formatText(textView: TextView, count: Int) {
        val text = if (count >= 1000) {
            val format = count / 1000
            "${format}k"
        } else {
            count.toString()
        }
        textView.text = text
    }

    @JvmStatic
    @BindingAdapter("refreshHide")
    fun refreshHide(view: SwipeRefreshLayout, isRefreshing: Boolean) {
        if (view.isRefreshing) {
            view.isRefreshing = isRefreshing
        }
    }

}
