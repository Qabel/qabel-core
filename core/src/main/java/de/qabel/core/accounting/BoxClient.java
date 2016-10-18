package de.qabel.core.accounting;

import de.qabel.core.exceptions.QblCreateAccountFailException;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.util.ArrayList;

public interface BoxClient extends RequestAuthorizer {


    void login() throws IOException, QblInvalidCredentials;

    QuotaState getQuotaState() throws IOException, QblInvalidCredentials;

    void updatePrefixes() throws IOException, QblInvalidCredentials;

    void createPrefix() throws IOException, QblInvalidCredentials;

    URIBuilder buildUri(String resource);

    URIBuilder buildBlockUri(String resource);

    ArrayList<String> getPrefixes() throws IOException, QblInvalidCredentials;

    AccountingProfile getProfile();

    void resetPassword(String email) throws IOException;

    void createBoxAccount(String email) throws IOException, QblCreateAccountFailException;
}
