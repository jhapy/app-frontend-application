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

package org.jhapy.frontend.utils.i18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.i18n.Action;
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.domain.i18n.Element;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.domain.i18n.Message;
import org.jhapy.dto.domain.i18n.MessageTrl;
import org.jhapy.dto.messageQueue.I18NUpdateTypeEnum;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.utils.AppConst;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Component
public class MyI18NProvider implements I18NProvider, HasLogger {

  private static Locale[] availableLanguages = null;
  private Map<String, ElementTrl> elementMap = new HashMap<>();
  private Map<String, ActionTrl> actionMap = new HashMap<>();
  private Map<String, MessageTrl> messageMap = new HashMap<>();
  private String loadedLocale;

  public static List<Locale> getAvailableLanguagesInDB(Locale currentLanguage) {
    ServiceResult<List<String>> _languages = I18NServices.getI18NService()
        .getExistingLanguages(new BaseRemoteQuery());
    if (_languages.getIsSuccess() && _languages.getData() != null) {
      List<Locale> result = new ArrayList<>();
      _languages.getData().forEach(s -> result.add(new Locale(s)));

      return result.stream().sorted(
          Comparator.comparing(o -> o.getDisplayLanguage(currentLanguage)))
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  public static Locale[] getAvailableLanguages() {
    if (availableLanguages == null) {
      Set<String> langs = new HashSet<>();
      for (Locale l : Locale.getAvailableLocales()) {
        if (StringUtils.isNotBlank(l.getLanguage())) {
          langs.add(l.getLanguage());
        }
      }
      Set<Locale> locals = new HashSet<>();
      langs.forEach(lang -> locals.add(new Locale(lang)));

      availableLanguages = locals.stream().toArray(Locale[]::new);
    }

    return availableLanguages;
  }

  public static Locale[] getAvailableLanguages(Locale currentLanguage) {
    Locale[] locales = getAvailableLanguages();
    List<Locale> localeList = Arrays.asList(locales);
    return localeList.stream().sorted(
        Comparator.comparing(o -> o.getDisplayLanguage(currentLanguage))).toArray(Locale[]::new);
  }

  @Override
  public List<Locale> getProvidedLocales() {

    return Collections.unmodifiableList(
        Arrays.asList(Locale.getAvailableLocales()));
  }

  public void reload(String iso3Lang) {
    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(iso3Lang)) {
      reloadElements();
      reloadMessages();
      reloadActions();
    }
  }

  public void reloadElements() {
    elementMap = new HashMap<>();
  }

  public void reloadMessages() {
    messageMap = new HashMap<>();
  }

  public void reloadActions() {
    actionMap = new HashMap<>();
  }

  @Override
  public String getTranslation(String s, Locale locale, Object... objects) {
    String loggerPrefix = getLoggerPrefix("getTranslation", locale, objects);
    String iso3Language = locale.getLanguage();
    if (StringUtils.isBlank(iso3Language)) {
      iso3Language = AppConst.APP_LOCALE.getLanguage();
    }

    loadRemoteLocales(iso3Language);

    if (s.startsWith("element.")) {
      return getElementTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else if (s.startsWith("action.")) {
      return getActionTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else if (s.startsWith("message.")) {
      return getMessageTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else {
      logger().error(loggerPrefix + "Translation do not have a correct prefix : " + s);
      return s;
    }
  }

  public String getTooltip(String s) {
    String loggerPrefix = getLoggerPrefix("getTooltip", s);
    String iso3Language = UI.getCurrent().getLocale().getLanguage();
    if (StringUtils.isBlank(iso3Language)) {
      iso3Language = AppConst.APP_LOCALE.getLanguage();
    }

    loadRemoteLocales(iso3Language);

    if (s.startsWith("element.")) {
      String val = getElementTooltip(s.substring(s.indexOf('.') + 1), iso3Language);
      if (StringUtils.isBlank(val)) {
        return s;
      } else {
        return val;
      }
    } else if (s.startsWith("action.")) {
      String val = getActionTooltip(s.substring(s.indexOf('.') + 1), iso3Language);
      if (StringUtils.isBlank(val)) {
        return s;
      } else {
        return val;
      }
    } else {
      logger().error(loggerPrefix + "Tooltip do not have a correct prefix : " + s);
      return s;
    }
  }

  protected String getElementTranslation(String s, String iso3Language, Object... objects) {
    String loggerPrefix = getLoggerPrefix("getElementTranslation", s, iso3Language);
    ElementTrl elementTrl = getElementTrl(s, iso3Language);

    if (elementTrl != null) {
      if (objects.length > 0) {
        return String.format(elementTrl.getValue(), objects);
      } else {
        return elementTrl.getValue();
      }
    } else {
      logger().debug(loggerPrefix + "Translation for '" + s + "' in " + iso3Language
          + " not found");
      return s;
    }
  }

  protected String getElementTooltip(String s, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getElementTooltip", s, iso3Language);
    ElementTrl elementTrl = getElementTrl(s, iso3Language);

    if (elementTrl != null) {
      return elementTrl.getTooltip();
    } else {
      logger().debug(loggerPrefix + "Tooltip for '" + s + "' in " + iso3Language + " not found");
      return s;
    }
  }

  protected String getActionTranslation(String s, String iso3Language, Object... objects) {
    String loggerPrefix = getLoggerPrefix("getActionTranslation", s, iso3Language);
    ActionTrl actionTrl = getActionTrl(s, iso3Language);

    if (actionTrl != null) {
      if (objects.length > 0) {
        return String.format(actionTrl.getValue(), objects);
      } else {
        return actionTrl.getValue();
      }
    } else {
      logger().debug(loggerPrefix + "Translation for '" + s + "' in " + iso3Language
          + " not found");
      return s;
    }
  }

  protected String getActionTooltip(String s, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getActionTooltip", s, iso3Language);
    ActionTrl actionTrl = getActionTrl(s, iso3Language);

    if (actionTrl != null) {
      return actionTrl.getTooltip();
    } else {
      logger().debug(loggerPrefix + "Tooltip for '" + s + "' in " + iso3Language
          + " not found");
      return s;
    }
  }

  protected String getMessageTranslation(String s, String iso3Language, Object... objects) {
    String loggerPrefix = getLoggerPrefix("getMessageTranslation", s, iso3Language);
    MessageTrl messageTrl = getMessageTrl(s, iso3Language);

    if (messageTrl != null) {
      if (objects.length > 0) {
        return String.format(messageTrl.getValue(), objects);
      } else {
        return messageTrl.getValue();
      }
    } else {
      logger().debug(loggerPrefix + "Translation for '" + s + "' in " + iso3Language
          + " not found");
      return s;
    }
  }

  public synchronized void loadRemoteLocales(String iso3Language) {
    String loggerPrefix = getLoggerPrefix("loadRemoteLocales", iso3Language);

    if (loadedLocale != null && loadedLocale.equals(iso3Language)) {
      return;
    }

    logger().debug(loggerPrefix + "Bootstrap " + iso3Language);
    elementMap.clear();
    ServiceResult<List<ElementTrl>> _elements = I18NServices.getElementTrlService()
        .findByIso3(new FindByIso3Query(iso3Language));
    if (_elements != null && _elements.getIsSuccess() && _elements.getData() != null) {
      List<ElementTrl> elements = _elements.getData();
      elements.forEach(element -> elementMap.put(element.getName(), element));
      logger().debug(loggerPrefix + elements.size() + " elements loaded");
    } else {
      logger().error(
          loggerPrefix + "Cannot get elements " + (_elements != null ? _elements.getMessage()
              : "Null service"));
    }

    actionMap.clear();
    ServiceResult<List<ActionTrl>> _actions = I18NServices.getActionTrlService()
        .findByIso3(new FindByIso3Query(iso3Language));
    if (_actions != null && _actions.getIsSuccess() && _actions.getData() != null) {
      List<ActionTrl> actions = _actions.getData();
      actions.forEach(action -> actionMap.put(action.getName(), action));
      logger().debug(loggerPrefix + actions.size() + " actions loaded");
    } else {
      logger().error(
          loggerPrefix + "Cannot get actions " + (_actions != null ? _actions.getMessage()
              : "Null service"));
    }

    messageMap.clear();
    ServiceResult<List<MessageTrl>> _messages = I18NServices.getMessageTrlService()
        .findByIso3(new FindByIso3Query(iso3Language));
    if (_messages != null && _messages.getIsSuccess() && _messages.getData() != null) {
      List<MessageTrl> messages = _messages.getData();
      messages.forEach(message -> messageMap.put(message.getName(), message));
      logger().debug(loggerPrefix + messages.size() + " messages loaded");
    } else {
      logger().error(
          loggerPrefix + "Cannot get messages " + (_messages != null ? _messages.getMessage()
              : "Null service"));
    }
    loadedLocale = iso3Language;

    logger().debug(loggerPrefix + "Bootstrap " + iso3Language + " done");
  }

  private ElementTrl getElementTrl(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getElementTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    ElementTrl element = elementMap.get(name);
    String altName = "baseEntity" + name.substring(name.indexOf('.') == -1 ? 0 : name.indexOf('.'));
    if (element == null) {
      element = elementMap.get(altName);
    }

    if (element == null) {
      logger().warn(loggerPrefix + "Element '" + name + "' not found locally, check on the server");
      ServiceResult<ElementTrl> _elementTrl = I18NServices.getElementTrlService()
          .getByNameAndIso3(new GetByNameAndIso3Query(name, iso3Language));
      if (_elementTrl != null && _elementTrl.getIsSuccess() && _elementTrl.getData() != null) {
        element = _elementTrl.getData();
        elementMap.put(name, element);

        return element;
      } else {
        logger().error(loggerPrefix + "Element '" + name + "' not found on the server");
        return null;
      }
    } else {
      return element;
    }
  }

  private ActionTrl getActionTrl(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getActionTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    ActionTrl action = actionMap.get(name);

    if (action == null) {
      logger().warn(loggerPrefix + "Action '" + name + "' not found locally, check on the server");
      ServiceResult<ActionTrl> _actionTrl = I18NServices.getActionTrlService()
          .getByNameAndIso3(new GetByNameAndIso3Query(name, iso3Language));
      if (_actionTrl != null && _actionTrl.getIsSuccess() && _actionTrl.getData() != null) {
        action = _actionTrl.getData();
        actionMap.put(name, action);

        return action;
      } else {
        logger().error(loggerPrefix + "Action '" + name + "' not found on the server");
        return null;
      }
    } else {
      return action;
    }
  }

  private MessageTrl getMessageTrl(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getMessageTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    MessageTrl message = messageMap.get(name);
    String altName = "baseEntity" + name.substring(name.indexOf('.'));
    if (message == null) {
      message = messageMap.get(altName);
    }

    if (message == null) {
      logger().warn(loggerPrefix + "Message '" + name + "' not found locally, check on the server");
      ServiceResult<MessageTrl> _messageTrl = I18NServices.getMessageTrlService()
          .getByNameAndIso3(new GetByNameAndIso3Query(name, iso3Language));
      if (_messageTrl != null && _messageTrl.getIsSuccess() && _messageTrl.getData() != null) {
        message = _messageTrl.getData();
        messageMap.put(name, message);

        return message;
      } else {
        logger().error(loggerPrefix + "Message '" + name + "' not found on the server");
        return null;
      }
    } else {
      return message;
    }
  }

  public void init(Locale locale) {
    loadRemoteLocales(locale.getISO3Language());
  }

  public void elementUpdate(I18NUpdateTypeEnum updateType, Element element) {
    String loggerPrefix = getLoggerPrefix("elementUpdate", updateType, element);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      logger().debug(loggerPrefix + "Delete record");
      elementMap.remove(element.getName());
    }
  }

  public void elementTrlUpdate(I18NUpdateTypeEnum updateType, ElementTrl elementTrl) {
    String loggerPrefix = getLoggerPrefix("elementTrlUpdate", updateType, elementTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(elementTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        logger().debug(loggerPrefix + "Delete record");
        elementMap.remove(elementTrl.getName());
      } else {
        logger().debug(loggerPrefix + "Create or Update record");
        elementMap.put(elementTrl.getName(), elementTrl);
      }
    }
  }

  public void actionUpdate(I18NUpdateTypeEnum updateType, Action action) {
    String loggerPrefix = getLoggerPrefix("actionUpdate", updateType, action);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      logger().debug(loggerPrefix + "Delete record");
      actionMap.remove(action.getName());
    }
  }

  public void actionTrlUpdate(I18NUpdateTypeEnum updateType, ActionTrl actionTrl) {
    String loggerPrefix = getLoggerPrefix("actionTrlUpdate", updateType, actionTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(actionTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        logger().debug(loggerPrefix + "Delete record");
        actionMap.remove(actionTrl.getName());
      } else {
        logger().debug(loggerPrefix + "Create or Update record");
        actionMap.put(actionTrl.getName(), actionTrl);
      }
    }
  }

  public void messageUpdate(I18NUpdateTypeEnum updateType, Message message) {
    String loggerPrefix = getLoggerPrefix("messageUpdate", updateType, message);

    if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
      logger().debug(loggerPrefix + "Delete record");
      messageMap.remove(message.getName());
    }
  }

  public void messageTrlUpdate(I18NUpdateTypeEnum updateType, MessageTrl messageTrl) {
    String loggerPrefix = getLoggerPrefix("messageTrlUpdate", updateType, messageTrl);

    if (loadedLocale != null && loadedLocale.equalsIgnoreCase(messageTrl.getIso3Language())) {
      if (updateType.equals(I18NUpdateTypeEnum.DELETE)) {
        logger().debug(loggerPrefix + "Delete record");
        messageMap.remove(messageTrl.getName());
      } else {
        logger().debug(loggerPrefix + "Create or Update record");
        messageMap.put(messageTrl.getName(), messageTrl);
      }
    }
  }
}
