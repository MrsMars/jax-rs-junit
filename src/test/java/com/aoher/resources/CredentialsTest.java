package com.aoher.resources;

import com.aoher.ArchiveFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URL;

import static com.aoher.domain.Role.ADMINISTRATOR;
import static com.aoher.domain.Role.USER;
import static com.aoher.util.Constants.PASSWORD_FIELD;
import static com.aoher.util.Constants.USERNAME_FIELD;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@RunAsClient
public class CredentialsTest {

    @Deployment
    public static WebArchive createArchive() {
        return ArchiveFactory.createArchive();
    }

    @ArquillianResource
    private URL base;

    private WebTarget target;

    @Before
    public void setUp() {
        target = ClientBuilder.newClient().target(base.toExternalForm() + "/api/credentials");
    }

    @Test
    public void testEmptyUsername() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(PASSWORD_FIELD, "supersecret")));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testEmptyPassword() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "someuser")));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUsernameNotAString() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "123", PASSWORD_FIELD, "supersecret")));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testPasswordNotAString() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "unknownuser", PASSWORD_FIELD, "123")));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUnknownUser() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "unknownuser", PASSWORD_FIELD, "supersecret")));
        assertEquals(200, response.getStatus());
        assertEquals("[]", response.readEntity(String.class));
    }

    @Test
    public void testInvalidPassword() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "someuser", PASSWORD_FIELD, "invalidpassword")));
        assertEquals(200, response.getStatus());
        assertEquals("[]", response.readEntity(String.class));
    }

    @Test
    public void testValidCredentialsAdministrator() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "someuser", PASSWORD_FIELD, "supersecret")));
        assertEquals(200, response.getStatus());
        assertEquals(String.format("[\"%s\"]", ADMINISTRATOR), response.readEntity(String.class));
    }

    @Test
    public void testValidCredentialsUser() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "someotheruser", PASSWORD_FIELD, "supersecret")));
        assertEquals(200, response.getStatus());
        assertEquals(String.format("[\"%s\"]", USER), response.readEntity(String.class));
    }

    @Test
    public void testInvalidJson() {
        Response response = target.request(APPLICATION_JSON)
                .post(Entity.json(getJson(USERNAME_FIELD, "someuser", PASSWORD_FIELD, "supersecret")));
        assertEquals(400, response.getStatus());
    }

    private String getJson(String key, String value) {
        return String.format("{\"%s\":\"%s\"}", key, value);
    }

    private String getJson(String key1, String value1, String key2, String value2) {
        return String.format("\"%s\":\"%s\",\"%s\":\"%s\"", key1, value1, key2, value2);
    }
}