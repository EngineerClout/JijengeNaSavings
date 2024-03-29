package com.ekenya.echama.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ekenya.echama.dao.ContributionDao
import com.ekenya.echama.dao.ContributionTypeDao
import com.ekenya.echama.dao.GroupDao
import com.ekenya.echama.dao.ScheduleTypeDao
import com.ekenya.echama.dao.TransactionDao
import com.ekenya.echama.inc.AppInfo
import com.ekenya.echama.inc.ChamaRD
import com.ekenya.echama.model.AccountType
import com.ekenya.echama.model.Contribution
import com.ekenya.echama.model.ContributionType
import com.ekenya.echama.model.Group
import com.ekenya.echama.model.GroupAccount
import com.ekenya.echama.model.GroupInvite
import com.ekenya.echama.model.GroupPenalty
import com.ekenya.echama.model.Loan
import com.ekenya.echama.model.LoanApproved
import com.ekenya.echama.model.LoanPayment
import com.ekenya.echama.model.LoanProduct
import com.ekenya.echama.model.Member
import com.ekenya.echama.model.Payment
import com.ekenya.echama.model.Permission
import com.ekenya.echama.model.ScheduleType
import com.ekenya.echama.model.Transaction
import com.ekenya.echama.model.Withdraw
import com.ekenya.echama.network.RestClient.apiService
import com.ekenya.echama.util.DataUtil
import com.ekenya.echama.util.toJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber

class GroupRepository(val scope: CoroutineScope) {
    var groupDao: GroupDao = ChamaRD.getDatabase(AppInfo.appC, scope).groupDao()
    var transactionDao: TransactionDao =
        ChamaRD.getDatabase(AppInfo.appC, scope).recentActivitiesDao()
    var contributionDao: ContributionDao =
        ChamaRD.getDatabase(AppInfo.appC, scope).contributionDao()
    var scheduleTypeDao: ScheduleTypeDao =
        ChamaRD.getDatabase(AppInfo.appC, scope).scheduleTypeDao()
    var contributionTypeDao: ContributionTypeDao =
        ChamaRD.getDatabase(AppInfo.appC, scope).contributionTypeDao()

    var groupLD: LiveData<List<Group>>
    var groupInvitesLD: LiveData<List<GroupInvite>>
    var myApiResponse: MutableLiveData<MyApiResponse> = MutableLiveData<MyApiResponse>()
    var myError = MyError()

    init {
        groupLD = groupDao.getGroups()
        groupInvitesLD = groupDao.getGroupsInvite()
    }

    fun createGroup(json: HashMap<String, Any>) {
        scope.launch {
            try {

                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                //   var res = "{\"status\":\"success\",\"message\":\"group created successfully\",\"data\":[],\"timestamp\":\"Tue Mar 24 11:38:24 EAT 2020\",\"metadata\":{\"groupid\":19}}"

                //  var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()

                // var apisRes =  ad.fromJson(res)!!

                val apisRes =
                    apiService.createGroup(DataUtil.getHashToken(), json.toJson().toRequestBody())

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("success")!!) {

                    val ad1: JsonAdapter<Map<String, Any>> =
                        AppInfo.moshi.adapter<Map<String, Any>>(Map::class.java).lenient()
                    val apisRes1: Map<String, Any> = ad1.fromJson(apisRes.metadata.toString())!!
                    val id = apisRes1["groupid"]?.toString()?.toDouble()?.toInt()
                    val group = Group(id!!)
                    group.groupname = json["groupname"].toString()
                    group.description = json["description"].toString()
                    group.location = json["location"].toString()
                    groupDao.insert(group)

                    var message = ""
                    apisRes.message?.let { message = it }

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "createGroupRequest",
                            message,
                            group
                        )
                    )

                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "createGroupRequest",
                            myError.getFormattedError()
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
                        "createGroupRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "createGroupRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }


