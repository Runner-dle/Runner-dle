# 🏃‍♂️ Runner-dle

# 간편하게 러닝 코스를 짜서, 달리기만 하면 되도록!

우리 팀은 러너들이 복잡한 고민 없이, 원하는 거리와 경로를 설정해 바로 뛰어들 수 있는 간편한 러닝 코스 앱을 만드는 것을 목표로 합니다.  
사용자가 코스 걱정 없이 발만 내디딜 수 있도록, 최고의 루트 설계 비서를 자처하며, 모두가 만족할 수 있는 러닝 경험을 제공합니다.

"어디로 뛰지?"라는 고민은 이제 그만! 우리가 필요한 모든 것을 간단하고 스마트하게 해결해 드립니다.  
**목표:** 러너들이 코스 걱정 없이 달리는 즐거움만을 느낄 수 있도록 돕는 것입니다.

## 🚀 프로젝트 개요
**Runner-dle**는 다음과 같은 핵심 기능을 제공합니다:

- **현재 위치 마커 표시**: 사용자의 현재 위치를 자동으로 감지하고 지도에 표시합니다.
- **도착지 마커 표시**: 도착지 주소를 입력하면, 해당 주소를 지도에 마커로 표시합니다.
- **실시간 경로 탐색**: 출발지와 도착지 간의 경로를 지도에 표시합니다.
- **사용자 인터페이스(UI)**: 사용자가 출발지와 도착지를 입력할 수 있는 간단한 텍스트 필드를 제공합니다.
- **원하는 거리(KM)에 맞춘 추천 경로 제공**: 사용자가 원하는 km 수를 입력하면, 그에 가장 근접한 추천 경로를 지도에 표시해줍니다. 이는 기존 러닝 앱들과 차별화된 기능으로, 특정 거리 훈련을 원하는 사용자에게 유용합니다.

---

## 🛠️ 기능 목록

- **📍 현재 위치 탐색**:
  - 사용자의 현재 위치를 자동으로 탐색하고 지도에 표시
  - `FusedLocationProviderClient`를 사용하여 위치 추적

- **🗺️ 도착지 검색 및 마커 표시**:
  - 입력한 도착지 주소를 **Geocoding API**를 통해 좌표로 변환
  - 도착지 좌표를 지도에 마커로 표시

- **💻 사용자 인터페이스**:
  - 출발지와 도착지를 입력할 수 있는 텍스트 필드 제공
  - 지도의 크기를 조정하고 화면 레이아웃 최적화

---

## 📚 기술 스택

- **언어**: Kotlin
- **UI 프레임워크**: Jetpack Compose
- **지도 API**: 네이버 지도 SDK (Naver Maps API)
- **위치 서비스**: FusedLocationProviderClient
- **네트워킹**: 네이버 Geocoding API

---

## 🚀 설치 및 실행 방법

1. **프로젝트 클론**:
   ```bash
   git clone https://github.com/your-username/runner-dle.git

   ## 네이버 지도 API 설정:

1. **네이버 개발자 콘솔에서 API 키 발급**
2. `local.properties` 파일에 다음 내용을 추가:
   ```properties
   NAVER_CLIENT_ID=your-client-id
   NAVER_CLIENT_SECRET=your-client-secret

   ## 프로젝트 실행:
- Android Studio에서 프로젝트를 열고, 에뮬레이터 또는 실제 기기에서 실행

## 🎯 향후 개선 사항
- ❗ **도착지 마커 오류 수정**: 도착지 주소를 입력했을 때 마커가 제대로 표시되지 않는 문제 해결
- ➕ **다중 경로 제공**: 최단 거리, 추천 경로 등의 다양한 경로 제공 기능 추가
- 💾 **경로 저장 기능**: 사용자 지정 경로 탐색 및 경로 저장 기능 추가

