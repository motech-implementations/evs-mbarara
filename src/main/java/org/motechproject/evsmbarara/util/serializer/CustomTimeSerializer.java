package org.motechproject.evsmbarara.util.serializer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.motechproject.commons.date.model.Time;

import java.io.IOException;

public class CustomTimeSerializer extends JsonSerializer<Time> {

    @Override
    public void serialize(Time value, JsonGenerator gen,
                          SerializerProvider arg2)
            throws IOException {
        gen.writeString(String.format("%02d:%02d", value.getHour(), value.getMinute()));
    }
}
