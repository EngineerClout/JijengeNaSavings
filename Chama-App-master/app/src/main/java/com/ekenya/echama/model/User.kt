package com.ekenya.echama.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ekenya.echama.inc.AppInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "user")
data class User (@PrimaryKey var phonenumber: String = ""){
    @ColumnInfo(name = "token") var access_token: String = ""
    var dateofbirth: String? = ""
    var email: String? = ""
    var firstname: String? = ""
    var gender: String? = ""
    var identification: String? = ""
    var othernames: String? = ""
  //  var phonenumber: String? = ""
    var nationality: String? = ""
    var pass: String? = ""
    var walletbalance: Double? = 0.00
    var currency: String = "Ksh"
    @Json(name = "active") var isactive: Boolean? = false
    @Ignore var otp: String? = ""


 fun getFWalletBalance():String{
     return String.format(" ${currency} %.2f",walletbalance)
 }
    fun fromJson(groupJson:String):User {
        val adapterRegion: JsonAdapter<User> = AppInfo.moshi.adapter(User::class.java)
        adapterRegion.fromJson(groupJson)?.let {
            return it
        }
        return User("")
    }

    fun toJson():String{
        val adapterRegion: JsonAdapter<User> = AppInfo.moshi.adapter(User::class.java)
        return adapterRegion.toJson(this)
    }


}