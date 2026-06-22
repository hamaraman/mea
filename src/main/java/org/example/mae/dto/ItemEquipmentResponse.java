package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ItemEquipmentResponse {
    @JsonProperty("character_gender") private String characterGender;
    @JsonProperty("character_class")  private String characterClass;
    @JsonProperty("item_equipment")   private List<ItemEquipmentEntry> itemEquipment;
}
