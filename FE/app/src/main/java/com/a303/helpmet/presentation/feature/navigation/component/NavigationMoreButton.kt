package com.a303.helpmet.presentation.feature.navigation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.a303.helpmet.R
import com.a303.helpmet.ui.theme.HelpmetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationMoreButton(onFinish: () -> Unit) {
    var isSheetOpen by remember { mutableStateOf(false) }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        onFinish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HelpmetTheme.colors.black1,
                        contentColor = HelpmetTheme.colors.primary
                    ),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "안내 종료",
                        style = HelpmetTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
    IconButton(
        onClick = { isSheetOpen = true },
        modifier = Modifier.size(44.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_dot_vertical),
            contentDescription = "더보기",
            tint = Color.Black
        )
    }
}

