package com.ekenya.echama.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ekenya.echama.model.AccountType
import com.ekenya.echama.model.Group
import com.ekenya.echama.model.GroupAccount
import com.ekenya.echama.model.GroupInvite
import com.ekenya.echama.model.LoanApproved
import com.ekenya.echama.model.LoanProduct
import com.ekenya.echama.model.Member

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: Group)

    @Query("DELETE FROM mygroup")
    suspend fun deleteAll()

    @Query("SELECT * FROM mygroup")
     fun getGroups(): LiveData<List<Group>>
    @Query("SELECT * FROM mygroup where groupid=:groupid")
    fun getSingleGroups(groupid:Int): LiveData<Group>

    //GROUP INVITES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvite(groupInvite: GroupInvite)

    @Query("DELETE FROM groupinvite")
    suspend fun deleteAllInvites()

    @Query("DELETE FROM groupinvite WHERE id=:id")
    suspend fun deleteInvite(id:Int)

    @Query("SELECT * FROM groupinvite")
    fun getGroupsInvite(): LiveData<List<GroupInvite>>

    //ACCOUNT TYPES
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountType(accountType: AccountType)

    @Query("DELETE FROM account_type")
    suspend fun deleteAllAccountTypes()

    @Query("SELECT * FROM account_type")
    fun getAccountTypes(): LiveData<List<AccountType>>

    //GROUP ACCOUNT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGAccount(groupAccount: GroupAccount)

    @Query("DELETE FROM group_account")
    suspend fun deleteGAccount()

    @Query("SELECT * FROM group_account where groupid=:grpId ")
    fun getGAccount( grpId:Int): LiveData<List<GroupAccount>>

    //GROUP MEMBERS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Query("DELETE FROM members")
    suspend fun deleteMembers()

    @Query("SELECT * FROM members where groupid= :gid")
    fun getGroupMembers(gid:Int): LiveData<List<Member>>

    @Query("DELETE FROM members WHERE phoneNumber=:memberphone AND groupid=:gid ")
    suspend fun removeMember(memberphone:String,gid:Int)

    //LOAN PRODUCTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanProduct(loanProduct: LoanProduct)

    @Query("DELETE FROM loan_product")
    suspend fun deleteLoanProduct()

    @Query("SELECT * FROM loan_product where groupid= :gid")
    fun getLoanProduct(gid:Int): LiveData<List<LoanProduct>>

    //LOAN PRODUCTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanApproved)

    @Query("DELETE FROM loan")
    suspend fun deleteLoans()

    @Query("SELECT * FROM loan where groupid= :gid")
    fun getGroupLoans(gid:Int): LiveData<List<LoanApproved>>

    @Query("SELECT * FROM loan where memberName= :membername")
    fun getGroupLoans(membername:String): LiveData<List<LoanApproved>>


//    /**
//     * Updating only group
//
//     */
//    @Query("UPDATE mygroup SET na=:token WHERE firstname = :firstname")
//    fun updateGroup(token: String, firstname: String)

}