package org.jhapy.frontend.dataproviders;

import java.io.Serializable;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-07
 */
@FunctionalInterface
public interface FilterProvider extends Serializable {

  String getFilter();
}
