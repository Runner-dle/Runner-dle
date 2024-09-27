package com.example.runner_dle.marker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.yourapp.repository.GeocodingRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMarkerScreenWithGeo() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var currentLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var endLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var startLocation by remember { mutableStateOf(TextFieldValue("내 위치")) }  // 내 위치는 기본값
    var endLocation by remember { mutableStateOf(TextFieldValue("인천광역시 서구 불로동 372")) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geocodingRepository = GeocodingRepository()

    // AndroidManifest에서 NAVER_CLIENT_ID와 NAVER_CLIENT_SECRET을 불러오는 코드
    val naverClientId = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_ID")

    val naverClientSecret = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_SECRET")

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
    }

    // 위치 권한 확인 및 요청
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

    // 내 위치 가져오기
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                    Log.d("Location", "Current location: ${it.latitude}, ${it.longitude}")
                }
            }
        }
    }

    // Geocoding을 통해 endPoint의 좌표를 설정
    LaunchedEffect(endLocation.text) {
        if (endLocation.text.isNotBlank()) { // endLocation에 값이 있을 때만 Geocoding을 실행
            coroutineScope.launch {
                try {
                    val endPointResponse = geocodingRepository.getGeocodedLocation(
                        endLocation.text, naverClientId!!, naverClientSecret!!
                    )
                    endLatLng = LatLng(
                        endPointResponse?.addresses?.firstOrNull()?.latitude?.toDouble() ?: 0.0,
                        endPointResponse?.addresses?.firstOrNull()?.longitude?.toDouble() ?: 0.0
                    )

                    Log.d("Geocode", "End: $endLatLng")
                } catch (e: Exception) {
                    Log.e("Geocode", "Error during Geocoding API call: ${e.message}", e)
                }
            }
        } else {
            Log.e("Geocode", "endLocation.text is blank!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Current and End Location Markers") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 지도의 크기를 줄여서 50% 정도로 맞춤
            AndroidView(factory = { ctx: Context ->
                val mapView = MapView(ctx)
                mapView.getMapAsync { naverMap ->

                    // 내 위치에 마커 설정
                    val currentLocationMarker = Marker().apply {
                        position = currentLatLng
                        captionText = "내 위치"
                        map = naverMap
                    }
                    Log.d("Geocode", "End LatLng: ${endLatLng.latitude}, ${endLatLng.longitude}")

                    // EndPoint 마커 설정
                    if (endLatLng.latitude != 0.0) {
                        val endMarker = Marker().apply {
                            position = endLatLng
                            captionText = "도착지"
                            map = naverMap
                        }

                        // 카메라를 도착지로 이동
                        naverMap.moveCamera(CameraUpdate.scrollTo(endLatLng))
                    } else {
                        // 카메라를 내 위치로 이동
                        naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng))
                    }

                    Log.d("Marker", "Current marker at: ${currentLatLng.latitude}, ${currentLatLng.longitude}")
                    Log.d("Marker", "End marker at: ${endLatLng.latitude}, ${endLatLng.longitude}")
                }
                mapView
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp))  // 지도의 높이를 300dp로 설정

            // 출발지와 도착지를 입력하는 텍스트 필드
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = startLocation,
                    onValueChange = { startLocation = it },
                    label = { Text("출발지") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = endLocation,
                    onValueChange = { endLocation = it },
                    label = { Text("도착지") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
