/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.deviceinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.android.settingslib.spa.debug.UiModePreviews
import com.android.settingslib.spa.framework.theme.SettingsDimension
import com.android.settingslib.spa.framework.theme.SettingsShape.CornerExtraLarge1
import com.android.settingslib.spa.framework.theme.SettingsSpace
import com.android.settingslib.spa.framework.theme.SettingsTheme
import org.lineageos.updater.R

// Brand guide: "Mark height based on text x-height". Approximate Roboto x-height from font size.
private const val MARK_X_HEIGHT_RATIO = 0.55f

// Brand guide: "Do not warp, transform". Derive width from height to keep logo proportions.
private const val MARK_WIDTH_MULTIPLIER = 506.27f / 81.72f

// Standalone header mark: nudge above the text-x-height baseline used with version text.
private const val MARK_SIZE_MULTIPLIER = 1.12f

@Composable
fun UpdaterCard(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
    shape: Shape = CornerExtraLarge1,
    maintainer: String? = null,
    device: String? = null,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = MaterialTheme.colorScheme.onSurface
    val accentStart = colorResource(R.color.brand_accent_gradient_start)
    val accentEnd = colorResource(R.color.brand_accent_gradient_end)

    val density = LocalDensity.current
    val displayLarge = MaterialTheme.typography.displayLarge

    val versionStyle = remember(displayLarge) {
        /*
         * Brand guide: "Roboto Light version text, spaced in 8%".
         */
        displayLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            letterSpacing = (-0.08).em,
            lineHeight = displayLarge.fontSize,
        )
    }

    val markHeight = remember(versionStyle, density) {
        with(density) { (versionStyle.fontSize.toPx() * MARK_X_HEIGHT_RATIO).toDp() }
    }
    val markWidth = markHeight * MARK_WIDTH_MULTIPLIER * MARK_SIZE_MULTIPLIER
    val bylineText = if (!maintainer.isNullOrBlank()) {
        stringResource(R.string.updater_maintainer_by, maintainer)
    } else {
        stringResource(R.string.updater_build_unofficial)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
        ),
    ) {
        UpdaterHeaderRippleBox(
            baseColor = containerColor,
            accentStart = accentStart,
            accentEnd = accentEnd,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SettingsDimension.paddingLarge)
                        .padding(top = SettingsSpace.medium3)
                        .semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.derpfest_mark_tight),
                        contentDescription = stringResource(R.string.brand_name),
                        modifier = Modifier.width(markWidth),
                        contentScale = ContentScale.FillWidth,
                        // Match onSurface so the mark reads on surfaceContainerHighest.
                        colorFilter = ColorFilter.tint(contentColor),
                    )

                    Spacer(modifier = Modifier.height(SettingsSpace.medium3))

                    Text(
                        text = bylineText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(SettingsSpace.medium5))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = SettingsDimension.paddingLarge,
                            vertical = SettingsDimension.paddingLarge,
                        ),
                    verticalArrangement = Arrangement.spacedBy(SettingsDimension.paddingLarge),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(SettingsDimension.paddingLarge),
                    ) {
                        InfoColumn(
                            label = stringResource(R.string.header_android_version, androidVersion),
                            value = device.orEmpty(),
                            modifier = Modifier.weight(1f),
                        )
                        InfoColumn(
                            label = stringResource(R.string.build_date),
                            value = buildDate,
                            modifier = Modifier.weight(1f),
                        )
                        InfoColumn(
                            label = stringResource(R.string.security_update),
                            value = securityPatch,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SettingsSpace.extraSmall2),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Normal,
            ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@UiModePreviews
@Composable
private fun UpdaterCardPreview() {
    SettingsTheme {
        UpdaterCard(
            buildVersion = "16.2",
            androidVersion = "16",
            buildDate = "Feb 20",
            securityPatch = "Feb 2026",
            modifier = Modifier.padding(SettingsDimension.itemPadding),
            maintainer = "Alexander Brunswig (@NurKeinNeid)",
            device = "Google Pixel 9 Pro XL",
        )
    }
}
