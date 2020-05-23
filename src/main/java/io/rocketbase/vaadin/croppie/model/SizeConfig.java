package io.rocketbase.vaadin.croppie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SizeConfig {

  private int width;
  private int height;

  public String getJsonString() {
    return String.format("{\"width\": %d,\"height\": %d }", width, height);
  }

}
