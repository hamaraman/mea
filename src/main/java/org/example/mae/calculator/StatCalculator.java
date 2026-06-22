package org.example.mae.calculator;

import org.example.mae.dto.CharacterBasicResponse;
import org.example.mae.dto.CharacterResult;
import org.example.mae.dto.CharacterStatResponse;
import org.example.mae.dto.StatEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.mae.calculator.ConversionConstants.*;

public class StatCalculator {

    public static CharacterResult calculate(
            String nickname,
            CharacterBasicResponse basic,
            CharacterStatResponse stat) {

        String characterClass = basic.getCharacterClass();
        String statType = JobGroup.getStatType(characterClass);

        Map<String, Double> statMap = parseStatMap(stat.getFinalStat());

        double str  = statMap.getOrDefault("STR", 0.0);
        double dex  = statMap.getOrDefault("DEX", 0.0);
        double intV = statMap.getOrDefault("INT", 0.0);
        double luk  = statMap.getOrDefault("LUK", 0.0);
        double hp   = statMap.getOrDefault("최대 HP", 0.0);
        double atk  = statMap.getOrDefault("공격력", 0.0);
        double matk = statMap.getOrDefault("마력", 0.0);

        // 이미 % 단위로 저장된 스탯 (예: "100.00" → 100.0%)
        double damage    = statMap.getOrDefault("데미지", 0.0);
        double bossDmg   = statMap.getOrDefault("보스 몬스터 데미지", 0.0);
        double defIgnore = statMap.getOrDefault("방어율 무시", 0.0);
        double critDmg   = statMap.getOrDefault("크리티컬 데미지", 0.0);
        double finalDmg  = statMap.getOrDefault("최종 데미지", 0.0);

        long mainStat, subStat1, subStat2, atkVal;
        String mainStatType;

        switch (statType) {
            case "STR"     -> { mainStat = (long) str;  subStat1 = (long) dex;  subStat2 = 0;      atkVal = (long) atk;  mainStatType = "STR"; }
            case "DEX"     -> { mainStat = (long) dex;  subStat1 = (long) str;  subStat2 = 0;      atkVal = (long) atk;  mainStatType = "DEX"; }
            case "INT"     -> { mainStat = (long) intV; subStat1 = (long) luk;  subStat2 = 0;      atkVal = (long) matk; mainStatType = "INT"; }
            case "LUK"     -> { mainStat = (long) luk;  subStat1 = (long) dex;  subStat2 = (long) str; atkVal = (long) atk; mainStatType = "LUK"; }
            case "ALL"     -> { mainStat = (long)(str + dex + luk); subStat1 = 0; subStat2 = 0; atkVal = (long) atk; mainStatType = "STR+DEX+LUK"; }
            case "HP"      -> { mainStat = (long)(hp / 100); subStat1 = (long) str; subStat2 = 0; atkVal = (long) atk; mainStatType = "HP(/100)"; }
            default        -> { mainStat = (long) str;  subStat1 = (long) dex;  subStat2 = 0;      atkVal = (long) atk;  mainStatType = "STR"; }
        }

        // BASE = 주스탯 + 부스탯 환산 + 공마 환산
        long subStatContrib = (subStat1 + subStat2) / SUB_STAT_RATIO;
        long atkContrib     = atkVal * ATK_TO_STAT;
        long base           = mainStat + subStatContrib + atkContrib;

        // 각 배율 스탯 기여도 (BASE 기준 additive 근사)
        long damageContrib    = Math.round(base * (damage    / 100.0) * DAMAGE_WEIGHT);
        long bossDmgContrib   = Math.round(base * (bossDmg   / 100.0) * BOSS_DMG_WEIGHT);
        long critContrib      = Math.round(base * (critDmg   / 100.0) * CRIT_DMG_WEIGHT);
        long finalDmgContrib  = Math.round(base * (finalDmg  / 100.0) * FINAL_DMG_WEIGHT);

        long total = base + damageContrib + bossDmgContrib + critContrib + finalDmgContrib;

        return CharacterResult.builder()
                .nickname(nickname)
                .characterClass(characterClass)
                .level(basic.getCharacterLevel())
                .worldName(basic.getWorldName())
                .imageUrl(basic.getCharacterImage())
                .mainStatType(mainStatType)
                .mainStat(mainStat)
                .subStat1(subStat1)
                .subStat2(subStat2)
                .attackOrMagic(atkVal)
                .damage(damage)
                .bossDamage(bossDmg)
                .defIgnore(defIgnore)
                .criticalDamage(critDmg)
                .finalDamage(finalDmg)
                .mainStatContrib(mainStat)
                .subStatContrib(subStatContrib)
                .atkContrib(atkContrib)
                .damageContrib(damageContrib)
                .bossDamageContrib(bossDmgContrib)
                .critContrib(critContrib)
                .finalDamageContrib(finalDmgContrib)
                .convertedMainStat(total)
                .hasError(false)
                .build();
    }

    /** stat_value 파싱: 쉼표 제거 후 double 변환 */
    private static Map<String, Double> parseStatMap(List<StatEntry> entries) {
        Map<String, Double> map = new HashMap<>();
        if (entries == null) return map;
        for (StatEntry entry : entries) {
            try {
                String raw = entry.getStatValue();
                if (raw == null || raw.isBlank()) continue;
                raw = raw.replace(",", "").replace("%", "").trim();
                map.put(entry.getStatName(), Double.parseDouble(raw));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }
}
