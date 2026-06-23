/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.debug.UiModePreviews
import com.android.settingslib.spa.framework.theme.SettingsDimension
import com.android.settingslib.spa.framework.theme.SettingsTheme
import org.lineageos.updater.data.DeviceMetadata
import org.lineageos.updater.deviceinfo.DeviceInfoUtils

private sealed interface LinkIcon {
    data class Vector(val imageVector: ImageVector) : LinkIcon
    data class Drawable(@DrawableRes val resId: Int) : LinkIcon
}

private data class DeviceLink(
    val icon: LinkIcon,
    @StringRes val labelRes: Int,
    val url: String,
)

@Composable
fun DeviceLinksRow(
    metadata: DeviceMetadata,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val downloadsUrl = stringResource(R.string.menu_downloads_url, DeviceInfoUtils.device)

    val links = buildList {
        metadata.telegram?.takeIf(String::isNotBlank)?.let {
            add(DeviceLink(LinkIcon.Drawable(R.drawable.ic_telegram), R.string.updater_link_telegram, it))
        }
        add(DeviceLink(LinkIcon.Vector(Icons.Outlined.Language), R.string.updater_link_downloads, downloadsUrl))
    }
    if (links.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(SettingsDimension.itemPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        links.forEach { link ->
            LinkTile(
                icon = link.icon,
                labelRes = link.labelRes,
                modifier = Modifier.weight(1f),
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun LinkTile(
    icon: LinkIcon,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            when (icon) {
                is LinkIcon.Vector -> Icon(
                    imageVector = icon.imageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp),
                )

                is LinkIcon.Drawable -> Icon(
                    painter = painterResource(icon.resId),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@UiModePreviews
@Composable
private fun DeviceLinksRowPreview() {
    SettingsTheme {
        DeviceLinksRow(
            metadata = DeviceMetadata(
                maintainer = "NurKeinNeid",
                telegram = "https://t.me/DerpFestAOSP",
            ),
        )
    }
}
