package org.example.mae.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CharacterBasicResponse {
    @JsonProperty("character_name")
    private String characterName;

    @JsonProperty("world_name")
    private String worldName;

    @JsonProperty("character_gender")
    private String characterGender;

    @JsonProperty("character_class")
    private String characterClass;

    @JsonProperty("character_class_level")
    private String characterClassLevel;

    @JsonProperty("character_level")
    private int characterLevel;

    @JsonProperty("character_exp")
    private long characterExp;

    @JsonProperty("character_guild_name")
    private String characterGuildName;

    @JsonProperty("character_image")
    private String characterImage;
}
