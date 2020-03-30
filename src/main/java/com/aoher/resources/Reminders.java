package com.aoher.resources;

import com.aoher.domain.List;
import com.aoher.domain.Location;
import com.aoher.domain.Reminder;
import com.aoher.domain.Role;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Set;

import static com.aoher.util.Constants.*;
import static com.aoher.util.Utilities.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("lists/{listid}/reminders")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Reminders {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private Validator validator;

    @Context
    private SecurityContext context;

    @GET
    @Produces(APPLICATION_JSON)
    public java.util.List<Reminder> getRemindersInList(@PathParam("listid") long listId) {

        List list = em.find(List.class, listId);

        if (list == null) {
            throw new NotFoundException();
        }

        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        TypedQuery<Reminder> q = em.createNamedQuery("Reminder.findByList", Reminder.class).setParameter(LIST_PARAMETER, list);
        return q.getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReminderToList(@PathParam("listid") long listId, Reminder reminder) {
        List list = em.find(List.class, listId);

        if (list == null) {
            throw new NotFoundException();
        }

        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        reminder.setList(list);

        Set<ConstraintViolation<Reminder>> violations = validator.validate(reminder);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.persist(reminder);
        return Response.created(URI.create("/lists/" + listId + "/reminders/" + reminder.getId())).build();
    }

    @GET
    @Path("{reminderid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reminder getReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        return reminder;
    }

    @PUT
    @Path("{reminderid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId, InputStream in) {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        em.detach(reminder);

        try (JsonReader reader = Json.createReader(in)){
            JsonObject reminderUpdate = reader.readObject();

            if (reminderUpdate.containsKey(LIST_PARAMETER)) {
                List list = em.find(List.class, reminderUpdate.getJsonNumber(LIST_PARAMETER).longValue());

                if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName())
                        && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                } else {
                    reminder.setList(list);
                }
            }

            if (reminderUpdate.containsKey(TITLE_FIELD)) {
                reminder.setTitle(reminderUpdate.getString(TITLE_FIELD));
            }

            if (reminderUpdate.containsKey(DATE_FIELD)) {
                if (reminderUpdate.isNull(DATE_FIELD)) {
                    reminder.setDate(null);
                } else {
                    Calendar date = Calendar.getInstance();
                    date.setTimeInMillis(reminderUpdate.getJsonNumber(DATE_FIELD).longValue());
                    reminder.setDate(date);
                }
            }

            if (reminderUpdate.containsKey(LOCATION_FIELD)) {
                if (reminderUpdate.isNull(LOCATION_FIELD)) {
                    reminder.setLocation(null);
                } else {
                    Location location = new Location();
                    JsonObject jsonLocation = reminderUpdate.getJsonObject(LOCATION_FIELD);
                    if (jsonLocation.containsKey(LATITUDE_FIELD)) {
                        location.setLatitude(jsonLocation.getJsonNumber(LATITUDE_FIELD).doubleValue());
                    }
                    if (jsonLocation.containsKey(LONGITUDE_FIELD)) {
                        location.setLongitude(jsonLocation.getJsonNumber(LONGITUDE_FIELD).doubleValue());
                    }
                    reminder.setLocation(location);
                }
            }
        } catch (JsonException | ClassCastException ex) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }

        Set<ConstraintViolation<Reminder>> violations = validator.validate(reminder);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.merge(reminder);
    }

    @DELETE
    @Path("{reminderid}")
    public void removeReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        if (reminder.getImage() != null) {
            Files.deleteIfExists(IMAGES_BASE_DIR.resolve(reminder.getImage()));
        }
        em.remove(reminder);
    }

    @GET
    @Path("{reminderid}/image")
    @Produces("image/jpeg")
    public InputStream getImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId || reminder.getImage() == null) {
            throw new NotFoundException();
        }

        java.nio.file.Path path = IMAGES_BASE_DIR.resolve(reminder.getImage());
        if (!Files.exists(path)) {
            throw new InternalServerErrorException("Could not load image " + reminder.getImage());
        }

        return Files.newInputStream(path);
    }

    @PUT
    @Path("{reminderid}/image")
    @Consumes("image/jpeg")
    public void setImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId, @HeaderParam("Content-Length") long fileSize, InputStream in) throws IOException {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(reminder.getList().getOwner().getUsername())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        if (fileSize > 1024 * 1024 * MAX_IMAGE_SIZE_IN_MB) {
            throw new BadRequestException(EXCEPTION_MESSAGE_REMINDER_IMAGE);
        }

        Files.copy(in, IMAGES_BASE_DIR.resolve(reminder.getId() + ".jpg"), StandardCopyOption.REPLACE_EXISTING);
        reminder.setImage(reminder.getId() + ".jpg");
    }

    @DELETE
    @Path("{reminderid}/image")
    public void removeImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId || reminder.getImage() == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(reminder.getList().getOwner().getUsername())
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(reminder.getImage()));
        reminder.setImage(null);
    }
}
