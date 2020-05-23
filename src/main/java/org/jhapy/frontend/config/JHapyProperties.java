package org.jhapy.frontend.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app")
public class JHapyProperties {

  private final RemoteServices remoteServices = new RemoteServices();

  private final Authorization authorization = new Authorization();

  private final LoginForm loginForm = new LoginForm();

  private final Security security = new Security();

  @Data
  public static class LoginForm {

    private Boolean displaySignup = Boolean.TRUE;
    private Boolean displayForgetPassword = Boolean.TRUE;
    private Boolean displaySocialLogin = Boolean.TRUE;
    private Boolean displayRememberMe = Boolean.TRUE;
    private Boolean displayLanguage = Boolean.TRUE;
  }

  @Data
  public static class RemoteServices {

    private RemoteServer backendServer = new RemoteServer();
    private RemoteServer authorizationServer = new RemoteServer();
    private RemoteServer i18nServer = new RemoteServer();
    private RemoteServer resourceServer = new RemoteServer();
    private RemoteServer auditServer = new RemoteServer();

    @Data
    public static final class RemoteServer {

      private String url;
      private String name;
    }
  }

  @Data
  public static final class Authorization {

    private String facebookUrl;
    private String googleUrl;
    private String publicKey;
  }

  @Data
  public static class Security {
    private final Security.ClientAuthorization clientAuthorization = new Security.ClientAuthorization();
    private final Security.Authentication authentication = new Security.Authentication();
    private final Security.RememberMe rememberMe = new Security.RememberMe();
    private final Security.OAuth2 oauth2 = new Security.OAuth2();

    @Data
    public static class OAuth2 {
      private List<String> audience = new ArrayList();
    }

    @Data
    public static class RememberMe {
      private String key;
    }

    @Data
    public static class Authentication {
      private final Security.Authentication.Jwt jwt = new Security.Authentication.Jwt();

      public Authentication() {
      }

      @Data
      public static class Jwt {
        private String secret;
        private String base64Secret;
        private long tokenValidityInSeconds = 1800L;
        private long tokenValidityInSecondsForRememberMe = 2592000L;
      }
    }

    @Data
    public static class ClientAuthorization {
      private String accessTokenUri;
      private String tokenServiceId;
      private String clientId;
      private String clientSecret;
    }
  }
}
