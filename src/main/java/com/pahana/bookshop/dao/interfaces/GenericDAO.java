package com.pahana.bookshop.dao.interfaces;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    boolean deleteById(ID id);
    boolean existsById(ID id);
    long count();
}