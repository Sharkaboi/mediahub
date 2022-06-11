package com.sharkaboi.mediahub.modules.manga_ranking.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.haroldadmin.cnradapter.NetworkResponse
import com.sharkaboi.mediahub.common.extensions.getCatchingPaging
import com.sharkaboi.mediahub.data.api.constants.ApiConstants
import com.sharkaboi.mediahub.data.api.enums.MangaRankingType
import com.sharkaboi.mediahub.data.api.models.ApiError
import com.sharkaboi.mediahub.data.api.models.manga.MangaRankingResponse
import com.sharkaboi.mediahub.data.api.retrofit.MangaService
import com.sharkaboi.mediahub.data.wrappers.MHError
import timber.log.Timber

class MangaRankingDataSource(
    private val mangaService: MangaService,
    private val accessToken: String?,
    private val mangaRankingType: MangaRankingType,
    private val showNsfw: Boolean = false
) : PagingSource<Int, MangaRankingResponse.Data>() {

    /**
     *   prevKey == null -> first page
     *   nextKey == null -> last page
     *   both prevKey and nextKey null -> only one page
     */
    override fun getRefreshKey(state: PagingState<Int, MangaRankingResponse.Data>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(ApiConstants.API_PAGE_LIMIT)
                ?: anchorPage?.nextKey?.minus(ApiConstants.API_PAGE_LIMIT)
        }
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, MangaRankingResponse.Data> = getCatchingPaging {
        Timber.d("params : ${params.key}")
        val offset = params.key ?: ApiConstants.API_START_OFFSET
        val limit = ApiConstants.API_PAGE_LIMIT
        if (accessToken == null) {
            return@getCatchingPaging LoadResult.Error(
                MHError.LoginExpiredError.getThrowable()
            )
        }

        val response = mangaService.getMangaRankingAsync(
            authHeader = ApiConstants.BEARER_SEPARATOR + accessToken,
            offset = offset,
            limit = limit,
            rankingType = mangaRankingType.name,
            nsfw = if (showNsfw) ApiConstants.NSFW_ALSO else ApiConstants.SFW_ONLY
        ).await()

        return@getCatchingPaging when (response) {
            is NetworkResponse.Success -> {
                val nextOffset = if (response.body.data.isEmpty()) null else offset + limit
                LoadResult.Page(
                    data = response.body.data,
                    prevKey = if (offset == ApiConstants.API_START_OFFSET) null else offset - limit,
                    nextKey = nextOffset
                )
            }
            is NetworkResponse.UnknownError -> {
                LoadResult.Error(response.error)
            }
            is NetworkResponse.ServerError -> {
                LoadResult.Error(
                    response.body?.getThrowable() ?: ApiError.DefaultError.getThrowable()
                )
            }
            is NetworkResponse.NetworkError -> {
                LoadResult.Error(response.error)
            }
        }
    }
}
