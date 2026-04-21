package com.github.innertube.models.bodies

import com.github.innertube.models.Context
import com.github.innertube.models.YouTubeClient
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val localized: Boolean = true,
    val context: Context,
    val browseId: String,
    val params: String? = null
) {
    constructor(
        localized: Boolean = true,
        browseId: String,
        params: String? = null
    ) : this(
        localized = localized,
        context = YouTubeClient.WEB_REMIX.toContext(
            localized = localized,
            visitorData = com.github.innertube.Innertube.visitorData
        ),
        browseId = browseId,
        params = params
    )
}
