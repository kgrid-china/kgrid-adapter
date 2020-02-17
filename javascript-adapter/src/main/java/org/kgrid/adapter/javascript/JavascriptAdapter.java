package org.kgrid.adapter.javascript;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.kgrid.shelf.ShelfResourceNotFound;
import org.kgrid.shelf.repository.CompoundDigitalObjectStore;


public class JavascriptAdapter implements Adapter {

  Map<String, Object> endpoints;
  ScriptEngine engine;
  CompoundDigitalObjectStore cdoStore;
  private ActivationContext activationContext;

  @Override
  public String getType() {
    return "javascript".toUpperCase();
  }

  @Override
  public void initialize(ActivationContext context) {

    activationContext = context;

    engine = new ScriptEngineManager().getEngineByName("JavaScript");
    engine.getBindings(ScriptContext.GLOBAL_SCOPE).put("context", activationContext);
  }

  @Override
  public Executor activate(String objectLocation, JsonNode deploymentSpec) {
    return activate(Paths.get(objectLocation, deploymentSpec.get("artifact").asText()),
        deploymentSpec.get("entry").asText());
  }

  @Override
  public Executor activate(Path artifact, String entry) {

    CompiledScript script = getCompiledScript(artifact.toString(), entry);

    final ScriptContext context = new SimpleScriptContext();
    context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);

    final Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
//    bindings.put("endpoints", endpoints);
//    bindings.put("context", activationContext);

    return new Executor() {

      @Override
      public synchronized Object execute(Object input) {

        try {
          script.eval(context);
        } catch (ScriptException ex) {
          throw new AdapterException(
              "unable to reset script context " + artifact.toString() + " : " + ex.getMessage(),
              ex);
        }
        Object output = ((ScriptObjectMirror) bindings).callMember(entry, input);

        return output;
      }
    };
  }

  private CompiledScript getCompiledScript(String artifact, String entry) {
    byte[] binary;
    try {
//      binary = cdoStore.getBinary(artifact);
      binary = activationContext.getBinary(artifact);
    } catch (ShelfResourceNotFound e) {
      throw new AdapterException(e.getMessage(), e);
    }
    if (binary == null) {
      throw new AdapterException(
          String.format("Can't find endpoint %s in path %s", entry, artifact));
    }
    CompiledScript script;
    try {
      script = ((Compilable) engine)
          .compile(new String(binary, Charset.defaultCharset()));
    } catch (ScriptException e) {
      throw new AdapterException("unable to compile script " + artifact + " : " + e.getMessage(),
          e);
    }
    return script;
  }

  @Override
  public String status() {
    if (engine == null) {
      return "DOWN";
    }
    if (activationContext == null) {
      return "DOWN";
    }
    return "UP";
  }

}
