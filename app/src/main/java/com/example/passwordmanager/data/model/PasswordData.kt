package com.example.passwordmanager.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "passwordData")
data class PasswordData(

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,

    @ColumnInfo(name = "account_type") val accountType: String,

    @ColumnInfo(name = "username") val username: String,

    @ColumnInfo(name = "password") val password: String,

    @ColumnInfo(name = "logo") val logo: Int,

    @ColumnInfo(name = "date") val date: String,

    ) {
    constructor(accountType: String, username: String, password: String, logo: Int, date: String) : this(
        0, accountType, username, password, logo, date
    )
}
