package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CharacterStatResponse {
    @JsonProperty("character_class")
    private String characterClass;

    @JsonProperty("final_stat")
    private List<StatEntry> finalStat;

    @JsonProperty("remain_ap")
    private int remainAp;
}
