package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemTotalOption {
    @JsonProperty("str")          private String str;
    @JsonProperty("dex")          private String dex;
    @JsonProperty("int")          private String intStat;
    @JsonProperty("luk")          private String luk;
    @JsonProperty("max_hp")       private String maxHp;
    @JsonProperty("max_mp")       private String maxMp;
    @JsonProperty("attack_power") private String attackPower;
    @JsonProperty("magic_power")  private String magicPower;
    @JsonProperty("armor")        private String armor;
    @JsonProperty("boss_damage")           private String bossDamage;
    @JsonProperty("ignore_monster_armor")  private String ignoreMonsterArmor;
    @JsonProperty("damage")       private String damage;
    @JsonProperty("all_stat")     private String allStat;
}
