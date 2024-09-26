package com.yourapp.repository

import com.yourapp.network.GeocodingApi
import com.yourapp.network.GeocodeResponse

class GeocodingRepository {

    // Geocoding API 호출
    suspend fun getGeocodedLocation(query: String, apiKeyId: String, apiKey: String): GeocodeResponse {
        return GeocodingApi.service.getGeocode(query, apiKeyId, apiKey)
    }
}
