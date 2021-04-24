package org.jhapy.frontend.config;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.jhapy.commons.utils.HasLogger;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 03/11/2020
 */
public class CustomOAuth2AuthorizationRequestResolver implements
    OAuth2AuthorizationRequestResolver, HasLogger {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final boolean forceHttps;

    public CustomOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository repo, String authorizationRequestBaseUri, boolean forceHttps) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo,
            authorizationRequestBaseUri);
        this.forceHttps = forceHttps;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        if (req != null) {
            req = customizeAuthorizationRequest(req);
        }
        return req;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
        String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        if (req != null) {
            req = customizeAuthorizationRequest(req);
        }
        return req;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
        OAuth2AuthorizationRequest req) {
        String loggerPrefix = getLoggerPrefix("customizeAuthorizationRequest", forceHttps);
        logger().debug(loggerPrefix + "Initial Redirect URI = " + req.getRedirectUri());
        if (forceHttps) {
            URI uri = null;
            try {
                uri = new URI(req.getRedirectUri());
                String newRedirectUri = "https://" + uri.getAuthority() + uri.getPath();
                logger().debug(loggerPrefix + "New redirect URI = " + newRedirectUri);
                return OAuth2AuthorizationRequest.from(req).redirectUri(newRedirectUri).build();
            } catch (URISyntaxException e) {
                logger().error(loggerPrefix + "Unexpected error : " + e.getMessage(), e);
            }
        }
        return OAuth2AuthorizationRequest.from(req).build();
    }
}
