package com.example.runner_dle.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.yourapp.repository.GeocodingRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningRoutesScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val locationSource =
        remember { FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE) }

    // 출발지와 도착지 하드코딩 (테스트용)
    val startPoint = "서울특별시 종로구 사직로9길 18-1"
    val endPoint = "인천광역시 서구 검단로 836"
    var distanceKm by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var roadAddress by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val geocodingRepository = GeocodingRepository()

    // 출발지와 도착지 좌표 상태 저장
    var startLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var endLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }

    // Snackbar 상태 저장
    val snackbarHostState = remember { SnackbarHostState() }

    // AndroidManifest에서 NAVER_CLIENT_ID와 NAVER_CLIENT_SECRET을 불러오는 코드
    val naverClientId = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_ID")

    val naverClientSecret = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_SECRET")

    // 추가: API 키 값을 로그로 확인
    Log.d("API_KEYS", "Client ID: $naverClientId, Secret: $naverClientSecret")

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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) // Snackbar 표시할 위치 설정
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            // Geocoding API 호출
                            val startPointResponse = naverClientId?.let {
                                geocodingRepository.getGeocodedLocation(
                                    startPoint,
                                    it,
                                    naverClientSecret!!
                                )
                            }
                            val endPointResponse = naverClientId?.let {
                                geocodingRepository.getGeocodedLocation(
                                    endPoint,
                                    it,
                                    naverClientSecret!!
                                )
                            }

                            // Geocoding API 응답 확인 로그
                            Log.d("Geocode", "StartPointResponse: $startPointResponse")
                            Log.d("Geocode", "EndPointResponse: $endPointResponse")

                            // 응답이 null이 아닌지 확인
                            if (startPointResponse != null && endPointResponse != null) {
                                // 출발지와 도착지 좌표 설정
                                startLatLng = LatLng(
                                    startPointResponse.addresses?.firstOrNull()?.latitude?.toDouble() ?: 0.0,
                                    startPointResponse.addresses?.firstOrNull()?.longitude?.toDouble() ?: 0.0
                                )
                                endLatLng = LatLng(
                                    endPointResponse.addresses?.firstOrNull()?.latitude?.toDouble() ?: 0.0,
                                    endPointResponse.addresses?.firstOrNull()?.longitude?.toDouble() ?: 0.0
                                )

                                // 반환된 좌표값을 로그로 출력
                                Log.d(
                                    "LatLng",
                                    "Start LatLng: ${startLatLng.latitude}, ${startLatLng.longitude}"
                                )
                                Log.d(
                                    "LatLng",
                                    "End LatLng: ${endLatLng.latitude}, ${endLatLng.longitude}"
                                )
                            } else {
                                Log.e("Geocode", "Failed to fetch geocoding data.")
                            }

                            // Snackbar로 결과 표시
                            snackbarHostState.showSnackbar("Start: ${startPointResponse?.addresses?.firstOrNull()?.roadAddress}, End: ${endPointResponse?.addresses?.firstOrNull()?.roadAddress}")

                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error during Geocoding API call: ${e.message}", e)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Show Recommended Route")
            }

            if (hasLocationPermission) {
                // 지도 보여주기 및 경로 표시
                NaverMapWithRoute(startLatLng = startLatLng, endLatLng = endLatLng)
            } else {
                Text("Location permission is required to show the map.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KM 입력 필드
            OutlinedTextField(
                value = distanceKm,
                onValueChange = { distanceKm = it },
                label = { Text("Desired Distance (KM)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (roadAddress.isNotEmpty()) {
                Text(
                    text = "Found Address: $roadAddress",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate("main")
            }) {
                Text("Go Back to Main")
            }
        }
    }
}
@Composable
fun NaverMapWithRoute(startLatLng: LatLng, endLatLng: LatLng) {
    val context = LocalContext.current
    AndroidView(factory = { ctx: Context ->
        val mapView = MapView(ctx)
        mapView.getMapAsync { naverMap ->

            // 경로 그리기
            if (startLatLng.latitude != 0.0 && endLatLng.latitude != 0.0) {
                val path = PathOverlay().apply {
                    coords = listOf(startLatLng, endLatLng)
                }
                path.map = naverMap

                // 마커 설정
                val startMarker = Marker().apply {
                    position = startLatLng
                    captionText = "출발지"
                    map = naverMap // 마커를 지도에 추가
                }
                val endMarker = Marker().apply {
                    position = endLatLng
                    captionText = "도착지"
                    map = naverMap // 마커를 지도에 추가
                }

                // 마커가 추가됐는지 로그 확인
                Log.d("Marker", "Start marker at: ${startLatLng.latitude}, ${startLatLng.longitude}")
                Log.d("Marker", "End marker at: ${endLatLng.latitude}, ${endLatLng.longitude}")

                // 카메라 위치를 경로에 맞춰 이동
                val bounds = com.naver.maps.geometry.LatLngBounds.Builder()
                    .include(startLatLng)
                    .include(endLatLng)
                    .build()

                // 카메라 이동 로그
                Log.d("CameraUpdate", "Moving camera to fit bounds")

                // 경로가 모두 화면에 보이도록 카메라 이동
                naverMap.moveCamera(CameraUpdate.fitBounds(bounds, 100))
            } else {
                Log.e("Marker", "Start or End LatLng is invalid.")
            }
        }
        mapView
    }, modifier = Modifier.fillMaxSize())
}


private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
