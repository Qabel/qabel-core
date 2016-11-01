package de.qabel.core.config;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#account
 */
public class Account extends SyncSettingItem {
    private static final long serialVersionUID = -6902585443982494539L;
    /**
     * Provider of the account
     * Field name in serialized json: "provider"
     */
    private String provider;
    /**
     * User of the account
     * Field name in serialized json: "user"
     */
    private String user;
    /**
     * Authentication of the account
     * Field name in serialized json: "auth"
     */
    private String auth;
    /**
     * AuthToken of the account
     * May expire but should be used until proven to be expired
     */
    private String token;

    /**
     * Creates an instance of Account
     *
     * @param provider Provider of the account.
     * @param user     User of the account
     * @param auth     Authentication of the account
     */
    public Account(String provider, String user, String auth) {
        this(provider, user);
        setAuth(auth);
    }

    /**
     * Creates an instance of Account
     *
     * @param provider Provider of the account.
     * @param user     User of the account
     */
    public Account(String provider, String user) {
        setProvider(provider);
        setUser(user);
    }

    public void setProvider(String value) {
        provider = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setUser(String value) {
        user = value;
    }

    public String getUser() {
        return user;
    }

    public void setAuth(String value) {
        auth = value;
    }

    public String getAuth() {
        return auth;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result;

        result = super.hashCode();

        result = prime * result + (provider == null ? 0 : provider.hashCode());
        result = prime * result + (user == null ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Account other = (Account) obj;
        if (auth == null) {
            if (other.auth != null) {
                return false;
            }
        } else if (!auth.equals(other.auth)) {
            return false;
        }
        if (provider == null) {
            if (other.provider != null) {
                return false;
            }
        } else if (!provider.equals(other.provider)) {
            return false;
        }
        if (user == null) {
            return other.user == null;
        }
        return user.equals(other.user);
    }
}
