package org.jhapy.frontend.utils.css;

public enum TextOverflow {

  CLIP("clip"),
  ELLIPSIS("ellipsis");

  private String value;

  TextOverflow(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
