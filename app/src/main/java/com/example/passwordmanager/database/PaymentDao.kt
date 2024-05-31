package com.example.passwordmanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passwordmanager.data.model.PasswordData

@Dao
interface PasswordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAccount(passwordData: PasswordData)

    @Query("SELECT * FROM passwordData")
    fun getAllPasswordData(): List<PasswordData>

    @Query("UPDATE passwordData SET account_type = :accountType, username = :username, password = :password, logo = :logo, date = :date WHERE id = :id")
    suspend fun updatePasswordDataFields(
        id: Int, accountType: String, username: String, password: String, logo: Int, date: String
    )

    @Query("DELETE FROM passwordData WHERE id = :id")
    suspend fun deletePasswordDataById(id: Int)

}