package com.priyanshumaurya8868.noteapp.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.priyanshumaurya8868.noteapp.R

fun ImageView.load(uri: String) {
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    Glide.with(this).load(uri).placeholder(R.drawable.image_loading_placeholder)
        .apply(requestOptions)
        .into(this)
}

fun ImageView.loadPfp(uri: String) {
    val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
    Glide.with(this).load(uri).placeholder(R.drawable.man).apply(requestOptions)
        .into(this)
}