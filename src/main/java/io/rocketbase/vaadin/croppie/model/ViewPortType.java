package io.rocketbase.vaadin.croppie.model;

public enum ViewPortType {

  SQUARE,
  CIRCLE;

  public static ViewPortType fromString(String key) {
    return key == null
        ? null
        : ViewPortType.valueOf(key.toUpperCase());
  }

  public String getKey() {
    return name().toLowerCase();
  }

}
