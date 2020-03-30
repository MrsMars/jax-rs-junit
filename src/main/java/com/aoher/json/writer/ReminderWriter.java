package com.aoher.json.writer;

import com.aoher.domain.Reminder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.aoher.util.Constants.*;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ReminderWriter implements MessageBodyWriter<Reminder> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return Reminder.class.isAssignableFrom(aClass);
    }

    @Override
    public void writeTo(Reminder reminder, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
                        OutputStream outputStream) {

        JsonObjectBuilder jsonReminder = Json.createObjectBuilder();
        jsonReminder.add(ID_FIELD, reminder.getId());
        jsonReminder.add(TITLE_FIELD, reminder.getTitle());

        if (reminder.getDate() != null) {
            jsonReminder.add(DATE_FIELD, reminder.getDate().getTimeInMillis());
        }

        if (reminder.getLocation() != null) {
            JsonObjectBuilder jsonLocation = Json.createObjectBuilder();
            jsonLocation.add(LATITUDE_FIELD, reminder.getLocation().getLatitude());
            jsonLocation.add(LONGITUDE_FIELD, reminder.getLocation().getLongitude());
            jsonReminder.add(LOCATION_FIELD, jsonLocation);
        }
        jsonReminder.add(IMAGE_FIELD, reminder.getImage() != null);

        try (JsonWriter writer = Json.createWriter(outputStream)) {
            writer.writeObject(jsonReminder.build());
        }
    }
}
