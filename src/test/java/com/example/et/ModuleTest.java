package com.example.et;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModuleTest {
  @Test
  void writeDocumentationSnippets() {

    var modules = ApplicationModules.of(EtApplication.class).verify();

    new Documenter(modules)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml();
  }
}
