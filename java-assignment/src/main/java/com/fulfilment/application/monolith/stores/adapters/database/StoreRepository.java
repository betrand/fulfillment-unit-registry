package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class StoreRepository implements PanacheRepository<Store>, StoreStore {

  @Override
  public List<Store> listAllByName() {
    return listAll(Sort.by("name"));
  }

  @Override
  public Store findById(Long id) {
    return find("id", id).firstResult();
  }

  @Override
  public void create(Store store) {
    persist(store);
  }

  @Override
  public void remove(Store store) {
    delete("id", store.id);
  }
}
