package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AbilityResponse {
    @JsonProperty("ability_grade") private String abilityGrade;
    @JsonProperty("ability_info")  private List<AbilityInfo> abilityInfo;
}
