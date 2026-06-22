package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SymbolResponse {
    @JsonProperty("character_class") private String characterClass;
    @JsonProperty("symbol")          private List<SymbolEntry> symbols;
}
