package org.example.mae.calculator;

/**
 * 환산 주스탯 계산 상수 (커뮤니티 기준이며 수정 가능)
 * 참고: 실제 수치는 캐릭터 스펙·직업·버프 등에 따라 달라질 수 있음
 */
public final class ConversionConstants {

    private ConversionConstants() {}

    /** 부스탯 N당 주스탯 1에 해당 (기본값 4: 부스탯 4 = 주스탯 1) */
    public static final int SUB_STAT_RATIO = 4;

    /** 공격력/마력 1당 주스탯 환산값 */
    public static final int ATK_TO_STAT = 4;

    /** 데미지 1%가 BASE 대비 기여하는 비율 (1.0 = 100% 데미지 → BASE만큼 추가) */
    public static final double DAMAGE_WEIGHT = 1.0;

    /** 보스 몬스터 데미지 1%가 BASE 대비 기여하는 비율 */
    public static final double BOSS_DMG_WEIGHT = 1.0;

    /** 최종 데미지 1%가 BASE 대비 기여하는 비율 */
    public static final double FINAL_DMG_WEIGHT = 1.0;

    /**
     * 크리티컬 데미지 1%가 BASE 대비 기여하는 비율
     * 평균 크리티컬 확률 50% 가정 (0.5)
     * 크리 확률이 높으면 이 값을 올려 조정 가능
     */
    public static final double CRIT_DMG_WEIGHT = 0.5;
}
