package com.github.innertube.models.bodies

import com.github.innertube.models.Context
import com.github.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String,
    val params: String
) {
    constructor(
        query: String,
        params: String
    ) : this(
        context = YouTubeClient.WEB_REMIX.toContext(visitorData = com.github.innertube.Innertube.visitorData),
        query = query,
        params = params
    )
}
