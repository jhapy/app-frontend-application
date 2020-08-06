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
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.BaseServices;
import org.jhapy.frontend.client.ResourceService;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.ListItem;
import org.jhapy.frontend.components.PdfViewer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.components.events.AttachmentsFieldValueChangeEvent;
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

  private final List<ValueChangeListener<? super AttachmentsFieldValueChangeEvent>> changeListeners = new ArrayList<>();

  public AttachmentField() {
    this(null);
  }

  public AttachmentField(String label) {
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
    upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/tiff",
        "application/pdf");
    upload.setAutoUpload(true);
    upload.setMaxFiles(10);

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
    Image fileIcon = new Image(UIUtils.IMG_PATH + "mimetypes/" + file.getFilename()
        .substring(file.getFilename().lastIndexOf(".") + 1) + "-icon-48x48.png", "");
    fileIcon.addClassName(IconSize.M.getClassName());

    ListItem item = new ListItem(fileIcon, file.getFilename());
    item.setSuffix(createViewButton(file), createRemoveButton(file, item));

    documentList.add(item);
  }

  private Button createViewButton(StoredFile item) {
    Button infoButton = UIUtils.createSmallButton(VaadinIcon.GLASSES);
    infoButton.addClickListener(
        e -> {
          if (item != null && item.getId() != null) {
            StreamResource streamResource = new StreamResource(
                item.getFilename(),
                () -> {
                  if ( item.getContent() != null )
                    return new ByteArrayInputStream(item.getContent());
                  else {
                    ServiceResult<StoredFile> _storedFile = BaseServices.getResourceService().getById(new GetByStrIdQuery(item.getId()));
                    if ( _storedFile.getIsSuccess() && _storedFile.getData() != null ) {
                      item.setContent(_storedFile.getData().getContent());
                      item.setOrginalContent( _storedFile.getData().getOrginalContent());
                      return new ByteArrayInputStream(item.getContent());
                    } else {
                      return null;
                    }
                  }
                });

            if (item.getMimeType().equals("application/pdf")) {

              PdfViewer viewer = new PdfViewer(streamResource);
              createPopup(item, viewer);
            } else {
              createPopup(item, new Image(streamResource, item.getFilename()));
            }
          }
        });
    return infoButton;
  }

  private void createPopup(StoredFile item, Component viewer) {
    Dialog popupDialog = new Dialog();

    DetailsDrawer popup = new DetailsDrawer();
    DetailsDrawerHeader popupHeader = new DetailsDrawerHeader(item.getFilename());
    popupHeader.addCloseListener(event -> popupDialog.close());

    popup.setHeader(popupHeader);

    CustomDetailsDrawerFooter popupFooter = new CustomDetailsDrawerFooter(
        new StreamResource(item.getFilename(), () -> new ByteArrayInputStream(item.getContent())));
    popupFooter.addCloseListener(event -> popupDialog.close());

    popup.setFooter(popupFooter);

    popupDialog.setWidth("600px");

    popup.setContent(viewer);

    popupDialog.add(popup);
    popupDialog.open();
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
