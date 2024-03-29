package com.ekenya.echama.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ekenya.echama.model.Contribution

@Dao
interface ContributionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Contribution)

    @Query("DELETE FROM contribution")
    suspend fun deleteAll()
    //fetches all activities
    @Query("SELECT * FROM contribution")
    fun getAllContribution(): LiveData<List<Contribution>>

    //fetches all user activities
     @Query("SELECT * FROM contribution WHERE groupid = :groupid ")
     fun getGrpContribution(groupid:Int): LiveData<List<Contribution>>


}