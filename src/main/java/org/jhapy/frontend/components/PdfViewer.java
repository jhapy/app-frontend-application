package org.jhapy.frontend.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import java.net.URI;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 9/12/19
 */
@Tag("pdf-browser-viewer")
@NpmPackage(value = "@polymer/paper-button", version = "3.0.1")
@NpmPackage(value = "@polymer/paper-card", version = "3.0.1")
@JsModule("./pdf-viewer/pdf-browser-viewer.js")
public class PdfViewer extends Component {

  private StreamRegistration streamRegistration;

  public PdfViewer(StreamResource streamResource) {
    streamRegistration = VaadinSession.getCurrent().getResourceRegistry()
        .registerResource(streamResource);
    URI uri = StreamResourceRegistry.getURI(streamResource);
    setFile(uri.toASCIIString());
  }

  public void setFile(String file) {
    getElement().setAttribute("file", file);
  }

  public void setNotSupportedMessage(String message) {
    getElement().setAttribute("not-supported-message", message);
  }

  public void setNotSupportedLinkMessage(String message) {
    getElement().setAttribute("not-supported-link-message", message);
  }

  public void setCard(boolean card) {
    getElement().setAttribute("card", card);
  }

  public void setDownloadLabel(String label) {
    getElement().setAttribute("downloadLabel", label);
  }

  public void setElevation(String elevation) {
    getElement().setAttribute("elevation", elevation);
  }

  public void setHeight(String height) {
    getElement().setAttribute("height", height);
    getElement().getStyle().set("height", height);
  }

  public void setWidth(String width) {
    getElement().setAttribute("width", width);
    getElement().getStyle().set("width", width);
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    super.onDetach(detachEvent);
    if (streamRegistration != null) {
      streamRegistration.unregister();
    }
  }
}
