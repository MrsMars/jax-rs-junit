package com.aoher.json.reader;

import com.aoher.domain.Role;
import com.aoher.domain.User;

import javax.json.*;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.IntStream;

import static com.aoher.util.Constants.*;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class UserReader implements MessageBodyReader<User> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return User.class.isAssignableFrom(aClass);
    }

    @Override
    public User readFrom(Class<User> aClass, Type type, Annotation[] annotations, MediaType mediaType,
                         MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) {
        User user = new User();

        try (JsonReader jsonReader = Json.createReader(inputStream)) {
            JsonObject jsonUser = jsonReader.readObject();

            if (jsonUser.containsKey(USERNAME_FIELD)) {
                user.setUsername(jsonUser.getString(USERNAME_FIELD));
            }

            if (jsonUser.containsKey(FULL_NAME_FIELD)) {
                user.setFullName(jsonUser.getString(FULL_NAME_FIELD));
            }

            if (jsonUser.containsKey(PASSWORD_FIELD)) {
                user.setPassword(jsonUser.getString(PASSWORD_FIELD));
            }

            JsonArray roles = jsonUser.getJsonArray(ROLES_FIELD);
            if (roles != null) {
                IntStream.range(0, roles.size()).forEach(i -> {
                    try {
                        Role role = Role.valueOf(roles.getString(i).toUpperCase());
                        user.getRoles().add(role);
                    } catch (IllegalArgumentException ex) {
                        throw new BadRequestException(EXCEPTION_MESSAGE_USER_ROLES);
                    }
                });
            }
        } catch (JsonException | ClassCastException ex) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }
        return user;
    }
}
