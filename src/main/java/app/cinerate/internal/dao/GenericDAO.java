package app.cinerate.internal.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T> {

    void createTable();

    long insert(T entity);

    void update(T entity);

    void delete(T entity);

    Optional<T> findById(Long id);


}
