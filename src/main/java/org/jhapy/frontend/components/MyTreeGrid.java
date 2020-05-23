package org.jhapy.frontend.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.function.ValueProvider;

/**
 * A grid component for displaying hierarchical tabular data.
 *
 * @param <T> the grid bean type
 * @author Vaadin Ltd
 */
public class MyTreeGrid<T> extends TreeGrid<T> {

  /**
   * Adds a new Hierarchy column to this {@link Grid} with a value provider. The value is converted
   * to String when sent to the client by using {@link String#valueOf(Object)}.
   * <p>
   * Hierarchy column is rendered by using 'vaadin-grid-tree-toggle' web component.
   *
   * @param valueProvider the value provider
   * @return the created hierarchy column
   */

  public Column<T> addHierarchyColumn(ValueProvider<T, ?> valueProvider) {
    /*
        column.setComparator(
                ((a, b) -> compareMaybeComparables(valueProvider.apply(a),
                        valueProvider.apply(b))));
        */
    return addColumn(TemplateRenderer
        .<T>of("<vaadin-grid-tree-toggle "
            + "leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>[[item.name]]"
            + "</vaadin-grid-tree-toggle>")
        .withProperty("leaf",
            item -> !getDataCommunicator().hasChildren(item))
        .withProperty("name",
            value -> String.valueOf(valueProvider.apply(value))));
  }
}
