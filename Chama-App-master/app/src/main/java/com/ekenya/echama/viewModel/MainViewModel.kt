package com.ekenya.echama.viewModel

import androidx.lifecycle.*
import com.ekenya.echama.model.Country
import com.ekenya.echama.model.User
import com.ekenya.echama.util.DataUtil
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import com.ekenya.echama.repository.UserAuthRepository as RepositoryUserAuthRepository

class MainViewModel:ViewModel() {
    private val parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    private val scope = CoroutineScope(coroutineContext)



    var  currentUser:User = User("")
    var  userLD:LiveData<List<User>>
    val userRepository : RepositoryUserAuthRepository =  RepositoryUserAuthRepository(viewModelScope )
    val myApiResponseLD =  userRepository.myApiResponse
   // val myApiResponse =   RepoImpl(viewModelScope).myApiResponse

    init {
        userLD = userRepository.userLD
    }

    /**
     * method to validate user otp
     * @param json with otp details
     * @response string response wrapper
     */
    fun validateOtp(json: HashMap<String,Any>){
         userRepository.verifyOtp(json)

//        when(results){
//            is ResultWrapper.NetworkError -> validateOtpMediatorLiveData.value = ViewModelWrapper.error("Kidly check you network and try again")
//            is ResultWrapper.GenericError -> validateOtpMediatorLiveData.value = ViewModelWrapper.error("Oops! error occurred")
//            is ResultWrapper.Success -> returnData(validateOtpMediatorLiveData,results.value)
//        }
//        return validateOtpMediatorLiveData

    }
    /**
     * Registers member
     * @return MediatorLiveData<ResponseWrapper>
     */
    fun registerMember(registerJson: HashMap<String,String>){
        // ViewModelWrapper.error("")
        //ViewModelWrapper.response("")
         userRepository.registerMember(registerJson)

//        when(results){
//            is ResultWrapper.NetworkError -> registerMemberMediatorData.value=ViewModelWrapper.error("Network Error occurred")
//            is ResultWrapper.GenericError -> registerMemberMediatorData.value=ViewModelWrapper.error("Generic error")
//            is ResultWrapper.Success -> returnData(registerMemberMediatorData,results.value)//registerMemberMediatorData.value=ViewModelWrapper.response(results.value.listData2().toString())
//        }
//
//        return registerMemberMediatorData
    }
    /**
     * get Nationaliries
     * @return MediatorLiveData<ResponseWrapper>
     */
     fun getCountries():LiveData<List<Country>> {
        Timber.v("getCountries()")
        if(userRepository.countriesLD.value.isNullOrEmpty()){
            userRepository.getCountries()
        }
//      userRepository.getCountries2()
        return userRepository.countriesLD
    }
    /**
     * user login
     * params usser json
     */
     fun userlogin1(hData: HashMap<String, String>){
        currentUser.pass= DataUtil.getHashedPin256(hData["password"].toString())
        hData["password"] =  currentUser.pass!!
        userRepository.getUser(hData)
    }
//    suspend fun userlogin(json: JSONObject){
       // json.put("username","wycliffmuriithi@gmail.com")
       // json.put("password","password")
//         RepoImpl(viewModelScope).userLogin(json.getString("username"),json.getString("password"),"password")


      //  when (results){
//            is ResultWrapper.NetworkError -> userLoginMediatorLiveData.value=ViewModelWrapper.error("Error")
//            is ResultWrapper.GenericError -> userLoginMediatorLiveData.value=ViewModelWrapper.error("${results.code} Error occurred try agin later")
//            is ResultWrapper.Success -> returnLoginData(userLoginMediatorLiveData,results.value)
    //    }
      //  return results

       // return userLoginMediatorLiveData
//    }

    /**
     * Change ppassword mediator data
     * @param JsonObjject
     */
     fun changePassword(json:HashMap<String,String>){
        json["password"] = DataUtil.getHashedPin256(json["password"].toString())!!
        currentUser.pass = currentUser.pass

        userRepository.updatePassword(json)

    }

    /**
     * get group type
     * params
     * @return MediatorLiveData<ResponseWrapper>
     */

