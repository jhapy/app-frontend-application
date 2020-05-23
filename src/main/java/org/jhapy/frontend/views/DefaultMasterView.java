package org.jhapy.frontend.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tabs;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.bar.AppBar.NaviMode;
import org.jhapy.frontend.dataproviders.DefaultDataProvider;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Top;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BoxSizing;

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
  private Class<T> entityType;
  private Tabs tabs;
  private Button newRecordButton;
  private Class entityViewClass;

  public DefaultMasterView(String I18N_PREFIX, Class<T> entityType,
      DefaultDataProvider<T, F> dataProvider, Class entityViewClass) {
    this.I18N_PREFIX = I18N_PREFIX;
    this.entityType = entityType;
    this.dataProvider = dataProvider;
    this.entityViewClass = entityViewClass;
  }

  protected Class<T> getEntityType() { return entityType; }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);

    initHeader();
    setViewContent(createContent());

    filter(null);
  }

  protected void initHeader() {
    AppBar appBar = JHapyMainView.get().getAppBar();
    appBar.setNaviMode(NaviMode.MENU);

    initSearchBar();

    if ( canCreateRecord() ) {
      newRecordButton = UIUtils
          .createTertiaryButton(VaadinIcon.PLUS);
      newRecordButton.addClickListener(event -> {
        try {
          showDetails(entityType.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
      });
      appBar.addActionItem(newRecordButton);
    }
  }

  protected boolean canCreateRecord() {
    return true;
  }

  protected void initSearchBar() {
    AppBar appBar = JHapyMainView.get().getAppBar();
    Button searchButton = UIUtils.createTertiaryButton(VaadinIcon.SEARCH);
    searchButton.addClickListener(event -> appBar.searchModeOn());
    appBar.addSearchListener(event -> filter((String) event.getValue()));
    appBar.setSearchPlaceholder(getTranslation("element.global.search"));
    appBar.addActionItem(searchButton);
  }

  private Component createContent() {
    FlexBoxLayout content = new FlexBoxLayout(createGrid());
    content.setBoxSizing(BoxSizing.BORDER_BOX);
    content.setHeightFull();
    content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    return content;
  }

  protected abstract Grid createGrid();

  protected void showDetails(T entity) {
    UI.getCurrent()
        .navigate(entityViewClass, entity.getId() == null ? "-1" : entity.getId().toString());
  }

  protected void filter(String filter) {
    dataProvider
        .setFilter((F) new DefaultFilter(
            StringUtils.isBlank(filter) ? null : filter,
            Boolean.TRUE));
  }
}
