package com.aoher.json.writer;

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.aoher.util.Constants.FULL_NAME_FIELD;
import static com.aoher.util.Constants.USERNAME_FIELD;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class UserListWriter implements MessageBodyWriter<List<User>> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (!List.class.isAssignableFrom(aClass)) {
            return false;
        }

        if (type instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            return arguments.length == 1 && arguments[0].equals(User.class);
        } else {
            return false;
        }
    }

    @Override
    public void writeTo(List<User> users, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
                        OutputStream outputStream) {

        JsonArrayBuilder jsonList = Json.createArrayBuilder();

        users.forEach(user -> {
            JsonObjectBuilder jsonUser = Json.createObjectBuilder();
            jsonUser.add(USERNAME_FIELD, user.getUsername());

            if (user.getFullName() != null) {
                jsonUser.add(FULL_NAME_FIELD, user.getFullName());
            }
            jsonList.add(jsonUser);
        });

        try (JsonWriter writer = Json.createWriter(outputStream)) {
            writer.writeArray(jsonList.build());
        }
    }
}
