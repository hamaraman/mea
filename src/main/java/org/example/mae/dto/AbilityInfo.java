package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbilityInfo {
    @JsonProperty("ability_no")    private String abilityNo;
    @JsonProperty("ability_grade") private String abilityGrade;
    @JsonProperty("ability_value") private String abilityValue;
}
