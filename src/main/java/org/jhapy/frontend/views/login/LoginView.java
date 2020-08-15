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

package org.jhapy.frontend.views.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.NumberParseException.ErrorType;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.templatemodel.TemplateModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.DatatypeConverter;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.SecurityConst;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.authentification.ForgetPasswordQuery;
import org.jhapy.dto.serviceQuery.authentification.LoginQuery;
import org.jhapy.dto.serviceQuery.authentification.PasswordResetQuery;
import org.jhapy.dto.serviceQuery.authentification.ResetVerificationTokenQuery;
import org.jhapy.dto.serviceQuery.authentification.SignUpQuery;
import org.jhapy.dto.serviceQuery.authentification.ValidateUserQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceResponse.authentification.AuthResponse;
import org.jhapy.dto.serviceResponse.authentification.AuthStatusEnum;
import org.jhapy.frontend.client.BaseServices;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.components.myOAuth2.MyOAuth2Signin;
import org.jhapy.frontend.config.AppProperties;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.i18n.I18NPageTitle;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.JHapyMainView;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.access.annotation.Secured;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
@I18NPageTitle(messageKey = AppConst.TITLE_LOGIN)
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@Secured(org.jhapy.dto.utils.SecurityConst.ROLE_ADMIN)
public class LoginView extends ViewFrame
    implements BeforeEnterObserver, HasStyle, HasLogger, LocaleChangeObserver {

  private final AppProperties appProperties;
  private final ResourceLoader resourceLoader;
  private final ObjectMapper jsonObjectMapper;

  private final Pattern emailPattern = Pattern
      .compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$");
  private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

  private final Div content = new Div();


  public LoginView(AppProperties appProperties, ResourceLoader resourceLoader,
      ObjectMapper jsonObjectMapper) {
    super();
    this.appProperties = appProperties;
    this.resourceLoader = resourceLoader;
    this.jsonObjectMapper = jsonObjectMapper;
/*
    setSizeFull();

    getElement().appendChild(new AppCookieConsent().getElement());

    Component loginForm = buildLoginForm();

    add(loginForm);


 */
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    JHapyMainView.get().beforeLogin();
    JHapyMainView.get().getAppBar().setTitle(getTranslation("element.settings.title"));
    setViewContent(buildLoginForm());
  }

  private Component buildLoginForm() {
    VerticalLayout contentLayout = new VerticalLayout();
    contentLayout.setSizeFull();

    final VerticalLayout loginPanel = new VerticalLayout();
    loginPanel.setClassName("component-card");
    loginPanel.setSizeUndefined();
    loginPanel.setSpacing(true);

    loginPanel.add(getHeader());

    switchToSignInForm(null);

    loginPanel.add(content);

    contentLayout.add(loginPanel);

    contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, loginPanel);
    contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

    return contentLayout;
  }

  private void switchToSignupForm() {
    content.removeAll();
    content.add(getSignupForm());
  }

  private void switchToPasswordResetForm(String username) {
    content.removeAll();
    content.add(getPasswordResetForm(username));
  }

  private void switchToSignInForm(String username) {
    content.removeAll();
    content.add(getSigninForm(username));
  }

  private void switchToVerificationForm(String securityUserId, String email) {
    content.removeAll();
    content.add(getVerificationForm(securityUserId, email));
  }

  private Component getHeader() {
    HorizontalLayout labels = new HorizontalLayout();
    labels.setMargin(false);

    H2 welcome = new H2(getTranslation("element.application.title"));
    welcome.setSizeUndefined();
    labels.add(welcome);

    H2 title = new H2(getTranslation("element.application.additionalInformation"));
    title.setSizeUndefined();
    labels.add(title);
    return labels;
  }

  private Component getSigninForm(String defaultUsername) {
    VerticalLayout signInFieldsLayout = new VerticalLayout();
    signInFieldsLayout.setSpacing(false);
    signInFieldsLayout.setPadding(false);

    if (appProperties.getLoginForm().getDisplaySocialLogin()) {
      Label socialLoginLabel = new Label(getTranslation("element.login.loginWith"));
      signInFieldsLayout.add(socialLoginLabel);
      signInFieldsLayout.add(getSocialSigninOrSignupFields());

      Span orP = new Span("Or");
      orP.setClassName("or-with-lines");
      signInFieldsLayout.add(orP);
    }

    Binder<SigninForm> binder = new Binder<>();
    SigninForm signinForm = new SigninForm();

    FormLayout signinFormLayout = new FormLayout();
    signinFormLayout.setWidth("310px");

    TextField usernameField = new TextField();
    if (StringUtils.isNotBlank(defaultUsername)) {
      usernameField.setValue(defaultUsername);
    }

    usernameField.setWidth("15em");
    signinFormLayout.addFormItem(usernameField, getTranslation("element.login.username"));
    signinFormLayout.add(new Html("<br/>"));

    PasswordField passwordField = new PasswordField();
    passwordField.setWidth("15em");
    signinFormLayout.addFormItem(passwordField, getTranslation("element.login.password"));
    signinFormLayout.add(new Html("<br/>"));

    Checkbox rememberMeCheckbox = null;
    if (appProperties.getLoginForm().getDisplayRememberMe()) {
      rememberMeCheckbox = new Checkbox();
      signinFormLayout
          .addFormItem(rememberMeCheckbox, getTranslation("element.login.createRememberMeToken"));
      signinFormLayout.add(new Html("<br/>"));
    }

    ComboBox<Locale> languageComboBox = null;
    if (appProperties.getLoginForm().getDisplayLanguage()) {
      languageComboBox = new ComboBox();
      languageComboBox.setItems(MyI18NProvider.getAvailableLanguages(getLocale()));
      languageComboBox.setWidth("100%");
      languageComboBox.setItemLabelGenerator(Locale::getDisplayName);
      languageComboBox.setValue(UI.getCurrent().getLocale());
      languageComboBox.addValueChangeListener(e -> {
        if (e.getValue() != null) {
          UI.getCurrent().setLocale(e.getValue());
          UI.getCurrent().getSession().setLocale(e.getValue());
          VaadinSession.getCurrent().setLocale(e.getValue());
        } else {
          UI.getCurrent().setLocale(AppConst.APP_LOCALE);
          UI.getCurrent().getSession().setLocale(AppConst.APP_LOCALE);
          VaadinSession.getCurrent().setLocale(AppConst.APP_LOCALE);
        }
        UI.getCurrent().getPage().executeJs("location.reload();");
      });
      signinFormLayout.addFormItem(languageComboBox, getTranslation("element.login.language"));
    }

    SerializablePredicate<String> usernameEmptyPredicate = value -> !usernameField.getValue().trim()
        .isEmpty();

    SerializablePredicate<String> passwordPredicate = value -> !passwordField.getValue().trim()
        .isEmpty();

    Binding<SigninForm, String> usernameBinding = binder.forField(usernameField)
        .withValidator(usernameEmptyPredicate,
            getTranslation("message.error.usernameNotEmpty"))
        .bind(SigninForm::getUsername, SigninForm::setUsername);
    Binding<SigninForm, String> passwordBinging = binder.forField(passwordField)
        .withValidator(passwordPredicate, getTranslation("message.error.passwordNotEmpty"))
        .bind(SigninForm::getPassword, SigninForm::setPassword);
    if (languageComboBox != null) {
      binder.forField(languageComboBox).bind(SigninForm::getLocale, SigninForm::setLocale);
    }
    if (rememberMeCheckbox != null) {
      binder.forField(rememberMeCheckbox)
          .bind(SigninForm::getIsRememberMe, SigninForm::setIsRememberMe);
    }

    usernameField.setRequiredIndicatorVisible(true);
    passwordField.setRequiredIndicatorVisible(true);
    if (languageComboBox != null) {
      languageComboBox.setRequiredIndicatorVisible(true);
    }

    final Label errorLabel = new Label();

    HorizontalLayout buttonsLayout = new HorizontalLayout();
    signinFormLayout.add(buttonsLayout);

    Button loginButton = new Button(getTranslation("action.login.doSignin"));
    buttonsLayout.add(loginButton);
    loginButton
        .addClickListener(event -> {
          if (binder.writeBeanIfValid(signinForm)) {
            if (doSignin(signinForm.getUsername(), signinForm.getPassword(),
                signinForm.getIsRememberMe() == null ? false : signinForm.getIsRememberMe())) {
              afterSuccessLogin();
            }
          } else {
            BinderValidationStatus<SigninForm> validate = binder.validate();
            String errorText = validate.getFieldValidationStatuses()
                .stream().filter(BindingValidationStatus::isError)
                .map(BindingValidationStatus::getMessage)
                .map(Optional::get).distinct()
                .collect(Collectors.joining(", "));
            errorLabel.setText(getTranslation("message.error.errors") + " : " + errorText);
          }
        });
    signinFormLayout.getElement().addEventListener("keypress",
        event -> {
          if (binder.writeBeanIfValid(signinForm)) {
            if (doSignin(signinForm.getUsername(), signinForm.getPassword(),
                signinForm.getIsRememberMe() == null ? false : signinForm.getIsRememberMe())) {
              afterSuccessLogin();
            }
          } else {
            BinderValidationStatus<SigninForm> validate = binder.validate();
            String errorText = validate.getFieldValidationStatuses()
                .stream().filter(BindingValidationStatus::isError)
                .map(BindingValidationStatus::getMessage)
                .map(Optional::get).distinct()
                .collect(Collectors.joining(", "));
            errorLabel.setText(getTranslation("message.error.errors") + " : " + errorText);
          }
        })
        .setFilter("event.key == 'Enter'");
    loginButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

    if (appProperties.getLoginForm().getDisplayForgetPassword()) {
      Button forgotPasswordButton = new Button(getTranslation("element.login.forgetPassword"));
      forgotPasswordButton.addClickListener(event -> {
        BaseServices.getAuthService()
            .forgetPassword(new ForgetPasswordQuery(usernameField.getValue()));
        switchToPasswordResetForm(usernameField.getValue());
      });
      buttonsLayout.add(forgotPasswordButton);
      forgotPasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    }

    if (appProperties.getLoginForm().getDisplaySignup()) {
      Button signupButton = new Button(getTranslation("element.login.doSignup"));
      signupButton.addClickListener(event -> switchToSignupForm());
      buttonsLayout.add(signupButton);
    }

    VerticalLayout fieldsLayout = new VerticalLayout();
    fieldsLayout.setSpacing(true);
    fieldsLayout.add(signInFieldsLayout, signinFormLayout, errorLabel);

    return fieldsLayout;
  }

  private Component getSocialSigninOrSignupFields() {
    HorizontalLayout socialLogin = new HorizontalLayout();

    MyOAuth2Signin facebookSignin = new MyOAuth2Signin(
        appProperties.getAuthorization().getFacebookUrl(), "") {
      @Override
      protected void configureButton(Button button) {
        button.setText(getTranslation("element.login.facebook"));
        button.setIcon(VaadinIcon.FACEBOOK.create());
      }
    };
    facebookSignin.addLoginListener(accessToken -> {
      SecurityUser securityUser = extractSecurityUser(accessToken);
      if (securityUser != null) {
        SecurityUtils.newSession(securityUser, true);

        afterSuccessLogin();
      } else {
        Notification.show(getTranslation("loginError"), 2000, Notification.Position.MIDDLE);
      }
    });

    MyOAuth2Signin googleSignin = new MyOAuth2Signin(
        appProperties.getAuthorization().getGoogleUrl(), "") {
      @Override
      protected void configureButton(Button button) {
        button.setText(getTranslation("element.login.google"));
        button.setIcon(VaadinIcon.GOOGLE_PLUS.create());
      }
    };
    googleSignin.addLoginListener(accessToken -> {
      SecurityUser securityUser = extractSecurityUser(accessToken);
      if (securityUser != null) {
        SecurityUtils.newSession(securityUser, true);

        afterSuccessLogin();
      } else {
        Notification.show(getTranslation("loginError"), 2000, Notification.Position.MIDDLE);
      }
    });

    socialLogin.add(facebookSignin, googleSignin);

    return socialLogin;
  }

  private void afterSuccessLogin() {
    JHapyMainView.get().afterLogin();
  }

  private Component getSignupForm() {
    VerticalLayout signupFields = new VerticalLayout();
    signupFields.setSpacing(false);
    signupFields.setPadding(false);

    Label socialLoginLabel = new Label(getTranslation("element.login.signupWith"));
    signupFields.add(socialLoginLabel);
    signupFields.add(getSocialSigninOrSignupFields());

    Paragraph orP = new Paragraph();
    orP.setClassName("or-with-lines");
    orP.add(new Span("Or"));
    signupFields.add(orP);

    FormLayout signupFormLayout = new FormLayout();
    Binder<SignupForm> binder = new Binder<>();
    SignupForm signupForm = new SignupForm();
    signupFormLayout.setWidth("310px");

    TextField emailOrMobileNumberField = new TextField();
    emailOrMobileNumberField.setWidth("15em");
    emailOrMobileNumberField.setRequired(true);
    signupFormLayout
        .addFormItem(emailOrMobileNumberField, getTranslation("element.login.emailOrMobileNumber"));
    signupFormLayout.add(new Html("<br/>"));

    PasswordField passwordField = new PasswordField();
    passwordField.setWidth("15em");
    passwordField.setRequired(true);
    signupFormLayout.addFormItem(passwordField, getTranslation("element.login.password"));
    signupFormLayout.add(new Html("<br/>"));

    List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
    locales.sort(Comparator.comparing(Locale::getDisplayName));
    ComboBox<Locale> langSelect = new ComboBox();
    langSelect.setItems(locales);
    langSelect.setRequired(true);
    langSelect.setWidth("100%");
    langSelect.setItemLabelGenerator(Locale::getDisplayName);
    langSelect.setValue(UI.getCurrent().getLocale());
    langSelect.addValueChangeListener(e -> {
      if (e.getValue() != null) {
        UI.getCurrent().setLocale(e.getValue());
        UI.getCurrent().getSession().setLocale(e.getValue());
      } else {
        UI.getCurrent().setLocale(AppConst.APP_LOCALE);
        UI.getCurrent().getSession().setLocale(AppConst.APP_LOCALE);
      }
    });
    signupFormLayout.addFormItem(langSelect, getTranslation("element.login.language"));

    SerializablePredicate<String> usernameEmptyPredicate = value -> !emailOrMobileNumberField
        .getValue().trim().isEmpty();

    SerializablePredicate<String> passwordPredicate = value -> !passwordField.getValue().trim()
        .isEmpty();

    Binding<SignupForm, String> usernameBinding = binder.forField(emailOrMobileNumberField)
        .withValidator(usernameEmptyPredicate,
            getTranslation("message.error.emailOrMobileNotEmpty"))
        .withValidator(new UsernameValidator())
        .bind(SignupForm::getUsername, SignupForm::setUsername);
    Binding<SignupForm, String> passwordBinging = binder.forField(passwordField)
        .withValidator(passwordPredicate, getTranslation("message.error.passwordNotEmpty"))
        //.withValidator(new RegexpValidator("^(|(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,})$", getTranslation("message.error.invalidPasswordComplexity")))
        .bind(SignupForm::getPassword, SignupForm::setPassword);
    Binding<SignupForm, Locale> localBinding = binder.forField(langSelect)
        .bind(SignupForm::getLocale, SignupForm::setLocale);

    emailOrMobileNumberField.setRequiredIndicatorVisible(true);
    passwordField.setRequiredIndicatorVisible(true);
    langSelect.setRequiredIndicatorVisible(true);

    final Label errorLabel = new Label();

    HorizontalLayout buttonsLayout = new HorizontalLayout();
    signupFormLayout.add(buttonsLayout);

    Button signupButton = new Button(getTranslation("element.login.doSignup"));
    buttonsLayout.add(signupButton);
    signupButton.addClickListener(event -> {
      if (binder.writeBeanIfValid(signupForm)) {
        doSignup(signupForm.getUsername(), signupForm.getPassword(),
            signupForm.getLocale().getLanguage());
      } else {
        BinderValidationStatus<SignupForm> validate = binder.validate();
        String errorText = validate.getFieldValidationStatuses()
            .stream().filter(BindingValidationStatus::isError)
            .map(BindingValidationStatus::getMessage)
            .map(Optional::get).distinct()
            .collect(Collectors.joining(", "));
        errorLabel.setText(getTranslation("message.error.errors") + " : " + errorText);
      }
    });
    signupButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

    Button backToLoginForumButton = new Button(getTranslation("element.login.backToLogin"));
    backToLoginForumButton.addClickListener(event -> switchToSignInForm(null));
    buttonsLayout.add(backToLoginForumButton);

    VerticalLayout fields = new VerticalLayout();
    fields.setSpacing(true);
    fields.add(signupFields, signupFormLayout, errorLabel);

    return fields;
  }

  private Component getVerificationForm(String securityUserId, String email) {
    VerticalLayout verificationFields = new VerticalLayout();
    verificationFields.setSpacing(false);
    verificationFields.setPadding(false);

    H1 verificationTitle = new H1(getTranslation("element.login.verificationTitle"));
    verificationFields.add(verificationTitle);
    H3 verificationText = new H3(getTranslation("element.login.verificationText"));
    verificationFields.add(verificationText);

    Binder<VerificationForm> binder = new Binder<>();
    VerificationForm verificationForm = new VerificationForm();

    FormLayout verificationFormLayout = new FormLayout();
    verificationFormLayout.setWidth("310px");

    TextField firstnameField = new TextField();
    firstnameField.setWidth("15em");
    verificationFormLayout.addFormItem(firstnameField, getTranslation("element.login.firstname"));
    verificationFormLayout.add(new Html("<br/>"));

    TextField lastnameField = new TextField();
    lastnameField.setWidth("15em");
    verificationFormLayout.addFormItem(lastnameField, getTranslation("element.login.lastname"));
    verificationFormLayout.add(new Html("<br/>"));

    TextField nicknameField = new TextField();
    nicknameField.setWidth("15em");
    verificationFormLayout.addFormItem(nicknameField, getTranslation("element.login.nickname"));
    verificationFormLayout.add(new Html("<br/>"));

    EmailField emailField = new EmailField();
    emailField.setWidth("15em");
    if (!StringUtils.isBlank(email)) {
      emailField.setValue(email);
      emailField.setReadOnly(true);
    }
    verificationFormLayout.addFormItem(emailField, getTranslation("element.login.email"));
    verificationFormLayout.add(new Html("<br/>"));

    TextField verificationCodeField = new TextField();
    verificationCodeField.setWidth("15em");
    verificationFormLayout
        .addFormItem(verificationCodeField, getTranslation("element.login.verificationCode"));
    verificationFormLayout.add(new Html("<br/>"));

    binder.forField(firstnameField)
        .withValidator(new StringLengthValidator(
            getTranslation("message.error.firstNameRequired"), 1, null))
        .bind(VerificationForm::getFirstName, VerificationForm::setFirstName);
    binder.forField(lastnameField)
        .withValidator(new StringLengthValidator(
            getTranslation("message.error.lastNameRequired"), 1, null))
        .bind(VerificationForm::getLastName, VerificationForm::setLastName);
    binder.forField(nicknameField)
        .withValidator(new StringLengthValidator(
            getTranslation("message.error.nickNameRequired"), 1, null))
        .bind(VerificationForm::getNickName, VerificationForm::setNickName);
    binder.forField(emailField)
        .withValidator(new EmailValidator(getTranslation("message.error.invalidEmailFormat")))
        .bind(VerificationForm::getEmail, VerificationForm::setEmail);
    binder
        .forField(verificationCodeField)
        .bind(VerificationForm::getVerificationCode, VerificationForm::setVerificationCode);

    firstnameField.setRequiredIndicatorVisible(true);
    lastnameField.setRequiredIndicatorVisible(true);
    nicknameField.setRequiredIndicatorVisible(true);
    verificationCodeField.setRequiredIndicatorVisible(true);

    final Label errorLabel = new Label();

    HorizontalLayout buttonsLayout = new HorizontalLayout();
    verificationFormLayout.add(buttonsLayout);

    Button verifyButton = new Button(getTranslation("element.login.verifiy"));
    buttonsLayout.add(verifyButton);
    verifyButton.addClickListener(
        event -> {
          if (binder.writeBeanIfValid(verificationForm)) {
            doVerifyUser(securityUserId, verificationForm.getFirstName(),
                verificationForm.getLastName(), verificationForm.getEmail(),
                verificationForm.getNickName(),
                verificationForm.getVerificationCode());
          } else {
            BinderValidationStatus<VerificationForm> validate = binder.validate();
            String errorText = validate.getFieldValidationStatuses()
                .stream().filter(BindingValidationStatus::isError)
                .map(BindingValidationStatus::getMessage)
                .map(Optional::get).distinct()
                .collect(Collectors.joining(", "));
            errorLabel.setText(getTranslation("message.error.errors") + " : " + errorText);
          }
        });

    verifyButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

    Button resendVerificationCodeButton = new Button(
        getTranslation("element.login.resendVerificationCode"));
    buttonsLayout.add(resendVerificationCodeButton);
    resendVerificationCodeButton.addClickListener(event -> BaseServices.getAuthService()
        .resetVerificationToken(new ResetVerificationTokenQuery(securityUserId)));
    resendVerificationCodeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    VerticalLayout fields = new VerticalLayout();
    fields.setSpacing(true);
    fields.add(verificationFields, verificationFormLayout, errorLabel);

    return fields;
  }

  private Component getPasswordResetForm(String username) {
    VerticalLayout verificationFields = new VerticalLayout();
    verificationFields.setSpacing(false);
    verificationFields.setPadding(false);

    H1 passwordResetTitle = new H1(getTranslation("element.login.passwordResetTitle"));
    verificationFields.add(passwordResetTitle);
    H3 passwordResetText = new H3(getTranslation("element.login.passwordResetText"));
    verificationFields.add(passwordResetText);

    Binder<PasswordResetForm> binder = new Binder<>();
    PasswordResetForm passwordResetForm = new PasswordResetForm();

    FormLayout passwordResetFormLayout = new FormLayout();
    passwordResetFormLayout.setWidth("310px");

    TextField passwordResetCodeField = new TextField();
    passwordResetCodeField.setWidth("15em");
    passwordResetFormLayout
        .addFormItem(passwordResetCodeField, getTranslation("element.login.passwordResetCode"));
    passwordResetFormLayout.add(new Html("<br/>"));

    PasswordField newPasswordField = new PasswordField();
    newPasswordField.setWidth("15em");
    passwordResetFormLayout
        .addFormItem(newPasswordField, getTranslation("element.login.newPassword"));
    passwordResetFormLayout.add(new Html("<br/>"));

    SerializablePredicate<String> passwordPredicate = value -> !newPasswordField.getValue().trim()
        .isEmpty();

    binder.forField(newPasswordField)
        .withValidator(passwordPredicate, getTranslation("message.error.passwordNotEmpty"))
        .withValidator(new RegexpValidator("^(|(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,})$",
            getTranslation("message.error.invalidPasswordComplexity")))
        .bind(PasswordResetForm::getNewPassword, PasswordResetForm::setNewPassword);
    binder
        .forField(passwordResetCodeField)
        .bind(PasswordResetForm::getPasswordResetCode, PasswordResetForm::setPasswordResetCode);

    passwordResetCodeField.setRequiredIndicatorVisible(true);
    newPasswordField.setRequiredIndicatorVisible(true);

    final Label errorLabel = new Label();

    HorizontalLayout buttonsLayout = new HorizontalLayout();
    passwordResetFormLayout.add(buttonsLayout);

    Button verifyButton = new Button(getTranslation("element.login.doChangePassword"));
    buttonsLayout.add(verifyButton);
    verifyButton.addClickListener(
        event -> {
          if (binder.writeBeanIfValid(passwordResetForm)) {
            doChangePassword(username, passwordResetForm.getPasswordResetCode(),
                passwordResetForm.getNewPassword());
          } else {
            BinderValidationStatus<PasswordResetForm> validate = binder.validate();
            String errorText = validate.getFieldValidationStatuses()
                .stream().filter(BindingValidationStatus::isError)
                .map(BindingValidationStatus::getMessage)
                .map(Optional::get).distinct()
                .collect(Collectors.joining(", "));
            errorLabel.setText(getTranslation("message.error.errors") + " : " + errorText);
          }
        });

    verifyButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

    Button resendVerificationCodeButton = new Button(
        getTranslation("element.login.resendVerificationCode"));
    buttonsLayout.add(resendVerificationCodeButton);
    resendVerificationCodeButton.addClickListener(
        event -> BaseServices.getAuthService().forgetPassword(new ForgetPasswordQuery(username)));
    resendVerificationCodeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    VerticalLayout fields = new VerticalLayout();
    fields.setSpacing(true);
    fields.add(verificationFields, passwordResetFormLayout, errorLabel);

    return fields;
  }


  private void doVerifyUser(String securityUserId, String firstName, String lastName, String email,
      String nickname,
      String verificationCode) {
    ServiceResult<String> response = BaseServices.getAuthService()
        .validateUser(new ValidateUserQuery(lastName, firstName, email, nickname, securityUserId,
            verificationCode));
    if (response.getIsSuccess()) {
      String result = response.getData();
      if (result.equals(SecurityConst.TOKEN_VALID)) {
        SecurityUser securityUser = SecurityServices.getSecurityUserService()
            .getById(new GetByStrIdQuery(securityUserId)).getData();
        SecurityUtils.newSession(securityUser, false);
        afterSuccessLogin();
      } else if (result.equals(SecurityConst.TOKEN_EXPIRED)) {
        Notification.show(getTranslation("tokenExpired"), 2000, Notification.Position.MIDDLE);
      } else {
        Notification.show(getTranslation("tokenInvalid"), 2000, Notification.Position.MIDDLE);
      }
    }
  }

  private void doChangePassword(String username, String passwordResetCode, String newPassword) {
    ServiceResult<Void> response = BaseServices.getAuthService()
        .passwordReset(new PasswordResetQuery(username, passwordResetCode, newPassword));
    if (response.getIsSuccess()) {
      switchToSignInForm(username);
    }
  }

  private boolean doSignin(String username, String password, Boolean isRememberMe) {
    String loggerPrefix = getLoggerPrefix("doSignin");
    ServiceResult<AuthResponse> response = BaseServices.getAuthService()
        .authenticateUser(new LoginQuery(username, password));
    logger().trace(loggerPrefix + "Response = " + response);
    if (response.getIsSuccess()) {
      SecurityUser securityUser = extractSecurityUser(response.getData().getAccessToken());
      if (securityUser != null) {
        SecurityUtils.newSession(securityUser, isRememberMe);
        return true;
      } else {
        Notification.show(getTranslation("loginError"), 2000, Notification.Position.MIDDLE);
        return false;
      }
    } else {
      if (response.getData() == null) {
        Notification.show(getTranslation("message.error.cannotConnectToServer"), 2000,
            Notification.Position.MIDDLE);
        return false;
      } else if (response.getData().getStatus().equals(AuthStatusEnum.NOT_ACTIVATED)) {
        SecurityUser securityUser = extractSecurityUser(response.getData().getAccessToken());
        switchToVerificationForm(securityUser.getId(), securityUser.getEmail());
        return false;
      } else {
        if (response.getData().getStatus().equals(AuthStatusEnum.BAD_CREDENTIALS)) {
          Notification.show(getTranslation("message.error.badCredentials"), 2000,
              Notification.Position.MIDDLE);
          return false;
        } else if (response.getData().getStatus().equals(AuthStatusEnum.LOCKED)) {
          Notification.show(getTranslation("message.error.accountLocked"), 2000,
              Notification.Position.MIDDLE);
          return false;
        } else {
          Notification
              .show(getTranslation("message.error.loginError"), 2000, Notification.Position.MIDDLE);
          return false;
        }
      }
    }
  }

  private boolean doSignup(String username, String password, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("doSignup");

    Matcher mat = emailPattern.matcher(username);
    String email = null;
    String phoneNumber = null;

    if (mat.matches()) {
      email = username;
    } else {
      PhoneNumber number;
      try {
        number = phoneUtil.parseAndKeepRawInput(username, "");
        boolean isNumberValid = phoneUtil.isValidNumber(number);

      } catch (NumberParseException e) {
        if (e.getErrorType().equals(ErrorType.INVALID_COUNTRY_CODE)) {
          Notification.show("Invalid country code");
        } else if (e.getErrorType().equals(ErrorType.NOT_A_NUMBER)) {
          Notification.show("Not a valid number");
        } else if (e.getErrorType().equals(ErrorType.TOO_LONG)) {
          Notification.show("Number is too long");
        } else {
          Notification.show("Number is too short");
        }
        return false;
      }
      phoneNumber = phoneUtil.format(number, PhoneNumberFormat.E164);
    }

    ServiceResult<String> response = BaseServices.getAuthService()
        .registerUser(new SignUpQuery(email, phoneNumber, password, iso3Language));
    logger().debug(loggerPrefix + "Response = " + response);
    if (response.getIsSuccess()) {
      switchToVerificationForm(response.getData(), email);
    } else {
      Notification.show("An error has occurred : " + response.getMessage());
    }

    return true;
  }

  private SecurityUser extractSecurityUser(String accessToken) {
    String loggerPrefix = getLoggerPrefix("extractSecurityUser");
    Resource resource = resourceLoader.getResource(appProperties.getAuthorization().getPublicKey());
    String publicKeyStr = null;
    try {
      publicKeyStr = IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----\n", "");
    publicKeyStr = publicKeyStr.replace("-----END PUBLIC KEY-----", "");
    try {
      logger().debug(loggerPrefix + "Decode public key.");
      byte[] publicKeyBytes = DatatypeConverter.parseBase64Binary(publicKeyStr);

      // create a key object from the bytes
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(keySpec);

      Claims claims = Jwts.parser()
          .setSigningKey(publicKey)
          .parseClaimsJws(accessToken)
          .getBody();

      if (!claims.isEmpty()) {

        return jsonObjectMapper
            .readValue(claims.get("securityUser").toString(), SecurityUser.class);
      } else {
        return null;
      }
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
      logger().error(loggerPrefix
          + "An error has occured while getting the security user from the access token : " + e
          .getMessage(), e);

      return null;
    }
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (SecurityUtils.isUserLoggedIn()) {
      // Needed manually to change the URL because of https://github.com/vaadin/flow/issues/4189
      UI.getCurrent().getPage().getHistory().replaceState(null, "");
      event.rerouteTo(JHapyMainView.get().getHomePage());
    }
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    //Application.getAppContext().setCurrentIso3Language(event.getLocale().getLanguage());
  }

  public interface Model extends TemplateModel {

    void setError(boolean error);
  }

  @Data
  static class SigninForm {

    private String username;
    private String password;
    private Boolean isRememberMe;
    private Locale locale;
  }

  @Data
  static class SignupForm {

    private String username;
    private String password;
    private Locale locale;
  }

  @Data
  static class VerificationForm {

    private String verificationCode;
    private String firstName;
    private String lastName;
    private String nickName;
    private String email;
  }

  @Data
  static class PasswordResetForm {

    private String passwordResetCode;
    private String newPassword;
  }

  class UsernameValidator implements Validator<String> {

    @Override
    public ValidationResult apply(String username, ValueContext valueContext) {
      Matcher mat = emailPattern.matcher(username);

      if (mat.matches()) {
        return ValidationResult.ok();
      } else {
        PhoneNumber number;
        try {
          number = phoneUtil.parseAndKeepRawInput(username, "");
          boolean isNumberValid = phoneUtil.isValidNumber(number);
          if (isNumberValid) {
            return ValidationResult.ok();
          } else {
            return ValidationResult.error(getTranslation("message.error.invalidNumberOrEmail"));
          }
        } catch (NumberParseException e) {
          if (e.getErrorType().equals(ErrorType.INVALID_COUNTRY_CODE)) {
            return ValidationResult.error(getTranslation("message.error.invalidCountryCode"));
          } else if (e.getErrorType().equals(ErrorType.NOT_A_NUMBER)) {
            return ValidationResult.error(getTranslation("message.error.invalidNumberOrEmail"));
          } else if (e.getErrorType().equals(ErrorType.TOO_LONG)) {
            return ValidationResult.error(getTranslation("message.error.toLongForAMobileNumber"));
          } else if (e.getErrorType().equals(ErrorType.TOO_SHORT_AFTER_IDD) || e.getErrorType()
              .equals(ErrorType.TOO_SHORT_NSN)) {
            return ValidationResult.error(getTranslation("message.error.toShortForAMobileNumber"));
          } else {
            return ValidationResult.error(getTranslation("message.error.invalidNumberOrEmail"));
          }
        }
      }
    }
  }
}
