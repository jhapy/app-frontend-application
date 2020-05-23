package org.jhapy.frontend.utils;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapPosition implements Serializable {

  private Double latitude;
  private Double longitude;

  private String placeId;
}
