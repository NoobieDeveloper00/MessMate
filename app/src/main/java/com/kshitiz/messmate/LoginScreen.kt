package com.kshitiz.messmate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.ui.theme.MessMateTheme


@Composable
fun LoginScreen(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onAdminLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Top Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ){
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = "Meal Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "MessMate",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = "Your daily meal companion",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In Button
                Button(
                    onClick = onSignInClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Sign In", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Create Account Button
                OutlinedButton(
                    onClick = onCreateAccountClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Create Account", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom small text
                Text(
                    text = "Track your meals, give feedback, and stay connected with your mess",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            }
        }
        // Admin Login Button <-- ADDED THIS
        TextButton(
            onClick = onAdminLoginClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom =  32.dp)
        ) {
            Text(text = "Are you an admin? Login here")
        }
    }
}

@Preview(showSystemUi = true, name = "Light Mode")
@Composable
fun LoginScreenPreviewLight() {
    MessMateTheme(darkTheme = false) {
        LoginScreen(onSignInClick = {}, onCreateAccountClick = {}, onAdminLoginClick = {})
    }
}

//@Preview(showSystemUi = true, name = "Dark Mode")
//@Composable
//fun LoginScreenPreviewDark() {
//    MessMateTheme(darkTheme = true) {
//        LoginScreen(onSignInClick = {}, onCreateAccountClick = {}, onAdminLoginClick = {})
//    }
//}
