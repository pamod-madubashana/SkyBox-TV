package com.skybox.tv.data.catalog

interface MediaCatalogRepository {
    suspend fun search(query: String): List<Any>
    suspend fun getAll(): List<Any>
}
