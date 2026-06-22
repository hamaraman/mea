package org.example.mae.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mae.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NexonApiClient {

    private final RestTemplate restTemplate;

    @Value("${nexon.api.key}")
    private String apiKey;

    @Value("${nexon.api.base-url}")
    private String baseUrl;

    private static final Pattern ERROR_NAME_PATTERN    = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");

    // ===== API 메서드 =====

    public String getOcid(String characterName) {
        return get(baseUrl + "/maplestory/v1/id?character_name={n}",
                OcidResponse.class, Map.of("n", characterName)).getOcid();
    }

    public CharacterBasicResponse getBasicInfo(String ocid) {
        return get(baseUrl + "/maplestory/v1/character/basic?ocid={o}",
                CharacterBasicResponse.class, Map.of("o", ocid));
    }

    public CharacterStatResponse getStatInfo(String ocid) {
        return get(baseUrl + "/maplestory/v1/character/stat?ocid={o}",
                CharacterStatResponse.class, Map.of("o", ocid));
    }

    public ItemEquipmentResponse getEquipment(String ocid) {
        return get(baseUrl + "/maplestory/v1/character/item-equipment?ocid={o}",
                ItemEquipmentResponse.class, Map.of("o", ocid));
    }

    public AbilityResponse getAbility(String ocid) {
        return get(baseUrl + "/maplestory/v1/character/ability?ocid={o}",
                AbilityResponse.class, Map.of("o", ocid));
    }

    public SymbolResponse getSymbol(String ocid) {
        return get(baseUrl + "/maplestory/v1/character/symbol-equipment?ocid={o}",
                SymbolResponse.class, Map.of("o", ocid));
    }

    // ===== 공통 GET =====

    private <T> T get(String urlTemplate, Class<T> responseType, Map<String, ?> vars) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-nxopen-api-key", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                return restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, responseType, vars).getBody();
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429 && attempt < 4) {
                    log.warn("429 Rate limited, {}초 후 재시도 ({}회차)", attempt, attempt);
                    try { Thread.sleep(attempt * 1000L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
                    continue;
                }
                log.warn("NEXON API 4xx [{}] body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new NexonApiException(parseNexonError(e.getResponseBodyAsString(), e.getStatusCode().value()));
            } catch (HttpServerErrorException e) {
                log.error("NEXON API 5xx [{}]", e.getStatusCode());
                throw new NexonApiException(parseNexonError(e.getResponseBodyAsString(), e.getStatusCode().value()));
            }
        }
        throw new NexonApiException("API 호출량 초과. 잠시 후 다시 시도해 주세요.");
    }

    // ===== 에러 파싱 =====

    private String parseNexonError(String body, int httpStatus) {
        if (body != null && !body.isBlank()) {
            String name    = extract(ERROR_NAME_PATTERN, body);
            String message = extract(ERROR_MESSAGE_PATTERN, body);
            if (name != null) return formatError(name, message);
        }
        return fallback(httpStatus);
    }

    private static String extract(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private String formatError(String code, String msg) {
        return switch (code) {
            case "OPENAPI00001" -> "서버 내부 오류입니다. 잠시 후 다시 시도해 주세요.";
            case "OPENAPI00002" -> "접근 권한이 없습니다.";
            case "OPENAPI00003" -> "유효하지 않은 캐릭터 식별자입니다.";
            case "OPENAPI00004" -> "캐릭터를 찾을 수 없습니다: " + (msg != null ? msg : "닉네임을 확인해 주세요.");
            case "OPENAPI00005" -> "API KEY가 유효하지 않습니다. 환경변수 MAPLESTORY_API_KEY를 확인해 주세요.";
            case "OPENAPI00006" -> "유효하지 않은 API 경로입니다.";
            case "OPENAPI00007" -> "API 호출량 초과. 잠시 후 다시 시도해 주세요.";
            case "OPENAPI00009" -> "데이터 준비 중입니다. 잠시 후 다시 시도해 주세요.";
            case "OPENAPI00010" -> "게임 점검 중입니다.";
            case "OPENAPI00011" -> "API 점검 중입니다. 잠시 후 다시 시도해 주세요.";
            default -> msg != null ? msg : "알 수 없는 오류 [" + code + "]";
        };
    }

    private String fallback(int status) {
        return switch (status) {
            case 400 -> "잘못된 요청입니다. 닉네임을 확인해 주세요.";
            case 401 -> "인증 실패: API KEY를 확인해 주세요.";
            case 403 -> "접근 권한이 없습니다.";
            case 429 -> "API 호출량 초과. 잠시 후 다시 시도해 주세요.";
            case 503 -> "서비스 점검 중입니다.";
            default  -> "NEXON API 오류 (HTTP " + status + ")";
        };
    }

    public static class NexonApiException extends RuntimeException {
        public NexonApiException(String message) { super(message); }
    }
}
