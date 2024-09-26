package com.yourapp.ui.routes

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.PathOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDisplayScreen(startPoint: LatLng, endPoint: LatLng) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Route") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AndroidView(factory = { ctx: Context ->
                val mapView = MapView(ctx)
                mapView.getMapAsync { naverMap ->
                    // 경로를 PathOverlay로 그리기
                    val path = PathOverlay().apply {
                        coords = listOf(startPoint, endPoint)
                    }
                    path.map = naverMap

                    // 출발지, 도착지 카메라 위치 설정
                    naverMap.moveCamera(com.naver.maps.map.CameraUpdate.scrollTo(startPoint))
                }
                mapView
            }, modifier = Modifier.fillMaxSize())
        }
    }
}
