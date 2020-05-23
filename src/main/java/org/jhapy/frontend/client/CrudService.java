package org.jhapy.frontend.client;


import org.jhapy.dto.domain.BaseEntity;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public interface CrudService<T extends BaseEntity> {

  T save(T entity);

  void delete(T entity);

  void delete(long id);

  long count();

  T load(long id);

  Iterable<T> findAll();
}