    fun getGrpContributions(json: HashMap<String, Any>): LiveData<List<Contribution>> {

        scope.launch {
            try {
                val apisRes = apiService.getGrpContributions(
                    DataUtil.getHashToken(),
                    json["groupid"] as Int,
                    json["page"] as Int,
                    json["size"] as Int
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpContributionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(
                        MutableList::class.java,
                        Contribution::class.java
                    )
                    val adapterRegion: JsonAdapter<List<Contribution>> = AppInfo.moshi.adapter(type)
                    var contributionList = adapterRegion.fromJsonValue(apisRes.data)!!

                    for (contribution in contributionList) {
                        contribution.groupid = json["groupid"] as Int
                        contributionDao.insert(contribution)
                    }
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getAllContributionRequest",
                            myError.getFormattedError(),
                            contributionList
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
                        "getAllContributionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getAllContributionRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return contributionDao.getGrpContribution(json["groupid"] as Int)
    }

    fun getContributionTypes(): LiveData<List<ContributionType>> {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.getContributionType(DataUtil.getHashToken())

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getContributionTypesRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(
                        MutableList::class.java,
                        ContributionType::class.java
                    )
                    val adapterRegion: JsonAdapter<List<ContributionType>> =
                        AppInfo.moshi.adapter(type)
                    var cTypeList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("cTypeList ${cTypeList.size} ")
                    for (group in cTypeList) {
                        contributionTypeDao.insert(group)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getContributionTypesRequest",
                            myError.getFormattedError(),
                            cTypeList
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
                        "getContributionTypesRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getContributionTypesRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return contributionTypeDao.getAllContributionType()
    }

    fun getContributionSchedules(): LiveData<List<ScheduleType>> {

        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.getContributionSchedule(DataUtil.getHashToken())

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getContributionSchedulesRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(
                        MutableList::class.java,
                        ScheduleType::class.java
                    )
                    val adapterRegion: JsonAdapter<List<ScheduleType>> = AppInfo.moshi.adapter(type)
                    var scheduleTypeList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("scheduleTypeList ${scheduleTypeList.size} ")
                    for (scheduleType in scheduleTypeList) {
                        Timber.v("scheduleType ${scheduleType.name} ")
                        scheduleTypeDao.insert(scheduleType)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getContributionSchedulesRequest",
                            myError.getFormattedError(),
                            scheduleTypeList
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
                        "getContributionSchedulesRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getContributionSchedulesRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return scheduleTypeDao.getAllScheduleType()
    }

    fun createContribution(json: HashMap<String, Any>) {
        //Timber.v(json.toString())
        //Timber.v(json.toJson())
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.createGrpContribution(
                    DataUtil.getHashToken(),
                    json.toJson().toRequestBody()
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "createContributionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Group::class.java)
                    val adapterRegion: JsonAdapter<List<Group>> = AppInfo.moshi.adapter(type)
                    var grpList = adapterRegion.fromJsonValue(apisRes.data)!!

                    for (group in grpList) {
                        groupDao.insert(group)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "createContributionRequest",
                            apisRes.message.toString(),
                            apisRes.data
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
                        "createContributionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "createContributionRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }

    fun approveDeclineGrpWithdrawal(jData: HashMap<String, Any>) {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.approveDeclineGrpWithdrawal(
                    DataUtil.getHashToken(),
                    jData["filter"].toString(),
                    jData["filterid"].toString()
                )
                //{"status":"success","message":"invite accepted","data":[],"timestamp":"Tue Mar 31 15:54:23 EAT 2020","metadata":null}
                //{"status":"failed","message":"invite does not exist","data":[],"timestamp":"Tue Mar 31 16:02:59 EAT 2020","metadata":null}
                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "approveDeclineGrpWithdrawalRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "approveDeclineGrpWithdrawalRequest",
                            apisRes.message.toString()
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
                        "approveDeclineGrpWithdrawalRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "approveDeclineGrpWithdrawalRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }
    fun approveDeclineGrpLoanPaymentRequest(jData: HashMap<String, Any>) {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.approveDeclineGrpLoanPayment(
                    DataUtil.getHashToken(),
                    jData["action"].toString(),
                    jData["loanpaymentrecord"].toString().toInt()
                )
                //{"status":"success","message":"invite accepted","data":[],"timestamp":"Tue Mar 31 15:54:23 EAT 2020","metadata":null}
                //{"status":"failed","message":"invite does not exist","data":[],"timestamp":"Tue Mar 31 16:02:59 EAT 2020","metadata":null}
                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "approveDeclineGrpLoanPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "approveDeclineGrpLoanPaymentRequest",
                            apisRes.message.toString()
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
                        "approveDeclineGrpLoanPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "approveDeclineGrpLoanPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }
    fun approveDeclineGrpPaymentRequest(jData: HashMap<String, Any>) {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.approveDeclineGrpPayment(
                    DataUtil.getHashToken(),
                    jData["filter"].toString(),
                    jData["filterid"].toString()
                )
                //{"status":"success","message":"invite accepted","data":[],"timestamp":"Tue Mar 31 15:54:23 EAT 2020","metadata":null}
                //{"status":"failed","message":"invite does not exist","data":[],"timestamp":"Tue Mar 31 16:02:59 EAT 2020","metadata":null}
                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "approveDeclineGrpPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "approveDeclineGrpPaymentRequest",
                            apisRes.message.toString()
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
                        "approveDeclineGrpPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "approveDeclineGrpPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }
    fun acceptDeclineGrpInvite(jData: HashMap<String, Any>) {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val apisRes = apiService.acceptDeclineGrpInvite(
                    DataUtil.getHashToken(),
                    jData["action"].toString(),
                    jData["inviteid"] as Int
                )
                //{"status":"success","message":"invite accepted","data":[],"timestamp":"Tue Mar 31 15:54:23 EAT 2020","metadata":null}
                //{"status":"failed","message":"invite does not exist","data":[],"timestamp":"Tue Mar 31 16:02:59 EAT 2020","metadata":null}
                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "acceptDeclineGrpInviteRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    groupDao.deleteInvite(jData["inviteid"] as Int)
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "acceptDeclineGrpInviteRequest",
                            myError.getFormattedError(),
                            apisRes.data
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
                        "acceptDeclineGrpInviteRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "acceptDeclineGrpInviteRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }


    fun getAllMembers(json: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getAllMembers(
                    DataUtil.getHashToken(),
                    json["groupid"] as Int,
                    json["page"] as Int,
                    json["size"] as Int
                )


                Timber.v(apiRes.status)
                if (apiRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apiRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getAllMembersRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Group::class.java)
                    val adapterRegion: JsonAdapter<List<Group>> = AppInfo.moshi.adapter(type)
                    var grpList = adapterRegion.fromJsonValue(apiRes.data)!!

                    for (group in grpList) {
                        groupDao.insert(group)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getAllMembersRequest",
                            myError.getFormattedError(),
                            apiRes.data
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
                        "getAllMembersRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getAllMembersRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }

    }

    fun getAllGroups(page: Int, size: Int) {
        scope.launch {
            try {
                //val changePinResponse = apiService.getGrpInvites(DataUtil.getHashToken(), gData["page"] as Int, gData["size"] as Int)
                val getAllGroupsResponse =
                    apiService.getAllGroups(DataUtil.getHashToken(), page, size)



                Timber.v(getAllGroupsResponse.status)
                if (getAllGroupsResponse.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = getAllGroupsResponse.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getAllGroupsRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Group::class.java)
                    val adapterRegion: JsonAdapter<List<Group>> = AppInfo.moshi.adapter(type)
                    var grpList = adapterRegion.fromJsonValue(getAllGroupsResponse.data)!!

                    for (group in grpList) {
                        groupDao.insert(group)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getAllGroupsRequest",
                            myError.getFormattedError(),
                            getAllGroupsResponse.data
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
                        "getAllGroupsRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getAllGroupsRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }

    }

    fun getGrpInvites(gData: HashMap<String, Any>) {
        scope.launch {
            try {

                //  var res = "{\"status\":\"success\",\"message\":\"pending group invites\",\"data\":[{\"inviteid\":2,\"groupname\":\"AllinOne Investments\",\"createdon\":\"2020-03-31\"}],\"timestamp\":\"Tue Mar 31 15:05:53 EAT 2020\",\"metadata\":{\"numofrecords\":1}}"
                //  var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
                // var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getGrpInvites(
                    DataUtil.getHashToken(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )


                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpInvitesRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, GroupInvite::class.java)
                    val adapterRegion: JsonAdapter<List<GroupInvite>> = AppInfo.moshi.adapter(type)
                    var grpInviteList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("grpInviteList ${grpInviteList.size}")

                    for (group in grpInviteList) {
                        groupDao.insertInvite(group)
                    }
                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpInvitesRequest",
                            myError.getFormattedError(),
                            grpInviteList
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
                        "getGrpInvitesRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getGrpInvitesRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }


    }

    fun uploadGrpFiles(
        groupid: Int,
        fileToUpload: List<MultipartBody.Part>
    ){
        scope.launch {
            try {
                val apisRes = apiService.uploadFiles(DataUtil.getHashToken(), groupid, fileToUpload)
                if (apisRes.status?.toLowerCase()?.contains("success")!!) {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "uploadGrpFileRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "uploadGrpFileRequest",
                            myError.getFormattedError()
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
                        "uploadGrpFileRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "uploadGrpFileRequest",
                        myError.getFormattedError()
                    )
                )
            }

        }
    }
    fun uploadPaymentReceipt(
        contributionId: Int,
        fileToUpload: List<MultipartBody.Part>
    ) {
        scope.launch {
            try {
                val apisRes = apiService.uploadPaymentFiles(DataUtil.getHashToken(), contributionId, fileToUpload)
                if (apisRes.status?.toLowerCase()?.contains("success")!!) {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "uploadPaymentReceiptRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "uploadPaymentReceiptRequest",
                            myError.getFormattedError()
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
                        "uploadPaymentReceiptRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "uploadGrpFileRequest",
                        myError.getFormattedError()
                    )
                )
            }

        }
    }

    fun getContributionTransactions(gData: HashMap<String, Any>): LiveData<List<Transaction>> {
        scope.launch {
            try {
//                  var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                  var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                 var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getContributionTrx(
                    DataUtil.getHashToken(),
                    gData["contributionid"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getCTrxRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Transaction::class.java)
                    val adapterRegion: JsonAdapter<List<Transaction>> = AppInfo.moshi.adapter(type)
                    val grpCTrxList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("grpTrxList ${grpCTrxList.size}")

                    for (rActivities in grpCTrxList) {
                        rActivities.groupid = DataUtil.selectedGrpId
                        rActivities.contributionid = gData["contributionid"] as Int
                        transactionDao.insert(rActivities)
                    }

                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(MyApiResponse(200, "getCTrxRequest", "", grpCTrxList))
                }

            } catch (e: HttpException) {
                val jerror: String = e.response()?.errorBody()?.string()!!
                Timber.v(jerror)
                myError = MyError(jerror)
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getCTrxRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getCTrxRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return transactionDao.getAllContributionTrx(gData["contributionid"] as Int)
    }

    fun getGroupTransactions(gData: HashMap<String, Any>): LiveData<List<Transaction>> {
        scope.launch {
            try {
//                  var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                  var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                 var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getGroupTransactions(
                    DataUtil.getHashToken(),
                    gData["groupid"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpTrxRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Transaction::class.java)
                    val adapterRegion: JsonAdapter<List<Transaction>> = AppInfo.moshi.adapter(type)
                    var grpInviteList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("grpTrxList ${grpInviteList.size}")

                    for (rActivities in grpInviteList) {
                        rActivities.groupid = gData["groupid"] as Int
                        transactionDao.insert(rActivities)
                    }

                    myError.message = ""
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpTrxRequest",
                            apisRes.message.toString(),
                            grpInviteList
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
                        "getGrpTrxRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getGrpTrxRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return transactionDao.getAllGroupTrx(gData["groupid"] as Int)
    }

    fun getGroupDetails(gData: HashMap<String, Any>): LiveData<Group> {
        scope.launch {
            try {

//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getGroupDetails(
                    DataUtil.getHashToken(),
                    gData["groupid"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpDetailRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val adapterRegion: JsonAdapter<Group> = AppInfo.moshi.adapter(Group::class.java)
                    var grp = adapterRegion.fromJsonValue(apisRes.data)!!

                    groupDao.insert(grp)

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpDetailRequest",
                            apisRes.message.toString(),
                            grp
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
                        "getGrpDetailRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getGrpDetailRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return groupDao.getSingleGroups(gData["groupid"] as Int)
    }

    fun getAmountType() {
        scope.launch {
            try {

//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getAmountType(DataUtil.getHashToken())

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getAmountTypeRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val adapterRegion: JsonAdapter<Group> = AppInfo.moshi.adapter(Group::class.java)
                    var grp = adapterRegion.fromJsonValue(apisRes.data)!!

                    //  groupDao.insert(grp)

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getAmountTypeRequest",
                            apisRes.message.toString(),
                            grp
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
                        "getAmountTypeRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getAmountTypeRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }

    fun getAccountTypes(): LiveData<List<AccountType>>? {
        scope.launch {
            try {

//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getAccountTypes(DataUtil.getHashToken())

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getAccountTypesRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, AccountType::class.java)
                    val adapterRegion: JsonAdapter<List<AccountType>> = AppInfo.moshi.adapter(type)
                    var gATList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("grpTrxList ${gATList.size}")
                    for (gat in gATList) {
                        groupDao.insertAccountType(gat)
                    }

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getAccountTypesRequest",
                            apisRes.message.toString(),
                            gATList
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
                        "getAccountTypesRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getAccountTypesRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
        return groupDao.getAccountTypes()
    }

    fun addGroupAccount(gAcc: GroupAccount) {
        scope.launch {
            try {

//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.addGroupAccount(
                    DataUtil.getHashToken(),
                    gAcc.toJson().toRequestBody()
                )
                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "addGroupAccountRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "addGroupAccountRequest",
                            apisRes.message.toString()
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
                        "addGroupAccountRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "addGroupAccountRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }

    }

    fun getGroupMembers(gData: HashMap<String, Any>): LiveData<List<Member>>? {
        scope.launch {
            try {

//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.getGroupMembers(
                    DataUtil.getHashToken(),
                    gData["groupid"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGroupMembersRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, Member::class.java)
                    val adapterRegion: JsonAdapter<List<Member>> = AppInfo.moshi.adapter(type)
                    var gATList = adapterRegion.fromJsonValue(apisRes.data)!!
                    Timber.v("grpTrxList ${gATList.size}")
                    for (gat in gATList) {
                        groupDao.insertMember(gat)
                    }

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGroupMembersRequest",
                            apisRes.message.toString(),
                            gATList
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
                        "getGroupMembersRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "getGroupMembersRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }


        return groupDao.getGroupMembers(gData["groupid"] as Int)
    }

    fun addGroupMembers(gData: HashMap<String, Any>) {

        scope.launch {
            try {
//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                val apisRes = apiService.addGroupMembers(
                    DataUtil.getHashToken(), gData.toJson().toRequestBody()
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "addGroupMembersRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "addGroupMembersRequest",
                            apisRes.message.toString()

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
                        "addGroupMembersRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "addGroupMembersRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }

    }

    fun exitGroup(gData: HashMap<String, Any>) {
        scope.launch {
            try {
//                var res = "{\"status\":\"success\",\"message\":\"contribution transactions by group\",\"data\":[],\"timestamp\":\"Tue Mar 31 22:04:30 EAT 2020\",\"metadata\":{\"numofrecords\":0}}"
//                var ad = AppInfo.moshi.adapter(ResponseWrapper::class.java).lenient()
//                var apisRes =  ad.fromJson(res)!!
                Timber.v(gData.toJson())
                val apisRes = apiService.exitGroup(
                    DataUtil.getHashToken(), gData.toJson().toRequestBody()
                )

                Timber.v(apisRes.status)
                if (apisRes.status?.toLowerCase()?.contains("fail")!!) {
                    myError.message = apisRes.message.toString()
                    myError.error = ""
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "exitGroupRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    groupDao.removeMember(
                        gData["memberphonenumber"].toString(),
                        gData["groupid"] as Int
                    )
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "exitGroupRequest",
                            apisRes.message.toString()

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
                        "exitGroupRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(
                        400,
                        "exitGroupRequest",
                        myError.getFormattedError()
                    )
                )
            }
        }
    }

    fun contributionWithdrawRequest(withdrawData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.contributionWithdraw(
                    DataUtil.getHashToken(),
                    withdrawData.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "contributionWithdrawRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "contributionWithdrawRequest",
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
                        "contributionWithdrawRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "contributionWithdrawRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getGroupAccounts(): LiveData<List<GroupAccount>>? {
        scope.launch {
            try {
                val apiRes = apiService.getGroupAccounts(
                    DataUtil.getHashToken(),
                    DataUtil.selectedGrpId.toString()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGroupAccountsRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(
                        MutableList::class.java,
                        GroupAccount::class.java
                    )
                    val adapterRegion: JsonAdapter<List<GroupAccount>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("grpAccList ${gAccList.size}")
                    groupDao.deleteGAccount()
                    for (gat in gAccList) {
                        Timber.v("grp json ${gat.accountdetails?.toJson()}")
                        gat.accountdetailsJson = gat.accountdetails?.toJson()
                        groupDao.insertGAccount(gat)
                    }
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGroupAccountsRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGroupAccountsRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGroupAccountsRequest", myError.getFormattedError())
                )
            }
        }
        return groupDao.getGAccount(DataUtil.selectedGrpId)
    }

    fun getCPenalties(gData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getCPenalties(
                    DataUtil.getHashToken(),
                    gData["contributionId"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getCPenaltiesRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(
                        MutableList::class.java,
                        GroupPenalty::class.java
                    )
                    val adapterRegion: JsonAdapter<List<GroupPenalty>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("grppenaltyList ${gAccList.size}")

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getCPenaltiesRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getCPenaltiesRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getCPenaltiesRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun deactivateContribution(cData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.deactivateContribution(
                    DataUtil.getHashToken(),
                    cData.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse cdeactivate data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "deactivateContributionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "deactivateContributionRequest",
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
                        "deactivateContributionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "deactivateContributionRequest", myError.getFormattedError())
                )
            }
        }
    }

    fun getLoanProducts(gData: HashMap<String, Any>): LiveData<List<LoanProduct>>? {
        scope.launch {
            try {
                val apiRes = apiService.getLoanProducts(
                    DataUtil.getHashToken(),
                    gData["groupid"] as Int,
                    gData["page"] as Int,
                    gData["size"] as Int
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getLoanProductsRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type =
                        Types.newParameterizedType(MutableList::class.java, LoanProduct::class.java)
                    val adapterRegion: JsonAdapter<List<LoanProduct>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("loanProductList ${gAccList.size}")

                    for (gat in gAccList) {
                        gat.groupid = gData["groupid"] as Int
                        groupDao.insertLoanProduct(gat)
                    }
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getLoanProductsRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getLoanProductsRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getLoanProductsRequest", myError.getFormattedError())
                )
            }
        }

        return groupDao.getLoanProduct(gData["groupid"] as Int)
    }

    fun getGrpPendingLoanRequest(gData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getGrpPendingLoanRequest(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpPendingLoanRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, Loan::class.java)
                    val adapterRegion: JsonAdapter<List<Loan>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("pendingLoanList ${gAccList.size}")

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpPendingLoanRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpPendingLoanRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpPendingLoanRequest", myError.getFormattedError())
                )
            }

        }

    }
    fun getGrpLoanPaymentRequest(gData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getGrpLoanPaymentRequest(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpLoanPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, Payment::class.java)
                    val adapterRegion: JsonAdapter<List<Payment>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("getGrpLoanPaymentRequest ${gAccList.size}")
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpLoanPaymentRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpLoanPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpLoanPaymentRequest", myError.getFormattedError())
                )
            }
        }

    }
    fun getGrpPaymentRequest(gData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getGrpPaymentRequest(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString()
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, Payment::class.java)
                    val adapterRegion: JsonAdapter<List<Payment>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("getGrpPaymentRequestlist ${gAccList.size}")
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpPaymentRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpPaymentRequest", myError.getFormattedError())
                )
            }
        }

    }
    fun getGrpWithdrawalRequest(gData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.getGrpWithdrawalRequest(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpWithdrawalRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, Withdraw::class.java)
                    val adapterRegion: JsonAdapter<List<Withdraw>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("withdrawList ${gAccList.size}")
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpWithdrawalRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpWithdrawalRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpWithdrawalRequest", myError.getFormattedError())
                )
            }
        }

    }
    fun getLoanPayment(gData: java.util.HashMap<String, Any>) {
        scope.launch {

            try {
                val apiRes = apiService.getLoanPayment(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getLoanPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, LoanPayment::class.java)
                    val adapterRegion: JsonAdapter<List<LoanPayment>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("loanPaymentList ${gAccList.size}")

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getLoanPaymentRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getLoanPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getLoanPaymentRequest", myError.getFormattedError())
                )
            }

        }

    }
    fun updateMemberPermission(gData: HashMap<String, Any>) {
        scope.launch {

            try {
                val apiRes = apiService.updateMemberPermission(
                    DataUtil.getHashToken(),gData.toJson().toRequestBody()
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "updateMemberPermissionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "updateMemberPermissionRequest",
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
                        "updateMemberPermissionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "updateMemberPermissionRequest", myError.getFormattedError())
                )
            }
        }
    }
    fun getMemberPermission(gData: java.util.HashMap<String, Any>) {
        scope.launch {

            try {
                var apiRes = apiService.getMemberPermission(
                    DataUtil.getHashToken(),
                    gData["groupid"] as Int,
                    gData["phonenumber"].toString()
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
                            "getMemberPermissionRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {


                   var myPermission = Permission(apiRes.data!!)

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getMemberPermissionRequest",
                            apiRes.message.toString(),
                            myPermission
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
                        "getMemberPermissionRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getMemberPermissionRequest", myError.getFormattedError())
                )
            }
        }
    }
    fun getGrpBadLoans(gData: java.util.HashMap<String, Any>) {
        scope.launch {

            try {
                val apiRes = apiService.getGrpBadLoans(
                    DataUtil.getHashToken(),
                    gData["enddate"].toString(),
                    gData["startdate"].toString(),
                    gData["groupid"] as Int ,
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpBadLoanRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, LoanApproved::class.java)
                    val adapterRegion: JsonAdapter<List<LoanApproved>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("loansList ${gAccList.size}")
                    for (gat in gAccList) {
                        groupDao.insertLoan(gat)
                    }
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpBadLoanRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpBadLoanRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpBadLoanRequest", myError.getFormattedError())
                )
            }

        }

    }
    fun getGrpLoans(gData: java.util.HashMap<String, Any>): LiveData<List<LoanApproved>>? {
        scope.launch {
            try {
                val apiRes = apiService.getGrpLoans(
                    DataUtil.getHashToken(),
                    gData["filter"].toString(),
                    gData["filterid"].toString(),
                    gData["page"] as Int,
                    gData["size"] as Int
                )

                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "getGrpLoansRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {
                    val type = Types.newParameterizedType(MutableList::class.java, LoanApproved::class.java)
                    val adapterRegion: JsonAdapter<List<LoanApproved>> = AppInfo.moshi.adapter(type)
                    var gAccList = adapterRegion.fromJsonValue(apiRes.data)!!
                    Timber.v("loansList ${gAccList.size}")
                    for (gat in gAccList) {
                        groupDao.insertLoan(gat)
                    }
                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "getGrpLoansRequest",
                            apiRes.message.toString(),
                            gAccList
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
                        "getGrpLoansRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "getGrpLoansRequest", myError.getFormattedError())
                )
            }

        }
        if(gData["filter"].toString().contentEquals("group")){
            return groupDao.getGroupLoans(gData["filterid"] as Int)
        }
        return groupDao.getGroupLoans(gData["membername"].toString() )
    }

    fun recordLoanPayment(paymentData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.recordLoanPayment(
                    DataUtil.getHashToken(),
                    paymentData.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "recordLoanPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "recordLoanPaymentRequest",
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
                        "recordLoanPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "recordLoanPaymentRequest", myError.getFormattedError())
                )
            }

        }

    }
    fun recordPayment(paymentData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.recordcontributionpayment(
                    DataUtil.getHashToken(),
                    paymentData.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "recordPaymentRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "recordPaymentRequest",
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
                        "recordPaymentRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "recordPaymentRequest", myError.getFormattedError())
                )
            }

        }

    }

    fun applyLoan(jsonLoanApply: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.applyLoan(
                    DataUtil.getHashToken(),
                    jsonLoanApply.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "applyLoanRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "applyLoanRequest",
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
                        "applyLoanRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "applyLoanRequest", myError.getFormattedError())
                )
            }

        }
    }

    fun approveLoan(loanData: HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.approveLoan(
                    DataUtil.getHashToken(),
                    loanData.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "approveLoanRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "approveLoanRequest",
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
                        "approveLoanRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "approveLoanRequest", myError.getFormattedError())
                )
            }
        }

    }

    fun createLoanProduct(loanProductjson: java.util.HashMap<String, Any>) {
        scope.launch {
            try {
                val apiRes = apiService.createLoanProduct(
                    DataUtil.getHashToken(),
                    loanProductjson.toJson().toRequestBody()
                )
                Timber.v(" myApiResponse data %s", apiRes.data)
                if (apiRes.status.toString().contains("fail")) {
                    myError.message = apiRes.message.toString()
                    myApiResponse.postValue(
                        MyApiResponse(
                            400,
                            "createLoanProductRequest",
                            myError.getFormattedError()
                        )
                    )
                } else {

                    myApiResponse.postValue(
                        MyApiResponse(
                            200,
                            "createLoanProductRequest",
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
                        "createLoanProductRequest",
                        myError.getFormattedError()
                    )
                )
            } catch (e: Exception) {
                Timber.v(e.message)
                myError.message = e.message
                myApiResponse.postValue(
                    MyApiResponse(400, "createLoanProductRequest", myError.getFormattedError())
                )
            }
        }

    }
}


