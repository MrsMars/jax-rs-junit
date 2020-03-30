package com.aoher.json.writer;

import com.aoher.domain.Role;
import com.aoher.domain.User;

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
import java.lang.reflect.Type;

import static com.aoher.util.Constants.*;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class UserWriter implements MessageBodyWriter<User> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return User.class.isAssignableFrom(aClass);
    }

    @Override
    public void writeTo(User user, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
                        OutputStream outputStream) {

        JsonObjectBuilder jsonUser = Json.createObjectBuilder();

        jsonUser.add(USERNAME_FIELD, user.getUsername());

        if (user.getFullName() != null) {
            jsonUser.add(FULL_NAME_FIELD, user.getFullName());
        }

        JsonArrayBuilder roles = Json.createArrayBuilder();
        for (Role role : user.getRoles()) {
            roles.add(role.name());

        }
        jsonUser.add(ROLES_FIELD, roles);

        try (JsonWriter writer = Json.createWriter(outputStream)) {
            writer.writeObject(jsonUser.build());
        }
    }
}
