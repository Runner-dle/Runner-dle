package com.yourapp.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.util.FusedLocationSource
import com.yourapp.repository.GeocodingRepository
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningRoutesScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val locationSource = remember { FusedLocationSource(activity, LOCATION_PERMISSION_REQUEST_CODE) }

    var startPoint by remember { mutableStateOf("") }
    var endPoint by remember { mutableStateOf("") }
    var distanceKm by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var roadAddress by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val geocodingRepository = GeocodingRepository()

    // Manifest에서 NAVER_CLIENT_ID 불러오기
    val naverClientId = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_ID")

    // local.properties에서 NAVER_CLIENT_SECRET 가져오기
    val naverClientSecret = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData.getString("NAVER_CLIENT_SECRET")

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
            verticalArrangement = Arrangement.Top
        ) {
            if (hasLocationPermission) {
                // 지도 보여주기
                NaverMapContainer(
                    locationSource = locationSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // 지도의 크기를 적절히 설정
                )
            } else {
                Text("Location permission is required to show the map.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 출발지 입력 필드
            OutlinedTextField(
                value = startPoint,
                onValueChange = { startPoint = it },
                label = { Text("Enter Starting Point") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 도착지 입력 필드
            OutlinedTextField(
                value = endPoint,
                onValueChange = { endPoint = it },
                label = { Text("Enter Destination") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

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

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            // URL 인코딩된 주소로 Geocoding API 호출
                            val encodedStartPoint = URLEncoder.encode(startPoint, "UTF-8")
                            val encodedEndPoint = URLEncoder.encode(endPoint, "UTF-8")

                            val startPointResponse = naverClientId?.let {
                                geocodingRepository.getGeocodedLocation(encodedStartPoint, it, naverClientSecret!!)
                            }
                            val endPointResponse = naverClientId?.let {
                                geocodingRepository.getGeocodedLocation(encodedEndPoint, it, naverClientSecret!!)
                            }

                            val startAddress = startPointResponse?.addresses?.firstOrNull()?.roadAddress ?: "주소를 찾을 수 없습니다"
                            val endAddress = endPointResponse?.addresses?.firstOrNull()?.roadAddress ?: "주소를 찾을 수 없습니다"

                            roadAddress = "Start: $startAddress, End: $endAddress"
                            Log.d("API_RESPONSE", "Start Address: $startAddress, End Address: $endAddress")

                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error during Geocoding API call: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Show Recommended Route")
            }

            // 검색된 주소를 표시
            if (roadAddress.isNotEmpty()) {
                Text(text = "Found Address: $roadAddress", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 메인 화면으로 돌아가기 버튼
            TextButton(onClick = {
                navController.navigate("main")
            }) {
                Text("Go Back to Main")
            }
        }
    }
}

@Composable
fun NaverMapContainer(locationSource: FusedLocationSource, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    AndroidView(
        factory = { ctx: Context ->
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
        },
        modifier = modifier // 전달된 Modifier로 크기 제어
    )
}

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
