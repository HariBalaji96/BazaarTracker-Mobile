package com.example.bazaartrackermobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val companyName: String,
    val role: String,
    val isActive: Boolean
)