    suspend fun getGroupTypes(){
        userRepository.getGroupType()

//        when(results){
//            is ResultWrapper.NetworkError -> groupTypesLiveData.value=ViewModelWrapper.error("Error occurred")
//            is ResultWrapper.GenericError -> groupTypesLiveData.value=ViewModelWrapper.error("Error occurred")
//            is ResultWrapper.Success -> returnData(groupTypesLiveData,results.value)
//        }
//        return groupTypesLiveData
    }



    suspend fun getRegionList(){
         userRepository.getConfigRegionList()
       // return results
//        when(results){
//            is ResultWrapper.NetworkError -> regionsMediatorData.value=ViewModelWrapper.error("Error")
//            is ResultWrapper.GenericError -> regionsMediatorData.value=ViewModelWrapper.error(results.toString())
//            is ResultWrapper.Success -> regionsMediatorData.value=ViewModelWrapper.response(results.value.listData2().toString())
//        }
//        return regionsMediatorData

    }

    fun getGenderList():List<String>{
        val genderList = ArrayList<String>()
        genderList.add("--Select Gender--")
        genderList.add("Female")
        genderList.add("Male")
        genderList.add("I would not wish to say")
        return genderList
    }

    fun logout() {
        currentUser = User("")
        userRepository.logOut()
    }

    fun resetPassword() {
        val registerJson = HashMap<String, String>()
        registerJson["account"] =  currentUser.phonenumber.toString()
        registerJson["identification"] = currentUser.identification.toString()

        userRepository.resetPassword(registerJson)
    }

    fun getUserDetails() {
        userRepository.getUserDetails()

    }

    fun updateUserDetails(userData:HashMap<String, String>) {
        userRepository.updateUserDetails(userData)

    }

    fun getUserTransactions(page:Int, size:Int) {
        userRepository.getUserTransactions(page,size)
    }

    fun deleteUser() {
        userRepository.deleteUser(currentUser.phonenumber)
    }

    fun userWithdraw(withdrawData: java.util.HashMap<String, Any>) {
        userRepository.userWithdraw(withdrawData)
    }

    fun makePayment(jsonGroupDetails: HashMap<String, Any>) {
        userRepository.makePayment(jsonGroupDetails)
    }

    fun getUserFullStatement(jsonGroupDetails: HashMap<String, Any>) {
        userRepository.getUserFullStatement(jsonGroupDetails)
    }

    fun getMemberPermission() {
        val memberDetails = HashMap<String,Any>()
        memberDetails["groupid"] = DataUtil.selectedGrpId
        memberDetails["phonenumber"] =  currentUser.phonenumber
        userRepository.getMemberPermission(memberDetails)
    }

    /**
     * function to return consumed data from json response
     * @return response wrapper class
     */
//    private fun returnData(mediatorData: ResponseWrapper, value: ResponseWrapper) {
//        Log.e("anything wrong","This is the error: " + value.toString())
//        Log.e("anything wrong 1","This is the error: " + value.status)
////        Log.e("anything wrong 2","This is the error: " + value.data.toString())
//        Log.e("anything wrong 3","This is the error: " + value.status)
//        if (value.status == "success"){
//            mediatorData.value = ViewModelWrapper.response(value.listData2().toString())
//        }
//        else if (value.status == "failed"){
//            mediatorData.value = ViewModelWrapper.error(value.getApiResponse())
//        }
//        else{
//            mediatorData.value = ViewModelWrapper.error(value.status)
//        }
//
//    }

//    private fun returnLoginData(mediatorData: ResponseWrapper, value: LoginWrapper) {
//        Log.e("anything wrong","This is the error: " + value.toString())
//        Log.e("anything wrong 1","This is the error: " + value.status)
////        Log.e("anything wrong 2","This is the error: " + value.data.toString())
//        Log.e("anything wrong 3","This is the error: " + value.status)
//
//        if (value.status == "success"){
//            mediatorData.value = ViewModelWrapper.response(value.listData2().toString())
//        }
//        else if (value.status == "failed"){
//            mediatorData.value = ViewModelWrapper.error(value.getApiResponse())
//        }
//        else{
//            mediatorData.value = ViewModelWrapper.error(value.status)
//        }
//    }


}