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

package org.jhapy.frontend.components;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.FontWeight;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BorderRadius;

public class Initials extends FlexBoxLayout {

  private final String CLASS_NAME = "initials";

  public Initials(String initials) {
    setAlignItems(FlexComponent.Alignment.CENTER);
    setBackgroundColor(LumoStyles.Color.Contrast._10);
    setBorderRadius(BorderRadius.L);
    setClassName(CLASS_NAME);
    UIUtils.setFontSize(FontSize.S, this);
    UIUtils.setFontWeight(FontWeight._600, this);
    setHeight(LumoStyles.Size.M);
    setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
    setWidth(LumoStyles.Size.M);

    add(initials);
  }

}
