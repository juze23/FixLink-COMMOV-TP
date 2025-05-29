package com.example.fixlink.data.repository

import com.example.fixlink.data.entities.User
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserRepository {
    suspend fun signUp(email: String, password: String, phone: String): Result<User> {
        return try {
            //creates the user in supabase auth
            val response = SupabaseClient.supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            if (response == null) {
                throw Exception("Failed to create user in authentication")
            }

            //gets the user ID from auth response
            val userId = response.id

            //creates user record in the database
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            val user = User(
                id = userId,
                name = email.split("@")[0], //CHANGE AFTER CHANGING REGISTER UI
                email = email,
                phoneNumber = phone,
                typeId = "1",
                createdAt = currentTime,
                updatedAt = currentTime
            )

            //inserts user into the database
            SupabaseClient.supabase.postgrest["User"].insert(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /*
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val user = SupabaseClient.supabase.postgrest["User"]
                .select {
                    eq("user_id", userId)
                }
                .decodeSingle<User>()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<User> {
        return try {
            val updatedUser = user.copy(
                updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            )
            SupabaseClient.supabase.postgrest["User"]
                .update(updatedUser) {
                    eq("user_id", user.id)
                }
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            SupabaseClient.supabase.postgrest["User"]
                .delete {
                    eq("user_id", userId)
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }*/
}