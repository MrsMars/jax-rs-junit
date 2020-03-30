package com.aoher.json.reader;

import com.aoher.domain.Location;
import com.aoher.domain.Reminder;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Calendar;

import static com.aoher.util.Constants.*;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class ReminderReader implements MessageBodyReader<Reminder> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return Reminder.class.isAssignableFrom(aClass);
    }

    @Override
    public Reminder readFrom(Class<Reminder> aClass, Type type, Annotation[] annotations, MediaType mediaType,
                             MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) {
        Reminder reminder = new Reminder();

        try (JsonReader jsonReader = Json.createReader(inputStream)) {
            JsonObject jsonReminder = jsonReader.readObject();

            if (jsonReminder.containsKey(TITLE_FIELD)) {
                reminder.setTitle(jsonReminder.getString(TITLE_FIELD));
            }

            if (jsonReminder.containsKey(DATE_FIELD)) {
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(jsonReminder.getJsonNumber(DATE_FIELD).longValue());
                reminder.setDate(date);
            }

            if (jsonReminder.containsKey(LOCATION_FIELD)) {
                Location location = new Location();
                JsonObject jsonLocation = jsonReminder.getJsonObject(LOCATION_FIELD);

                if (jsonLocation.containsKey(LATITUDE_FIELD)) {
                    location.setLatitude(jsonLocation.getJsonNumber(LATITUDE_FIELD).doubleValue());
                }

                if (jsonLocation.containsKey(LONGITUDE_FIELD)) {
                    location.setLongitude(jsonLocation.getJsonNumber(LONGITUDE_FIELD).doubleValue());
                }
                reminder.setLocation(location);
            }

        } catch (JsonException | ClassCastException ex) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }
        return reminder;
    }
}
