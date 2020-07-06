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

package org.jhapy.frontend.views.admin.i18n;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.i18n.Action;
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.FindByActionQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.components.CheckboxColumnComponent;
import org.jhapy.frontend.components.ImportFileDialog;
import org.jhapy.frontend.customFields.ActionTrlListField;
import org.jhapy.frontend.dataproviders.ActionDataProvider;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;
import org.jhapy.frontend.views.JHapyMainView;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.olli.FileDownloadWrapper;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@I18NPageTitle(messageKey = AppConst.PAGE_ACTIONS)
@Secured({SecurityConst.ROLE_I18N_WRITE, SecurityConst.ROLE_ADMIN})
public class ActionsView extends DefaultMasterDetailsView<Action, DefaultFilter> {

  public ActionsView(MyI18NProvider myI18NProvider) {
    super("action.", Action.class, new ActionDataProvider(),
        (e) -> {
          ServiceResult<Action> _elt = I18NServices.getActionService().save(new SaveQuery<>(e));
          if (_elt.getIsSuccess()) {
            myI18NProvider.reloadActions();
          }
          return _elt;
        },
        e -> I18NServices.getActionService().delete(new DeleteByIdQuery(e.getId())));
  }

  @Override
  protected boolean beforeSave(Action entity) {
    long hasDefault = 0;
    List<ActionTrl> actionTrlList = entity.getTranslations();
    for (ActionTrl actionTrl : actionTrlList) {
      if (actionTrl != null && actionTrl.getIsDefault() != null && actionTrl.getIsDefault()) {
        hasDefault++;
      }
    }
    if (hasDefault == 0) {
      JHapyMainView.get().displayErrorMessage(getTranslation(
          "message.global.translationNeedsDefault"));
    } else if (hasDefault > 1) {
      JHapyMainView.get().displayErrorMessage(getTranslation(
          "message.global.translationMaxDefault"));
    }
    return hasDefault == 1;
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem()
        .ifPresent(this::showDetails));

    grid.setDataProvider(dataProvider);
    grid.setHeight("100%");

    Column categoryColumn = grid.addColumn(Action::getCategory).setKey("category")
        .setSortable(true);
    Column nameColumn = grid.addColumn(Action::getName).setKey("name").setSortable(true);
    grid.addComponentColumn(action -> new CheckboxColumnComponent(action.getIsTranslated()))
        .setKey("isTranslated").setSortable(true);

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
      }
    });

    HeaderRow headerRow = grid.prependHeaderRow();

    HeaderCell buttonsCell = headerRow.join(categoryColumn, nameColumn);

    Button exportI18NButton = new Button(getTranslation("action.i18n.download"));
    exportI18NButton.addClickListener(buttonClickEvent -> {
      ServiceResult<Byte[]> result = I18NServices.getI18NService()
          .getI18NFile(new BaseRemoteQuery());
      final StreamResource resource = new StreamResource("i18n.xlsx",
          () -> new ByteArrayInputStream(ArrayUtils.toPrimitive(result.getData())));
      final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry()
          .registerResource(resource);
      UI.getCurrent().getPage().setLocation(registration.getResourceUri());
    });

    Button importI18NButton = new Button(getTranslation("action.i18n.upload"));
    importI18NButton.addClickListener(buttonClickEvent -> {
      ImportFileDialog<byte[]> importFileDialog = new ImportFileDialog();
      importFileDialog
          .open(getTranslation("element.i18n.upload"), getTranslation("message.i18n.upload"), null,
              getTranslation("action.i18n.upload"), bytes -> {
                importFileDialog.close();
                ServiceResult<Void> result = I18NServices.getI18NService()
                    .importI18NFile(new ImportI18NFileQuery(ArrayUtils.toObject(bytes)));
                if (result.getIsSuccess()) {
                  JHapyMainView.get()
                      .displayInfoMessage(getTranslation("message.fileImport.success"));
                } else {
                  JHapyMainView.get().displayInfoMessage(
                      getTranslation("message.fileImport.error", result.getMessage()));
                }
              }, () -> importFileDialog.close());
    });

    HorizontalLayout headerHLayout = new HorizontalLayout(exportI18NButton, importI18NButton);
    buttonsCell.setComponent(headerHLayout);

    return grid;
  }

  protected Component createDetails(Action action) {
    boolean isNew = action.getId() == null;
    detailsDrawerHeader.setTitle(isNew ? getTranslation("element.global.new") + " : "
        : getTranslation("element.global.update") + " : " + action.getName());

    detailsDrawerFooter.setDeleteButtonVisible(!isNew);

    TextField name = new TextField();
    name.setWidth("100%");

    TextField categoryField = new TextField();
    categoryField.setWidth("100%");

    Checkbox isActive = new Checkbox();

    Checkbox isTranslated = new Checkbox();

    ActionTrlListField actionTrl = new ActionTrlListField();
    actionTrl.setReadOnly(false);
    actionTrl.setWidth("100%");

    // Form layout
    FormLayout editingForm = new FormLayout();
    editingForm.addClassNames(LumoStyles.Padding.Bottom.L,
        LumoStyles.Padding.Horizontal.L, LumoStyles.Padding.Top.S);
    editingForm.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1,
            FormLayout.ResponsiveStep.LabelsPosition.TOP),
        new FormLayout.ResponsiveStep("21em", 2,
            FormLayout.ResponsiveStep.LabelsPosition.TOP));

    FormLayout.FormItem nameItem = editingForm
        .addFormItem(name, getTranslation("element." + I18N_PREFIX + "name"));
    FormLayout.FormItem categoryItem = editingForm
        .addFormItem(categoryField, getTranslation("element." + I18N_PREFIX + "category"));
    FormLayout.FormItem translationsItem = editingForm
        .addFormItem(actionTrl, getTranslation("element." + I18N_PREFIX + "translations"));
    editingForm
        .addFormItem(isTranslated, getTranslation("element." + I18N_PREFIX + "isTranslated"));
    editingForm
        .addFormItem(isActive, getTranslation("element." + I18N_PREFIX + "isActive"));

    UIUtils.setColSpan(2, nameItem, categoryItem, translationsItem);

    if (action.getTranslations().size() == 0) {
      action.setTranslations(
          I18NServices.getActionTrlService().findByAction(new FindByActionQuery(action.getId()))
              .getData());
    }

    binder.setBean(action);

    binder.bind(name, Action::getName, Action::setName);
    binder.bind(categoryField, Action::getCategory, Action::setCategory);
    binder.bind(isActive, Action::getIsActive, Action::setIsActive);
    binder.bind(isTranslated, Action::getIsTranslated, Action::setIsTranslated);
    binder.bind(actionTrl, Action::getTranslations, Action::setTranslations);

    return editingForm;
  }

  protected void filter(String filter) {
    dataProvider
        .setFilter(new DefaultFilter(
            StringUtils.isBlank(filter) ? null : "%" + filter + "%",
            Boolean.TRUE));
  }
}
