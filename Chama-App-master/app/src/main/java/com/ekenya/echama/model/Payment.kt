package com.ekenya.echama.model

import com.ekenya.echama.util.DataUtil
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Payment (
    var paymentid:Int
){
     var creditaccountid:Int = 0
     var creditaccountname:String = ""
     var creditaccounttype:String = ""
     var debitaccount:String = ""
     var narration:String = ""
     var amount:Double = 0.00
     var contributionid:Int = 0
     var capturedby:String = ""

    fun getFAmount():String{
        return String.format(DataUtil.gcurrency+" %.2f",amount)
    }


}


