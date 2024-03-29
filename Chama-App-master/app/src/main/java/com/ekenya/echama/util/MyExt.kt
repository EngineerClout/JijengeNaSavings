package com.ekenya.echama.util

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ekenya.echama.MainActivity
import com.ekenya.echama.R
import com.ekenya.echama.inc.AppInfo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun String.getHashedPin256(): String {
    //String password="4544545454trerrt8";
    var digest: MessageDigest? = null
    val hash: String
    try {
        digest = MessageDigest.getInstance("SHA-256")
        digest.update(this.toByteArray())
        hash = DataUtil.bytesToHexString(digest.digest())
        return hash
    } catch (e1: NoSuchAlgorithmException) {
    }
    return ""
}
fun Fragment.successAlert(title:String = "Success",message:String="",navigateup:Boolean = true){
    val builder = AlertDialog.Builder(context)
    //set title for alert dialog
    builder.setTitle(title)
    //set message for alert dialog
    builder.setMessage(message)
    builder.setIcon(android.R.drawable.ic_dialog_alert)
    //performing positive action
    builder.setPositiveButton("Ok"){dialogInterface, which ->
        if(navigateup){
            findNavController().navigateUp()
        }

        dialogInterface.dismiss()
    }
    // Create the AlertDialog
    val alertDialog: AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    alertDialog.show()

}
fun AppCompatActivity.showInDevoplopmentDialogue(message:String=""){
    val builder = AlertDialog.Builder(this)
    //set title for alert dialog
    builder.setTitle("In development")
    //set message for alert dialog
    builder.setMessage(message)
    builder.setIcon(android.R.drawable.ic_dialog_alert)
    //performing positive action
    builder.setPositiveButton("Okay"){dialogInterface, _ ->
        dialogInterface.dismiss()
    }
    // Create the AlertDialog
    val alertDialog: AlertDialog = builder.create()
    // Set other dialog properties
    alertDialog.setCancelable(false)
    alertDialog.show()
}
lateinit var alertDialog:AlertDialog
fun Fragment.expiredTokenDialogue(){
       if( ::alertDialog.isInitialized) {
           if (alertDialog.isShowing) {
               return
           }
       }
        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle("Expired Token")
        //set message for alert dialog
         builder.setMessage("Kindly login again for a new session")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Okay"){dialogInterface, which ->
            MainActivity.mainVM.logout()
            findNavController().navigate(R.id.nav_user_login)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Cancel"){dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
         alertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

}

fun HashMap<String, Any>.toJson(): String {
    val ad1 : JsonAdapter<HashMap<String, Any>> = AppInfo.moshi.adapter<HashMap<String,Any>>(Map::class.java).lenient()
    return ad1.toJson(this)

}
fun HashMap<String, String>.toJsonObj(): String {
    val ad1 : JsonAdapter<HashMap<String, String>> = AppInfo.moshi.adapter<HashMap<String,String>>(Map::class.java).lenient()
    return ad1.toJson(this)

}
fun List<Any>.toJsonObj(): String {
    val type = Types.newParameterizedType(List::class.java, Any::class.java)
    val adapterRegion: JsonAdapter<List<Any>> = AppInfo.moshi.adapter(type)
    Timber.v("adapterRegion.toJson(phoneAr)")
    Timber.v(adapterRegion.toJson(this))
    return adapterRegion.toJson(this)

}

