package org.motechproject.evsmbarara.util.serializer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.motechproject.evsmbarara.domain.enums.ScreeningStatus;

import java.io.IOException;

public class CustomScreeningStatusSerializer extends JsonSerializer<ScreeningStatus> {

    @Override
    public void serialize(ScreeningStatus status, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(status.getValue());
    }
}
