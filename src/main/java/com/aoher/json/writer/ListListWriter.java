package com.aoher.json.writer;

import com.aoher.domain.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.aoher.util.Constants.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Provider
@Produces(APPLICATION_JSON)
public class ListListWriter implements MessageBodyWriter<java.util.List<List>> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (!java.util.List.class.isAssignableFrom(aClass)) {
            return false;
        }

        if (type instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            return arguments.length == 1 && arguments[0].equals(List.class);
        } else {
            return false;
        }
    }

    @Override
    public void writeTo(java.util.List<List> lists, Class<?> aClass, Type type,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) {

        JsonArrayBuilder jsonListList = Json.createArrayBuilder();

        lists.forEach(list -> {
            JsonObjectBuilder jsonList = Json.createObjectBuilder();
            jsonList.add(ID_FIELD, list.getId());
            jsonList.add(TITLE_FIELD, list.getTitle());

            EntityManagerFactory emf = Persistence.createEntityManagerFactory(REMINDER_ENTITY_FACTORY);
            EntityManager em = emf.createEntityManager();
            Query q = em.createNamedQuery("List.findSize").setParameter(LIST_PARAMETER, list);
            jsonList.add(SIZE_FIELD, (Long) q.getSingleResult());
            em.close();
            emf.close();

            jsonListList.add(jsonList);
        });

        try (JsonWriter writer = Json.createWriter(outputStream)) {
            writer.writeArray(jsonListList.build());
        }
    }
}
