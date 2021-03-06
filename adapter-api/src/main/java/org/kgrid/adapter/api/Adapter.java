package org.kgrid.adapter.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import org.kgrid.shelf.domain.ArkId;

public interface Adapter {

  String getType();

  void initialize(ActivationContext context);

  // resource Path: .../99999-fk4r73t/v.1.1.2/models/resource, endpoint: "score"
  Executor activate(Path resource, String entry);

  Executor activate(String objectLocation, ArkId arkId, String endpointName, JsonNode deploymentSpec);

  String status();
}
