package com.aoher.resources;

import com.aoher.domain.List;
import com.aoher.domain.Reminder;
import com.aoher.domain.Role;
import com.aoher.domain.User;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import static com.aoher.util.Constants.*;
import static com.aoher.util.Utilities.mergeMessages;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("lists")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Lists {

    @Inject
    private Reminders remindersResource;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private Validator validator;

    @Context
    private SecurityContext context;

    @GET
    @Consumes(APPLICATION_JSON)
    public java.util.List<List> getLists(List list) {
        TypedQuery<List> query = em.createQuery("List.findByOwner", List.class);
        query.setParameter(OWNER_FIELD, em.find(User.class, context.getUserPrincipal().getName()));
        return query.getResultList();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response addList(List list) {
        if (list.getOwner() == null) {
            list.setOwner(em.find(User.class, context.getUserPrincipal().getName()));
        }

        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        Set<ConstraintViolation<List>> violations = validator.validate(list);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.persist(list);
        return Response.created(URI.create("/lists/" + list.getId())).build();
    }

    @GET
    @Path("{listid}")
    @Produces(APPLICATION_JSON)
    public List getList(@PathParam("listid") long id) {
        List list = em.find(List.class, id);

        if (list == null) {
            throw new NotFoundException();
        }

        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }
        return list;
    }

    @PUT
    @Path("{listid}")
    @Consumes(APPLICATION_JSON)
    public void updateList(@PathParam("listid") long id, InputStream in) {

        List list = em.find(List.class, id);

        if (list == null) {
            throw new NotFoundException();
        }

        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        em.detach(list);
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject update = reader.readObject();

            if (update.containsKey(TITLE_FIELD)) {
                list.setTitle(update.getString(TITLE_FIELD));
            }

            if (update.containsKey(OWNER_FIELD)) {
                User newOwner = em.find(User.class, update.getString(OWNER_FIELD));

                if (newOwner == null) {
                    throw new NotFoundException();
                }

                if (!newOwner.equals(list.getOwner())
                        && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                } else {
                    list.setOwner(newOwner);
                }
            }
        } catch (JsonException | ClassCastException ex) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }

        Set<ConstraintViolation<List>> violations = validator.validate(list);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.merge(list);
    }

    @DELETE
    @Path("{listid}")
    public void removeList(@PathParam("listid") long id) throws IOException {
        List list = em.find(List.class, id);

        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can delete other user's lists.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        TypedQuery<Reminder> q = em.createNamedQuery("Reminder.findByList", Reminder.class).setParameter(LIST_PARAMETER, list);
        for (Reminder reminder : q.getResultList()) {
            remindersResource.removeReminder(list.getId(), reminder.getId());
        }
        em.remove(list);
    }
}
