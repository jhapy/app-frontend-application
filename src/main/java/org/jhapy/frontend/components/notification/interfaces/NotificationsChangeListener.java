package org.jhapy.frontend.components.notification.interfaces;

import org.jhapy.frontend.components.notification.NotificationHolder;
import org.jhapy.frontend.components.notification.interfaces.Notification;

public interface NotificationsChangeListener<T extends Notification> {
    default void onNotificationChanges(NotificationHolder<T> holder) {
    }

    default void onNotificationAdded(T notification) {
    }

    default void onNotificationRemoved(T notification) {
    }
}
