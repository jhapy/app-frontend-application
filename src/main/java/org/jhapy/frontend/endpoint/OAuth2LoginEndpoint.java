package org.jhapy.frontend.endpoint;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-17
 */
@Controller
public class OAuth2LoginEndpoint {

  @GetMapping("/oauth2/redirect")
  public String redirect(String token) {

    return "redirect:/frontend/myOAuth2/popupCallback.html?access_token=" + token;
  }
}
