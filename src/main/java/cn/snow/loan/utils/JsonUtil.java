package cn.snow.loan.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = getObjectMapper();

    private static ObjectMapper getObjectMapper() {
        return JsonMapper.builder()
                //.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .findAndAddModules()
                .build();
    }

    public static String toJson(Object data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (Exception var3) {
            return null;
        }
    }

    public static <T> T toEntity(String jsonStr, Class<T> javaType) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, javaType);
        } catch (Exception var4) {
            log.warn("toEntity(jsonStr) happen error, jsonStr={}, ex={}", jsonStr, var4.getMessage());
            return null;
        }
    }

    public static <T> List<T> jsonToList(String jsonStr, Class<T> cls) {
        try {
            JavaType t = OBJECT_MAPPER.getTypeFactory().constructParametricType(
                    List.class, cls);
            return OBJECT_MAPPER.readValue(jsonStr, t);
        } catch (Exception e) {
            log.warn("toEntity(jsonStr) happen error, jsonStr={}, ex={}", jsonStr, e.getMessage());
            return Collections.emptyList();
        }
    }

    public static <T> T jsonToBeanWithGeneric(String jsonStr, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, javaType);
        } catch (JsonProcessingException e) {
            log.warn("jsonToBeanWithGeneric(jsonStr) happen error, jsonStr={}, ex={}", jsonStr, e.getMessage());
            return null;
        }
    }

    public static JavaType buildBeanWithGeneric(Class<?> collectionClass, Class<?>... elementClasses) {
        return OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static String getValueByKey(String jsonStr, String key) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.readTree(jsonStr).get(key)).map(JsonNode::asText).orElse("");
        } catch (JsonProcessingException e) {
            log.warn("getValueByKey(jsonStr) happen error, jsonStr={}, key={}, ex={}", jsonStr, key, e.getMessage());
            return null;
        }
    }

    public static Map<String, String> toMap(String jsonStr) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("toMap(jsonStr) happen error, jsonStr={}, key={}, ex={}", jsonStr, e.getMessage());
            return null;
        }
    }

    public static JsonNode toJsonNode(String jsonStr) {
        try {
            return OBJECT_MAPPER.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            log.warn("toJson fail:", e);
            return null;
        }
    }

    public static ArrayNode createNewJsonArray(){
        return OBJECT_MAPPER.createArrayNode();
    }

    public static ObjectNode createNewObjectNode(){
        return OBJECT_MAPPER.createObjectNode();
    }

}
