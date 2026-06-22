package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SymbolEntry {
    @JsonProperty("symbol_name")                 private String name;
    @JsonProperty("symbol_icon")                 private String icon;
    @JsonProperty("symbol_level")                private int level;
    @JsonProperty("symbol_force")                private String force;
    @JsonProperty("symbol_str")                  private String str;
    @JsonProperty("symbol_dex")                  private String dex;
    @JsonProperty("symbol_int")                  private String intStat;
    @JsonProperty("symbol_luk")                  private String luk;
    @JsonProperty("symbol_hp")                   private String hp;
    @JsonProperty("symbol_growth_count")         private int growthCount;
    @JsonProperty("symbol_require_growth_count") private int requireGrowthCount;
}
