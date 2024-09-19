package com.yourapp.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.overlay.LocationOverlay
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningRoutesScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val locationSource = remember { FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE) }

    // 위치 권한을 관리하는 상태
    var hasLocationPermission by remember { mutableStateOf(false) }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
    }

    // 처음 실행 시 권한 확인 및 요청
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            hasLocationPermission = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Running Routes") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasLocationPermission) {
                // 네이버 지도 보여주기
                NaverMapContainer(locationSource = locationSource)
            } else {
                // 권한이 없을 때 안내 메시지 표시
                Text("Location permission is required to show the map.")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                // 메인 화면으로 돌아가기
                navController.navigate("main")
            }) {
                Text("Go Back to Main")
            }
        }
    }
}

@Composable
fun NaverMapContainer(locationSource: FusedLocationSource) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // MapView를 Jetpack Compose에서 사용
    AndroidView(factory = { ctx: Context ->
        val mapView = MapView(ctx)
        mapView.getMapAsync { naverMap ->
            naverMap.locationSource = locationSource

            // 위치 오버레이 활성화
            val locationOverlay = naverMap.locationOverlay
            locationOverlay.isVisible = true

            // 위치 추적 모드 설정 (Follow로 설정하여 내 위치를 따라감)
            naverMap.locationTrackingMode = com.naver.maps.map.LocationTrackingMode.Follow

            // 위치 정보 업데이트 (권한이 있는지 확인 후 수행)
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentPosition = LatLng(it.latitude, it.longitude)
                        locationOverlay.position = currentPosition

                        // 카메라를 현재 위치로 이동
                        val cameraUpdate = CameraUpdate.scrollTo(currentPosition)
                        naverMap.moveCamera(cameraUpdate)
                    }
                }
            }
        }
        mapView
    }, update = { mapView ->
        mapView.onResume() // 필요할 경우 생명주기에 맞춰 지도 갱신
    })
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
