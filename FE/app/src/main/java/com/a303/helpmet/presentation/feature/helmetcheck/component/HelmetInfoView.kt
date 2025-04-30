package com.a303.helpmet.presentation.feature.helmetcheck.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import com.a303.helpmet.presentation.feature.helmetcheck.HelmetCheckViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HelmetInfoView(
    viewModel: HelmetCheckViewModel = koinViewModel(),
) {
    Modifier.padding(32.dp)
    val isConnected by viewModel.isConnected.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(), //.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // String.xml 내에 있음
//        Row {
//            Text("헬프")
//            Text("멧")
//        }
//        Text("안전한 라이딩을 위한 정보,")
//        Text("보이지 않는 순간까지 지켜드립니다")

        Text("helpmet app info")
    }
    Modifier.padding(32.dp)
    Column {
        Text("헬멧 연결 정보")
        Button(onClick = { viewModel.checkConnection() }) {
            Text(if (isConnected) "연결됨" else "연결하기")
        }
    }
}