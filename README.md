# 메이플스토리 환산 주스탯 계산기

NEXON Open API를 활용한 캐릭터 환산 주스탯 계산 및 비교 웹사이트입니다.

## 기술 스택

- **백엔드**: Java 21 + Spring Boot 4.x + Gradle 9
- **프론트엔드**: HTML / CSS / Vanilla JavaScript (프레임워크 없음)
- **외부 API**: NEXON Open API (MapleStory)

## 프로젝트 구조

```
mae/
├── src/
│   └── main/
│       ├── java/org/example/mae/
│       │   ├── calculator/          # 환산 계산 로직
│       │   │   ├── ConversionConstants.java  # 계산 상수 (수정 가능)
│       │   │   ├── JobGroup.java             # 직업별 주스탯 매핑
│       │   │   └── StatCalculator.java       # 환산 공식 구현
│       │   ├── client/
│       │   │   └── NexonApiClient.java       # NEXON API 호출
│       │   ├── config/
│       │   │   └── WebConfig.java            # CORS + RestTemplate 설정
│       │   ├── controller/
│       │   │   └── CharacterController.java  # REST 엔드포인트
│       │   ├── dto/                          # 데이터 전송 객체
│       │   └── service/
│       │       └── CharacterService.java     # 비즈니스 로직
│       └── resources/
│           ├── application.properties
│           └── static/              # 프론트엔드 (Spring Boot가 서빙)
│               ├── index.html
│               ├── css/style.css
│               └── js/app.js
└── build.gradle
```

## 실행 방법

### 1. 사전 준비

- Java 21 이상
- NEXON Open API 키 발급: https://openapi.nexon.com

### 2. API 키 설정

**환경변수 방식 (권장):**

```bash
# Windows PowerShell
$env:MAPLESTORY_API_KEY = "발급받은_API_KEY"

# Windows CMD
set MAPLESTORY_API_KEY=발급받은_API_KEY

# Linux / macOS
export MAPLESTORY_API_KEY=발급받은_API_KEY
```

### 3. 빌드 및 실행

```bash
# Windows
./gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

서버 시작 후 브라우저에서 `http://localhost:8080` 접속

## API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/character?nickname=닉네임` | 단일 캐릭터 환산 결과 |
| POST | `/api/compare` | 여러 캐릭터 비교 (`{"nicknames": ["A", "B"]}`) |

## 환산 주스탯 계산 공식

> 커뮤니티 기반 근사 공식 (`ConversionConstants.java`에서 상수 수정 가능)

```
BASE = 주스탯 + 부스탯 ÷ 4 + 공격력(마력) × 4

환산 주스탯 = BASE
            + BASE × 데미지%       (× 1.0)
            + BASE × 보스데미지%   (× 1.0)
            + BASE × 크리티컬%     (× 0.5, 크리확률 50% 가정)
            + BASE × 최종데미지%   (× 1.0)
```

- 방어율 무시는 곱연산 구조로 환산이 복잡하여 표시만 제공
- 직업군 판별 → `JobGroup.java` 매핑 기준 (없는 직업은 STR 기본)
- NEXON API는 전날 스탯 기준으로 데이터를 제공합니다

## 참고

- 데이터 기준: NEXON Open API `final_stat` (전날 기준)
- 계산 상수는 `ConversionConstants.java`에서 자유롭게 조정 가능
