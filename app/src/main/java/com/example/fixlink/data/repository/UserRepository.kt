package com.example.fixlink.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fixlink.data.entities.User
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import android.util.Log
import kotlinx.coroutines.withContext

class UserRepository {
    suspend fun signUp(
        email: String,
        password: String,
        phone: String,
        typeId: Int,
        firstname: String,
        lastname: String
    ): Result<User> {
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            val user = User(
                user_id = userId,
                firstname = firstname,
                lastname = lastname,
                email = email,
                phoneNumber = phone,
                typeId = typeId, // Use provided typeId (2 for technician, 3 for admin)
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

    suspend fun logIn(email: String, password: String): Result<User> {
        return try {
            // Authenticate user with Supabase
            val response = try {
                val authResponse = SupabaseClient.supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                authResponse
            } catch (e: Exception) {
                throw e
            }

            if (response == null) {
                throw Exception("Failed to authenticate user")
            }
            
            // Get the user ID from auth response
            val currentUser = try {
                val user = SupabaseClient.supabase.auth.currentUserOrNull()
                user
            } catch (e: Exception) {
                throw e
            }
            
            val userId = currentUser?.id ?: throw Exception("No authenticated user found")

            // Fetch user data from the database
            val user = try {
                val userData = SupabaseClient.supabase.postgrest["User"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeSingle<User>()
                userData
            } catch (e: Exception) {
                throw e
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val users = SupabaseClient.supabase.postgrest["User"]
                .select()
                .decodeList<User>()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
                ?: throw Exception("No authenticated user found")

            val user = SupabaseClient.supabase.postgrest["User"]
                .select {
                    filter {
                        eq("user_id", currentUser.id)
                    }
                }
                .decodeSingle<User>()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        firstname: String,
        lastname: String,
        email: String,
        phone: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current user to keep their creation date and type
            val currentUser = SupabaseClient.supabase.postgrest["User"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<User>()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())

            val user = User(
                user_id = userId,
                email = email,
                phoneNumber = phone,
                typeId = currentUser.typeId, // Keep original type
                firstname = firstname,
                lastname = lastname,
                createdAt = currentUser.createdAt, // Keep original creation date
                updatedAt = currentTime
            )

            SupabaseClient.supabase.postgrest["User"]
                .update(user) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user profile: ", e)
            Result.failure(e)
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            // First verify current password
            val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
                ?: throw Exception("No authenticated user found")

            // Update password in Supabase Auth
            SupabaseClient.supabase.auth.updateUser {
                password = newPassword
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            SupabaseClient.supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
            currentUser?.id
        } catch (e: Exception) {
            null
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