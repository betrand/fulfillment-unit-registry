package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.Store;
import java.util.List;

public interface StoreStore {

  List<Store> listAllByName();

  Store findById(Long id);

  void create(Store store);

  void remove(Store store);
}
