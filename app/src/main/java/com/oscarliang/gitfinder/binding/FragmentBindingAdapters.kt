package com.oscarliang.gitfinder.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.oscarliang.gitfinder.R

class FragmentBindingAdapters {

    @BindingAdapter(value = ["imageUrl"])
    fun bindImage(imageView: ImageView, url: String?) {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.ic_gitfinder)
            .error(R.drawable.ic_error)
            .into(imageView)
    }

}
