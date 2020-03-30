package com.aoher.resources;

import com.aoher.domain.List;
import com.aoher.domain.Role;
import com.aoher.domain.User;
import com.aoher.validation.OnPasswordUpdate;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.*;
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.IntStream;

import static com.aoher.util.Constants.*;
import static com.aoher.util.Utilities.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("users")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Users {

    @Inject
    private Lists listsResource;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private Validator validator;

    @Context
    private SecurityContext context;

    @GET
    @Produces(APPLICATION_JSON)
    public java.util.List<User> getAllUsers(@QueryParam("from") @DefaultValue("0") int from,
                                            @QueryParam("results") @DefaultValue("20") int results) {
        TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
        query.setFirstResult(from);
        query.setMaxResults(results);
        return query.getResultList();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    public Response addUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user, OnPasswordUpdate.class);

        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        if (em.find(User.class, user.getUsername()) != null) {
            throw new BadRequestException(EXCEPTION_MESSAGE_USER_USERNAME);
        }

        user.getRoles().clear();
        user.getRoles().add(Role.USER);

        em.persist(user);

        return Response.created(URI.create("/users/" + user.getUsername())).build();
    }

    @GET
    @Path("{username}")
    @Produces(APPLICATION_JSON)
    public User getUser(@PathParam("username") String username) {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }
        return user;
    }

    @PUT
    @Path("{username}")
    @Consumes(APPLICATION_JSON)
    public void updateUser(@PathParam("username") String username, InputStream in) {

        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username)
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        em.detach(user);

        boolean passwordChanged = false;

        try (JsonReader reader = Json.createReader(in)){
            JsonObject userUpdate = reader.readObject();

            if (userUpdate.containsKey(FULL_NAME_FIELD)) {
                user.setFullName(userUpdate.isNull(FULL_NAME_FIELD) ? null : userUpdate.getString(FULL_NAME_FIELD));
            }

            JsonArray roles = userUpdate.getJsonArray(ROLES_FIELD);
            if (roles != null) {
                if (!context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                }

                user.getRoles().clear();
                addRole(user, roles);
            }

            if (userUpdate.containsKey(PASSWORD_FIELD)) {
                user.setPassword(userUpdate.getString(PASSWORD_FIELD));
                passwordChanged = true;
            }
        } catch (JsonException | ClassCastException ex) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }

        Set<ConstraintViolation<User>> violations = passwordChanged ?
                validator.validate(user, OnPasswordUpdate.class) :
                validator.validate(user);

        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.merge(user);
    }

    @DELETE
    @Path("{username}")
    public void removeUser(@PathParam("username") String username) throws IOException {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username)
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        TypedQuery<List> q = em.createNamedQuery("List.findByOwner", List.class).setParameter(OWNER_FIELD, user);
        for (List list : q.getResultList()) {
            listsResource.removeList(list.getId());
        }
        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(username + PNG_FORMAT_WITH_POINT));
        em.remove(user);
    }

    @GET
    @Path("{username}/picture")
    @Produces("image/png")
    public InputStream getProfilePicture(@PathParam("username") String username) throws IOException {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        java.nio.file.Path path = IMAGES_BASE_DIR.resolve(user.getProfilePicture());
        if (!Files.exists(path)) {
            throw new InternalServerErrorException("Could not load profile picture " + user.getProfilePicture());
        }

        return Files.newInputStream(path);
    }

    @PUT
    @Path("{username}/picture")
    @Consumes({"image/jpeg", "image/png"})
    public void setProfilePicture(@PathParam("username") String username,
                                  @HeaderParam("Content-Length") long fileSize,
                                  InputStream in) throws IOException {

        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username)
                && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        // Make sure the file is not larger than the maximum allowed size.
        if (fileSize > 1024 * 1024 * MAX_PROFILE_PICTURE_SIZE_IN_MB) {
            throw new BadRequestException(EXCEPTION_MESSAGE_USER_PICTURE);
        }

        BufferedImage image = ImageIO.read(in);

        // Scale the image to 200px x 200px.
        BufferedImage scaledImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, 200, 200, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();

        // Save the image. By default, {username}.png is used as the filename.
        OutputStream out = Files.newOutputStream(
                IMAGES_BASE_DIR.resolve(username + PNG_FORMAT_WITH_POINT),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        ImageIO.write(scaledImage, PNG_FORMAT, out);

        // Don't forget to set it.
        user.setProfilePicture(username + PNG_FORMAT_WITH_POINT);
    }

    @DELETE
    @Path("{username}/picture")
    public void removeProfilePicture(@PathParam("username") String username) throws IOException {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(username + PNG_FORMAT_WITH_POINT));

        user.setProfilePicture(null);
    }

    private void addRole(User user, JsonArray roles) {
        IntStream.range(0, roles.size()).forEach(i -> {
            try {
                Role role = Role.valueOf(roles.getString(i).toUpperCase());
                user.getRoles().add(role);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(EXCEPTION_MESSAGE_USER_ROLES);
            }
        });
    }
}
