package org.example.mae.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mae.calculator.StatCalculator;
import org.example.mae.client.NexonApiClient;
import org.example.mae.dto.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final NexonApiClient api;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 단일 캐릭터 전체 상세 조회 (장비·어빌리티·심볼 포함)
     * GET /api/character?nickname=X
     */
    public CharacterResult getCharacterDetail(String nickname) {
        String ocid = api.getOcid(nickname);

        // 기본 정보 / 스탯 / 장비 / 어빌리티 / 심볼 병렬 조회
        var basicF    = CompletableFuture.supplyAsync(() -> api.getBasicInfo(ocid), executor);
        var statF     = CompletableFuture.supplyAsync(() -> api.getStatInfo(ocid), executor);
        var equipF    = CompletableFuture.supplyAsync(() -> safeCall(() -> api.getEquipment(ocid)), executor);
        var abilityF  = CompletableFuture.supplyAsync(() -> safeCall(() -> api.getAbility(ocid)), executor);
        var symbolF   = CompletableFuture.supplyAsync(() -> safeCall(() -> api.getSymbol(ocid)), executor);

        CharacterBasicResponse   basic   = basicF.join();
        CharacterStatResponse    stat    = statF.join();
        ItemEquipmentResponse    equip   = equipF.join();
        AbilityResponse          ability = abilityF.join();
        SymbolResponse           symbol  = symbolF.join();

        return StatCalculator.calculate(nickname, basic, stat).toBuilder()
                .equipment(equip   != null ? equip.getItemEquipment()  : List.of())
                .abilities(ability != null ? ability.getAbilityInfo()  : List.of())
                .abilityGrade(ability != null ? ability.getAbilityGrade() : null)
                .symbols(symbol  != null ? symbol.getSymbols()       : List.of())
                .build();
    }

    /**
     * 다중 캐릭터 비교 (기본 스탯만 조회)
     * POST /api/compare
     */
    public List<CharacterResult> compareCharacters(List<String> nicknames) {
        return nicknames.stream()
                .map(this::fetchSummary)
                .sorted(Comparator.comparingLong(CharacterResult::getConvertedMainStat).reversed())
                .toList();
    }

    // ── 내부 헬퍼 ──

    private CharacterResult fetchSummary(String nickname) {
        try {
            String ocid = api.getOcid(nickname);
            CharacterBasicResponse basic = api.getBasicInfo(ocid);
            CharacterStatResponse  stat  = api.getStatInfo(ocid);
            return StatCalculator.calculate(nickname, basic, stat);
        } catch (NexonApiClient.NexonApiException e) {
            log.warn("비교 조회 실패 [{}]: {}", nickname, e.getMessage());
            return errorResult(nickname, e.getMessage());
        } catch (Exception e) {
            log.error("비교 조회 오류 [{}]", nickname, e);
            return errorResult(nickname, "서버 오류가 발생했습니다.");
        }
    }

    private <T> T safeCall(java.util.function.Supplier<T> supplier) {
        try { return supplier.get(); }
        catch (Exception e) { log.warn("부가 정보 조회 실패: {}", e.getMessage()); return null; }
    }

    private CharacterResult errorResult(String nickname, String msg) {
        return CharacterResult.builder()
                .nickname(nickname).hasError(true).error(msg).convertedMainStat(0).build();
    }
}
