package com.ttop.app.apex.repository

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ttop.app.apex.model.Contributor
import com.ttop.app.apex.util.PreferenceUtil

interface LocalDataRepository {
    fun contributors(): List<Contributor>
}

class RealLocalDataRepository(
    private val context: Context
) : LocalDataRepository {

    override fun contributors(): List<Contributor> {
        val jsonString = if (PreferenceUtil.isInternetConnected) {
            context.assets.open("contributors.json")
                .bufferedReader().use { it.readText() }
        }else {
            context.assets.open("contributors_no_internet.json")
                .bufferedReader().use { it.readText() }
        }

        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()
        val listContributorType = object : TypeToken<List<Contributor>>() {}.type
        return gson.fromJson(jsonString, listContributorType)
    }
}