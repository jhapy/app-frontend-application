/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Right;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-27
 */
public class DateTimePicker extends CustomField<LocalDateTime> implements Serializable {

  private final DatePicker datePicker = new DatePicker();
  private final TimePicker timePicker = new TimePicker();

  public DateTimePicker() {
    this(null);
  }

  public DateTimePicker(String label) {
    if (label != null) {
      setLabel(label);
    }
    datePicker.setWidth("150px");
    timePicker.setWidth("130px");
    FlexBoxLayout layout = new FlexBoxLayout(datePicker, timePicker);
    layout.setSpacing(Right.XS);
    add(layout);
  }

  @Override
  protected LocalDateTime generateModelValue() {
    final LocalDate date = datePicker.getValue();
    LocalTime time = timePicker.getValue();
    if (time == null) {
      time = LocalTime.of(0, 0);
    }
    return date != null && time != null ?
        LocalDateTime.of(date, time) :
        null;
  }

  @Override
  protected void setPresentationValue(
      LocalDateTime newPresentationValue) {
    datePicker.setValue(newPresentationValue != null ?
        newPresentationValue.toLocalDate() :
        null);
    timePicker.setValue(newPresentationValue != null ?
        newPresentationValue.toLocalTime() :
        null);

  }

}