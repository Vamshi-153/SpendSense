package com.example.spendsense

import android.content.Context
import java.io.*

object FileUtils {
    private const val DIR_NAME = "spendsense_data"
    private const val USER_FILE_SUFFIX = "_user_data.txt"

    // Save user data to text file
    fun saveUserData(
        context: Context,
        mobileNumber: String,
        name: String,
        email: String,
        profilePictureUri: String
    ): Boolean {
        try {
            val directory = File(context.filesDir, DIR_NAME)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, "$mobileNumber$USER_FILE_SUFFIX")
            FileWriter(file).use { writer ->
                writer.write("name=$name\n")
                writer.write("email=$email\n")
                writer.write("profilePictureUri=$profilePictureUri\n")
                writer.write("mobileNumber=$mobileNumber\n")
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Check if user exists
    fun doesUserExist(context: Context, mobileNumber: String): Boolean {
        val directory = File(context.filesDir, DIR_NAME)
        if (!directory.exists()) return false

        val file = File(directory, "$mobileNumber$USER_FILE_SUFFIX")
        return file.exists()
    }

    // Get user data
    fun getUserData(context: Context, mobileNumber: String): Map<String, String>? {
        try {
            val directory = File(context.filesDir, DIR_NAME)
            val file = File(directory, "$mobileNumber$USER_FILE_SUFFIX")

            if (!file.exists()) return null

            val userData = mutableMapOf<String, String>()

            BufferedReader(FileReader(file)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val parts = line?.split("=", limit = 2)
                    if (parts?.size == 2) {
                        userData[parts[0]] = parts[1]
                    }
                }
            }
            return userData
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Update user profile data
    fun updateUserProfile(
        context: Context,
        mobileNumber: String,
        name: String? = null,
        email: String? = null,
        profilePictureUri: String? = null
    ): Boolean {
        try {
            val userData = getUserData(context, mobileNumber)?.toMutableMap() ?: return false

            name?.let { userData["name"] = it }
            email?.let { userData["email"] = it }
            profilePictureUri?.let { userData["profilePictureUri"] = it }

            val directory = File(context.filesDir, DIR_NAME)
            val file = File(directory, "$mobileNumber$USER_FILE_SUFFIX")

            FileWriter(file).use { writer ->
                for ((key, value) in userData) {
                    writer.write("$key=$value\n")
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Delete user data
    fun deleteUser(context: Context, mobileNumber: String): Boolean {
        try {
            val directory = File(context.filesDir, DIR_NAME)
            val file = File(directory, "$mobileNumber$USER_FILE_SUFFIX")
            return if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}