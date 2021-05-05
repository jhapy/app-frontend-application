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

package org.jhapy.frontend.views;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import org.jhapy.frontend.utils.AppConst;

/**
 * Interface for views showing notifications to users
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
public interface HasNotifications extends HasElement {

  default void showNotification(String message) {
    showNotification(message, false);
  }

  default void showNotification(String message, boolean persistent) {
    if (persistent) {
      Button close = new Button("Close");
      close.getElement().setAttribute("theme", "tertiary small error");
      Notification notification = new Notification(new Text(message), close);
      notification.setPosition(Position.BOTTOM_START);
      notification.setDuration(0);
      close.addClickListener(event -> notification.close());
      notification.open();
    } else {
      Notification.show(message, AppConst.NOTIFICATION_DURATION, Position.BOTTOM_STRETCH);
    }
  }
}
