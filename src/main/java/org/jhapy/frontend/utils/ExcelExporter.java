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

package org.jhapy.frontend.utils;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.binder.PropertySet;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.WordUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.views.JHapyMainView;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 09/04/2020
 */
public class ExcelExporter<T> implements HasLogger {

  private static final String TMP_FILE_NAME = "tmp";

  private File file;
  private DataProvider dataProvider;
  private final Grid<T> grid;
  private final Class<T> entityType;

  private PropertySet<T> propertySet;

  public ExcelExporter(Grid<T> grid, Class<T> entityType) {
    this(grid, entityType, null);
  }

  public ExcelExporter(Grid<T> grid, Class<T> entityType, List<String> excludedColumns) {
    this.grid = grid;
    this.entityType = entityType;
  }

  public InputStream build() {
    try {
      initTempFile();
      resetContent();
      buildFileContent();
      writeToFile();
      return new FileInputStream(file);
    } catch (Exception e) {
      throw new ExcelExporterException("An error happened during exporting your Grid", e);
    }
  }

  private void initTempFile() throws IOException {
    if (file == null || file.delete()) {
      file = createTempFile();
    }
  }

  private File createTempFile() throws IOException {
    return File.createTempFile(TMP_FILE_NAME, getFileExtension());
  }

  protected void resetContent() {

  }

  private void buildFileContent() {
    buildHeaderRow();
    buildRows();
    buildFooter();
  }

  private void buildHeaderRow() {
    onNewRow();

    Method[] methods = entityType.getDeclaredMethods();
    String className = WordUtils.uncapitalize(entityType.getSimpleName());
    for (Method method : methods) {
      if (method.getName().startsWith("get")
          && !Collection.class.isAssignableFrom(method.getReturnType())
          && !method.getReturnType().isArray()
          && !method.getReturnType().equals(StoredFile.class)) {
        onNewCell();
        String attrName = WordUtils.uncapitalize(method.getName().substring(3));
        buildColumnHeaderCell("element." + className + "." + attrName);
      }
    }
  }

  void buildColumnHeaderCell(String header) {
    String loggerPrefix = getLoggerPrefix("buildColumnHeaderCell");

    logger().debug(
        loggerPrefix + "Build header for column name " + header + " : " + JHapyMainView.get()
            .getTranslation(header));
  }

  private void buildRows() {
    String loggerPrefix = getLoggerPrefix("buildRows");
    Object filter = null;
    try {
      Method method = DataCommunicator.class.getDeclaredMethod("getFilter");
      method.setAccessible(true);
      filter = method.invoke(grid.getDataCommunicator());
    } catch (Exception e) {
      logger().error("Unable to get filter from DataCommunicator");
    }

    Query streamQuery = new Query(0, grid.getDataProvider().size(new Query(filter)),
        grid.getDataCommunicator().getBackEndSorting(),
        grid.getDataCommunicator().getInMemorySorting(), null);
    Stream<T> dataStream = getDataStream(streamQuery);

    dataStream.forEach(t -> {
      buildRow(t);
    });
  }

  void buildFooter() {

  }

  void writeToFile() {

  }

  private void buildRow(T item) {
    onNewRow();
    Map<String, Object> values = new HashMap();
    Method[] methods = item.getClass().getMethods();
    for (Method method : methods) {
      try {
        if (method.getName().startsWith("get")
            && !Collection.class.isAssignableFrom(method.getReturnType())
            && !method.getReturnType().isArray()
            && !method.getReturnType().equals(StoredFile.class)) {
          String attrName = WordUtils.uncapitalize(method.getName().substring(3));
          values.put(attrName, method.invoke(item));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    values.forEach((s, o) -> {
      onNewCell();
      buildCell(o);
    });
  }

  String getFileExtension() {
    return ".xlsx";
  }

  void buildCell(Object value) {
    String loggerPrefix = getLoggerPrefix("buildCell");

    logger().debug(loggerPrefix + "Build cell with value " + value);
  }

  void onNewRow() {
    String loggerPrefix = getLoggerPrefix("onNewRow");

    logger().debug(loggerPrefix + "New Row");
  }

  void onNewCell() {
    String loggerPrefix = getLoggerPrefix("onNewCell");

    logger().debug(loggerPrefix + "New Cell");
  }

  private Stream<T> getDataStream(Query newQuery) {
    String loggerPrefix = getLoggerPrefix("getDataStream");
    Stream<T> stream = grid.getDataProvider().fetch(newQuery);
    if (stream.isParallel()) {
      logger().debug("Data provider {} has returned "
              + "parallel stream on 'fetch' call",
          grid.getDataProvider().getClass());
      stream = stream.collect(Collectors.toList()).stream();
      assert !stream.isParallel();
    }
    return stream;
  }
}
