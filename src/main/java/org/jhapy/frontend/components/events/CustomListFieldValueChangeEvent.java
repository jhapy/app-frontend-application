package org.jhapy.frontend.components.events;

import com.vaadin.flow.component.HasValue;
import java.util.List;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.frontend.customFields.DefaultCustomListField;

public class CustomListFieldValueChangeEvent<C extends BaseEntity> implements
    HasValue.ValueChangeEvent<List<C>> {

  private final List<C> oldValues;
  private final List<C> newValues;
  private final DefaultCustomListField src;

  public CustomListFieldValueChangeEvent(List<C> oldValues, List<C> newValues,
      DefaultCustomListField src) {
    this.oldValues = oldValues;
    this.newValues = newValues;
    this.src = src;
  }

  @Override
  public HasValue<?, List<C>> getHasValue() {
    return src;
  }

  @Override
  public boolean isFromClient() {
    return true;
  }

  @Override
  public List<C> getOldValue() {
    return oldValues;
  }

  @Override
  public List<C> getValue() {
    return newValues;
  }
}
