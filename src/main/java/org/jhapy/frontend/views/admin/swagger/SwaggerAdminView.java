package org.jhapy.frontend.views.admin.swagger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.SecurityConst;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@I18NPageTitle(messageKey = AppConst.TITLE_SWAGGER_ADMIN)
@Secured(SecurityConst.ROLE_SWAGGER)
public class SwaggerAdminView extends ViewFrame implements RouterLayout, HasLogger,
    HasUrlParameter<String> {

    private IFrame swaggerView;
    private String appName;
    private String swaggerUrl;
    private Registration contextIconRegistration = null;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        initAppBar();

        JHapyMainView3.get().getAppBar().setTitle(appName);
        setViewContent(createContent());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (contextIconRegistration != null) {
            contextIconRegistration.remove();
        }
    }

    private AppBar initAppBar() {
        AppBar appBar = JHapyMainView3.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        if (contextIconRegistration == null) {
            contextIconRegistration = appBar.getContextIcon().addClickListener(event -> goBack());
        }
        return appBar;
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout();
        content.setFlexDirection(FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO, Vertical.RESPONSIVE_L);
        content.setSizeFull();

        swaggerView = new IFrame(swaggerUrl);
        swaggerView.setSizeFull();
        content.add(swaggerView);

        return content;
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if (StringUtils.isNoneBlank(parameter)) {
            String url = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getRequestURL()
                .toString();

            swaggerUrl = url + "swagger/" + parameter + "/swagger-ui.html";
            this.appName = parameter;
        }
    }

    private void goBack() {
        UI.getCurrent().navigate(SwaggersAdminView.class);
    }
}
