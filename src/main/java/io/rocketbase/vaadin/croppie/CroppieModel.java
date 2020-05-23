package io.rocketbase.vaadin.croppie;

import com.vaadin.flow.templatemodel.TemplateModel;

public interface CroppieModel extends TemplateModel {

  void setCroppieOptions(String jsonString);

}
