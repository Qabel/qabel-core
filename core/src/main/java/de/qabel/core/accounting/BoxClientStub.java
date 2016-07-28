package de.qabel.core.accounting;


import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class BoxClientStub implements BoxClient {

    public QuotaState quotaState = new QuotaState(1000000000L, 300000000L);

    @Override
    public void login() throws IOException, QblInvalidCredentials {

    }

    public QuotaState getQuotaState() throws IOException, QblInvalidCredentials {
        return quotaState;
    }


    @Override
    public void authorize(HttpRequest request) throws IOException, QblInvalidCredentials {

    }

    @Override
    public void updatePrefixes() throws IOException, QblInvalidCredentials {

    }

    @Override
    public void createPrefix() throws IOException, QblInvalidCredentials {

    }

    public URIBuilder buildUri(String resource) {
        return null;
    }


    public URIBuilder buildBlockUri(String resource) {
        return null;
    }

    @Override
    public ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials {
        return null;
    }

    public AccountingProfile getProfile() {
        return null;
    }

    @Override
    public void resetPassword(String email) throws IOException {

    }

    @Override
    public void createBoxAccount(String email) throws IOException, QblCreateAccountFailException {

    }
}
