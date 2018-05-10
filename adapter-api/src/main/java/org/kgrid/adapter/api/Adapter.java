package org.kgrid.adapter.api;

import java.nio.file.Path;

public interface Adapter {


  String getType();

  void initialize();

  // resource Path: .../99999-fk4r73t/v.1.1.2/models/resource, endpoint: "score"
  Executor activate(Path resource, String endpoint);

  String status();
}