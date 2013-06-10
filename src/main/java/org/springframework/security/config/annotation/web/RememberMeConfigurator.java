/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.config.annotation.web;

import java.util.UUID;

import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfiguratorAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * Configures Remember Me authentication. This typically involves the user
 * checking a box when they enter their username and password that states to
 * "Remember Me".
 *
 * <h2>Security Filters</h2>
 *
 * The following Filters are populated
 *
 * <ul>
 * <li>
 * {@link RememberMeAuthenticationFilter}</li>
 * </ul>
 *
 * <h2>Shared Objects Created</h2>
 *
 * The following shared objects are populated
 *
 * <ul>
 * <li>
 * {@link HttpConfiguration#authenticationProvider(org.springframework.security.authentication.AuthenticationProvider)}
 * is populated with a {@link RememberMeAuthenticationProvider}</li>
 * <li> {@link RememberMeServices} is populated as a shared object and available on {@link HttpConfiguration#getSharedObject(Class)}</li>
 * <li> {@link LogoutConfigurator#addLogoutHandler(LogoutHandler)} is used to add a logout handler to clean up the remember me authentication.</li>
 * </ul>
 *
 * <h2>Shared Objects Used</h2>
 *
 * The following shared objects are used:
 *
 * <ul>
 * <li>{@link HttpConfiguration#authenticationManager()}</li>
 * <li>{@link UserDetailsService} if no {@link #userDetailsService(UserDetailsService)} was specified.</li>
 * </ul>
 *
 * @author Rob Winch
 * @since 3.2
 */
public final class RememberMeConfigurator extends
        SecurityConfiguratorAdapter<DefaultSecurityFilterChain, HttpConfiguration> {
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private String key;
    private RememberMeServices rememberMeServices;
    private LogoutHandler logoutHandler;
    private String rememberMeParameter = "remember-me";
    private String rememberMeCookieName = "remember-me";
    private PersistentTokenRepository tokenRepository;
    private UserDetailsService userDetailsService;
    private Integer tokenValiditySeconds;
    private Boolean useSecureCookie;

    /**
     * Creates a new instance
     */
    RememberMeConfigurator() {
    }

    /**
     * Allows specifying how long (in seconds) a token is valid for
     *
     * @param tokenValiditySeconds
     * @return {@link RememberMeConfigurator} for further customization
     * @see AbstractRememberMeServices#setTokenValiditySeconds(int)
     */
    public RememberMeConfigurator tokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
        return this;
    }

    /**
     *Whether the cookie should be flagged as secure or not. Secure cookies can only be sent over an HTTPS connection
     * and thus cannot be accidentally submitted over HTTP where they could be intercepted.
     * <p>
     * By default the cookie will be secure if the request is secure. If you only want to use remember-me over
     * HTTPS (recommended) you should set this property to {@code true}.
     *
     * @param useSecureCookie set to {@code true} to always user secure cookies, {@code false} to disable their use.
     * @return the {@link RememberMeConfigurator} for further customization
     * @see AbstractRememberMeServices#setUseSecureCookie(boolean)
     */
    public RememberMeConfigurator useSecureCookie(boolean useSecureCookie) {
        this.useSecureCookie = useSecureCookie;
        return this;
    }

    /**
     * Specifies the {@link UserDetailsService} used to look up the
     * {@link UserDetails} when a remember me token is valid. The default is to
     * use the {@link UserDetailsService} found by invoking
     * {@link HttpConfiguration#getSharedObject(Class)} which is set when using
     * {@link WebSecurityConfigurerAdapter#registerAuthentication(org.springframework.security.config.annotation.authentication.AuthenticationRegistry)}.
     * Alternatively, one can populate {@link #rememberMeServices(RememberMeServices)}.
     *
     * @param userDetailsService
     *            the {@link UserDetailsService} to configure
     * @return the {@link RememberMeConfigurator} for further customization
     * @see AbstractRememberMeServices
     */
    public RememberMeConfigurator userDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        return this;
    }

    /**
     * Specifies the {@link PersistentTokenRepository} to use. The default is to
     * use {@link TokenBasedRememberMeServices} instead.
     *
     * @param tokenRepository
     *            the {@link PersistentTokenRepository} to use
     * @return the {@link RememberMeConfigurator} for further customization
     */
    public RememberMeConfigurator tokenRepository(PersistentTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        return this;
    }

    /**
     * Sets the key to identify tokens created for remember me authentication. Default is a secure randomly generated
     * key.
     *
     * @param key the key to identify tokens created for remember me authentication
     * @return  the {@link RememberMeConfigurator} for further customization
     */
    public RememberMeConfigurator key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Allows control over the destination a remembered user is sent to when they are successfully authenticated.
     * By default, the filter will just allow the current request to proceed, but if an
     * {@code AuthenticationSuccessHandler} is set, it will be invoked and the {@code doFilter()} method will return
     * immediately, thus allowing the application to redirect the user to a specific URL, regardless of what the original
     * request was for.
     *
     * @param authenticationSuccessHandler the strategy to invoke immediately before returning from {@code doFilter()}.
     * @return {@link RememberMeConfigurator} for further customization
     * @see RememberMeAuthenticationFilter#setAuthenticationSuccessHandler(AuthenticationSuccessHandler)
     */
    public RememberMeConfigurator authenticationSuccessHandler(AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        return this;
    }

    /**
     * Specify the {@link RememberMeServices} to use.
     * @param rememberMeServices the {@link RememberMeServices} to use
     * @return the {@link RememberMeConfigurator} for further customizations
     * @see RememberMeServices
     */
    public RememberMeConfigurator rememberMeServices(RememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
        return this;
    }

    @Override
    public void init(HttpConfiguration http) throws Exception {
        String key = getKey();
        RememberMeServices rememberMeServices = getRememberMeServices(http, key);
        http.setSharedObject(RememberMeServices.class, rememberMeServices);
        LogoutConfigurator logoutConfigurator = http.getConfigurator(LogoutConfigurator.class);
        if(logoutConfigurator != null) {
            logoutConfigurator.addLogoutHandler(logoutHandler);
        }

        RememberMeAuthenticationProvider authenticationProvider = new RememberMeAuthenticationProvider(
                key);
        http.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(HttpConfiguration http) throws Exception {
        RememberMeAuthenticationFilter rememberMeFilter = new RememberMeAuthenticationFilter(
                http.authenticationManager(), rememberMeServices);
        if (authenticationSuccessHandler != null) {
            rememberMeFilter
                    .setAuthenticationSuccessHandler(authenticationSuccessHandler);
        }
        http.addFilter(rememberMeFilter);
    }

    /**
     * Returns the HTTP parameter used to indicate to remember the user at time of login.
     * @return the HTTP parameter used to indicate to remember the user
     */
    String getRememberMeParameter() {
        return rememberMeParameter;
    }

    /**
     * Gets the {@link RememberMeServices} or creates the {@link RememberMeServices}.
     * @param http the {@link HttpConfiguration} to lookup shared objects
     * @param key the {@link #key(String)}
     * @return the {@link RememberMeServices} to use
     */
    private RememberMeServices getRememberMeServices(HttpConfiguration http,
            String key) {
        if (rememberMeServices != null) {
            if (rememberMeServices instanceof LogoutHandler
                    && logoutHandler == null) {
                this.logoutHandler = (LogoutHandler) rememberMeServices;
            }
            return rememberMeServices;
        }
        AbstractRememberMeServices tokenRememberMeServices = createRememberMeServices(
                http, key);
        tokenRememberMeServices.setParameter(rememberMeParameter);
        tokenRememberMeServices.setCookieName(rememberMeCookieName);
        if (tokenValiditySeconds != null) {
            tokenRememberMeServices
                    .setTokenValiditySeconds(tokenValiditySeconds);
        }
        if (useSecureCookie != null) {
            tokenRememberMeServices.setUseSecureCookie(useSecureCookie);
        }
        logoutHandler = tokenRememberMeServices;
        rememberMeServices = tokenRememberMeServices;
        return tokenRememberMeServices;
    }

    /**
     * Creates the {@link RememberMeServices} to use when none is provided. The
     * result is either {@link PersistentTokenRepository} (if a
     * {@link PersistentTokenRepository} is specified, else
     * {@link TokenBasedRememberMeServices}.
     *
     * @param http the {@link HttpConfiguration} to lookup shared objects
     * @param key the {@link #key(String)}
     * @return the {@link RememberMeServices} to use
     */
    private AbstractRememberMeServices createRememberMeServices(
            HttpConfiguration http, String key) {
        return tokenRepository == null ? createTokenBasedRememberMeServices(
                http, key) : createPersistentRememberMeServices(http, key);
    }

    /**
     * Creates {@link TokenBasedRememberMeServices}
     *
     * @param http the {@link HttpConfiguration} to lookup shared objects
     * @param key the {@link #key(String)}
     * @return the {@link TokenBasedRememberMeServices}
     */
    private AbstractRememberMeServices createTokenBasedRememberMeServices(
            HttpConfiguration http, String key) {
        UserDetailsService userDetailsService = getUserDetailsService(http);
        return new TokenBasedRememberMeServices(key, userDetailsService);
    }

    /**
     * Creates {@link PersistentTokenBasedRememberMeServices}
     *
     * @param http the {@link HttpConfiguration} to lookup shared objects
     * @param key the {@link #key(String)}
     * @return the {@link PersistentTokenBasedRememberMeServices}
     */
    private AbstractRememberMeServices createPersistentRememberMeServices(
            HttpConfiguration http, String key) {
        UserDetailsService userDetailsService = getUserDetailsService(http);
        return new PersistentTokenBasedRememberMeServices(key,
                userDetailsService, tokenRepository);
    }

    /**
     * Gets the {@link UserDetailsService} to use. Either the explicitly
     * configure {@link UserDetailsService} from
     * {@link #userDetailsService(UserDetailsService)} or a shared object from
     * {@link HttpConfiguration#getSharedObject(Class)}.
     *
     * @param http {@link HttpConfiguration} to get the shared {@link UserDetailsService}
     * @return the {@link UserDetailsService} to use
     */
    private UserDetailsService getUserDetailsService(HttpConfiguration http) {
        if(userDetailsService == null) {
            userDetailsService = http.getSharedObject(UserDetailsService.class);
        }
        if(userDetailsService == null) {
            throw new IllegalStateException("userDetailsService cannot be null. Invoke "
                    + RememberMeConfigurator.class.getSimpleName() + "#userDetailsService(UserDetailsService) or see its javadoc for alternative approaches.");
        }
        return userDetailsService;
    }

    /**
     * Gets the key to use for validating remember me tokens. Either the value
     * passed into {@link #key(String)}, or a secure random string if none was
     * specified.
     *
     * @return the remember me key to use
     */
    private String getKey() {
        if (key == null) {
            key = UUID.randomUUID().toString();
        }
        return key;
    }
}