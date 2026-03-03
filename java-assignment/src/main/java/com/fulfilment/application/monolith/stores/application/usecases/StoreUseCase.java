package com.fulfilment.application.monolith.stores.application.usecases;

import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import com.fulfilment.application.monolith.stores.domain.ports.StoreSyncPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class StoreUseCase {

  private final StoreStore storeStore;
  private final StoreSyncPublisher storeSyncPublisher;

  public StoreUseCase(StoreStore storeStore, StoreSyncPublisher storeSyncPublisher) {
    this.storeStore = storeStore;
    this.storeSyncPublisher = storeSyncPublisher;
  }

  public List<Store> list() {
    return storeStore.listAllByName();
  }

  public Store getSingle(Long id) {
    Store entity = storeStore.findById(id);
    if (entity == null) {
      throw new NotFoundException("Store with id of " + id + " does not exist.");
    }
    return entity;
  }

  @Transactional
  public Store create(Store store) {
    if (store.id != null) {
      throw new UnprocessableEntityException("Id was invalidly set on request.");
    }

    storeStore.create(store);
    storeSyncPublisher.publishCreated(store);
    return store;
  }

  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new UnprocessableEntityException("Store Name was not set on request.");
    }

    Store entity = storeStore.findById(id);

    if (entity == null) {
      throw new NotFoundException("Store with id of " + id + " does not exist.");
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    storeSyncPublisher.publishUpdated(entity);

    return entity;
  }

  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new UnprocessableEntityException("Store Name was not set on request.");
    }

    Store entity = storeStore.findById(id);

    if (entity == null) {
      throw new NotFoundException("Store with id of " + id + " does not exist.");
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    storeSyncPublisher.publishUpdated(entity);
    return entity;
  }

  @Transactional
  public void delete(Long id) {
    Store entity = storeStore.findById(id);
    if (entity == null) {
      throw new NotFoundException("Store with id of " + id + " does not exist.");
    }
    storeStore.remove(entity);
  }
}
