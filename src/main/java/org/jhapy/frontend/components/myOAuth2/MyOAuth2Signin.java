package org.jhapy.frontend.components.myOAuth2;

import static java.util.Objects.requireNonNull;

import com.github.scribejava.core.model.Response;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.templatemodel.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Tag("sign-in-2")
@JsModule("./myOAuth2/signin.js")
public abstract class MyOAuth2Signin extends PolymerTemplate<TemplateModel> {

  private final List<Consumer<String>> loginListeners = new ArrayList<>();
  private String accessToken;
  @Id("signin-button")
  private Button signinButton;

  protected MyOAuth2Signin(String authUrl, String redirectUri) {

    requireNonNull(authUrl);
    requireNonNull(redirectUri);

    addListener(AccessTokenReceivedEvent.class, e -> onAccessTokenReceived(e.getAccessToken()));

    getElement().setAttribute("auth-url", authUrl);
    getElement().setAttribute("redirect-uri", redirectUri);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    configureButton(signinButton);
  }

  protected abstract void configureButton(Button button);

  private void onAccessTokenReceived(String accessToken) {
    requireNonNull(accessToken);
    this.accessToken = accessToken;
    loginListeners.forEach(listener -> listener.accept(accessToken));
  }

  /**
   * An error-response was returned from the auth-provider. The default implementation will silently
   * return.
   *
   * @param response the response
   */
  protected void onResponseError(Response response) {
  }

  public String getAccessToken() {
    return accessToken;
  }

  public Registration addLoginListener(Consumer<String> listener) {
    requireNonNull(listener);

    loginListeners.add(listener);
    return () -> loginListeners.remove(listener);
  }
}
