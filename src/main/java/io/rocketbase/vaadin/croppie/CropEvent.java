package io.rocketbase.vaadin.croppie;

import com.vaadin.flow.component.ComponentEvent;
import io.rocketbase.vaadin.croppie.model.CropPoints;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CropEvent extends ComponentEvent<Croppie> {

  private CropPoints points;
  private float zoom;

  public CropEvent(Croppie source, boolean fromClient, CropPoints points, float zoom) {
    super(source, fromClient);
    this.points = points;
    this.zoom = zoom;
  }
}
