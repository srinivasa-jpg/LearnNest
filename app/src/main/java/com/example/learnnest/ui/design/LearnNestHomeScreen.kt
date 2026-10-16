package com.example.learnnest.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Screen 1 of the approved eight-screen reference.
 * Only Course Content and Quizzes are entry points.
 * Existing navigation and loading stay in MainActivity.
 */
@Composable
fun LearnNestHomeScreen(
    onCourseContent: () -> Unit,
    onQuizzes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(LearnNestColors.Cream, LearnNestColors.Ivory, LearnNestColors.Rose)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cloud = LearnNestColors.Rose.copy(alpha = 0.60f)
            drawCircle(cloud, radius = size.width * 0.25f,
                center = androidx.compose.ui.geometry.Offset(-size.width * 0.10f, size.height * 0.08f))
            drawCircle(cloud, radius = size.width * 0.15f,
                center = androidx.compose.ui.geometry.Offset(size.width * 1.04f, size.height * 0.20f))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LearnNestCapLogo(Modifier.width(110.dp).height(88.dp))
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = "LearnNest",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = LearnNestColors.WineDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Learn Anytime, Grow Everyday",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LearnNestColors.Ink,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                LearnNestActionCard(
                    title = "Course Content",
                    subtitle = "Study subjects, units and topics",
                    primary = true,
                    icon = HomeActionIcon.BOOK,
                    onClick = onCourseContent
                )
            }
            item {
                LearnNestActionCard(
                    title = "Quizzes",
                    subtitle = "Practice and test your knowledge",
                    primary = false,
                    icon = HomeActionIcon.QUIZ,
                    onClick = onQuizzes
                )
            }
            item {
                LearnNestBookLandscape(Modifier.fillMaxWidth().height(155.dp))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun LearnNestHomeReferencePreview() {
    MaterialTheme {
        LearnNestHomeScreen(onCourseContent = {}, onQuizzes = {})
    }
}
