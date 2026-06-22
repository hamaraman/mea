package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemEquipmentEntry {
    @JsonProperty("item_equipment_part")  private String part;
    @JsonProperty("item_equipment_slot")  private String slot;
    @JsonProperty("item_name")            private String name;
    @JsonProperty("item_icon")            private String icon;
    @JsonProperty("starforce")            private String starforce;

    @JsonProperty("potential_option_grade")             private String potentialGrade;
    @JsonProperty("potential_option_1")                 private String potential1;
    @JsonProperty("potential_option_2")                 private String potential2;
    @JsonProperty("potential_option_3")                 private String potential3;

    @JsonProperty("additional_potential_option_grade")  private String additionalGrade;
    @JsonProperty("additional_potential_option_1")      private String additional1;
    @JsonProperty("additional_potential_option_2")      private String additional2;
    @JsonProperty("additional_potential_option_3")      private String additional3;

    @JsonProperty("scroll_upgrade")       private String scrollUpgrade;
    @JsonProperty("item_total_option")    private ItemTotalOption totalOption;
}
