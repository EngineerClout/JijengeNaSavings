package com.ekenya.echama.repository

import com.ekenya.echama.network.Webservice

interface GroupRepository2 {
    //suspend fun createGroup(json:JSONObject): ResponseWrapper
   // suspend fun getAllGroups(page: Int, size: Int):ResponseWrapper
   // suspend fun getAllMembers(json: JSONObject):ResponseWrapper
//    suspend fun getAllContribution(json: JSONObject):ResponseWrapper
//    suspend fun getContributionTypes():ResponseWrapper
//    suspend fun getContributionSchedules():ResponseWrapper
//    suspend fun createContribution(json: JSONObject):ResponseWrapper
//    //suspend fun getGrpInvites(json: JSONObject):ResponseWrapper
//    suspend fun acceptGrpInvite(json: JSONArray):ResponseWrapper
//    suspend fun declineGrpInvite(json: JSONObject):ResponseWrapper
}

class GroupRepoImpl(private val service: Webservice) :GroupRepository2 {
   // val mtype = MediaType.get("application/json; charset=utf-8")





}