package com.aoher.json.writer;

import com.aoher.domain.Role;
import com.aoher.domain.User;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.aoher.util.Utilities.getResourceAsBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class UserWriterTest {
    private final UserWriter writer = new UserWriter();

    @Test
    public void testIsUserWritable() {
        assertTrue(writer.isWriteable(User.class, null, null, null));
    }

    @Test
    public void testWriteUser() throws IOException {
        User user = new User();
        user.setUsername("someuser");
        user.setFullName("Some User");
        user.setPassword("supersecret");
        user.getRoles().add(Role.ADMINISTRATOR);
        user.getRoles().add(Role.USER);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.writeTo(user, null, null, null, null, null, output);

        byte[] expectedOutput = getResourceAsBytes("/json/user/write.json");
        assertArrayEquals(expectedOutput, output.toByteArray());
    }
}