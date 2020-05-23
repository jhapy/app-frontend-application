package org.jhapy.frontend.components.events;

import com.vaadin.flow.component.HasValue;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.customFields.AttachmentField;

public class AttachmentsFieldValueChangeEvent implements HasValue.ValueChangeEvent<StoredFile[]> {

  private final StoredFile[] oldValue;
  private final StoredFile[] value;
  private final AttachmentField src;

  public AttachmentsFieldValueChangeEvent(StoredFile[] oldValue, StoredFile[] value,
      AttachmentField src) {
    this.oldValue = oldValue;
    this.value = value;
    this.src = src;
  }

  @Override
  public HasValue<?, StoredFile[]> getHasValue() {
    return src;
  }

  @Override
  public boolean isFromClient() {
    return true;
  }

  @Override
  public StoredFile[] getOldValue() {
    return oldValue;
  }

  @Override
  public StoredFile[] getValue() {
    return value;
  }
}
