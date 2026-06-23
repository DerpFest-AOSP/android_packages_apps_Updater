/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalSerializationApi::class)

package org.lineageos.updater.data.source.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.lineageos.updater.data.DeviceMetadata

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkDeviceInfoResponse(
    @SerialName("devices") val devices: List<NetworkDeviceInfo> = emptyList(),
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkDeviceInfo(
    @SerialName("codename") val codename: String,
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("maintainer") val maintainer: String? = null,
    @SerialName("support_group") val supportGroup: String? = null,
)

fun NetworkDeviceInfo.toDeviceMetadata(): DeviceMetadata = DeviceMetadata(
    maintainer = maintainer?.takeIf(String::isNotBlank),
    device = deviceName?.takeIf(String::isNotBlank),
    telegram = normalizeTelegramUrl(supportGroup) ?: DeviceMetadata.DEFAULT_TELEGRAM,
)

private fun normalizeTelegramUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null

    val trimmed = url.trim()
    return when {
        trimmed.startsWith("https://") || trimmed.startsWith("http://") -> trimmed
        trimmed.startsWith("t.me/") -> "https://$trimmed"
        trimmed.startsWith("telegram.me/") -> "https://$trimmed"
        else -> trimmed
    }
}
