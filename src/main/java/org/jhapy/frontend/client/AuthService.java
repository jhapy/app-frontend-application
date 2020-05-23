package org.jhapy.frontend.client;

import javax.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

@FeignClient(name = "${app.remote-services.authorization-server.name:null}", url = "${app.remote-services.authorization-server.url:}", path = "/uaa/auth", fallbackFactory = AuthServiceServiceFallback.class)
public interface AuthService {

  @PostMapping(value = "/login")
  ServiceResult<AuthResponse> authenticateUser(@RequestBody LoginQuery query);

  @PostMapping(value = "/signup")
  ServiceResult<String> registerUser(@RequestBody SignUpQuery query);

  @PostMapping(value = "/resetVerificationToken")
  ServiceResult<Void> resetVerificationToken(
      @RequestBody ResetVerificationTokenQuery query);

  @PostMapping(value = "/forgetPassword")
  ServiceResult<Void> forgetPassword(@Valid @RequestBody ForgetPasswordQuery query);

  @PostMapping(value = "/validateUser")
  ServiceResult<String> validateUser(@Valid @RequestBody ValidateUserQuery query);

  @PostMapping(value = "/passwordReset")
  ServiceResult<Void> passwordReset(@Valid @RequestBody PasswordResetQuery query);

  @PostMapping(value = "/createRememberMeToken")
  ServiceResult<RememberMeToken> createRememberMeToken(
      @Valid @RequestBody CreateRememberMeTokenQuery query);

  @PostMapping(value = "/clearRememberMeToken")
  ServiceResult<Void> clearRememberMeToken(
      @Valid @RequestBody ClearRememberMeTokenQuery query);

  @PostMapping(value = "/getSecurityUserIdByRememberMeToken")
  ServiceResult<SecurityUser> getSecurityUserByRememberMeToken(
      @Valid @RequestBody GetSecurityUserByRememberMeTokenQuery query);

}
