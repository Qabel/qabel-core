package de.qabel.core.accounting;


import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class BoxClientStub implements BoxClient {

    public ArrayList<String> prefixes = new ArrayList<>();
    public QuotaState quotaState = new QuotaState(1000000000L, 300000000L);

    public IOException ioException;
    public QblInvalidCredentials qblInvalidCredentials;
    public QblCreateAccountFailException qblCreateAccountFailException;

    @Override
    public void login() throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }
    }

    @Override
    public QuotaState getQuotaState() throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }
        return quotaState;
    }


    @Override
    public void authorize(HttpRequest request) throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }
    }

    @Override
    public void updatePrefixes() throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }

    }

    @Override
    public void createPrefix() throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }
        prefixes.add("prefix" + (prefixes.size() + 1));
    }

    public URIBuilder buildUri(String resource) {
        return null;
    }


    public URIBuilder buildBlockUri(String resource) {
        return null;
    }

    @Override
    public ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials {
        if (ioException != null) {
            throw ioException;
        }
        if (qblInvalidCredentials != null) {
            throw qblInvalidCredentials;
        }
        return prefixes;
    }

    public AccountingProfile getProfile() {
        return null;
    }

    @Override
    public void resetPassword(String email) throws IOException {

    }

    @Override
    public void createBoxAccount(String email) throws IOException, QblCreateAccountFailException {
        if (ioException != null) {
            throw ioException;
        }
        if (qblCreateAccountFailException != null) {
            throw qblCreateAccountFailException;
        }
    }
}
