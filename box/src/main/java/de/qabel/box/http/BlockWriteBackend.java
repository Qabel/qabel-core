package de.qabel.box.http;

import de.qabel.box.http.HttpWriteBackend;
import de.qabel.core.accounting.AccountingHTTP;
import de.qabel.core.exceptions.QblInvalidCredentials;
import org.apache.http.HttpRequest;

import java.io.IOException;
import java.net.URISyntaxException;

public class BlockWriteBackend extends HttpWriteBackend {
    private AccountingHTTP accountingHTTP;

    public BlockWriteBackend(String root, AccountingHTTP accountingHTTP) throws URISyntaxException {
        super(root);
        this.accountingHTTP = accountingHTTP;
    }

    @Override
    protected void prepareRequest(HttpRequest request) {
        super.prepareRequest(request);
        try {
            accountingHTTP.authorize(request);
        } catch (IOException | QblInvalidCredentials e) {
            throw new IllegalStateException("failed to authorize block request: " + e.getMessage(), e);
        }
    }
}
