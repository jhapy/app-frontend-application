package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import java.io.Serializable;

@Tag("full-width-custom-field")
@NpmPackage(value = "@vaadin/vaadin-custom-field", version = "1.0.11")
@JsModule("./full-width-custom-field.js")
public abstract class FullWidthCustomField<T> extends CustomField<T> implements Serializable {

}
