/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jhapy")
public class AppProperties extends org.jhapy.commons.config.AppProperties {

  private final Authorization authorization = new Authorization();

  private final LoginForm loginForm = new LoginForm();

  @Data
  public static class LoginForm {

    private Boolean displaySignup = Boolean.TRUE;
    private Boolean displayForgetPassword = Boolean.TRUE;
    private Boolean displaySocialLogin = Boolean.TRUE;
    private Boolean displayRememberMe = Boolean.TRUE;
    private Boolean displayLanguage = Boolean.TRUE;
  }

  @Data
  public static final class Authorization {
    private Boolean forceRealmToHttps = Boolean.FALSE;
    private String facebookUrl;
    private String googleUrl;
    private String publicKey;
  }
}
