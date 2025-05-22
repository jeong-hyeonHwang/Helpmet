## Gitlab 소스 클론 이후 빌드 및 배포할 수 있도록 정리한 문서

### FE(AOS)
- IDE: `Android Studio Meerkat(2024.3.2)`


- 환경변수: `local.properties` 파일에 아래의 값들을 넣습니다.

```
// ...
kakao.map.api.key={카카오로부터 발급받은 API 키}
backend.api.host={백엔드 서버 IP 주소}
helpmet_password={RASBERRY PI PASSWORD}
socket.port={PORT NUMBER}
```

- 비고: 앱 실행 시 개발자 옵션에서 `항상 모바일 데이터 활성화` 값을 true로 전환해야 합니다.

---

### BE
- IDE: `VS Code`

- 환경변수: `.env` 파일에 아래의 값을 넣습니다.
```
# database
DATABASE_URL = "postgresql+asyncpg://postgres:helmeta303@k12a303.p.ssafy.io:5432/helmet_db"

```

- 비고: BE/data/ 경로에 `time_assigned.graphml` 파일이 있어야 합니다.

---

### AI

---

### Rasberry PI