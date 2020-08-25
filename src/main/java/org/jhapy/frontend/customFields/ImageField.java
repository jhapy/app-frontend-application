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

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import io.rocketbase.vaadin.croppie.Croppie;
import io.rocketbase.vaadin.croppie.model.ViewPortConfig;
import io.rocketbase.vaadin.croppie.model.ViewPortType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawer;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerFooter;
import org.jhapy.frontend.components.detailsdrawers.DetailsDrawerHeader;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-13
 */
public class ImageField extends CustomField<StoredFile> implements HasStyle, Serializable {

  private final Image image;
  private StoredFile storedFile;
  private StoredFile initialImage;
  private final Div croppieContent;
  private ViewPortType viewPortType = ViewPortType.CIRCLE;

  public ImageField() {
    this(null);
  }

  public ImageField(String label) {
    if (label != null) {
      setLabel(label);
    }

    image = new Image(AppConst.NO_PICTURE, "");
    image.setWidthFull();
    //image.setHeight("150px");
    image.addClickListener(e -> addOrUpdateImage());

    croppieContent = new Div();
    croppieContent.setWidth("200px");
    croppieContent.setHeight("350px");

    add(image);
  }

  public void setViewPortType(ViewPortType viewPortType) {
    this.viewPortType = viewPortType;
  }

  private void addOrUpdateImage() {
    Dialog uploadDialog = new Dialog();

    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/*");
    upload.setAutoUpload(true);
    upload.setMaxFiles(1);

    final DetailsDrawer detailsDrawer = new DetailsDrawer(upload, croppieContent);
    upload.addSucceededListener(event -> {
      storedFile = new StoredFile();
      storedFile.setMimeType(event.getMIMEType());
      try {
        storedFile.setContent(IOUtils.toByteArray(buffer.getInputStream()));
      } catch (IOException e) {
        Notification.show(e.getLocalizedMessage());
      }
      storedFile.setOrginalContent(storedFile.getContent());
      storedFile.setFilesize((long) storedFile.getContent().length);
      storedFile.setFilename(buffer.getFileName());

      buildCropie(storedFile.getContent(), storedFile.getFilename(), null);

    });
    if (initialImage != null) {
      buildCropie(initialImage.getOrginalContent(), initialImage.getFilename(),
          storedFile != null ? storedFile.getZoom() : null);
    }

    DetailsDrawerHeader header = new DetailsDrawerHeader(
        getTranslation("element.global.addOrUpdate"));
    DetailsDrawerFooter footer = new DetailsDrawerFooter();
    footer.setSaveAndNewButtonVisible(false);
    footer.addCancelListener(cancelEvent -> {
      if (storedFile != null) {
        storedFile.setOrginalContent(initialImage.getOrginalContent());
        storedFile.setFilesize(initialImage.getFilesize());
        storedFile.setMimeType(initialImage.getMimeType());
        storedFile.setFilename(initialImage.getFilename());
        storedFile.setContent(initialImage.getContent());

        image.setSrc(new StreamResource(storedFile.getFilename(),
            () -> new ByteArrayInputStream(storedFile.getContent())));
      } else {
        image.setSrc(AppConst.NO_PICTURE);
      }
      uploadDialog.setOpened(false);
    });
    footer.addSaveListener(saveEvent -> {
      image.setSrc(new StreamResource(storedFile.getFilename(),
          () -> new ByteArrayInputStream(storedFile.getContent())));
      updateValue();
      uploadDialog.setOpened(false);
    });
    footer.addDeleteListener(deleteEvent -> {
      storedFile = null;
      initialImage = null;
      image.setSrc(AppConst.NO_PICTURE);
      updateValue();
      uploadDialog.setOpened(false);
    });
    detailsDrawer.setHeader(header);
    detailsDrawer.setFooter(footer);

    uploadDialog.add(detailsDrawer);

    uploadDialog.setOpened(true);
  }

  @Override
  protected StoredFile generateModelValue() {
    return storedFile;
  }

  protected void buildCropie(byte[] content, String filename, Float zoom) {
    StreamResource imageResource = new StreamResource(filename,
        () -> new ByteArrayInputStream(content));

    Croppie croppie = new Croppie(imageResource);
    croppie.setWidth("300px");
    croppie.setHeight("300px");

    if (zoom != null) {
      croppie.withViewport(new ViewPortConfig(150, 150, viewPortType))
          .withShowZoomer(true).withEnableResize(false).withEnableZoom(true).withZoom(zoom);
    } else {
      croppie.withViewport(new ViewPortConfig(150, 150, viewPortType))
          .withShowZoomer(true).withEnableResize(false).withEnableZoom(true);
    }

    croppie.addCropListener(e -> {
      if (content != null && e.isFromClient()) {
        try {
          final String originalExtension = filename.substring(filename.lastIndexOf('.') + 1);
          BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(content));

          int imgH = bufferedImage.getHeight();
          int imgW = bufferedImage.getWidth();

          int topX = e.getPoints().getTopLeftX();
          int topY = e.getPoints().getTopLeftY();

          int botX = e.getPoints().getBottomRightX();
          int botY = e.getPoints().getBottomRightY();

          int w = botX - topX;
          int h = botY - topY;

          if ((topY + h) > imgH) {
            h = topY - imgH;
          }

          if ((topX + w) > imgW) {
            w = topX - imgW;
          }

          BufferedImage dest = bufferedImage
              .getSubimage(topX, topY,
                  w,
                  h);
          final ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(dest, originalExtension, baos);
          storedFile.setZoom(e.getZoom());
          storedFile.setContent(baos.toByteArray());
          storedFile.setFilesize((long) storedFile.getContent().length);
          storedFile.setHasChanged(true);
        } catch (IOException ex) {
          Notification.show(ex.getLocalizedMessage());
        }
      }
    });
    croppieContent.removeAll();
    croppieContent.add(croppie);
  }

  @Override
  protected void setPresentationValue(StoredFile newPresentationValue) {
    if (newPresentationValue.getContent() != null) {
      storedFile = newPresentationValue;
      initialImage = new StoredFile();
      initialImage.setOrginalContent(newPresentationValue.getOrginalContent());
      initialImage.setFilesize(newPresentationValue.getFilesize());
      initialImage.setMimeType(newPresentationValue.getMimeType());
      initialImage.setFilename(newPresentationValue.getFilename());
      initialImage.setContent(newPresentationValue.getContent());
      if ( initialImage.getOrginalContent() == null )
        initialImage.setOrginalContent(initialImage.getContent());

      image.setSrc(new StreamResource(newPresentationValue.getFilename(),
          () -> new ByteArrayInputStream(initialImage.getContent())));
    } else {
      storedFile = null;
      initialImage = null;
      image.setSrc(AppConst.NO_PICTURE);
    }
  }
}
