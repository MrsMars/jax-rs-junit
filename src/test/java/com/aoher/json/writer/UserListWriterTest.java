package com.aoher.json.writer;

import com.aoher.domain.Role;
import com.aoher.domain.User;
import org.junit.Test;

import javax.ws.rs.core.GenericEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.aoher.util.Utilities.getResourceAsBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class UserListWriterTest {

    private final UserListWriter writer = new UserListWriter();
    private final List<User> users = new ArrayList<>();

    @Test
    public void isWriteable() {
        GenericEntity<List<User>> usersEntity = new GenericEntity<>(users) {};
        assertTrue(writer.isWriteable(usersEntity.getRawType(), usersEntity.getType(), null, null));
    }

    @Test
    public void writeTo() throws IOException {

        User first = new User();
        first.setUsername("someuser");
        first.setFullName("Some User");
        first.setPassword("supersecret");
        first.getRoles().add(Role.ADMINISTRATOR);

        User second = new User();
        second.setUsername("someotheruser");
        second.setFullName("Some Other User");
        second.setPassword("supersecret");
        second.getRoles().add(Role.USER);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.writeTo(users, null, null, null, null, null, output);

        byte[] expectedOutput = getResourceAsBytes("/json/user/write-list.json");
        assertArrayEquals(expectedOutput, output.toByteArray());
    }
}