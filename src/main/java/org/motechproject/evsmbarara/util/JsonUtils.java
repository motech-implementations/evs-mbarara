package org.motechproject.evsmbarara.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.motechproject.evsmbarara.domain.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private JsonUtils() {
    }

    public static String convertSubjectForZetes(Subject s) {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig serializationConfig = mapper.getSerializationConfig().withView(Views.Zetes.class);
        serializationConfig.addMixInAnnotations(Subject.class, SubjectMixin.class);
        mapper.setSerializationConfig(serializationConfig);

        mapper.configure(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION, false);
        try {
            return mapper.writeValueAsString(s);
        } catch (IOException e) {
            LOGGER.error("IOException while converting Subject to JSON: " + e.getMessage());
        }
        return null;
    }
}
