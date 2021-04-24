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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.DataProvider;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.bar.AppBar.NaviMode;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultFilter;
import org.jhapy.frontend.dataproviders.DefaultSliceDataProvider;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 8/27/19
 */
public abstract class DefaultMasterView<T extends BaseEntity, F extends DefaultFilter> extends
    ViewFrame {

    protected final String I18N_PREFIX;
    protected Grid<T> grid;
    protected DefaultDataProvider<T, F> dataProvider;
    protected DefaultSliceDataProvider<T, F> sliceProvider;
    private final Class<T> entityType;
    private Tabs tabs;
    private Button newRecordButton;
    protected final Class entityViewClass;
    protected final MyI18NProvider myI18NProvider;

    public DefaultMasterView(String I18N_PREFIX, Class<T> entityType,
        DefaultDataProvider<T, F> dataProvider, Class entityViewClass,
        MyI18NProvider myI18NProvider) {
        super();
        this.I18N_PREFIX = I18N_PREFIX;
        this.entityType = entityType;
        this.dataProvider = dataProvider;
        this.entityViewClass = entityViewClass;
        this.myI18NProvider = myI18NProvider;
    }

    public DefaultMasterView(String I18N_PREFIX, Class<T> entityType,
        DefaultSliceDataProvider<T, F> sliceProvider, Class entityViewClass,
        MyI18NProvider myI18NProvider) {
        super();
        this.I18N_PREFIX = I18N_PREFIX;
        this.entityType = entityType;
        this.sliceProvider = sliceProvider;
        this.entityViewClass = entityViewClass;
        this.myI18NProvider = myI18NProvider;
    }

    protected DataProvider<T, F> getDataProvider() {
        if (dataProvider != null) {
            return dataProvider;
        } else {
            return sliceProvider;
        }
    }

    protected Class<T> getEntityType() {
        return entityType;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        initHeader();
        setViewContent(createContent());

        filter(null);
    }

    protected void initHeader() {
        AppBar appBar = JHapyMainView3.get().getAppBar();
        appBar.setNaviMode(NaviMode.MENU);

        initSearchBar();

        String title = getTitle();
        if (title != null) {
            appBar.setTitle(title);
        }

        if (canCreateRecord()) {
            newRecordButton = UIUtils
                .createTertiaryButton(VaadinIcon.PLUS);
            addNewRecordButtonAction(newRecordButton);
            appBar.addActionItem(newRecordButton);
        }

        Button refreshButton = UIUtils.createTertiaryButton(VaadinIcon.REFRESH);
        refreshButton.addClickListener(buttonClickEvent -> getDataProvider().refreshAll());
        appBar.addActionItem(refreshButton);
    }

    protected void addNewRecordButtonAction(Button newRecordButton) {
        newRecordButton.addClickListener(event -> {
            try {
                showDetails(entityType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        });
    }

    protected String getTitle() {
        return null;
    }

    protected boolean canCreateRecord() {
        return true;
    }

    protected void initSearchBar() {
        AppBar appBar = JHapyMainView3.get().getAppBar();
        appBar.disableGlobalSearch();
        Button searchButton = UIUtils.createTertiaryButton(VaadinIcon.SEARCH);
        searchButton.addClickListener(event -> appBar.searchModeOn());
        appBar.addSearchListener(event -> filter(appBar.getSearchString()));
        appBar.setSearchPlaceholder(getTranslation("element.global.search"));
        appBar.addActionItem(searchButton);
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGrid());
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        Label nbRows = UIUtils.createH4Label(getTranslation("element.global.nbRows", 0));
        dataProvider.setPageObserver(executionPage -> {
            nbRows
                .setText(getTranslation("element.global.nbRows", executionPage.getTotalElements()));
        });

        FooterRow footerRow = grid.appendFooterRow();
        footerRow.getCell(grid.getColumns().get(0)).setComponent(nbRows);
        return content;
    }

    protected abstract Grid createGrid();

    protected void showDetails(T entity) {
        UI.getCurrent()
            .navigate(entityViewClass, entity.getId() == null ? "-1" : entity.getId().toString());
    }

    protected void filter(String filter) {
        if (dataProvider != null) {
            dataProvider
                .setFilter((F) new DefaultFilter(
                    StringUtils.isBlank(filter) ? null : filter,
                    JHapyMainView3.get().getAppBar().getSearchShowActive()));
        } else {
            sliceProvider.setFilter((F) new DefaultFilter(
                StringUtils.isBlank(filter) ? null : filter,
                JHapyMainView3.get().getAppBar().getSearchShowActive()));
        }
    }
}
