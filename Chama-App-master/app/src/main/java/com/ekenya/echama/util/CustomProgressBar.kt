package com.ekenya.echama.util

import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.ekenya.echama.R

class CustomProgressBar {
    companion object{
         var mydialog: Dialog? = null
        fun show(context: Context): Dialog {
            return show(context, null)
        }
        fun show(context: Context, title:CharSequence? = "Sending request"): Dialog {

            val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflator.inflate(R.layout.progress_bar, null)

            if (title != null) {

                view.findViewById<TextView>(R.id.cp_title).text = title

            }

            view.findViewById<ConstraintLayout>(R.id.cp_bg_view).setBackgroundColor(Color.parseColor("#60000000")) //Background Color

            view.findViewById<CardView>(R.id.cp_cardview).setCardBackgroundColor(Color.parseColor("#70000000")) //Box Color

            setColorFilter(view.findViewById<ProgressBar>(R.id.cp_pbar).indeterminateDrawable,

                ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null)) //Progress Bar Color

            view.findViewById<TextView>(R.id.cp_title).setTextColor(Color.WHITE) //Text Color

            mydialog = Dialog(context, R.style.CustomProgressBarTheme)

            mydialog!!.setContentView(view)
            mydialog!!.setCancelable(true)
            mydialog!!.show()

            return mydialog!!

        }
        fun hide(){
            try {

                if (mydialog != null && mydialog?.isShowing!!) {
                    mydialog?.dismiss()
                    //dialog.cancel()
                }
            }catch (e:Exception){

            }
        }

        fun setColorFilter(@NonNull drawable: Drawable, color:Int) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            } else {
                @Suppress("DEPRECATION")
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }

        }
    }






}