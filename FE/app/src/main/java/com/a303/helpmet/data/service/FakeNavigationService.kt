package com.a303.helpmet.data.service

import com.a303.helpmet.data.dto.response.NavigationResponseDto
import com.a303.helpmet.data.network.api_services.ApiResult
import com.a303.helpmet.data.network.api_services.BaseResponse
import kotlinx.serialization.json.Json

class FakeNavigationService : NavigationService {
    override suspend fun getBikeNavigationRouteList(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double
    ): ApiResult<List<NavigationResponseDto>> {

        val jsonString = """
{
  "status": 200,
  "message": "success",
  "data": [
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
      "distance_m": 1103.2,
      "estimated_time_sec": 264,
      "route": [
        {
          "from": {
            "lat": 37.5112694,
            "lon": 126.9583675
          },
          "to": {
            "lat": 37.5114421,
            "lon": 126.9579235
          },
          "is_cycleway": false,
          "distance_m": 43.617
        },
        {
          "from": {
            "lat": 37.5114421,
            "lon": 126.9579235
          },
          "to": {
            "lat": 37.5115298,
            "lon": 126.9576863
          },
          "is_cycleway": false,
          "distance_m": 23.083
        },
        {
          "from": {
            "lat": 37.5115298,
            "lon": 126.9576863
          },
          "to": {
            "lat": 37.5115326,
            "lon": 126.957608
          },
          "is_cycleway": false,
          "distance_m": 6.913
        },
        {
          "from": {
            "lat": 37.5115326,
            "lon": 126.957608
          },
          "to": {
            "lat": 37.5115039,
            "lon": 126.9575384
          },
          "is_cycleway": false,
          "distance_m": 6.919
        },
        {
          "from": {
            "lat": 37.5115039,
            "lon": 126.9575384
          },
          "to": {
            "lat": 37.5114398,
            "lon": 126.9574872
          },
          "is_cycleway": false,
          "distance_m": 8.438
        },
        {
          "from": {
            "lat": 37.5114398,
            "lon": 126.9574872
          },
          "to": {
            "lat": 37.5113095,
            "lon": 126.9574832
          },
          "is_cycleway": false,
          "distance_m": 14.493
        },
        {
          "from": {
            "lat": 37.5113095,
            "lon": 126.9574832
          },
          "to": {
            "lat": 37.5112795,
            "lon": 126.9574707
          },
          "is_cycleway": false,
          "distance_m": 3.513
        },
        {
          "from": {
            "lat": 37.5112795,
            "lon": 126.9574707
          },
          "to": {
            "lat": 37.5112732,
            "lon": 126.9574231
          },
          "is_cycleway": false,
          "distance_m": 4.257
        },
        {
          "from": {
            "lat": 37.5112732,
            "lon": 126.9574231
          },
          "to": {
            "lat": 37.5112833,
            "lon": 126.957359
          },
          "is_cycleway": false,
          "distance_m": 5.764
        },
        {
          "from": {
            "lat": 37.5112833,
            "lon": 126.957359
          },
          "to": {
            "lat": 37.5113379,
            "lon": 126.9572744
          },
          "is_cycleway": false,
          "distance_m": 9.62
        },
        {
          "from": {
            "lat": 37.5113379,
            "lon": 126.9572744
          },
          "to": {
            "lat": 37.5111206,
            "lon": 126.9571245
          },
          "is_cycleway": false,
          "distance_m": 27.544
        },
        {
          "from": {
            "lat": 37.5111206,
            "lon": 126.9571245
          },
          "to": {
            "lat": 37.5109986,
            "lon": 126.9569553
          },
          "is_cycleway": false,
          "distance_m": 20.168
        },
        {
          "from": {
            "lat": 37.5109986,
            "lon": 126.9569553
          },
          "to": {
            "lat": 37.5108875,
            "lon": 126.9568668
          },
          "is_cycleway": false,
          "distance_m": 14.613
        },
        {
          "from": {
            "lat": 37.5108875,
            "lon": 126.9568668
          },
          "to": {
            "lat": 37.5106711,
            "lon": 126.9567596
          },
          "is_cycleway": false,
          "distance_m": 25.854
        },
        {
          "from": {
            "lat": 37.5106711,
            "lon": 126.9567596
          },
          "to": {
            "lat": 37.5105805,
            "lon": 126.9567293
          },
          "is_cycleway": false,
          "distance_m": 10.423
        },
        {
          "from": {
            "lat": 37.5105805,
            "lon": 126.9567293
          },
          "to": {
            "lat": 37.5105084,
            "lon": 126.956694
          },
          "is_cycleway": false,
          "distance_m": 8.601
        },
        {
          "from": {
            "lat": 37.5105084,
            "lon": 126.956694
          },
          "to": {
            "lat": 37.5103983,
            "lon": 126.9568076
          },
          "is_cycleway": false,
          "distance_m": 15.82
        },
        {
          "from": {
            "lat": 37.5103983,
            "lon": 126.9568076
          },
          "to": {
            "lat": 37.51033,
            "lon": 126.9567677
          },
          "is_cycleway": false,
          "distance_m": 8.37
        },
        {
          "from": {
            "lat": 37.51033,
            "lon": 126.9567677
          },
          "to": {
            "lat": 37.5103373,
            "lon": 126.9566323
          },
          "is_cycleway": false,
          "distance_m": 11.97
        },
        {
          "from": {
            "lat": 37.5103373,
            "lon": 126.9566323
          },
          "to": {
            "lat": 37.5103642,
            "lon": 126.9565123
          },
          "is_cycleway": false,
          "distance_m": 10.999
        },
        {
          "from": {
            "lat": 37.5103642,
            "lon": 126.9565123
          },
          "to": {
            "lat": 37.5104154,
            "lon": 126.9563277
          },
          "is_cycleway": false,
          "distance_m": 17.249
        },
        {
          "from": {
            "lat": 37.5104154,
            "lon": 126.9563277
          },
          "to": {
            "lat": 37.5104447,
            "lon": 126.9561309
          },
          "is_cycleway": false,
          "distance_m": 17.662
        },
        {
          "from": {
            "lat": 37.5104447,
            "lon": 126.9561309
          },
          "to": {
            "lat": 37.5104032,
            "lon": 126.9560509
          },
          "is_cycleway": false,
          "distance_m": 8.431
        },
        {
          "from": {
            "lat": 37.5104032,
            "lon": 126.9560509
          },
          "to": {
            "lat": 37.5101782,
            "lon": 126.9559452
          },
          "is_cycleway": false,
          "distance_m": 26.7
        },
        {
          "from": {
            "lat": 37.5101782,
            "lon": 126.9559452
          },
          "to": {
            "lat": 37.5100299,
            "lon": 126.9558755
          },
          "is_cycleway": false,
          "distance_m": 17.599
        },
        {
          "from": {
            "lat": 37.5100299,
            "lon": 126.9558755
          },
          "to": {
            "lat": 37.5099127,
            "lon": 126.9558448
          },
          "is_cycleway": false,
          "distance_m": 13.31
        },
        {
          "from": {
            "lat": 37.5099127,
            "lon": 126.9558448
          },
          "to": {
            "lat": 37.5097322,
            "lon": 126.9557217
          },
          "is_cycleway": false,
          "distance_m": 22.82
        },
        {
          "from": {
            "lat": 37.5097322,
            "lon": 126.9557217
          },
          "to": {
            "lat": 37.5096509,
            "lon": 126.9556501
          },
          "is_cycleway": false,
          "distance_m": 11.028
        },
        {
          "from": {
            "lat": 37.5096509,
            "lon": 126.9556501
          },
          "to": {
            "lat": 37.5094717,
            "lon": 126.9558223
          },
          "is_cycleway": false,
          "distance_m": 25.055
        },
        {
          "from": {
            "lat": 37.5094717,
            "lon": 126.9558223
          },
          "to": {
            "lat": 37.5094145,
            "lon": 126.9558772
          },
          "is_cycleway": false,
          "distance_m": 7.994
        },
        {
          "from": {
            "lat": 37.5094145,
            "lon": 126.9558772
          },
          "to": {
            "lat": 37.509308,
            "lon": 126.9560773
          },
          "is_cycleway": false,
          "distance_m": 21.255
        },
        {
          "from": {
            "lat": 37.509308,
            "lon": 126.9560773
          },
          "to": {
            "lat": 37.5092582,
            "lon": 126.9561707
          },
          "is_cycleway": false,
          "distance_m": 9.927
        },
        {
          "from": {
            "lat": 37.5092582,
            "lon": 126.9561707
          },
          "to": {
            "lat": 37.5090727,
            "lon": 126.9563434
          },
          "is_cycleway": false,
          "distance_m": 25.642
        },
        {
          "from": {
            "lat": 37.5090727,
            "lon": 126.9563434
          },
          "to": {
            "lat": 37.5090119,
            "lon": 126.9564116
          },
          "is_cycleway": false,
          "distance_m": 9.05
        },
        {
          "from": {
            "lat": 37.5090119,
            "lon": 126.9564116
          },
          "to": {
            "lat": 37.5088952,
            "lon": 126.9565129
          },
          "is_cycleway": false,
          "distance_m": 15.755
        },
        {
          "from": {
            "lat": 37.5088952,
            "lon": 126.9565129
          },
          "to": {
            "lat": 37.5087832,
            "lon": 126.9566431
          },
          "is_cycleway": false,
          "distance_m": 16.941
        },
        {
          "from": {
            "lat": 37.5087832,
            "lon": 126.9566431
          },
          "to": {
            "lat": 37.5086942,
            "lon": 126.9567962
          },
          "is_cycleway": false,
          "distance_m": 16.742
        },
        {
          "from": {
            "lat": 37.5086942,
            "lon": 126.9567962
          },
          "to": {
            "lat": 37.508654,
            "lon": 126.9568649
          },
          "is_cycleway": false,
          "distance_m": 7.53
        },
        {
          "from": {
            "lat": 37.508654,
            "lon": 126.9568649
          },
          "to": {
            "lat": 37.5086047,
            "lon": 126.9571096
          },
          "is_cycleway": false,
          "distance_m": 22.269
        },
        {
          "from": {
            "lat": 37.5086047,
            "lon": 126.9571096
          },
          "to": {
            "lat": 37.5086005,
            "lon": 126.9572078
          },
          "is_cycleway": false,
          "distance_m": 8.674
        },
        {
          "from": {
            "lat": 37.5086005,
            "lon": 126.9572078
          },
          "to": {
            "lat": 37.508609,
            "lon": 126.957325
          },
          "is_cycleway": false,
          "distance_m": 10.381
        },
        {
          "from": {
            "lat": 37.508609,
            "lon": 126.957325
          },
          "to": {
            "lat": 37.5080638,
            "lon": 126.9580667
          },
          "is_cycleway": false,
          "distance_m": 89.193
        },
        {
          "from": {
            "lat": 37.5080638,
            "lon": 126.9580667
          },
          "to": {
            "lat": 37.5079538,
            "lon": 126.9586501
          },
          "is_cycleway": false,
          "distance_m": 52.894
        },
        {
          "from": {
            "lat": 37.5079538,
            "lon": 126.9586501
          },
          "to": {
            "lat": 37.5078736,
            "lon": 126.9588011
          },
          "is_cycleway": false,
          "distance_m": 16.029
        },
        {
          "from": {
            "lat": 37.5078736,
            "lon": 126.9588011
          },
          "to": {
            "lat": 37.5078353,
            "lon": 126.9588882
          },
          "is_cycleway": false,
          "distance_m": 8.784
        },
        {
          "from": {
            "lat": 37.5078353,
            "lon": 126.9588882
          },
          "to": {
            "lat": 37.507786,
            "lon": 126.9591001
          },
          "is_cycleway": false,
          "distance_m": 19.479
        },
        {
          "from": {
            "lat": 37.507786,
            "lon": 126.9591001
          },
          "to": {
            "lat": 37.5077596,
            "lon": 126.9593711
          },
          "is_cycleway": false,
          "distance_m": 24.084
        },
        {
          "from": {
            "lat": 37.5077596,
            "lon": 126.9593711
          },
          "to": {
            "lat": 37.5077447,
            "lon": 126.9595249
          },
          "is_cycleway": false,
          "distance_m": 13.667
        },
        {
          "from": {
            "lat": 37.5077447,
            "lon": 126.9595249
          },
          "to": {
            "lat": 37.5077022,
            "lon": 126.9597449
          },
          "is_cycleway": false,
          "distance_m": 19.973
        },
        {
          "from": {
            "lat": 37.5077022,
            "lon": 126.9597449
          },
          "to": {
            "lat": 37.50754,
            "lon": 126.9601302
          },
          "is_cycleway": false,
          "distance_m": 38.476
        },
        {
          "from": {
            "lat": 37.50754,
            "lon": 126.9601302
          },
          "to": {
            "lat": 37.5075028,
            "lon": 126.9601712
          },
          "is_cycleway": false,
          "distance_m": 5.495
        },
        {
          "from": {
            "lat": 37.5075028,
            "lon": 126.9601712
          },
          "to": {
            "lat": 37.5075335,
            "lon": 126.9602617
          },
          "is_cycleway": false,
          "distance_m": 8.682
        },
        {
          "from": {
            "lat": 37.5075335,
            "lon": 126.9602617
          },
          "to": {
            "lat": 37.5076214,
            "lon": 126.9605607
          },
          "is_cycleway": false,
          "distance_m": 28.127
        },
        {
          "from": {
            "lat": 37.5076214,
            "lon": 126.9605607
          },
          "to": {
            "lat": 37.5076561,
            "lon": 126.9606795
          },
          "is_cycleway": false,
          "distance_m": 11.167
        },
        {
          "from": {
            "lat": 37.5076561,
            "lon": 126.9606795
          },
          "to": {
            "lat": 37.5076757,
            "lon": 126.9607699
          },
          "is_cycleway": false,
          "distance_m": 8.266
        },
        {
          "from": {
            "lat": 37.5076757,
            "lon": 126.9607699
          },
          "to": {
            "lat": 37.5077193,
            "lon": 126.9610149
          },
          "is_cycleway": false,
          "distance_m": 22.148
        },
        {
          "from": {
            "lat": 37.5077193,
            "lon": 126.9610149
          },
          "to": {
            "lat": 37.507686,
            "lon": 126.9611611
          },
          "is_cycleway": false,
          "distance_m": 13.417
        },
        {
          "from": {
            "lat": 37.507686,
            "lon": 126.9611611
          },
          "to": {
            "lat": 37.5075828,
            "lon": 126.9615342
          },
          "is_cycleway": false,
          "distance_m": 34.854
        },
        {
          "from": {
            "lat": 37.5075828,
            "lon": 126.9615342
          },
          "to": {
            "lat": 37.5074181,
            "lon": 126.9618267
          },
          "is_cycleway": false,
          "distance_m": 31.64
        },
        {
          "from": {
            "lat": 37.5074181,
            "lon": 126.9618267
          },
          "to": {
            "lat": 37.5076599,
            "lon": 126.9622188
          },
          "is_cycleway": false,
          "distance_m": 43.808
        }
      ],
      "instructions": [
        {
          "index": 0,
          "location": {
            "lat": 37.5112694,
            "lon": 126.9583675
          },
          "distance_m": 89,
          "action": "직진",
          "message": "89m 직진하세요"
        },
        {
          "index": 5,
          "location": {
            "lat": 37.5114398,
            "lon": 126.9574872
          },
          "distance_m": 18,
          "action": "직진",
          "message": "18m 직진하세요"
        },
        {
          "index": 5,
          "location": {
            "lat": 37.5114398,
            "lon": 126.9574872
          },
          "distance_m": 89,
          "action": "우회전",
          "message": "구립동작실버센터 앞에서 우회전하세요"
        },
        {
          "index": 7,
          "location": {
            "lat": 37.5112795,
            "lon": 126.9574707
          },
          "distance_m": 20,
          "action": "직진",
          "message": "20m 직진하세요"
        },
        {
          "index": 7,
          "location": {
            "lat": 37.5112795,
            "lon": 126.9574707
          },
          "distance_m": 107,
          "action": "좌회전",
          "message": "구립동작실버센터 앞에서 좌회전하세요"
        },
        {
          "index": 10,
          "location": {
            "lat": 37.5113379,
            "lon": 126.9572744
          },
          "distance_m": 107,
          "action": "직진",
          "message": "107m 직진하세요"
        },
        {
          "index": 10,
          "location": {
            "lat": 37.5113379,
            "lon": 126.9572744
          },
          "distance_m": 127,
          "action": "우회전",
          "message": "구립동작실버센터 앞에서 우회전하세요"
        },
        {
          "index": 16,
          "location": {
            "lat": 37.5105084,
            "lon": 126.956694
          },
          "distance_m": 16,
          "action": "직진",
          "message": "16m 직진하세요"
        },
        {
          "index": 16,
          "location": {
            "lat": 37.5105084,
            "lon": 126.956694
          },
          "distance_m": 234,
          "action": "우회전",
          "message": "동양중학교 앞에서 우회전하세요"
        },
        {
          "index": 17,
          "location": {
            "lat": 37.5103983,
            "lon": 126.9568076
          },
          "distance_m": 8,
          "action": "직진",
          "message": "8m 직진하세요"
        },
        {
          "index": 17,
          "location": {
            "lat": 37.5103983,
            "lon": 126.9568076
          },
          "distance_m": 250,
          "action": "좌회전",
          "message": "동양중학교 앞에서 좌회전하세요"
        },
        {
          "index": 18,
          "location": {
            "lat": 37.51033,
            "lon": 126.9567677
          },
          "distance_m": 58,
          "action": "직진",
          "message": "58m 직진하세요"
        },
        {
          "index": 18,
          "location": {
            "lat": 37.51033,
            "lon": 126.9567677
          },
          "distance_m": 258,
          "action": "좌회전",
          "message": "동양중학교 앞에서 좌회전하세요"
        },
        {
          "index": 22,
          "location": {
            "lat": 37.5104447,
            "lon": 126.9561309
          },
          "distance_m": 8,
          "action": "직진",
          "message": "8m 직진하세요"
        },
        {
          "index": 22,
          "location": {
            "lat": 37.5104447,
            "lon": 126.9561309
          },
          "distance_m": 316,
          "action": "우회전",
          "message": "동양중학교 앞에서 우회전하세요"
        },
        {
          "index": 23,
          "location": {
            "lat": 37.5104032,
            "lon": 126.9560509
          },
          "distance_m": 91,
          "action": "직진",
          "message": "91m 직진하세요"
        },
        {
          "index": 23,
          "location": {
            "lat": 37.5104032,
            "lon": 126.9560509
          },
          "distance_m": 324,
          "action": "우회전",
          "message": "동양중학교 앞에서 우회전하세요"
        },
        {
          "index": 28,
          "location": {
            "lat": 37.5096509,
            "lon": 126.9556501
          },
          "distance_m": 197,
          "action": "직진",
          "message": "197m 직진하세요"
        },
        {
          "index": 28,
          "location": {
            "lat": 37.5096509,
            "lon": 126.9556501
          },
          "distance_m": 416,
          "action": "우회전",
          "message": "동양중학교 앞에서 우회전하세요"
        },
        {
          "index": 41,
          "location": {
            "lat": 37.508609,
            "lon": 126.957325
          },
          "distance_m": 288,
          "action": "직진",
          "message": "288m 직진하세요"
        },
        {
          "index": 41,
          "location": {
            "lat": 37.508609,
            "lon": 126.957325
          },
          "distance_m": 613,
          "action": "좌회전",
          "message": "약 613m 앞에서 좌회전하세요"
        },
        {
          "index": 51,
          "location": {
            "lat": 37.5075028,
            "lon": 126.9601712
          },
          "distance_m": 158,
          "action": "직진",
          "message": "158m 직진하세요"
        },
        {
          "index": 51,
          "location": {
            "lat": 37.5075028,
            "lon": 126.9601712
          },
          "distance_m": 901,
          "action": "우회전",
          "message": "백소정 중앙대점 앞에서 우회전하세요"
        },
        {
          "index": 59,
          "location": {
            "lat": 37.5074181,
            "lon": 126.9618267
          },
          "distance_m": 44,
          "action": "직진",
          "message": "44m 직진하세요"
        },
        {
          "index": 59,
          "location": {
            "lat": 37.5074181,
            "lon": 126.9618267
          },
          "distance_m": 1059,
          "action": "우회전",
          "message": "명수대아파트 앞에서 우회전하세요"
        }
      ]
    }
  ]
}
    """.trimIndent()

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val response = json.decodeFromString<BaseResponse<List<NavigationResponseDto>>>(jsonString)
        return ApiResult.Success(response.data)
    }
}
