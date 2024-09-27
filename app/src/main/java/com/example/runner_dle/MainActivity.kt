package com.example.runner_dle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runner_dle.marker.LocationMarkerScreenWithGeo
import com.example.runner_dle.routes.RouteDisplayScreen
import com.example.runner_dle.routes.RunningRoutesScreen
import com.example.runner_dle.ui.theme.RunnerdleTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.util.FusedLocationSource
import com.yourapp.ui.main.MainScreen
import com.yourapp.ui.routes.RouteSelectionScreen

class MainActivity : ComponentActivity() {
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var hasLocationPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // FusedLocationSource 초기화 (권한 코드 1000)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 권한 요청 런처
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            hasLocationPermission = isGranted
        }

        // 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            hasLocationPermission = true
        }

        setContent {
            RunnerdleTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigator(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        hasLocationPermission = hasLocationPermission
                    )
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

@Composable
fun AppNavigator(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean
) {
    var startPoint by remember { mutableStateOf<LatLng?>(null) }
    var endPoint by remember { mutableStateOf<LatLng?>(null) }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController = navController) }
        composable("runningRoutes") {
            if (hasLocationPermission) {
                RunningRoutesScreen(navController = navController)
            } else {
                Text("이 기능을 사용하려면 위치 권한이 필요합니다.")
            }
        }
        composable("routeSelection") {
            RouteSelectionScreen(navController = navController) { start, end ->
                startPoint = start
                endPoint = end
            }
        }
        composable("routeDisplay") {
            if (startPoint != null && endPoint != null) {
                RouteDisplayScreen(startPoint!!, endPoint!!)
            } else {
                Text("Please select a route first.")
            }
        }
        // 내 위치에 마커를 표시하는 LocationMarkerScreen을 추가
        composable("locationMarker") {
            if (hasLocationPermission) {
                LocationMarkerScreenWithGeo()
            } else {
                Text("위치 권한이 필요합니다.")
            }
        }
    }
}
