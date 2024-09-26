package com.yourapp.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSelectionScreen(navController: NavController, onRouteSelected: (LatLng, LatLng) -> Unit) {
    var startPoint by remember { mutableStateOf<LatLng?>(null) }
    var endPoint by remember { mutableStateOf<LatLng?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Route") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 출발지와 도착지 선택을 위한 버튼
            Button(onClick = {
                // 여기에 출발지 선택 로직을 추가
                startPoint = LatLng(37.5665, 126.9780) // 임의로 서울 시청 위치
            }) {
                Text(text = "Select Starting Point")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // 여기에 도착지 선택 로직을 추가
                endPoint = LatLng(37.5511, 126.9882) // 임의로 남산 타워 위치
            }) {
                Text(text = "Select Destination Point")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (startPoint != null && endPoint != null) {
                Button(onClick = {
                    // 출발지와 도착지를 넘기고 경로 확인 화면으로 이동
                    onRouteSelected(startPoint!!, endPoint!!)
                    navController.navigate("routeDisplay")
                }) {
                    Text(text = "Show Route")
                }
            }
        }
    }
}
