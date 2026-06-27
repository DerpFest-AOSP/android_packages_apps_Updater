/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.updater.deviceinfo

import android.graphics.RuntimeShader
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.isActive
import kotlin.math.max

private const val RIPPLE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float2 iTouch;
    uniform float iTouchStrength;
    uniform half3 iBaseColor;
    layout(color) uniform half4 iAccentStart;
    layout(color) uniform half4 iAccentEnd;

    float softBlob(float2 uv, float2 center, float radius) {
        float d = length(uv - center);
        return smoothstep(radius, radius * 0.18, d);
    }

    float2 rippleOffset(float2 coord, float2 origin, float strength, float time) {
        float2 delta = coord - origin;
        float dist = length(delta);
        if (dist < 0.001) {
            return float2(0.0);
        }

        float2 dir = delta / dist;
        float2 perp = float2(-dir.y, dir.x);
        float wave = strength * sin(dist * 0.045 - time * 3.2) * exp(-dist * 0.009);
        return dir * wave * 14.0 + perp * wave * 4.5;
    }

    half4 main(float2 fragCoord) {
        float2 coord = fragCoord;

        if (iTouchStrength > 0.001) {
            coord += rippleOffset(fragCoord, iTouch, iTouchStrength, iTime);
        }

        float2 center = iResolution * 0.5;
        coord += rippleOffset(fragCoord, center, 0.22, iTime * 0.65);

        float2 uv = coord / iResolution;
        float t = iTime;

        float2 blobA = float2(
            0.22 + 0.07 * sin(t * 0.31),
            0.38 + 0.06 * cos(t * 0.27)
        );
        float2 blobB = float2(
            0.78 + 0.06 * cos(t * 0.29),
            0.52 + 0.07 * sin(t * 0.25)
        );
        float2 blobC = float2(
            0.52 + 0.05 * sin(t * 0.23),
            0.22 + 0.05 * cos(t * 0.33)
        );

        half3 color = iBaseColor;
        color = mix(color, iAccentStart.rgb, half(softBlob(uv, blobA, 0.48) * 0.24));
        color = mix(color, iAccentEnd.rgb, half(softBlob(uv, blobB, 0.42) * 0.20));
        color = mix(
            color,
            (iAccentStart.rgb * 0.55 + iAccentEnd.rgb * 0.45),
            half(softBlob(uv, blobC, 0.36) * 0.14)
        );

        return half4(color, 1.0);
    }
"""

private fun RuntimeShader.setBaseColorUniform(color: Color) {
    setFloatUniform("iBaseColor", color.red, color.green, color.blue)
}

@Composable
fun UpdaterHeaderRippleBox(
    baseColor: Color,
    accentStart: Color,
    accentEnd: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val view = LocalView.current
    val shader = remember { RuntimeShader(RIPPLE_SHADER_SRC) }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }
    var touchPoint by remember { mutableStateOf(Offset.Zero) }
    var touchStrength by remember { mutableFloatStateOf(0f) }

    val animationsEnabled = remember(view) {
        Settings.Global.getFloat(
            view.context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect

        var lastFrameTimeNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                lastFrameTimeNanos = frameTimeNanos
                elapsedSeconds = frameTimeNanos / 1_000_000_000f
                if (touchStrength > 0f) {
                    touchStrength = max(0f, touchStrength - deltaSeconds * 1.8f)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.pressed } ?: continue
                        touchPoint = change.position
                        touchStrength = 1f
                    }
                }
            }
            .drawBehind {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", if (animationsEnabled) elapsedSeconds else 0f)
                shader.setFloatUniform("iTouch", touchPoint.x, touchPoint.y)
                shader.setFloatUniform("iTouchStrength", touchStrength)
                shader.setBaseColorUniform(baseColor)
                shader.setColorUniform("iAccentStart", accentStart.toArgb())
                shader.setColorUniform("iAccentEnd", accentEnd.toArgb())
                drawRect(ShaderBrush(shader))
            },
        content = content,
    )
}
