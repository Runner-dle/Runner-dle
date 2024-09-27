package com.example.runner_dle.routes

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.CameraPosition

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

                    // 출발지 마커
                    val startMarker = Marker().apply {
                        position = startPoint
                        map = naverMap
                        captionText = "출발지"
                    }

                    // 도착지 마커
                    val endMarker = Marker().apply {
                        position = endPoint
                        map = naverMap
                        captionText = "도착지"
                    }

                    // 카메라 위치를 두 포인트를 중심으로 설정
                    val cameraPosition = CameraPosition(
                        LatLng((startPoint.latitude + endPoint.latitude) / 2, (startPoint.longitude + endPoint.longitude) / 2),
                        14.0 // 적당한 줌 레벨
                    )
                    naverMap.cameraPosition = cameraPosition

                    // 카메라 애니메이션 설정 (시작 지점으로 이동)
                    naverMap.moveCamera(CameraUpdate.scrollTo(startPoint))
                }
                mapView
            }, modifier = Modifier.fillMaxSize())
        }
    }
}
