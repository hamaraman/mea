package org.example.mae.calculator;

import java.util.HashMap;
import java.util.Map;

/**
 * 직업별 주스탯 타입 매핑
 * STR / DEX / INT / LUK / STR_DEX / ALL / HP
 */
public final class JobGroup {

    private JobGroup() {}

    private static final Map<String, String> CLASS_STAT_MAP = new HashMap<>();

    static {
        // 전사 계열 (STR)
        String[] strClasses = {
            "히어로", "팔라딘", "다크나이트",
            "소울마스터", "미하일", "블래스터",
            "아란", "은월",
            "데몬슬레이어",
            "카이저", "아델", "제로",
            "아크", "영웅"
        };
        for (String c : strClasses) CLASS_STAT_MAP.put(c, "STR");

        // 궁수 계열 (DEX)
        String[] dexClasses = {
            "보우마스터", "신궁", "패스파인더",
            "메르세데스", "윈드브레이커", "와일드헌터",
            "카인"
        };
        for (String c : dexClasses) CLASS_STAT_MAP.put(c, "DEX");

        // 마법사 계열 (INT)
        String[] intClasses = {
            "불/독 아크메이지", "썬/콜 아크메이지", "비숍",
            "루미너스", "에반", "배틀메이지",
            "에레블", "키네시스", "라라",
            "불독아크메이지", "썬콜아크메이지",
            "일리움", "레인"
        };
        for (String c : intClasses) CLASS_STAT_MAP.put(c, "INT");

        // 도적 계열 (LUK)
        String[] lukClasses = {
            "나이트로드", "섀도어", "듀얼블레이더",
            "팬텀", "캐딩", "호영", "칼리", "엔쥬",
            "나이트워커"
        };
        for (String c : lukClasses) CLASS_STAT_MAP.put(c, "LUK");

        // 해적 계열 - STR 기반
        String[] strPirateClasses = {
            "바이퍼", "캐논슈터", "버키니어", "스트라이커"
        };
        for (String c : strPirateClasses) CLASS_STAT_MAP.put(c, "STR");

        // 해적 계열 - DEX 기반
        String[] dexPirateClasses = {
            "캡틴", "코르세어", "플레임위자드"
        };
        for (String c : dexPirateClasses) CLASS_STAT_MAP.put(c, "DEX");

        // 제논 (STR+DEX+LUK 합산)
        CLASS_STAT_MAP.put("제논", "ALL");

        // 데몬어벤져 (HP 기반)
        CLASS_STAT_MAP.put("데몬어벤져", "HP");
    }

    public static String getStatType(String characterClass) {
        if (characterClass == null) return "STR";
        return CLASS_STAT_MAP.getOrDefault(characterClass.trim(), "STR");
    }
}
