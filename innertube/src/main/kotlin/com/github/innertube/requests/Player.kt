package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.Context
import com.github.innertube.models.PlayerResponse
import com.github.innertube.models.YouTubeClient
import com.github.innertube.models.bodies.PlayerBody
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

private const val PLAYER_MASK =
    "playabilityStatus.status,playerConfig.audioConfig,streamingData.adaptiveFormats,videoDetails.videoId"

// Last-resort stream sources when no client returns direct URLs.
// Tried in order; each failure falls through to the next instance.
private val PIPED_INSTANCES = listOf(
    "https://pipedapi.adminforge.de",
    "https://pipedapi.kavin.rocks"
)

@Serializable
private data class PipedAudioStream(
    val url: String,
    val itag: Int? = null,
    val bitrate: Long? = null
)

@Serializable
private data class PipedResponse(
    val audioStreams: List<PipedAudioStream>
)

private val PlayerResponse.hasPlayableUrl: Boolean
    get() = playabilityStatus?.status == "OK" &&
            streamingData?.highestQualityFormat?.url != null

suspend fun Innertube.player(videoId: String) = runCatchingNonCancellable {
    // Clients are tried in order until one returns direct streaming URLs.
    // All requests go through playerClient (anonymous, no cookie/auth headers):
    // these native clients reject cookie-based auth, and keeping the player
    // path unauthenticated also avoids leaking YouTube cookies to Piped.
    val contexts = listOf(
        YouTubeClient.ANDROID_VR.toContext(visitorData = visitorData),
        YouTubeClient.IOS.toContext(visitorData = visitorData),
        YouTubeClient.TVHTML5_SIMPLY_EMBEDDED_PLAYER.toContext().copy(
            thirdParty = Context.ThirdParty(
                embedUrl = "https://www.youtube.com/watch?v=$videoId"
            )
        )
    )

    var bestResponse: PlayerResponse? = null
    var lastError: Throwable? = null

    for (context in contexts) {
        val response = runCatching {
            playerClient.post(PLAYER) {
                setBody(PlayerBody(context = context, videoId = videoId))
                mask(PLAYER_MASK)
            }.body<PlayerResponse>()
        }.getOrElse { error ->
            lastError = error
            null
        } ?: continue

        if (response.hasPlayableUrl) return@runCatchingNonCancellable response

        // Keep the most useful non-playable response: prefer one with status OK
        // (formats exist but lack direct URLs) over UNPLAYABLE/LOGIN_REQUIRED etc.
        if (bestResponse == null ||
            (response.playabilityStatus?.status == "OK" &&
                    bestResponse?.playabilityStatus?.status != "OK")
        ) {
            bestResponse = response
        }
    }

    // If a client answered OK but without direct URLs (cipher-protected),
    // patch the URLs in from a Piped instance, matching by itag first
    // (exact bitrate values rarely match across sources).
    val okResponse = bestResponse?.takeIf { it.playabilityStatus?.status == "OK" }

    if (okResponse != null) {
        for (instance in PIPED_INSTANCES) {
            val audioStreams = runCatching {
                playerClient.get("$instance/streams/$videoId") {
                    contentType(ContentType.Application.Json)
                }.body<PipedResponse>().audioStreams
            }.getOrNull() ?: continue

            val patched = okResponse.copy(
                streamingData = okResponse.streamingData?.copy(
                    adaptiveFormats = okResponse.streamingData.adaptiveFormats?.map { format ->
                        format.copy(
                            url = format.url
                                ?: audioStreams.find { it.itag == format.itag }?.url
                                ?: audioStreams.find { it.bitrate == format.bitrate }?.url
                        )
                    }
                )
            )

            if (patched.hasPlayableUrl) return@runCatchingNonCancellable patched
        }
    }

    // Nothing playable: surface the most informative response so the player
    // can map playabilityStatus to a user-facing error, or rethrow the last
    // network failure if no client answered at all.
    bestResponse ?: throw lastError ?: IllegalStateException("No player response")
}
