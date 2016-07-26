package de.qabel.core.accounting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.qabel.core.config.AccountingServer;
import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class BoxHttpClient implements BoxClient {
    private static final String EMAIL_KEY = "email";
    private static final Logger logger = LoggerFactory.getLogger(BoxHttpClient.class.getName());

    private AccountingServer server;
    private final CloseableHttpClient httpclient;
    private Gson gson;
    private AccountingProfile profile;

    public BoxHttpClient(AccountingServer server, AccountingProfile profile) {
        this(server, profile, HttpClients.createMinimal());
    }

    BoxHttpClient(AccountingServer server, AccountingProfile profile, CloseableHttpClient httpclient) {
        this.server = server;
        this.profile = profile;
        this.httpclient = httpclient;
        gson = new Gson();
    }

    @Override
    public void login() throws IOException, QblInvalidCredentials {
        URI uri;
        try {
            uri = buildUri("api/v0/auth/login").build();
        } catch (URISyntaxException e) {
            logger.error("Login url building failed", e);
            throw new RuntimeException("Login url building failed", e);
        }
        HttpPost httpPost = new HttpPost(uri);
        Map<String, String> params = new HashMap<>();
        params.put("username", server.getUsername());
        params.put("password", server.getPassword());
        String json = gson.toJson(params);
        StringEntity input = new StringEntity(json);
        input.setContentType("application/json");
        httpPost.setEntity(input);
        httpPost.setHeader("Accept", "application/json");
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer from login");
            }
            String responseString = EntityUtils.toString(entity);
            try {
                Map<String, Object> answer = gson.fromJson(responseString, HashMap.class);
                if (answer.containsKey("key")) {
                    server.setAuthToken((String) answer.get("key"));
                } else if (answer.containsKey("non_field_errors")) {
                    List<String> errors = (ArrayList<String>) answer.get("non_field_errors");
                    throw new QblInvalidCredentials(errors.get(0));
                } else {
                    throw new IOException("Illegal response from accounting server");
                }
            } catch (JsonSyntaxException e) {
                logger.error("Illegal response: {}", responseString);
                throw new IOException("Illegal response from accounting server", e);
            }
        }
    }

    public QuotaState getQuotaState() throws IOException, QblInvalidCredentials {
        getAuthToken();
        URI uri;
        try {
            uri = buildBlockUri("api/v0/quota").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Url building failed", e);
        }

        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/json");
        authorize(httpGet);

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 400) {
                throw new IllegalStateException(
                    "Server responded with " + statusCode + ": " + response.getStatusLine().getReasonPhrase()
                );
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer from quotaState");
            }
            String responseString = EntityUtils.toString(entity);
            try {
                QuotaState quotaState = gson.fromJson(responseString, QuotaState.class);
                profile.setQuota(quotaState.getQuota());
                return quotaState;
            } catch (JsonSyntaxException e) {
                throw new IllegalStateException("non-json response from server: " + responseString, e);
            }
        }
    }

    @Override
    public void authorize(HttpRequest request) throws IOException, QblInvalidCredentials {
        request.addHeader("Authorization", "Token " + getAuthToken());
    }

    private String getAuthToken() throws IOException, QblInvalidCredentials {
        if (server.getAuthToken() == null) {
            login();
        }
        return server.getAuthToken();
    }

    @Override
    public void updatePrefixes() throws IOException, QblInvalidCredentials {
        ArrayList<String> prefixes;
        URI uri;
        try {
            uri = buildBlockUri("api/v0/prefix").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Url building failed", e);
        }
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("Accept", "application/json");
        authorize(httpGet);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 400) {
                throw new IllegalStateException(
                    "Server responded with " + statusCode + ": " + response.getStatusLine().getReasonPhrase()
                );
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer from login");
            }
            String responseString = EntityUtils.toString(entity);
            try {
                PrefixListDto parsedDto = gson.fromJson(responseString, PrefixListDto.class);
                prefixes = new ArrayList<>(Arrays.asList(parsedDto.prefixes));
                profile.setPrefixes(prefixes);
            } catch (JsonSyntaxException e) {
                throw new IllegalStateException("non-json response from server: " + responseString, e);
            }
        }
    }

    @Override
    public void createPrefix() throws IOException, QblInvalidCredentials {
        URI uri;
        try {
            uri = buildBlockUri("api/v0/prefix").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Url building failed", e);
        }
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Authorization", "Token " + getAuthToken());
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer from login");
            }
            String responseString = EntityUtils.toString(entity);
            profile.addPrefix(gson.fromJson(responseString, PrefixDto.class).prefix);
        }
    }

    public URIBuilder buildUri(String resource) {
        return buildResourceUri(resource, server.getUri());
    }

    private URIBuilder buildResourceUri(String resource, URI server) {
        if (resource.endsWith("/") || resource.startsWith("/")) {
            logger.error("Resource {} starts or ends with /", resource);
            throw new RuntimeException("Illegal resource");
        }
        return new URIBuilder(server)
            .setPath('/' + resource + '/');
    }

    public URIBuilder buildBlockUri(String resource) {
        return buildResourceUri(resource, server.getBlockUri());
    }

    @Override
    public ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials {
        ArrayList<String> prefixes = profile.getPrefixes();
        if (prefixes.size() == 0) {
            updatePrefixes();
            prefixes = profile.getPrefixes();
        }
        return prefixes;
    }

    public AccountingProfile getProfile() {
        return profile;
    }

    @Override
    public void resetPassword(String email) throws IOException {
        URI uri;
        try {
            uri = buildUri("api/v0/auth/password/reset").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Url building failed", e);
        }
        HttpPost httpPost = new HttpPost(uri);
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        String json = gson.toJson(params);
        StringEntity input;
        try {
            input = new StringEntity(json);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("failed to encode request:" + e.getMessage(), e);
        }
        input.setContentType("application/json");
        httpPost.setEntity(input);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer received on reset password request");
            }
            String responseString = EntityUtils.toString(entity);
            try {
                Map<String, Object> answer = gson.fromJson(responseString, HashMap.class);
                String message = "failed to reset password";
                if (response.getStatusLine().getStatusCode() >= 300) {
                    if (response.getStatusLine().getStatusCode() < 500) {
                        if (answer.containsKey(EMAIL_KEY)) {
                            message = ((ArrayList<String>) answer.get(EMAIL_KEY)).get(0);
                        }
                        throw new IllegalArgumentException(message);
                    } else {
                        throw new IllegalStateException(message);
                    }
                }
            } catch (JsonSyntaxException | NumberFormatException | NullPointerException e) {
                logger.error("Illegal response: {}", responseString);
                throw new IOException("Illegal response from accounting server", e);
            }
        }
    }

    @Override
    public void createBoxAccount(String email) throws IOException, QblCreateAccountFailException {
        URI uri;

        try {
            uri = buildUri("api/v0/auth/registration").build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Url building failed", e);
        }

        HttpPost httpPost = new HttpPost(uri);
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("username", server.getUsername());
        params.put("password1", server.getPassword());
        params.put("password2", server.getPassword());
        String json = gson.toJson(params);
        StringEntity input;

        try {
            input = new StringEntity(json);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("failed to encode request:" + e.getMessage(), e);
        }

        input.setContentType("application/json");
        httpPost.setEntity(input);

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500) {
                String exceptionJson = IOUtils.toString(response.getEntity().getContent());
                HashMap map = gson.fromJson(exceptionJson, HashMap.class);
                throw new QblCreateAccountFailException(map);
            }

            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                throw new IllegalStateException("Failed to create box Account StatusCode: " +
                    response.getStatusLine().getStatusCode() + " " +
                    response.getStatusLine().getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("No answer received on reset password request");
            }
        }
    }
}
