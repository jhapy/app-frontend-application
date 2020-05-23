package io.rocketbase.vaadin.croppie.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CroppieConfiguration {

  private CropPoints points;

  private Float zoom;

  /**
   * The outer container of the cropper
   * <p>
   * Default will default to the size of the container
   */
  private SizeConfig boundary;
  /**
   * A class of your choosing to add to the container to add custom styles to your croppie
   * <p>
   * Default ""
   */
  @Builder.Default
  private String customClass = "";

  /**
   * The inner container of the coppie. The visible part of the image
   * <p>
   * Default { width: 100, height: 100, type: 'square' }
   */
  @Builder.Default
  private ViewPortConfig viewport = ViewPortConfig.DEFAULT_VALUE;

  /**
   * Enable or disable support for resizing the viewport area.
   * <p>
   * Default false
   */
  private boolean enableResize;

  /**
   * Enable zooming functionality. If set to false - scrolling and pinching would not zoom.
   * <p>
   * Default true
   */
  @Builder.Default
  private boolean enableZoom = true;

  /**
   * Enable or disable the ability to use the mouse wheel to zoom in and out on a croppie instance.
   * <p>
   * Default true
   */
  @Builder.Default
  private boolean mouseWheelZoom = true;

  /**
   * Hide or Show the zoom slider
   * <p>
   * Default true
   */
  @Builder.Default
  private boolean showZoomer = true;

  public String getJsonString() {
    List<String> parameters = new ArrayList<>();
    if (points != null) {
      parameters.add(String.format("\"points\": %s", points.getJsonString()));
    }
    if (boundary != null) {
      parameters.add(String.format("\"boundary\": %s", boundary.getJsonString()));
    }
    if (customClass != null && customClass.isEmpty()) {
      parameters.add(String.format("\"customClass\": \"%s\"", customClass));
    }
    if (viewport != null) {
      parameters.add(String.format("\"viewport\": %s", viewport.getJsonString()));
    }
    if (zoom != null) {
      parameters.add(String.format("\"zoom\": %s", zoom));
    }
    parameters.add(String.format("\"enableResize\": %s", enableResize));
    parameters.add(String.format("\"enableZoom\": %s", enableZoom));
    parameters.add(String.format("\"mouseWheelZoom\": %s", mouseWheelZoom));
    parameters.add(String.format("\"showZoomer\": %s", showZoomer));

    StringBuilder result = new StringBuilder("{");
    int paramSize = parameters.size();
    for (int x = 0; x < paramSize; x++) {
      result.append(parameters.get(x));
      if (x != paramSize - 1) {
        result.append(", ");
      }
    }
    result.append("}");
    return result.toString();
  }


}
