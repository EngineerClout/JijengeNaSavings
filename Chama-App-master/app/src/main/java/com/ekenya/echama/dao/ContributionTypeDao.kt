package com.ekenya.echama.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ekenya.echama.model.ContributionType

@Dao
interface ContributionTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contributionType: ContributionType)

    @Query("DELETE FROM contribution_type")
    suspend fun deleteAll()
    //fetches all activities
    @Query("SELECT * FROM contribution_type")
    fun getAllContributionType(): LiveData<List<ContributionType>>


}