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

package org.jhapy.frontend.views.admin.swagger;

import static org.jhapy.frontend.utils.AppConst.PAGE_ADMIN_SWAGGER_DETAILS;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.domain.security.SecurityRole;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.domain.security.SecurityUserTypeEnum;
import org.jhapy.dto.registry.EurekaApplication;
import org.jhapy.dto.registry.EurekaInfo;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.client.registry.RegistryServices;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.bar.AppBar.NaviMode;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.dataproviders.SecurityUserDataProvider;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.renderer.BooleanOkRenderer;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.DefaultMasterDetailsView;
import org.jhapy.frontend.views.DefaultMasterView;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.gatanaso.MultiselectComboBox;


@I18NPageTitle(messageKey = AppConst.TITLE_SWAGGERS_ADMIN)
@Secured(SecurityConst.ROLE_SWAGGER)
public class SwaggersAdminView extends ViewFrame {

  private static final String I18N_PREFIX = "swagger.";
  protected Grid<EurekaApplication> grid;
  private EurekaInfo eurekaInfo;

  public SwaggersAdminView() {
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();
    setViewContent(createContent());

    ServiceResult<EurekaInfo> applicationServiceResult = RegistryServices.getEurekaService()
        .getApplications(new BaseRemoteQuery());

    if (applicationServiceResult.getIsSuccess() && applicationServiceResult.getData() != null) {
      eurekaInfo = applicationServiceResult.getData();
      grid.setItems(eurekaInfo.getApplicationList());
    }
  }

  protected void initHeader() {
    AppBar appBar = JHapyMainView3.get().getAppBar();
    appBar.setNaviMode(NaviMode.MENU);
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  protected void showDetails(EurekaApplication app) {
    try {
      UI.getCurrent().navigate(SwaggerAdminView.class,
          URLEncoder.encode(app.getInstances().get(0).getHomePageUrl(), "UTF-8"));
    } catch (UnsupportedEncodingException e) {
    }
  }

  protected Grid createGrid() {
    grid = new Grid<>();
    grid.setSelectionMode(SelectionMode.SINGLE);

    grid.addSelectionListener(event -> event.getFirstSelectedItem()
        .ifPresent(this::showDetails));

    grid.setHeight("100%");

    grid.addColumn(EurekaApplication::getName).setKey("name");

    grid.getColumns().forEach(column -> {
      if (column.getKey() != null) {
        column.setHeader(getTranslation("element." + I18N_PREFIX + column.getKey()));
        column.setResizable(true);
        column.setSortable(true);
      }
    });
    return grid;
  }
}
