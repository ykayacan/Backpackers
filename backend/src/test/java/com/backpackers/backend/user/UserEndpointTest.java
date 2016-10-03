package com.backpackers.backend.user;

import com.google.api.client.util.Base64;

import com.backpackers.android.backend.api.TokenEndpoint;
import com.backpackers.android.backend.api.UserEndpoint;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.backend.util.TestBase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserEndpointTest extends TestBase {

    private UserEndpoint userEndpoint;
    private TokenEndpoint tokenEndpoint;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        userEndpoint = new UserEndpoint();
    }

    @Test
    public void get() throws Exception {

    }

    @Test
    public void testGetMe() throws Exception {
        Account account = userEndpoint.createYolooAccount(initCredentials(), "en_US");

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    public void testCreateGoogleAccount() throws Exception {

    }

    @Test
    public void testCreateFacebookAccount() throws Exception {

    }

    @Test
    public void testCreateYolooAccount() throws Exception {
        // Create Endpoint and execute your method.
        Account account = userEndpoint.createYolooAccount(initCredentials(), "en_US");

        assertEquals("krialix", account.getUsername());
        assertEquals("yasinsinan707@gmail.com", account.getEmail());
        assertTrue(account.isValidPassword("er49simpen"));
        assertEquals(0, account.getFolloweeCount());
        assertEquals(0, account.getFollowerCount());
        assertEquals(0, account.getQuestionCount());
    }

    @Test
    public void update() throws Exception {

    }

    @Test
    public void remove() throws Exception {

    }

    private String initCredentials() {
        final byte[] userCredential =
                "krialix:er49simpen:yasinsinan707@gmail.com".getBytes();

        return Base64.encodeBase64String(userCredential);
    }
}