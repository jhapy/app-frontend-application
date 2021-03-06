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

package org.jhapy.frontend.layout.size;

public enum Horizontal implements Size {

  AUTO("auto", null),

  XS("var(--lumo-space-xs)", "spacing-h-xs"), S("var(--lumo-space-s)",
      "spacing-h-s"), M("var(--lumo-space-m)", "spacing-h-m"), L(
      "var(--lumo-space-l)",
      "spacing-h-l"), XL("var(--lumo-space-xl)", "spacing-h-xl"),

  RESPONSIVE_M("var(--lumo-space-r-m)", null), RESPONSIVE_L(
      "var(--lumo-space-r-l)",
      null), RESPONSIVE_X("var(--lumo-space-r-x)", null);

  private final String variable;
  private final String spacingClassName;

  Horizontal(String variable, String spacingClassName) {
    this.variable = variable;
    this.spacingClassName = spacingClassName;
  }

  @Override
  public String[] getMarginAttributes() {
    return new String[]{"margin-left", "margin-right"};
  }

  @Override
  public String[] getPaddingAttributes() {
    return new String[]{"padding-left", "padding-right"};
  }

  @Override
  public String getSpacingClassName() {
    return this.spacingClassName;
  }

  @Override
  public String getVariable() {
    return this.variable;
  }
}
