package com.rei.examenbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * The server stores timestamps as UTC {@link LocalDateTime}. On the wire they carry an explicit
 * offset so browsers in any time zone read them correctly; incoming values with an offset are
 * normalized to UTC, and values without one are treated as UTC.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule utcDateTimeModule() {
        SimpleModule module = new SimpleModule("examen-utc-date-time");
        module.addSerializer(LocalDateTime.class, new ValueSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator generator, SerializationContext context) {
                generator.writeString(value.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
        });
        module.addDeserializer(LocalDateTime.class, new ValueDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) {
                if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                    // Numeric-array form, e.g. [2026, 1, 31, 20, 0]
                    return LocalDateTimeDeserializer.INSTANCE.deserialize(parser, context);
                }
                String text = parser.getString();
                if (text == null || text.isBlank()) {
                    return null;
                }
                return parseUtc(text.trim());
            }
        });
        return module;
    }

    static LocalDateTime parseUtc(String text) {
        try {
            return OffsetDateTime.parse(text).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(text);
        }
    }
}
