package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatEntry {
    @JsonProperty("stat_name")
    private String statName;

    @JsonProperty("stat_value")
    private String statValue;
}
