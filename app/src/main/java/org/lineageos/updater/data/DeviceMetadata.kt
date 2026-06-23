/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.data

data class DeviceMetadata(
    val maintainer: String? = null,
    val telegram: String? = DEFAULT_TELEGRAM,
) {
    companion object {
        const val DEFAULT_TELEGRAM = "https://t.me/DerpFestAOSP"
    }
}
