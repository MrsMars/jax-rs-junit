package com.aoher.json.reader;

import com.aoher.domain.Role;
import com.aoher.domain.User;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.aoher.util.Utilities.getResourceAsStream;
import static org.junit.Assert.*;

public class UserReaderTest {

    private final UserReader reader = new UserReader();

    @Test
    public void testIsUserReadable() {
        assertTrue(reader.isReadable(User.class, null, null, null));
    }

    @Test
    public void testReadUser() {
        User result = readFrom(getResourceAsStream("/json/user/read.json"));

        assertEquals("someuser", result.getUsername());
        assertEquals("Some User", result.getFullName());
        assertEquals("f75778f7425be4db0369d09af37a6c2b9a83dea0e53e7bd57412e4b060e607f7", result.getPassword());
        assertArrayEquals(new Role[] {Role.ADMINISTRATOR, Role.USER}, result.getRoles().toArray());
    }

    @Test(expected = BadRequestException.class)
    public void testUsernameNotAString()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-username.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testFullNameNotAString()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-fullname.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testPasswordNotAString()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-password.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidRole()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-role-1.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testRoleNotAString()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-role-2.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testRolesNotAnArray()  {
        readFrom(getResourceAsStream("/json/user/read-invalid-roles.json"));
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidJson() {
        String invalidJson = "{]";
        readFrom(new ByteArrayInputStream(invalidJson.getBytes()));
    }

    private User readFrom(InputStream is) {
        return reader.readFrom(
                User.class,
                null,
                null,
                null,
                null,
                is);
    }
}