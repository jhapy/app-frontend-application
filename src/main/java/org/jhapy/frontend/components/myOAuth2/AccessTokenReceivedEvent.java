package org.jhapy.frontend.components.myOAuth2;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

@DomEvent("oauth-success")
public class AccessTokenReceivedEvent extends ComponentEvent<MyOAuth2Signin> {

  private final String accessToken;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client
   */
  public AccessTokenReceivedEvent(MyOAuth2Signin source, boolean fromClient,
      @EventData("event.detail.token") String accessToken) {
    super(source, fromClient);
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
