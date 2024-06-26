package com.lorick.chatterbox.genrics

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

/**
 * You pass <M> data class type
 * and its received in <T> data
 * class type.
 * */

class GenericDiffUtil<T> : DiffUtil.ItemCallback<T>() {

    /**           
     *  Check old item and new item are same
     *  If they are both same
     *  */

    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return oldItem.toString() == newItem.toString()
    }
}
