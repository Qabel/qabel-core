package de.qabel.core.accounting;

import de.qabel.core.config.AccountingServer;

import java.net.URI;
import java.net.URISyntaxException;

public class TestAccountingServerBuilder {
    public static final String DEFAULT_PASS = "testuser";
    private URI accountingUri;
    private URI blockUri;
    private String user = "testuser";
    private String pass = DEFAULT_PASS;

    public TestAccountingServerBuilder() throws URISyntaxException {
        this.accountingUri = new URI("http://localhost:9696");
        this.blockUri = new URI("http://localhost:9697");
    }

    public TestAccountingServerBuilder user(String user) {
        this.user = user;
        if (pass.equals(DEFAULT_PASS)) {
            pass = "randomPass";
        }
        return this;
    }

    public TestAccountingServerBuilder pass(String pass) {
        this.pass = pass;
        return this;
    }

    public TestAccountingServerBuilder block(URI blockUri) {
        this.blockUri = blockUri;
        return this;
    }

    public TestAccountingServerBuilder accounting(URI accountingUri) {
        this.accountingUri = accountingUri;
        return this;
    }

    public AccountingServer build() {
        return new AccountingServer(
            accountingUri,
            blockUri,
            user,
            pass
        );
    }
}
