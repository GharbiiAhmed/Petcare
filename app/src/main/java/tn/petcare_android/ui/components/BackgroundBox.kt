package tn.petcare_android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import tn.petcare_android.R

/**
 * BackgroundBox - A composable that wraps content with a background image
 * The background image appears as an under layer, similar to SwiftUI's ZStack
 * 
 * Colors: White background + Orange (#FF9900) paw prints
 */
@Composable
fun BackgroundBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    backgroundImageRes: Int? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background layer (under layer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .zIndex(0f)
        ) {
            // Background image overlay (if provided)
            // Note: Only PNG, JPG, WEBP, or VectorDrawable are supported
            // XML shape drawables are NOT supported by painterResource
            backgroundImageRes?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds, // Fill the entire bounds
                    alpha = 0.25f // Temporarily increased to 25% for visibility - adjust back to 0.10f once confirmed working
                )
            }
        }
        
        // Content layer (on top)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            content()
        }
    }
}
