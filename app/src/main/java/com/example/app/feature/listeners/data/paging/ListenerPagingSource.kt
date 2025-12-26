package com.example.app.feature.listeners.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.app.feature.listeners.data.ListenerApi
import com.example.app.feature.listeners.domain.ListenerModel
import com.example.app.feature.listeners.data.toDomain
import retrofit2.HttpException
import java.io.IOException

class ListenerPagingSource(
    private val api: ListenerApi,
    private val pageSize: Int
) : PagingSource<Int, ListenerModel>() {

    /**
     * Page index starts from 1 (as per your backend contract)
     */
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, ListenerModel> {

        val page = params.key ?: 1

        return try {
            val response = api.getListeners(
                page = page,
                limit = pageSize
            )

            if (!response.success) {
                return LoadResult.Error(
                    Exception(response.message)
                )
            }

            val items = response.data.map { it.toDomain() }

            val total = (response.meta?.get("total") as? Number)?.toInt()
                ?: items.size

            val hasMore = (page * pageSize) < total

            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasMore) page + 1 else null
            )

        } catch (e: IOException) {
            // Network error
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // API error
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * Used when refresh happens (rotation, retry, etc.)
     */
    override fun getRefreshKey(
        state: PagingState<Int, ListenerModel>
    ): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}