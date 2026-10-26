package com.github.musicyou.service

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException
import java.io.InterruptedIOException

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class RecoveringDataSource private constructor(
    private val delegate: DataSource,
    private val invalidateUrl: (String) -> Unit
) : DataSource {
    class Factory(
        private val upstreamFactory: DataSource.Factory,
        private val invalidateUrl: (String) -> Unit
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource = RecoveringDataSource(
            delegate = upstreamFactory.createDataSource(),
            invalidateUrl = invalidateUrl
        )
    }

    private var originalDataSpec: DataSpec? = null
    private var bytesRead = 0L
    private var retries = 0
    private var opened = false

    override fun addTransferListener(transferListener: TransferListener) {
        delegate.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        closeDelegate()
        originalDataSpec = dataSpec
        bytesRead = 0
        retries = 0
        return openWithRecovery(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        while (true) {
            try {
                val result = delegate.read(buffer, offset, length)
                if (result > 0) bytesRead += result
                return result
            } catch (error: IOException) {
                val original = originalDataSpec ?: throw error
                if (retries >= MAX_RETRIES) throw error

                val position = original.position + bytesRead
                val remaining = original.length.takeIf { it != C.LENGTH_UNSET.toLong() }
                    ?.let { (it - bytesRead).coerceAtLeast(0L) }
                    ?: C.LENGTH_UNSET.toLong()
                val retrySpec = original.buildUpon()
                    .setPosition(position)
                    .setLength(remaining)
                    .build()

                closeDelegate()
                invalidateAndBackoff(original.key, error)
                openWithRecovery(retrySpec)
            }
        }
    }

    override fun getUri(): Uri? = if (opened) delegate.uri else null

    override fun getResponseHeaders(): Map<String, List<String>> =
        if (opened) delegate.responseHeaders else emptyMap()

    override fun close() {
        closeDelegate()
        originalDataSpec = null
        bytesRead = 0
        retries = 0
    }

    private fun openWithRecovery(dataSpec: DataSpec): Long {
        while (true) {
            try {
                return delegate.open(dataSpec).also { opened = true }
            } catch (error: IOException) {
                if (retries >= MAX_RETRIES) throw error
                closeDelegate()
                invalidateAndBackoff(dataSpec.key, error)
            }
        }
    }

    private fun invalidateAndBackoff(key: String?, cause: IOException) {
        retries++
        key?.let(invalidateUrl)

        try {
            Thread.sleep(RETRY_DELAY_MS * retries)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw InterruptedIOException("Stream recovery interrupted").apply {
                initCause(cause)
            }
        }
    }

    private fun closeDelegate() {
        if (!opened) return
        delegate.close()
        opened = false
    }

    private companion object {
        const val MAX_RETRIES = 4
        const val RETRY_DELAY_MS = 300L
    }
}
