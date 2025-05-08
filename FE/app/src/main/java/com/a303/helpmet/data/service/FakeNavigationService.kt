package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import kotlinx.serialization.json.Json
import retrofit2.Response

class FakeNavigationService : NavigationService {
    override suspend fun getBikeNavigationRouteList(): Response<List<NavigationResponseDto>> {
        val jsonString = """
            [
            {
                "distance_m": 339.2,
                "estimated_time_sec": 81,
                "route": [
                    {
                        "from": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "to": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "is_cycleway": true,
                        "distance_m": 29.957
                    },
                    {
                        "from": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "to": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "is_cycleway": true,
                        "distance_m": 36.397
                    },
                    {
                        "from": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "to": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "is_cycleway": false,
                        "distance_m": 4.215
                    },
                    {
                        "from": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "to": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "is_cycleway": false,
                        "distance_m": 42.999
                    },
                    {
                        "from": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "to": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "is_cycleway": false,
                        "distance_m": 89.905
                    },
                    {
                        "from": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "to": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "is_cycleway": false,
                        "distance_m": 41.648
                    },
                    {
                        "from": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "to": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "is_cycleway": false,
                        "distance_m": 56.123
                    },
                    {
                        "from": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "to": {
                            "lat": 37.5012198,
                            "lon": 127.042809
                        },
                        "is_cycleway": false,
                        "distance_m": 37.964
                    }
                ],
                "instructions": [
                    {
                        "index": 0,
                        "location": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "distance_m": 114,
                        "action": "직진",
                        "message": "114m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 226,
                        "action": "직진",
                        "message": "226m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 114,
                        "action": "우회전",
                        "message": "소풍 앞에서 우회전하세요"
                    }
                ]
            },
            {
                "distance_m": 339.2,
                "estimated_time_sec": 81,
                "route": [
                    {
                        "from": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "to": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "is_cycleway": false,
                        "distance_m": 29.957
                    },
                    {
                        "from": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "to": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "is_cycleway": false,
                        "distance_m": 36.397
                    },
                    {
                        "from": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "to": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "is_cycleway": false,
                        "distance_m": 4.215
                    },
                    {
                        "from": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "to": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "is_cycleway": false,
                        "distance_m": 42.999
                    },
                    {
                        "from": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "to": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "is_cycleway": true,
                        "distance_m": 89.905
                    },
                    {
                        "from": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "to": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "is_cycleway": false,
                        "distance_m": 41.648
                    },
                    {
                        "from": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "to": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "is_cycleway": false,
                        "distance_m": 56.123
                    },
                    {
                        "from": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "to": {
                            "lat": 37.5012198,
                            "lon": 127.042809
                        },
                        "is_cycleway": true,
                        "distance_m": 37.964
                    }
                ],
                "instructions": [
                    {
                        "index": 0,
                        "location": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "distance_m": 114,
                        "action": "직진",
                        "message": "114m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 226,
                        "action": "직진",
                        "message": "226m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 114,
                        "action": "우회전",
                        "message": "소풍 앞에서 우회전하세요"
                    }
                ]
            },
            {
                "distance_m": 339.2,
                "estimated_time_sec": 81,
                "route": [
                    {
                        "from": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "to": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "is_cycleway": false,
                        "distance_m": 29.957
                    },
                    {
                        "from": {
                            "lat": 37.5012806,
                            "lon": 127.0400408
                        },
                        "to": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "is_cycleway": false,
                        "distance_m": 36.397
                    },
                    {
                        "from": {
                            "lat": 37.5009753,
                            "lon": 127.0401896
                        },
                        "to": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "is_cycleway": false,
                        "distance_m": 4.215
                    },
                    {
                        "from": {
                            "lat": 37.5009408,
                            "lon": 127.0402094
                        },
                        "to": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "is_cycleway": false,
                        "distance_m": 42.999
                    },
                    {
                        "from": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "to": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "is_cycleway": false,
                        "distance_m": 89.905
                    },
                    {
                        "from": {
                            "lat": 37.5008822,
                            "lon": 127.0413418
                        },
                        "to": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "is_cycleway": true,
                        "distance_m": 41.648
                    },
                    {
                        "from": {
                            "lat": 37.5010206,
                            "lon": 127.0417805
                        },
                        "to": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "is_cycleway": false,
                        "distance_m": 56.123
                    },
                    {
                        "from": {
                            "lat": 37.5010909,
                            "lon": 127.0424105
                        },
                        "to": {
                            "lat": 37.5012198,
                            "lon": 127.042809
                        },
                        "is_cycleway": false,
                        "distance_m": 37.964
                    }
                ],
                "instructions": [
                    {
                        "index": 0,
                        "location": {
                            "lat": 37.5015331,
                            "lon": 127.0399224
                        },
                        "distance_m": 114,
                        "action": "직진",
                        "message": "114m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 226,
                        "action": "직진",
                        "message": "226m 직진하세요"
                    },
                    {
                        "index": 4,
                        "location": {
                            "lat": 37.5005832,
                            "lon": 127.0403949
                        },
                        "distance_m": 114,
                        "action": "우회전",
                        "message": "소풍 앞에서 우회전하세요"
                    }
                ]
            }
]
    """.trimIndent()

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val response = json.decodeFromString<List<NavigationResponseDto>>(jsonString)
        return Response.success(response)
    }
}
