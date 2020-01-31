package dao;

import java.util.Optional;
import java.util.Set;

public interface ModelRepository<T, P> {

    Optional<T> get(P id);

    Set<T> getAll();

    T save(T object);

    void update(T object);

    boolean delete(P id);
}
