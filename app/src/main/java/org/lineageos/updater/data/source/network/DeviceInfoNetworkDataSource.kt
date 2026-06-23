/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.data.source.network

import android.content.Context
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.lineageos.updater.R
import org.lineageos.updater.data.DeviceMetadata
import org.lineageos.updater.deviceinfo.DeviceInfoUtils
import java.io.IOException
import java.util.concurrent.TimeUnit

class DeviceInfoNetworkDataSource(private val context: Context) {
    private val deviceInfoUrl: String
        get() {
            val base = context.getString(R.string.device_info_url)
            require(base.startsWith("https://")) {
                "Device info URL must use HTTPS: $base"
            }
            return base
        }

    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun fetchDeviceMetadata(): DeviceMetadata {
        val request = Request.Builder()
            .url(deviceInfoUrl)
            .build()

        val responseBody = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP status: ${response.code}")
            }

            response.body?.string() ?: throw IOException("Empty response body")
        }

        val codename = DeviceInfoUtils.device
        return json.decodeFromString<NetworkDeviceInfoResponse>(responseBody)
            .devices
            .firstOrNull { it.codename.equals(codename, ignoreCase = true) }
            ?.toDeviceMetadata()
            ?: DeviceMetadata(telegram = DeviceMetadata.DEFAULT_TELEGRAM)
    }
}
