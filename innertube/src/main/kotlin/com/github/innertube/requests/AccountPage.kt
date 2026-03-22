package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.AccountInfo
import com.github.innertube.models.YouTubeClient
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
private data class AccountMenuBody(
    val context: com.github.innertube.models.Context = YouTubeClient.WEB_REMIX.toContext()
)

@Serializable
private data class AccountMenuResponse(
    val actions: List<Action>? = null
) {
    @Serializable
    data class Action(
        val openPopupAction: OpenPopupAction? = null
    )

    @Serializable
    data class OpenPopupAction(
        val popup: Popup? = null
    )

    @Serializable
    data class Popup(
        val multiPageMenuRenderer: MultiPageMenuRenderer? = null
    )

    @Serializable
    data class MultiPageMenuRenderer(
        val header: Header? = null
    )

    @Serializable
    data class Header(
        val activeAccountHeaderRenderer: ActiveAccountHeaderRenderer? = null
    )

    @Serializable
    data class ActiveAccountHeaderRenderer(
        val accountName: AccountText? = null,
        val email: AccountText? = null,
        val channelHandle: AccountText? = null,
        val accountPhoto: AccountPhoto? = null
    )

    @Serializable
    data class AccountText(
        val simpleText: String? = null
    )

    @Serializable
    data class AccountPhoto(
        val thumbnails: List<PhotoThumbnail>? = null
    )

    @Serializable
    data class PhotoThumbnail(
        val url: String? = null,
        val width: Int? = null,
        val height: Int? = null
    )
}

suspend fun Innertube.accountInfo() = runCatchingNonCancellable {
    val response = client.post(ACCOUNT_MENU) {
        setBody(AccountMenuBody())
    }.body<AccountMenuResponse>()

    val header = response.actions
        ?.firstOrNull()
        ?.openPopupAction
        ?.popup
        ?.multiPageMenuRenderer
        ?.header
        ?.activeAccountHeaderRenderer

    header?.let {
        val photoUrl = it.accountPhoto?.thumbnails
            ?.maxByOrNull { thumb -> thumb.width ?: 0 }
            ?.url

        AccountInfo(
            name = it.accountName?.simpleText ?: "",
            email = it.email?.simpleText,
            channelHandle = it.channelHandle?.simpleText,
            photoUrl = photoUrl
        )
    }
}
