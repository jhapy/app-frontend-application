package org.jhapy.frontend.utils;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 01/10/2020
 */
public interface AttributeContextListener {

  void onAttributeContextChanged(String attributeName, Object attributeValue);
}
