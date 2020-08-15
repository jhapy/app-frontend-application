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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.claspina.confirmdialog.ButtonOption;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class DefaultDetailsView<T extends BaseEntity> extends ViewFrame implements
    RouterLayout, HasLogger, HasUrlParameter<String> {

  protected final String I18N_PREFIX;
  protected DetailsDrawer detailsDrawer;
  protected DetailsDrawerHeader detailsDrawerHeader;
  protected DetailsDrawerFooter detailsDrawerFooter;
  protected Binder<T> binder;
  protected T currentEditing;
  private final Class<T> entityType;
  private Function<T, ServiceResult<T>> saveHandler;
  private final Consumer<T> deleteHandler;
  private Tabs tabs;
  private final Class parentViewClassname;

  public DefaultDetailsView(String I18N_PREFIX, Class<T> entityType, Class parentViewClassname) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = null;
    this.deleteHandler = null;
    this.parentViewClassname = parentViewClassname;
  }

  public DefaultDetailsView(String I18N_PREFIX, Class<T> entityType, Class parentViewClassname,
      Function<T, ServiceResult<T>> saveHandler, Consumer<T> deleteHandler) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = saveHandler;
    this.deleteHandler = deleteHandler;
    this.parentViewClassname = parentViewClassname;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();

    setViewContent(createContent());
  }

  protected void setSaveHandler(Function<T, ServiceResult<T>> saveHandler) {
    this.saveHandler = saveHandler;
  }
  protected T getCurrentEditing() {
    return currentEditing;
  }

  protected void setCurrentEditing(T currentEditing) {
    this.currentEditing = currentEditing;
  }

  protected void initHeader() {
    AppBar appBar = JHapyMainView3.get().getAppBar();
    appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
    appBar.getContextIcon().addClickListener(event -> goBack());
    appBar.setTitle(getTitle(currentEditing));
  }

  protected void checkForDetailsChanges(Runnable action) {
    if (currentEditing != null && this.binder.hasChanges()) {
      org.claspina.confirmdialog.ConfirmDialog.createQuestion()
          .withCaption(getTranslation("element.global.unsavedChanged.title"))
          .withMessage(getTranslation("message.global.unsavedChanged"))
          .withOkButton(() -> action.run(), ButtonOption.focus(),
              ButtonOption.caption(getTranslation("action.global.yes")))
          .withCancelButton(ButtonOption.caption(getTranslation("action.global.no")))
          .open();
    } else {
      action.run();
    }
  }

  protected abstract String getTitle(T entity);

  protected abstract Component createDetails(T entity);

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(getContentTab());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  protected Component buildContent() {
    return new Div();
  }

  protected Tabs buildTabs() {
    Tab details = new Tab(getTranslation("element.title.details"));
    Tab audit = new Tab(getTranslation("element.title.audit"));

    tabs = new Tabs(details, audit);
    tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
    tabs.addSelectedChangeListener(e -> {
      Tab selectedTab = tabs.getSelectedTab();
      if (selectedTab.equals(details)) {
        detailsDrawer
            .setContent(createDetails(currentEditing));
      } else if (selectedTab.equals(audit)) {
        detailsDrawer
            .setContent(createAudit(currentEditing));
      }
    });

    return tabs;
  }

  private Component getContentTab() {
    detailsDrawer = new DetailsDrawer();
    detailsDrawer.setWidthFull();
    // Header

    tabs = buildTabs();

    if  ( tabs == null )
      detailsDrawer.setContent( buildContent());
    else
    detailsDrawer
        .setContent(createDetails(currentEditing));

    if ( tabs != null )
    detailsDrawerHeader = new DetailsDrawerHeader("", tabs, false, false);
    else
      detailsDrawerHeader = new DetailsDrawerHeader("", false, false);

    detailsDrawer.setHeader(detailsDrawerHeader);

    // Footer
    detailsDrawerFooter = new DetailsDrawerFooter();
    if (saveHandler == null || !canSave()) {
      detailsDrawerFooter.setSaveButtonVisible(false);
      detailsDrawerFooter.setSaveAndNewButtonVisible(false);
    }
    if (deleteHandler == null || !canDelete()) {
      detailsDrawerFooter.setDeleteButtonVisible(false);
    }

    detailsDrawerFooter.addCancelListener(e -> {
      detailsDrawer.hide();
      currentEditing = null;
    });
    if (saveHandler != null && canSave()) {
      detailsDrawerFooter.addSaveListener(e -> save(false));
      detailsDrawerFooter.addSaveAndNewListener(e -> save(true));
    }
    if (deleteHandler != null && canDelete()) {
      detailsDrawerFooter.addDeleteListener(e -> delete());
    }
    detailsDrawer.setFooter(detailsDrawerFooter);

    return detailsDrawer;
  }

  protected boolean canSave() {
    return true;
  }

  protected boolean canDelete() {
    return true;
  }

  protected void showDetails(T entity) {
    this.binder = new BeanValidationBinder<>(entityType);

    currentEditing = entity;
    detailsDrawer.setContent(createDetails(entity));
    tabs.setSelectedIndex(0);
  }

  protected Component createAudit(T entity) {
    TextField id = new TextField();
    id.setWidthFull();

    Checkbox isActive = new Checkbox();

    TextField created = new TextField();
    created.setWidthFull();

    TextField updated = new TextField();
    updated.setWidthFull();

    TextField createdBy = new TextField();
    createdBy.setWidthFull();

    TextField updatedBy = new TextField();
    updatedBy.setWidthFull();

    // Form layout
    FormLayout auditForm = new FormLayout();
    auditForm.addClassNames(LumoStyles.Padding.Bottom.L,
        LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    auditForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1,
            FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("26em", 2,
            FormLayout.ResponsiveStep.LabelsPosition.TOP));

    auditForm.addFormItem(id, getTranslation("element.baseEntity.id"));
    auditForm.addFormItem(isActive, getTranslation("element.baseEntity.isActive"));
    auditForm.addFormItem(created, getTranslation("element.baseEntity.created"));
    auditForm.addFormItem(updated, getTranslation("element.baseEntity.updated"));
    auditForm.addFormItem(createdBy, getTranslation("element.baseEntity.createdBy"));
    auditForm.addFormItem(updatedBy, getTranslation("element.baseEntity.updatedBy"));

    binder.bind(id, entity1 -> entity1.getId() == null ? null :
            entity1.getId().toString(),
        null);
    binder.bind(isActive, BaseEntity::getIsActive, BaseEntity::setIsActive);
    binder.bind(created, entity1 -> entity1.getCreated() == null ? ""
        : DateTimeFormatter.format(entity1.getCreated(), getLocale()), null);
    binder.bind(createdBy,
        activityDisplay -> entity.getCreatedBy(),
        null);
    binder.bind(updated, entity1 -> entity1.getModified() == null ? ""
        : DateTimeFormatter.format(entity1.getModified(), getLocale()),null);
    binder.bind(updatedBy,
        activityDisplay -> entity.getModifiedBy(),
        null);

    return auditForm;
  }

  protected boolean beforeSave(T entity) {
    return true;
  }

  protected void afterSave(T entity) {
  }

  protected boolean beforeDelete(T entity) {
    return true;
  }

  protected void afterDelete() {
  }

  private void save(boolean saveAndNew) {
    if (binder.writeBeanIfValid(currentEditing)) {
      boolean isNew = currentEditing.getId() == null;

      if (beforeSave(currentEditing)) {
        ServiceResult<T> result = saveHandler.apply(currentEditing);
        if (result.getIsSuccess() && result.getData() != null) {
          currentEditing = result.getData();
        } else {
          JHapyMainView.get()
              .displayErrorMessage(
                  getTranslation("message.global.unknownError", result.getMessage()));
          return;
        }
      }
      afterSave(currentEditing);

      JHapyMainView.get()
          .displayInfoMessage(getTranslation("message.global.recordSavedMessage"));

      if (saveAndNew) {
        try {
          showDetails(entityType.getDeclaredConstructor().newInstance());

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
        return;
      }

      if (isNew) {
        UI.getCurrent()
            .navigate(getClass(), currentEditing.getId().toString());
      } else {
        showDetails(currentEditing);
      }
    } else {
      BinderValidationStatus<T> validate = binder.validate();
      String errorText = validate.getFieldValidationStatuses()
          .stream().filter(BindingValidationStatus::isError)
          .map(BindingValidationStatus::getMessage)
          .map(Optional::get).distinct()
          .collect(Collectors.joining(", "));

      Notification
          .show(getTranslation("message.global.validationErrorMessage", errorText), 3000,
              Notification.Position.BOTTOM_CENTER);
    }
  }

  public void delete() {
    ConfirmDialog dialog = new ConfirmDialog(getTranslation("message.global.confirmDelete.title"),
        getTranslation("message.global.confirmDelete.message"),
        getTranslation("action.global.deleteButton"), event -> deleteConfirmed(),
        getTranslation("action.global.cancelButton"), event -> {
    });
    dialog.setConfirmButtonTheme("error primary");

    dialog.setOpened(true);
  }

  private void deleteConfirmed() {
    if (beforeDelete(currentEditing)) {
      deleteHandler.accept(currentEditing);
    }

    afterDelete();

    goBack();
  }

  protected void goBack() {
    UI.getCurrent().navigate(parentViewClassname);
  }
}
