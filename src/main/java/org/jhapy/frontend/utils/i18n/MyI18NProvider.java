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

import com.vaadin.flow.i18n.I18NProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.domain.i18n.MessageTrl;
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

  public void reload() {
    reloadElements();
    reloadMessages();
    reloadActions();
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
      return getElementTranslation(s.substring(s.indexOf('.') + 1), iso3Language);
    } else if (s.startsWith("action.")) {
      return getActionTranslation(s.substring(s.indexOf('.') + 1), iso3Language);
    } else if (s.startsWith("message.")) {
      return getMessageTranslation(s.substring(s.indexOf('.') + 1), iso3Language, objects);
    } else {
      logger().error(loggerPrefix + "Translation do not have a correct prefix : " + s);
      return s;
    }
  }

  protected String getElementTranslation(String s, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getElementTranslation", s, iso3Language);
    ElementTrl elementTrl = getElementTrl(s, iso3Language);

    if (elementTrl != null) {
      return elementTrl.getValue();
    } else {
      logger().debug(loggerPrefix + "Translation for '" + s + "' in " + iso3Language
          + " not found");
      return s;
    }
  }

  protected String getActionTranslation(String s, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getActionTranslation", s, iso3Language);
    ActionTrl actionTrl = getActionTrl(s, iso3Language);

    if (actionTrl != null) {
      return actionTrl.getValue();
    } else {
      logger().debug(loggerPrefix + "Translation for '" + s + "' in " + iso3Language
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

  private synchronized void loadRemoteLocales(String iso3Language) {
    String loggerPrefix = getLoggerPrefix("loadRemoteLocales", iso3Language);

    if (loadedLocale != null && loadedLocale.equals(iso3Language)) {
      return;
    }

    logger().debug(loggerPrefix + "Bootstrap " + iso3Language);
    elementMap.clear();
    List<ElementTrl> elements = I18NServices.getElementTrlService()
        .findByIso3(new FindByIso3Query(iso3Language)).getData();
    elements.forEach(element -> elementMap.put(element.getName(), element));
    logger().debug(loggerPrefix + elements.size() + " elements loaded");

    actionMap.clear();
    List<ActionTrl> actions = I18NServices.getActionTrlService()
        .findByIso3(new FindByIso3Query(iso3Language)).getData();
    actions.forEach(action -> actionMap.put(action.getName(), action));
    logger().debug(loggerPrefix + actions.size() + " actions loaded");

    messageMap.clear();
    List<MessageTrl> messages = I18NServices.getMessageTrlService()
        .findByIso3(new FindByIso3Query(iso3Language)).getData();
    messages.forEach(message -> messageMap.put(message.getName(), message));
    logger().debug(loggerPrefix + messages.size() + " messages loaded");

    loadedLocale = iso3Language;

    logger().debug(loggerPrefix + "Bootstrap " + iso3Language + " done");
  }

  private ElementTrl getElementTrl(String name, String iso3Language) {
    String loggerPrefix = getLoggerPrefix("getElementTrl");

    if (!loadedLocale.equals(iso3Language)) {
      loadRemoteLocales(iso3Language);
    }

    ElementTrl element = elementMap.get(name);
    String altName = "baseEntity" + name.substring(name.indexOf('.'));
    if (element == null) {
      element = elementMap.get(altName);
    }

    if (element == null) {
      logger().warn(loggerPrefix + "Element '" + name + "' not found locally, check on the server");
      ServiceResult<ElementTrl> _elementTrl = I18NServices.getElementTrlService()
          .getByNameAndIso3(new GetByNameAndIso3Query(name, iso3Language));
      if (_elementTrl.getIsSuccess()) {
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
      if (_actionTrl.getIsSuccess()) {
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
      if (_messageTrl.getIsSuccess()) {
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
}
