package tn.petcare_android.ui.screens.petdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.petcare_android.ui.theme.CardBackground
import tn.petcare_android.ui.theme.OrangeAccent
import tn.petcare_android.ui.theme.TextPrimary
import tn.petcare_android.ui.theme.TextSecondary
import tn.petcare_android.util.Constants

/**
 * Reusable components for Pet Detail Screen
 * Extracted from main screen file for better organization
 */

/**
 * Displays a stat card with a value and label
 */
@Composable
fun PetStatCard(
    statValue: String,
    statLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Constants.UI.Card.CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = Constants.UI.Card.ELEVATION)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Constants.UI.Card.PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statValue,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            Spacer(modifier = Modifier.height(Constants.UI.Spacing.SMALL))
            Text(
                text = statLabel,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Displays an info row with label and value
 */
@Composable
fun PetInfoCard(
    infoLabel: String,
    infoValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Constants.UI.Card.CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = Constants.UI.Card.ELEVATION)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Constants.UI.Card.PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = infoLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = infoValue,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent
            )
        }
    }
}

/**
 * Empty state card when no additional info is available
 */
@Composable
fun NoInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Constants.UI.Card.CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = Constants.UI.Card.ELEVATION)
    ) {
        Text(
            text = "No additional information available",
            modifier = Modifier.padding(Constants.UI.Card.PADDING),
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

