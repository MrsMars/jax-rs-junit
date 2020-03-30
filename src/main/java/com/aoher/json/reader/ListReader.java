package com.aoher.json.reader;

import com.aoher.domain.List;
import com.aoher.domain.User;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.aoher.util.Constants.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
@Consumes(APPLICATION_JSON)
public class ListReader implements MessageBodyReader<List> {


    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return List.class.isAssignableFrom(aClass);
    }

    @Override
    public List readFrom(Class<List> aClass, Type type, Annotation[] annotations,
                         MediaType mediaType, MultivaluedMap<String, String> multivaluedMap,
                         InputStream inputStream) {

        List list = new List();
        try (JsonReader jsonReader = Json.createReader(inputStream)) {
            JsonObject jsonList = jsonReader.readObject();

            if (jsonList.containsKey(TITLE_FIELD)) {
                list.setTitle(jsonList.getString(TITLE_FIELD));
            }

            if (jsonList.containsKey(OWNER_FIELD)) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory(REMINDER_ENTITY_FACTORY);
                EntityManager em = emf.createEntityManager();

                list.setOwner(em.find(User.class, jsonList.getString(OWNER_FIELD)));

                em.close();
                emf.close();
            }
        } catch (JsonException | ClassCastException e) {
            throw new BadRequestException (EXCEPTION_MESSAGE_JSON);
        }
        return list;
    }
}
