package com.fulfilment.application.monolith.stores;

public class StoreSyncEvent {

  public enum Operation {
    CREATE,
    UPDATE
  }

  public final Operation operation;
  public final Store store;

  private StoreSyncEvent(Operation operation, Store store) {
    this.operation = operation;
    this.store = store;
  }

  public static StoreSyncEvent created(Store store) {
    return new StoreSyncEvent(Operation.CREATE, store);
  }

  public static StoreSyncEvent updated(Store store) {
    return new StoreSyncEvent(Operation.UPDATE, store);
  }
}
