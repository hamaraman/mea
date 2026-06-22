package org.example.mae.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mae.client.NexonApiClient;
import org.example.mae.dto.CharacterResult;
import org.example.mae.dto.CompareRequest;
import org.example.mae.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    /** 단일 캐릭터 전체 상세 (장비·어빌리티·심볼 포함) */
    @GetMapping("/character")
    public ResponseEntity<CharacterResult> getCharacter(@RequestParam String nickname) {
        log.info("상세 조회: {}", nickname);
        return ResponseEntity.ok(characterService.getCharacterDetail(nickname.trim()));
    }

    /** 다중 캐릭터 환산 비교 */
    @PostMapping("/compare")
    public ResponseEntity<List<CharacterResult>> compare(@RequestBody CompareRequest request) {
        List<String> nicknames = request.getNicknames();
        if (nicknames == null || nicknames.isEmpty()) return ResponseEntity.badRequest().build();
        log.info("비교 조회: {}", nicknames);
        return ResponseEntity.ok(characterService.compareCharacters(nicknames));
    }

    @ExceptionHandler(NexonApiClient.NexonApiException.class)
    public ResponseEntity<Map<String, String>> handleNexon(NexonApiClient.NexonApiException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        log.error("컨트롤러 오류", e);
        return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다."));
    }
}
