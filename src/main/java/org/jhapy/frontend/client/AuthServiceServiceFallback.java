package org.jhapy.frontend.client;

import feign.hystrix.FallbackFactory;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.RememberMeToken;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.authentification.ClearRememberMeTokenQuery;
import org.jhapy.dto.serviceQuery.authentification.CreateRememberMeTokenQuery;
import org.jhapy.dto.serviceQuery.authentification.ForgetPasswordQuery;
import org.jhapy.dto.serviceQuery.authentification.GetSecurityUserByRememberMeTokenQuery;
import org.jhapy.dto.serviceQuery.authentification.LoginQuery;
import org.jhapy.dto.serviceQuery.authentification.PasswordResetQuery;
import org.jhapy.dto.serviceQuery.authentification.ResetVerificationTokenQuery;
import org.jhapy.dto.serviceQuery.authentification.SignUpQuery;
import org.jhapy.dto.serviceQuery.authentification.ValidateUserQuery;
import org.jhapy.dto.serviceResponse.authentification.AuthResponse;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class AuthServiceServiceFallback implements AuthService, HasLogger,
    FallbackFactory<AuthServiceServiceFallback> {

  final Throwable cause;

  public AuthServiceServiceFallback() {
    this(null);
  }

  AuthServiceServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public AuthServiceServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage = StringUtils.isNotBlank(cause.getMessage()) ? cause.getMessage()
          : "Unknown error occurred : " + cause.toString();
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new AuthServiceServiceFallback(cause);
  }

  @Override
  public ServiceResult<AuthResponse> authenticateUser(LoginQuery query) {
    logger().error(getLoggerPrefix("authenticateUser") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<String> registerUser(SignUpQuery query) {
    logger().error(getLoggerPrefix("registerUser") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> resetVerificationToken(
      ResetVerificationTokenQuery query) {
    logger().error(getLoggerPrefix("resetVerificationToken") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> forgetPassword(
      @Valid ForgetPasswordQuery query) {
    logger().error(getLoggerPrefix("forgetPassword") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<String> validateUser(@Valid ValidateUserQuery query) {
    logger().error(getLoggerPrefix("validateUser") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> passwordReset(
      @Valid PasswordResetQuery query) {
    logger().error(getLoggerPrefix("passwordReset") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<RememberMeToken> createRememberMeToken(
      @Valid CreateRememberMeTokenQuery query) {
    logger().error(getLoggerPrefix("createRememberMeToken") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> clearRememberMeToken(
      @Valid ClearRememberMeTokenQuery query) {
    logger().error(getLoggerPrefix("clearRememberMeToken") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<SecurityUser> getSecurityUserByRememberMeToken(
      @Valid GetSecurityUserByRememberMeTokenQuery query) {
    logger().error(
        getLoggerPrefix("getSecurityUserByRememberMeToken") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}
