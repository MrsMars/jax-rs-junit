package com.aoher.json.writer;

import com.aoher.domain.Reminder;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.aoher.util.Constants.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
@Produces(APPLICATION_JSON)
public class ReminderListWriter implements MessageBodyWriter<java.util.List<Reminder>> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (!java.util.List.class.isAssignableFrom(aClass)) {
            return false;
        }

        if (type instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            return arguments.length == 1 && arguments[0].equals(Reminder.class);
        } else {
            return false;
        }
    }

    @Override
    public void writeTo(List<Reminder> reminders, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
                        OutputStream outputStream) {

        JsonArrayBuilder jsonReminderList = Json.createArrayBuilder();

        reminders.forEach(reminder -> {
            JsonObjectBuilder jsonReminder = Json.createObjectBuilder();
            jsonReminder.add(ID_FIELD, reminder.getId());
            jsonReminder.add(TITLE_FIELD, reminder.getTitle());
            if (reminder.getDate() != null) {
                jsonReminder.add(DATE_FIELD, reminder.getDate().getTimeInMillis());
            }
            jsonReminderList.add(jsonReminder);
        });

        try (JsonWriter writer = Json.createWriter(outputStream)) {
            writer.writeArray(jsonReminderList.build());
        }
    }
}
