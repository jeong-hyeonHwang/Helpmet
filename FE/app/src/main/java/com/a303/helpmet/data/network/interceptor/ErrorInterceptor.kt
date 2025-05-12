package com.a303.helpmet.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 서버 응답에 특정 에러 코드가 있으면
 * 전역 에러 핸들러로 전달하는 로직을 수행하는 용도로 사용합니다.
 */
class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request  = chain.request()
        val response = chain.proceed(request)

        // 예시: HTTP 401 같은 인증 오류 처리
        if (response.code == 401) {
            // 토큰 만료 등을 감지해서 처리
            // GlobalErrorHandler.emitError("AUTH_REQUIRED")
        }

        // 추가로 응답 바디 JSON 내부의 에러 코드 파싱 로직을 넣어도 되고,
        // 필요한 만큼 여기에 구현하시면 됩니다.

        return response
    }
}
