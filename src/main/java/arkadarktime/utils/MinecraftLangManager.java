package arkadarktime.utils;

import arkadarktime.CookiesPower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MinecraftLangManager {
    private final Map<String, String> langData = new HashMap<>();

    public MinecraftLangManager(CookiesPower plugin) {
        String langCode = plugin.getConfig().getString("lang");
        try (InputStream inputStream = plugin.getResource("minecraft_lang/" + langCode + ".json"); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.lines().forEach(line -> {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    String key = parts[0].trim().replace("\"", "");
                    String value = parts[1].trim().replace("\"", "").replace(",", "");
                    langData.put(key, value);
                }
            });
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public String getEntityName(String key) {
        return langData.getOrDefault("entity.minecraft." + key, "Не найдено");
    }

    public String getAdvancementTitle(String key) {
        return langData.getOrDefault("advancements." + key + ".title", "Не найдено");
    }

    public String getAdvancementDescription(String key) {
        return langData.getOrDefault("advancements." + key + ".description", "Не найдено");
    }
}
