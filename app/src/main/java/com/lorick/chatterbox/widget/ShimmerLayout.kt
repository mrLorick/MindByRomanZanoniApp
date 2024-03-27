package com.lorick.chatterbox.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.facebook.shimmer.ShimmerFrameLayout
import com.lorick.chatterbox.R


class CustomWidget : ShimmerFrameLayout {

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    private fun initialize() {
        // Inflate your custom layout here
      try {
          val linerLayoutMainLayer = LinearLayout(context)

          val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
          val view = inflater.inflate(R.layout.shimmer_item_likes, this, true)

          // Access views by their IDs
          val parentView = findViewById<ConstraintLayout>(R.id.mainLayerCommentList)
          parentView?.setBackgroundColor(ContextCompat.getColor(context,R.color.shimmerColor))
          parentView.addView(view)
          // Check if textView is not null before using it
          linerLayoutMainLayer.addView(parentView)
          addView(linerLayoutMainLayer)

      }catch (_:Exception){}
        // You can perform other operations or setups with these views
    }
}