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

package org.jhapy.frontend.customFields;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.BaseServices;
import org.jhapy.frontend.client.ResourceService;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ImageViewerDialog;
import org.jhapy.frontend.components.InputDialog;
import org.jhapy.frontend.components.InputDialog.FluentButton;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.components.PdfViewer;
import org.jhapy.frontend.components.PdfViewerDialog;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.components.events.AttachmentsFieldValueChangeEvent;
import org.jhapy.frontend.config.AppProperties;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.IconSize;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BorderRadius;
import org.jhapy.frontend.utils.css.Shadow;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-13
 */
public class AttachmentField extends FlexBoxLayout implements HasStyle, HasSize,
    HasValue<AttachmentsFieldValueChangeEvent, StoredFile[]> {

  private List<StoredFile> storedFiles = new ArrayList<>();
  private final Upload upload;
  private final Div documentList;
private AppProperties appProperties;
  private final List<ValueChangeListener<? super AttachmentsFieldValueChangeEvent>> changeListeners = new ArrayList<>();

  public AttachmentField(AppProperties appProperties) {
    this(appProperties, null, new String[]{"image/jpeg", "image/png", "image/gif", "image/tiff",
        "application/pdf"},10 , 10  );
  }

  public AttachmentField(AppProperties appProperties, String label, String[] acceptedFileType, int maxFileSizeMb, int maxFiles) {
    this.appProperties = appProperties;
    setFlexDirection(FlexDirection.COLUMN);

    documentList = new Div();

    documentList.addClassNames(LumoStyles.Padding.Vertical.S);

    UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, documentList);
    UIUtils.setBorderRadius(BorderRadius.S, documentList);
    UIUtils.setShadow(Shadow.XS, documentList);

    FlexBoxLayout documents = new FlexBoxLayout(documentList);
    //reports.addClassName(CLASS_NAME + "__reports");
    documents.setFlexDirection(FlexDirection.COLUMN);
    //reports.setPadding(Bottom.XL, Left.RESPONSIVE_L);

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    upload = new Upload(buffer);
    if ( acceptedFileType != null && acceptedFileType.length > 0 )
    upload.setAcceptedFileTypes(acceptedFileType);
    upload.setAutoUpload(true);
    upload.setMaxFileSize(maxFileSizeMb * 1024 * 124);
    upload.setMaxFiles(maxFiles);

    upload.addSucceededListener(event -> {
      StoredFile storedFile = new StoredFile();
      storedFile.setMimeType(event.getMIMEType());
      try {
        storedFile.setContent(IOUtils.toByteArray(buffer.getInputStream(event.getFileName())));
      } catch (IOException e) {
        Notification.show(e.getLocalizedMessage());
      }
      storedFile.setOrginalContent(storedFile.getContent());
      storedFile.setFilesize((long) storedFile.getContent().length);
      storedFile.setFilename(event.getFileName());

      List<StoredFile> oldValues = new ArrayList<>(storedFiles);
      storedFiles.add(storedFile);

      changeListeners.forEach(listener -> listener
          .valueChanged(new AttachmentsFieldValueChangeEvent(oldValues.toArray(new StoredFile[0]),
              storedFiles.toArray(new StoredFile[0]), this)));
    });

    add(upload);
    add(documentList);

  }

  protected void addDocumentInList(StoredFile file) {
    Image fileIcon = new Image("images/filesExt/" + file.getFilename().substring(file.getFilename().lastIndexOf(".") + 1) + "-icon-48x48.png", file.getFilename());
    fileIcon.addClassName(IconSize.M.getClassName());

    ListItem item = new ListItem(fileIcon, file.getFilename());
    item.setSuffix(createDownloadButton(file), createViewButton(file), createRemoveButton(file, item));

    documentList.add(item);
  }

  private Component createDownloadButton(StoredFile item) {
    Button downloadButton = UIUtils.createSmallButton(VaadinIcon.DOWNLOAD);
    downloadButton.addClickListener( event -> {} );

      Anchor downloadLink = new Anchor(new StreamResource(item.getFilename(), () -> new ByteArrayInputStream(item.getContent())), "");
      downloadLink.getElement().setAttribute("download", true);

      downloadLink.add(downloadButton);

      return downloadLink;
  }

  private Button createViewButton(StoredFile item) {
    Button infoButton = UIUtils.createSmallButton(VaadinIcon.GLASSES);
    infoButton.addClickListener(
        e -> {
          if (item != null && item.getId() != null) {
            if (item.getMimeType().startsWith("image") ) {
              ImageViewerDialog imageViewerDialog = new ImageViewerDialog(item, false );
              imageViewerDialog.open();
            } else {
              PdfViewerDialog pdfViewerDialog = new PdfViewerDialog(appProperties, item);
              pdfViewerDialog.open();
              pdfViewerDialog.setWidth("600px");
              pdfViewerDialog.setHeight("800px");
            }
          }
        });
    return infoButton;
  }

  private Button createRemoveButton(StoredFile item, ListItem listItem) {
    Button infoButton = UIUtils.createSmallButton(VaadinIcon.CLOSE_SMALL);
    infoButton.addClickListener(
        e -> {
          List<StoredFile> oldValues = new ArrayList<>(storedFiles);

          storedFiles.remove(item);
          documentList.remove(listItem);

          changeListeners.forEach(listener -> listener
              .valueChanged(
                  new AttachmentsFieldValueChangeEvent(oldValues.toArray(new StoredFile[0]),
                      storedFiles.toArray(new StoredFile[0]), this)));
        });
    return infoButton;
  }

  @Override
  public StoredFile[] getValue() {
    return storedFiles.toArray(new StoredFile[0]);
  }

  @Override
  public void setValue(StoredFile[] value) {
    documentList.removeAll();
    if (value == null) {
      storedFiles = new ArrayList<>();
      return;
    } else {
      storedFiles = new ArrayList<>(Arrays.asList(value));
      storedFiles.stream().filter(storedFile -> storedFile.getId() != null)
          .forEach(storedFile -> addDocumentInList(storedFile));
    }
  }

  @Override
  public Registration addValueChangeListener(
      ValueChangeListener<? super AttachmentsFieldValueChangeEvent> valueChangeListener) {
    changeListeners.add(valueChangeListener);
    return () -> changeListeners.remove(valueChangeListener);
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void setReadOnly(boolean b) {
  }

  @Override
  public boolean isRequiredIndicatorVisible() {
    return false;
  }

  @Override
  public void setRequiredIndicatorVisible(boolean b) {
  }

  class CustomDetailsDrawerFooter extends FlexBoxLayout {

    private final Button download;
    private final Button close;

    public CustomDetailsDrawerFooter(StreamResource downloadResource) {
      setBackgroundColor(LumoStyles.Color.Contrast._5);
      setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
      setSpacing(Right.S);
      setWidthFull();

      Anchor downloadLink = new Anchor(downloadResource, "");
      downloadLink.getElement().setAttribute("download", true);

      download = UIUtils.createPrimaryButton(getTranslation("action.global.download"));
      downloadLink.add(download);

      close = UIUtils.createTertiaryButton(getTranslation("action.global.close"));
      close.addClickShortcut(Key.ESCAPE);

      HorizontalLayout leftButton = new HorizontalLayout();
      leftButton.add(downloadLink);
      leftButton.setDefaultVerticalComponentAlignment(Alignment.START);

      HorizontalLayout rightButtons = new HorizontalLayout();
      rightButtons.add(close);
      rightButtons.setDefaultVerticalComponentAlignment(Alignment.END);
      setJustifyContent("space-between");
      add(leftButton, rightButtons);
    }

    public Registration addDownloadListener(
        ComponentEventListener<ClickEvent<Button>> listener) {
      return download.addClickListener(listener);
    }

    public Registration addCloseListener(
        ComponentEventListener<ClickEvent<Button>> listener) {
      return close.addClickListener(listener);
    }
  }
}
