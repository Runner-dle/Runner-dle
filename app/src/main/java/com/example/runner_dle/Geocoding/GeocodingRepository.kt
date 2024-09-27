package com.yourapp.repository

import com.yourapp.network.GeocodingApi
import com.example.runner_dle.Geocoding.GeocodeResponse

class GeocodingRepository {

    // Geocoding API 호출
    suspend fun getGeocodedLocation(query: String, apiKeyId: String, apiKey: String): GeocodeResponse {
        return GeocodingApi.service.getGeocode(query, apiKeyId, apiKey)
    }
}
