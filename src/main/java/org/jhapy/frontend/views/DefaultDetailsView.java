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
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.shared.Registration;
import dev.mett.vaadin.tooltip.Tooltips;
import dev.mett.vaadin.tooltip.config.TC_HIDE_ON_CLICK;
import dev.mett.vaadin.tooltip.config.TooltipConfiguration;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.claspina.confirmdialog.ButtonOption;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.DateTimeFormatter;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class DefaultDetailsView<T extends BaseEntity> extends ViewFrame implements
    HasLogger, HasUrlParameter<String> {

  protected static final Set<String> rtlSet;

  static {

    // Yiddish
    rtlSet = Set.of("ar", // Arabic
        "dv", // Divehi
        "fa", // Persian
        "ha", // Hausa
        "he", // Hebrew
        "iw", // Hebrew
        "ji", // Yiddish
        "ps", // Pushto
        "sd", // Sindhi
        "ug", // Uighur
        "ur", // Urdu
        "yi");
  }

  protected final String I18N_PREFIX;
  protected DetailsDrawerFooter detailsDrawerFooter;
  protected Binder<T> binder;
  protected T currentEditing;
  private final Class<T> entityType;
  private Function<T, ServiceResult<T>> saveHandler;
  private final Consumer<T> deleteHandler;
  private Class parentViewClassname;
  private AppBar appBar;
  protected final MyI18NProvider myI18NProvider;
  protected Registration contextIconRegistration = null;
  protected DefaultDetailsContent detailsDrawer;

  public DefaultDetailsView(String I18N_PREFIX, Class<T> entityType, Class parentViewClassname,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = null;
    this.deleteHandler = null;
    this.parentViewClassname = parentViewClassname;
    this.myI18NProvider = myI18NProvider;
  }

  public DefaultDetailsView(String I18N_PREFIX, Class<T> entityType, Class parentViewClassname,
      Function<T, ServiceResult<T>> saveHandler, Consumer<T> deleteHandler,
      MyI18NProvider myI18NProvider) {
    this(null, I18N_PREFIX, entityType, parentViewClassname, saveHandler, deleteHandler,
        myI18NProvider);
  }

  public DefaultDetailsView(AppBar appBar, String I18N_PREFIX, Class<T> entityType,
      Class parentViewClassname,
      Function<T, ServiceResult<T>> saveHandler, Consumer<T> deleteHandler,
      MyI18NProvider myI18NProvider) {
    super();
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.binder = new BeanValidationBinder<>(entityType);
    this.saveHandler = saveHandler;
    this.deleteHandler = deleteHandler;
    this.parentViewClassname = parentViewClassname;
    this.appBar = appBar;
    this.myI18NProvider = myI18NProvider;
  }

  protected AppBar getAppBar() {
    return JHapyMainView3.get().getAppBar();
  }


  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();

    setViewContent(createContent());
  }

  @Override
  protected void onDetach(DetachEvent detachEvent) {
    if (contextIconRegistration != null) {
      contextIconRegistration.remove();
    }
  }

  protected void setParentViewClassname(Class parentViewClassname) {
    this.parentViewClassname = parentViewClassname;
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
    appBar = JHapyMainView3.get().getAppBar();
    appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
    if (contextIconRegistration == null) {
      contextIconRegistration = appBar.getContextIcon().addClickListener(event -> goBack());
    }
    appBar.setTitle(getTitle(currentEditing));

    if (canCreateRecord() && saveHandler != null) {
      Button newRecordButton = UIUtils.createTertiaryButton(VaadinIcon.PLUS);
      newRecordButton.addClickListener(event -> {
        try {
          showDetails(getNewInstance());
          appBar.setTitle(getTitle(currentEditing));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        }
      });
      appBar.addActionItem(newRecordButton);
    }

    if (isShowTabs()) {
      buildTabs();
    }
  }

  protected T getNewInstance()
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    return entityType.getDeclaredConstructor().newInstance();
  }

  protected boolean canCreateRecord() {
    return true;
  }

  protected void checkForDetailsChanges(Runnable action) {
    if (currentEditing != null && this.binder.hasChanges()) {
      org.claspina.confirmdialog.ConfirmDialog.createQuestion()
          .withCaption(getTranslation("element.global.unsavedChanged.title"))
          .withMessage(getTranslation("message.global.unsavedChanged"))
          .withOkButton(action::run, ButtonOption.focus(),
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
    return getContentTab();
  }

  protected Component buildContent() {
    return new Div();
  }

  protected void buildTabs() {
    Tab details = getAppBar().addTab(getTranslation("element.title.details"));
    Tab audit = getAppBar().addTab(getTranslation("element.title.audit"));

    getAppBar().addTabSelectionListener(selectedChangeEvent -> {
      Tab selectedTab = selectedChangeEvent.getSelectedTab();
      if (selectedTab.equals(details)) {
        detailsDrawer.setContent(createDetails(currentEditing));
      } else if (selectedTab.equals(audit)) {
        detailsDrawer.setContent(createAudit(currentEditing));
      }
    });
  }

  protected boolean isShowTabs() {
    return true;
  }

  protected boolean isShowFooter() {
    return true;
  }

  private Component getContentTab() {
    detailsDrawer = new DefaultDetailsContent(createDetails(currentEditing));

    FlexBoxLayout contentTab = new FlexBoxLayout();
    contentTab.add(detailsDrawer);
    contentTab.setFlexGrow(1, detailsDrawer);
    contentTab.setSizeFull();
    contentTab.setFlexDirection(FlexDirection.COLUMN);

    if (isShowFooter()) {
      detailsDrawerFooter = new DetailsDrawerFooter();
      detailsDrawerFooter.setWidth("");
      if (saveHandler == null || !canSave()) {
        detailsDrawerFooter.setSaveButtonVisible(false);
        detailsDrawerFooter.setSaveAndNewButtonVisible(false);
      }
      if (deleteHandler == null || !canDelete()) {
        detailsDrawerFooter.setDeleteButtonVisible(false);
      }

      detailsDrawerFooter.addCancelListener(e -> {
        currentEditing = null;
        UI.getCurrent().getPage().getHistory().back();
      });
      if (saveHandler != null && canSave()) {
        detailsDrawerFooter.addSaveListener(e -> save(false));
        detailsDrawerFooter.addSaveAndNewListener(e -> save(true));
      }
      if (deleteHandler != null && canDelete()) {
        detailsDrawerFooter.addDeleteListener(e -> delete());
      }

      contentTab.add(detailsDrawerFooter);
    }
    return contentTab;
  }

  protected boolean canSave() {
    return true;
  }

  protected boolean canDelete() {
    return true;
  }

  protected void showDetails(T entity) {
    if (entity == null) {
      return;
    }

    this.binder = new BeanValidationBinder<>(entityType);
    currentEditing = entity;
    detailsDrawer.setContent(createDetails(entity));
    if (appBar != null && appBar.getTabCount() > 0) {
      appBar.setSelectedTabIndex(0);
    }
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
    auditForm.setWidthFull();

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
        : DateTimeFormatter.format(entity1.getModified(), getLocale()), null);
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
        if (result != null && result.getIsSuccess() && result.getData() != null) {
          currentEditing = result.getData();
        } else {
          JHapyMainView3.get()
              .displayErrorMessage(
                  getTranslation("message.global.unknownError", result.getMessage()));
          return;
        }
      }
      afterSave(currentEditing);

      JHapyMainView3.get()
          .displayInfoMessage(getTranslation("message.global.recordSavedMessage"));

      if (saveAndNew) {
        try {
          showDetails(entityType.getDeclaredConstructor().newInstance());

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
        return;
      }

      if (isNew) {
        redirectAfterNewRecordSave(currentEditing);
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

      String errorText2 = validate.getBeanValidationErrors()
          .stream()
          .map(ValidationResult::getErrorMessage)
          .collect(Collectors.joining(", "));

      Notification
          .show(
              getTranslation("message.global.validationErrorMessage", errorText + errorText2),
              3000,
              Notification.Position.BOTTOM_CENTER);
    }
  }

  protected void redirectAfterNewRecordSave(T entity) {
    UI.getCurrent().navigate(getClass(), entity.getId().toString());
  }

  public void delete() {
    ConfirmDialog dialog = new ConfirmDialog(
        getTranslation("message.global.confirmDelete.title"),
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

  protected Label getLabel(String element) {
    Label label = new Label(getTranslation(element));
    TooltipConfiguration ttconfig = new TooltipConfiguration(
        myI18NProvider.getTooltip(element));
    ttconfig.setDelay(1000);
    ttconfig.setHideOnClick(TC_HIDE_ON_CLICK.TRUE);
    ttconfig.setShowOnCreate(false);

    Tooltips.getCurrent().setTooltip(label, ttconfig);
    return label;
  }

  protected Button getButton(String action) {
    return getButton(null, action, false, true);
  }

  protected Button getButton(String action, boolean isSmall) {
    return getButton(null, action, isSmall, true);
  }

  protected Button getButton(VaadinIcon icon, String action) {
    return getButton(icon, action, false, false);
  }

  protected Button getButton(VaadinIcon icon, String action, boolean isSmall,
      boolean displayText) {
    Button button;
    if (isSmall) {
      if (displayText) {
        if (icon == null) {
          button = UIUtils.createSmallButton(getTranslation(action));
        } else {
          button = UIUtils.createSmallButton(getTranslation(action), icon);
        }
      } else {
        button = UIUtils.createSmallButton(icon);
      }
    } else if (displayText) {
      if (icon == null) {
        button = UIUtils.createButton(getTranslation(action));
      } else {
        button = UIUtils.createButton(getTranslation(action), icon);
      }
    } else {
      button = UIUtils.createButton(icon);
    }
    TooltipConfiguration ttconfig = new TooltipConfiguration(myI18NProvider.getTooltip(action));
    ttconfig.setDelay(1000);
    ttconfig.setHideOnClick(TC_HIDE_ON_CLICK.TRUE);
    ttconfig.setShowOnCreate(false);

    Tooltips.getCurrent().setTooltip(button, ttconfig);

    return button;
  }
}
