/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalSerializationApi::class)

package org.lineageos.updater.data.source.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.lineageos.updater.data.Update

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkUpdateResponse(
    @SerialName("response") val response: List<NetworkUpdate> = emptyList(),
)

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
@JsonIgnoreUnknownKeys
data class NetworkUpdate(
    @SerialName("datetime") val datetime: Long,
    @SerialName("filename") val filename: String,
    @SerialName("id") val id: String,
    @SerialName("romtype") val romtype: String? = null,
    @SerialName("size") val size: Long,
    @SerialName("url") val url: String,
    @SerialName("version") val version: String,
)

fun NetworkUpdate.toUpdate(): Update = Update(
    downloadId = id,
    name = filename,
    timestamp = datetime,
    type = romtype,
    fileSize = size,
    downloadUrl = url,
    version = version,
    isAvailableOnline = true,
)
