package com.backpackers.backend.post;

import com.google.api.client.util.Base64;

import com.backpackers.android.backend.api.PostEndpoint;
import com.backpackers.android.backend.api.TokenEndpoint;
import com.backpackers.android.backend.api.UserEndpoint;
import com.backpackers.android.backend.model.Token;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.oauth2.GrantType;
import com.backpackers.backend.util.TestBase;
import com.backpackers.android.backend.Constants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostEndpointTest extends TestBase {

    private UserEndpoint userEndpoint;
    private TokenEndpoint tokenEndpoint;
    private PostEndpoint postEndpoint;

    private Account account;
    private Token token;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        userEndpoint = new UserEndpoint();
        tokenEndpoint = new TokenEndpoint();
        postEndpoint = new PostEndpoint();

        account = userEndpoint.createYolooAccount(initCredentials(), "en_US");

        token = tokenEndpoint.token(
                mockHttpServletRequest(null),
                GrantType.PASSWORD.toString(),
                account.getEmail(), "er49simpen", null);
    }


    @Test
    public void list() throws Exception {

    }

    private String initCredentials() {
        final byte[] userCredential =
                "krialix:er49simpen:yasinsinan707@gmail.com".getBytes();

        return Base64.encodeBase64String(userCredential);
    }

    private HttpServletRequest mockHttpServletRequest(String token) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token == null
                ? Constants.BASE64_CLIENT_ID
                : "Bearer " + token);

        // create an Enumeration over the header keys
        final Iterator<String> iterator = headers.keySet().iterator();
        Enumeration headerNames = new Enumeration<String>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getHeaderNames()).thenReturn(headerNames);

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return headers.get(args[0]);
            }
        }).when(req).getHeader("Authorization");

        return req;
    }
}