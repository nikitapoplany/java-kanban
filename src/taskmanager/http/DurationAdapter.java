package taskmanager.http;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * Адаптер для сериализации/десериализации Duration в JSON
 */
public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toMinutes());
    }

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }
        return Duration.ofMinutes(json.getAsLong());
    }
}