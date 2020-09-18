package org.jhapy.frontend.utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Data
public class SessionInfo implements Serializable {

  private String username;
  private String sourceIp;
  private LocalDateTime loginDateTime;
  private LocalDateTime logoutDateTime;
  private LocalDateTime lastContact;
  private String jSessionId;
}
