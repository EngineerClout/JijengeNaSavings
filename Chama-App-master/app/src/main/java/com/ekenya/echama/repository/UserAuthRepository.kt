package com.ekenya.echama.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ekenya.echama.dao.CountryDao
import com.ekenya.echama.dao.UserDao
import com.ekenya.echama.inc.AppInfo
import com.ekenya.echama.inc.ChamaRD
import com.ekenya.echama.model.Country
import com.ekenya.echama.model.Permission
import com.ekenya.echama.model.Transaction
import com.ekenya.echama.model.User
import com.ekenya.echama.network.RestClient.apiService
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.getHashedPin256
import com.ekenya.echama.util.toJson
import com.ekenya.echama.util.toJsonObj
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber

class UserAuthRepository(val scope: CoroutineScope) {
     var userDao:UserDao =ChamaRD.getDatabase(AppInfo.appC,scope).userDao()
     var countryDao: CountryDao =ChamaRD.getDatabase(AppInfo.appC,scope).countryDao()

     var  userLD:LiveData<List<User>>
     var  countriesLD:LiveData<List<Country>>
     var  myApiResponse:MutableLiveData<MyApiResponse> = MutableLiveData<MyApiResponse>()
    var   myError: MyError = MyError()

    init {
        scope.launch {
            //userDao.deleteAll()
        }
        userLD = userDao.getUser()
        countriesLD = countryDao.getCountries()
    }
    fun updatePassword(json: HashMap<String,String>) {
        scope.launch {
            try {
                val changePinResponse = apiService.updatePassword(json.toJsonObj().toRequestBody())

                if(changePinResponse.status?.toLowerCase()?.contains("fail")!!){
                    myError.message =  changePinResponse.message.toString()
                    myError.error =    ""
                    myApiResponse.postValue(MyApiResponse(400,"updatePassRequest",myError.getFormattedError()))
                }else{
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(MyApiResponse(200,"updatePassRequest",myError.getFormattedError()))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "updatePassRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(MyApiResponse(400, "updatePassRequest", myError.getFormattedError()))
            }


        }

    }

    fun verifyOtp(json: HashMap<String,Any>) {
        scope.launch {
            try {
                val otpResponse = apiService.verificationOtp(json.toJson().toRequestBody())
                if(otpResponse.status?.toLowerCase()?.contains("fail")!!){
                    myError.message =  otpResponse.message // errorDt.message.toString()
                    myError.error =    ""//errorDt.message.toString()
                    myApiResponse.postValue(MyApiResponse(400,"verifyOtpRequest",myError.getFormattedError()))
                }else{
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(MyApiResponse(200,"verifyOtpRequest",myError.getFormattedError()))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400,"verifyOtpRequest",myError.getFormattedError() ))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(MyApiResponse(400,"verifyOtpRequest", myError.getFormattedError() ))
            }


        }
    }
    fun registerMember(json:HashMap<String,String>) {
        scope.launch {
            try {
                Timber.v("registerMember ")

//              var  loginWrapper = RestClient.apiService.registerMember(json.toJson().toRequestBody())
                var  responseWrapper = apiService.registerMember(json.toJsonObj().toRequestBody())
                Timber.v(responseWrapper.status)
                Timber.v(responseWrapper.data.toString())
                //val user = User(loginWrapper.data)
                //userDao.insert(user)
                // AppInfo.moshi.adapter<User>(loginWrapper.data.toString())
                if(responseWrapper.status?.toLowerCase()?.contains("fail")!!){
                    myError.message =    responseWrapper.message.toString()
                    myError.error =    responseWrapper.message.toString()
                    myApiResponse.postValue(MyApiResponse(400,"registerUserRequest",myError.getFormattedError()))
                }else{
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(MyApiResponse(200,"registerUserRequest",myError.getFormattedError()))
                }

            }catch (e: HttpException){
                val jerror:String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400,"registerUserRequest",myError.getFormattedError()))
            }catch (e : Exception){
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(MyApiResponse(400,"registerUserRequest",myError.getFormattedError()))
            }
        }
    }
    fun getUser(reqData: HashMap<String, String>) {

       scope.launch {
           //Timber.v(loginWrapper.access_token)
           userDao.deleteAll()
           try {
               //Todo remove below code when in production
               if(reqData["username"].toString().contentEquals("254715702887")){
                   reqData["password"] = "testpassword"
               }
               //Todo remove above code when inproduction

               var  loginWrapper = apiService.getAuthUser(reqData)
               val user = User(reqData["username"].toString())
               user.access_token = loginWrapper.accessToken
               user.pass = reqData["password"].toString().getHashedPin256()
               DataUtil.pass =  user.pass.toString()
               userDao.insert(user)

               DataUtil.authToken = "Bearer "+loginWrapper.accessToken //Todo remove this to current user
               Timber.v("Token ")
               Timber.v(loginWrapper.accessToken)
               myError.error = ""
               myError.message = ""
               myApiResponse.postValue(MyApiResponse(200,"loginUserRequest",myError.getFormattedError(),user.pass))

           }catch (e: HttpException){
               val jerror:String = e.response()?.errorBody()?.string()!!
               Timber.v(jerror)
               myError = MyError(jerror)
               myApiResponse.postValue(MyApiResponse(400,"loginUserRequest",myError.getFormattedError()))
           }catch (e : Exception){
                 Timber.v(e)
                 myApiResponse.postValue(MyApiResponse(400,"loginUserRequest",myError.getFormattedError()))
           }
       }
   }
    fun getCountries() {
      //  var countryListLD = countryDao.getCountries()
        Timber.v("getCountries size %s",countriesLD.value?.size)
        // if(countryListLD.value?.size == 0){
        if(!countriesLD.value.isNullOrEmpty()) {
            return
        }

        scope.launch {
            try {
                    val countriesResponse = apiService.getNationalities()
                    Timber.v("getCountries myApiResponse %s", countriesResponse.data?.size)
                 if(countriesResponse.status.toString().contains("fail")){
                     myError.message = countriesResponse.message.toString()
                     myApiResponse.postValue(MyApiResponse(400,"getCountriesRequest",myError.getFormattedError()))

                 }else{

                     for (country in countriesResponse.data!!) {
                       //  Timber.v(country.countryName)
                         countryDao.insert(country)
                     }
                     myError.message = ""
                     myError.error = ""
                     myApiResponse.postValue(MyApiResponse(200,"getCountriesRequest",myError.getFormattedError(),countriesResponse.data))
                 }

            }catch (e: HttpException){
                val jerror:String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400,"getCountriesRequest",myError.getFormattedError()))
            }catch (e : Exception){
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(MyApiResponse(400,"getCountriesRequest",myError.getFormattedError()))
            }
            // }
        }

    }

    fun getConfigRegionList() {
        scope.launch {
            try {
                val apiRes =  apiService.getConfigRegion(HashMap(), "".toRequestBody())
                Timber.v("getCountries myApiResponse %s", apiRes.data)
                if(apiRes.status.toString().contains("fail")){
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400,"getConfigRegionRequest",myError.getFormattedError()))

                }else{

                    myApiResponse.postValue(MyApiResponse(200,"getConfigRegionRequest",myError.getFormattedError(),apiRes.data))
                }

            }catch (e: HttpException){
                val jerror:String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400,"getConfigRegionRequest",myError.getFormattedError()))
            }catch (e : Exception){
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(MyApiResponse(400,"getConfigRegionRequest",myError.getFormattedError()))
            }

        }
    }

    fun getGroupType() {

        scope.launch {
            try {
                val apiRes = apiService.getGroupType(HashMap(), "".toRequestBody())
                Timber.v("getCountries myApiResponse %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "getGroupTypeRequest", myError.getFormattedError()))

                } else {

                    myApiResponse.postValue(MyApiResponse(200, "getGroupTypeRequest", myError.getFormattedError(), apiRes.data))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "getGroupTypeRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGroupTypeRequest", myError.getFormattedError())
                )
            }
        }

    }

    fun logOut() {
        scope.launch {
            userDao.deleteAll()
        }
    }

    fun resetPassword(registerJson: HashMap<String, String>) {
        scope.launch {
            try {
                val apiRes = apiService.resetPass(registerJson["account"].toString(), registerJson["identification"].toString())
                Timber.v("resetPassRequest myApiResponse %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "resetPassRequest", myError.getFormattedError()))
                } else {
                    myApiResponse.postValue(MyApiResponse(200, "resetPassRequest", myError.getFormattedError(), apiRes.data))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "resetPassRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "resetPassRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getUserDetails() {
        Timber.v(" myApiResponse %s", DataUtil.getHashToken())
        scope.launch {
            try {

                val apiRes = apiService.getUserDetails(DataUtil.getHashToken())
                Timber.v(" myApiResponse %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "getUserDetailsRequest", myError.getFormattedError()))
                } else {

                    val adapterRegion: JsonAdapter<User> = AppInfo.moshi.adapter(User::class.java)
                    apiRes.data
                    var cUser =   adapterRegion.fromJsonValue(apiRes.data)!!
                    cUser.access_token = DataUtil.authToken
                    cUser.pass = DataUtil.pass
                    userDao.insert(cUser)


                    myApiResponse.postValue(MyApiResponse(200, "getUserDetailsRequest", myError.getFormattedError(), cUser))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "getUserDetailsRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getUserDetailsRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun updateUserDetails(userData: HashMap<String, String>) {
        scope.launch {
            try {
                val apiRes = apiService.updateUserDetails(DataUtil.getHashToken(),userData.toJsonObj().toRequestBody())
                Timber.v("updateUserDetails myApiResponse %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "updateUserDetailsRequest", myError.getFormattedError()))
                } else {
                    myApiResponse.postValue(MyApiResponse(200, "updateUserDetailsRequest",  apiRes.message.toString()))
                }

            } catch (e: HttpException) {

                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "updateUserDetailsRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "updateUserDetailsRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getUserTransactions(page:Int, size:Int) {
        scope.launch {
            try {
//                val gres = "{\"status\":\"success\",\"message\":\"transactions made by user\",\"data\":[{\"transactionid\":\"BA1584979255631\",\"narration\":\"member muriithi gicheru deposited 500.000000 for Machakos Flats\",\"debitaccount\":\"254715702887\",\"creditaccount\":\"4342543223\",\"creditaccountname\":\"All in One Investors\",\"amount\":500.0,\"contributionid\":3,\"contributionname\":\"Machakos Flats\",\"groupname\":\"AllinOne Investments\",\"membername\":\"muriithi gicheru\",\"capturedby\":\"254715702887\",\"transactiondate\":1584979256000},{\"transactionid\":\"BA1584979265438\",\"narration\":\"member muriithi gicheru deposited 500.000000 for Machakos Flats\",\"debitaccount\":\"254715702887\",\"creditaccount\":\"4342543223\",\"creditaccountname\":\"All in One Investors\",\"amount\":500.0,\"contributionid\":3,\"contributionname\":\"Machakos Flats\",\"groupname\":\"AllinOne Investments\",\"membername\":\"muriithi gicheru\",\"capturedby\":\"254715702887\",\"transactiondate\":1584979265000}],\"timestamp\":\"Fri Apr 10 09:31:26 EAT 2020\",\"metadata\":{\"numofrecords\":2}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apiRes =  ad.fromJson(gres)!!
                val apiRes = apiService.getUserTransactions(DataUtil.getHashToken(),page,size)
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "getUserTrxRequest", myError.getFormattedError()))
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, Transaction::class.java)
                    val adapterRegion: JsonAdapter<List<Transaction>> = AppInfo.moshi.adapter(type)
                    var rActivityList:List<Transaction> =   adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v(" rActivityList size %s", rActivityList.size)

                    myApiResponse.postValue(MyApiResponse(200, "getUserTrxRequest", myError.getFormattedError(), rActivityList))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "getUserTrxRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getUserTrxRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun deleteUser(phonenumber:String){
        scope.launch {
            try {
                //Todo waiting to delete account api
                val apiRes = apiService.deleteUser(DataUtil.getHashToken(),phonenumber)
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "deleteUserRequest", myError.getFormattedError()))
                } else {
                    logOut()
                    myApiResponse.postValue(MyApiResponse(200, "deleteUserRequest", myError.getFormattedError()))
                }

            } catch (e: HttpException) {

                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "deleteUserRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "deleteUserRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun userWithdraw(withdrawData: HashMap<String, Any>) {
        scope.launch {
            try {
                //Todo waiting for witdrawal payment
                val apiRes = apiService.userWithdraw(DataUtil.getHashToken(),withdrawData.toJson().toRequestBody())
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "userWithdrawRequest", myError.getFormattedError()))
                } else {
                    myApiResponse.postValue(MyApiResponse(200, "userWithdrawRequest", myError.getFormattedError()))
                }

            } catch (e: HttpException) {

                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "userWithdrawRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "userWithdrawRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun makePayment(jsonGroupDetails: HashMap<String, Any>) {
        scope.launch {
            try {
                //Todo waiting to delete account api
                val apiRes = apiService.makePayment(DataUtil.getHashToken(),jsonGroupDetails.toJson().toRequestBody())
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "makePaymentRequest", myError.getFormattedError()))
                } else {

                    myApiResponse.postValue(MyApiResponse(200, "makePaymentRequest", myError.getFormattedError()))
                }

            } catch (e: HttpException) {

                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "makePaymentRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "makePaymentRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getUserFullStatement(jsonGroupDetails: HashMap<String, Any>) {
        scope.launch {
            try {
                //Todo waiting to delete account api
                val apiRes = apiService.getUserFullStatement(DataUtil.getHashToken(),jsonGroupDetails.toJson().toRequestBody())
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(MyApiResponse(400, "getFullStatementRequest", myError.getFormattedError()))
                } else {

                    myApiResponse.postValue(MyApiResponse(200, "getFullStatementRequest", myError.getFormattedError()))
                }

            } catch (e: HttpException) {

                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(MyApiResponse(400, "getFullStatementRequest", myError.getFormattedError()))
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getFullStatementRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getMemberPermission(memberDetails: HashMap<String, Any>) {
        scope.launch {

            try {
                var apiRes = apiService.getMemberPermission(
                    DataUtil.getHashToken(),
                    memberDetails["groupid"] as Int,
                    memberDetails["phonenumber"].toString()
                )
//                var res = "{\"status\":\"success\",\"message\":\"user permissions per group\",\"data\":{\"loans\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"invites\":[\"cancreate\",\"canview\",\"candelete\"],\"members\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"dueloans\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"documents\":[\"cancreate\",\"canview\",\"candelete\"],\"loanpayment\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"loanproduct\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"contribution\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"groupaccount\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"groupdetails\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"contributionpayment\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"loanspendingapproval\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"contributionwithdrawal\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"],\"loanpaymentspendingapproval\":[\"cancreate\",\"canedit\",\"canview\",\"candelete\"]},\"timestamp\":\"Thu Apr 30 11:01:01 EAT 2020\",\"metadata\":null}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                apiRes =  ad.fromJson(res)!!

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getUserGrpPermissionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    DataUtil.userGrpPermission = Permission(apiRes.data!!)
                    //= myPermission

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getUserGrpPermissionRequest",
                            apiRes.message.toString()
                        )
                    )
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getUserGrpPermissionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getUserGrpPermissionRequest", myError.getFormattedError())
                )
            }
        }
    }

}
