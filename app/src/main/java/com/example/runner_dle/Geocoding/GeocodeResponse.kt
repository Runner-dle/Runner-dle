package com.example.runner_dle.Geocoding

// Geocoding API 응답 데이터 클래스
data class GeocodeResponse(
    val status: String,
    val meta: Meta,
    val addresses: List<Address>
)

data class Meta(
    val totalCount: Int,
    val page: Int,
    val count: Int
)

data class Address(
    val roadAddress: String?,
    val jibunAddress: String?,
    val x: String?, // 경도 (longitude)
    val y: String?  // 위도 (latitude)
) {
    val latitude: Double?
        get() = y?.toDoubleOrNull()

    val longitude: Double?
        get() = x?.toDoubleOrNull()
}
