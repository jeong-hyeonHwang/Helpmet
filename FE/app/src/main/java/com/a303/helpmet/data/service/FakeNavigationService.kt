package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.BaseResponse
import kotlinx.serialization.json.Json

class FakeNavigationService : NavigationService {
    override suspend fun getBikeNavigationRouteList(
        lat: Double,
        lng: Double,
        maxMinutes: Int
    ): BaseResponse<List<NavigationResponseDto>> {

        val jsonString = """
{
    "status": 200,
    "message": "success",
    "data": [{
        "distance_m": 243.2,
        "estimated_time_sec": 58,
        "start_addr": "해미정",
        "end_addr": "역삼빌딩 앞(하나은행 옆) 대여소",
        "route": [
            {
                "coords": [
                    {
                        "lat": 37.5026875,
                        "lon": 127.0393049
                    },
                    {
                        "lat": 37.5024566,
                        "lon": 127.0394238
                    },
                    {
                        "lat": 37.5023294,
                        "lon": 127.0394956
                    },
                    {
                        "lat": 37.50211,
                        "lon": 127.0396034
                    },
                    {
                        "lat": 37.5018872,
                        "lon": 127.0397251
                    },
                    {
                        "lat": 37.5017994,
                        "lon": 127.039773
                    }
                ],
                "is_cycleway": false,
                "distance_m": 107.054
            },
            {
                "coords": [
                    {
                        "lat": 37.5017994,
                        "lon": 127.039773
                    },
                    {
                        "lat": 37.5015717,
                        "lon": 127.0390986
                    },
                    {
                        "lat": 37.501415820173534,
                        "lon": 127.03860761228685
                    }
                ],
                "is_cycleway": false,
                "distance_m": 136.1904157414968
            }
        ],
        "instructions": [
            {
                "index": 0,
                "location": {
                    "lat": 37.5026875,
                    "lon": 127.0393049
                },
                "distance_m": 107.0,
                "action": "직진",
                "message": "107m 직진하세요"
            },
            {
                "index": 1,
                "location": {
                    "lat": 37.5017994,
                    "lon": 127.039773
                },
                "distance_m": 107.0,
                "action": "우회전",
                "message": "스타벅스 역삼대로점 앞에서 우회전하세요"
            },
            {
                "index": 1,
                "location": {
                    "lat": 37.5017994,
                    "lon": 127.039773
                },
                "distance_m": 136.0,
                "action": "직진",
                "message": "136m 직진하세요"
            }
        ]
    }]
}
""".trimIndent()


        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val response = json.decodeFromString<BaseResponse<List<NavigationResponseDto>>>(jsonString)
        return response
    }

    override suspend fun getBikeNavigationNearBy(
        lat: Double,
        lng: Double,
        placeType: String
    ): BaseResponse<NavigationResponseDto> {
        // 예시로 더미 데이터 반환
        return BaseResponse(
            status = 200,
            message = "success",
            data = NavigationResponseDto(
                distance = 1000.0,
                estimatedTimeSec = 600,
                startAddr = "현위치",
                endAddr = "화장실",
                route = listOf(), // 빈 RouteSegmentDto 리스트
                instructions = listOf() // 빈 InstructionDto 리스트
            )
        )
    }
}
