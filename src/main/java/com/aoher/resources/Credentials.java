package com.aoher.resources;

import com.aoher.domain.User;

import javax.json.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import java.io.InputStream;

import static com.aoher.util.Constants.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("credentials")
@Transactional(dontRollbackOn = {BadRequestException.class})
public class Credentials {

    @PersistenceContext
    private EntityManager em;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public String checkCredentials(InputStream in) {

        try (JsonReader reader = Json.createReader(in)) {
            JsonObject credentials = reader.readObject();

            String username = null;
            String password = null;

            if (credentials.containsKey(USERNAME_FIELD)) {
                username = credentials.getString(USERNAME_FIELD);
            }

            if (credentials.containsKey(PASSWORD_FIELD)) {
                password = credentials.getString(PASSWORD_FIELD);
            }

            if (username == null) {
                throw new BadRequestException(EXCEPTION_MESSAGE_CREDENTIALS_USERNAME);
            } else if (password == null) {
                throw new BadRequestException(EXCEPTION_MESSAGE_CREDENTIALS_PASSWORD);
            }

            User existingUser = em.find(User.class, username);

            if (existingUser == null) {
                return "[]";
            }

            User tempUser = new User();
            tempUser.setPassword(password);

            if (!existingUser.getPassword().equals(tempUser.getPassword())) {
                return "[]";
            }

            JsonArrayBuilder roles = Json.createArrayBuilder();
            existingUser.getRoles().stream().map(Enum::name).forEach(roles::add);

            return roles.build().toString();
        } catch (JsonException | ClassCastException e) {
            throw new BadRequestException(EXCEPTION_MESSAGE_JSON);
        }
    }
}
