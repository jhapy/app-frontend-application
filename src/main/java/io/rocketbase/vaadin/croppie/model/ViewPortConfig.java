package io.rocketbase.vaadin.croppie.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ViewPortConfig extends SizeConfig {

  public static ViewPortConfig DEFAULT_VALUE = new ViewPortConfig(100, 100, ViewPortType.SQUARE);

  private ViewPortType type;

  public ViewPortConfig(int width, int height, ViewPortType type) {
    super(width, height);
    this.type = type;
  }


  public String getJsonString() {
    return String
        .format("{\"width\": %d,\"height\": %d, \"type\": \"%s\" }", getWidth(), getHeight(),
            getType().getKey());
  }
}
