package com.skybox.tv.data.catalog

class RoomMediaCatalogRepository : MediaCatalogRepository {
    override suspend fun search(query: String): List<Any> = emptyList()
    override suspend fun getAll(): List<Any> = emptyList()
}
