package org.jhapy.frontend.views;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
public interface HasConfirmation {

  ConfirmDialog getConfirmDialog();

  void setConfirmDialog(ConfirmDialog confirmDialog);
}
