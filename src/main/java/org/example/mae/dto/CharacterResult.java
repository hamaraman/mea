package org.example.mae.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class CharacterResult {
    // 기본 정보
    private String nickname;
    private String characterClass;
    private int level;
    private String worldName;
    private String guildName;
    private String imageUrl;
    private String mainStatType;

    // 원본 스탯
    private long mainStat;
    private long subStat1;
    private long subStat2;
    private long attackOrMagic;
    private double damage;
    private double bossDamage;
    private double defIgnore;
    private double criticalDamage;
    private double finalDamage;

    // 환산 기여도 분해
    private long mainStatContrib;
    private long subStatContrib;
    private long atkContrib;
    private long damageContrib;
    private long bossDamageContrib;
    private long critContrib;
    private long finalDamageContrib;

    // 최종 환산 주스탯
    private long convertedMainStat;

    // ── 상세 정보 (단일 캐릭터 조회 시에만 포함) ──
    private List<ItemEquipmentEntry> equipment;
    private List<AbilityInfo> abilities;
    private String abilityGrade;
    private List<SymbolEntry> symbols;

    // 에러 처리
    private boolean hasError;
    private String error;
}
