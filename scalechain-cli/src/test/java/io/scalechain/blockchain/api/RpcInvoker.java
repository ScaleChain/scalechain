package io.scalechain.blockchain.api;

import com.google.gson.*;
import io.scalechain.util.HttpRequester;

/**
 * Small utility class for calling rpc interface of Scalechain daemon.
 * For test purpose only.
 *
 * Created by shannon on 16. 12. 1.
 */
public class RpcInvoker {
  String url;
  String user;
  String password;
  boolean verbose = false;

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public RpcInvoker(String host, int port, String user, String password) {
    this.user = user;
    this.password = password;
    StringBuilder sb = new StringBuilder("http://").append(host).append(':').append(port).append('/');
    url = sb.toString();
  }

  public JsonObject invoke(String method, JsonArray args) throws RpcCallException {
    JsonObject request = new JsonObject();
    request.addProperty("jsonrpc", "1.0");
    request.addProperty("id", 1);
    request.addProperty("method", method);
    request.add("params", args);
    if (verbose) System.out.println("Rpc.invoke: request=" + request.toString());
    String response = HttpRequester.post(url, request.toString(), user, password);
    if (verbose) System.out.println("Rpc.invoke: response=" + response.toString());
    JsonObject json = new JsonParser().parse(response).getAsJsonObject();
    JsonElement error = json.get("error");
    if (error != null && !(error instanceof JsonNull)) {
      int code = Integer.MIN_VALUE;
      String message = "";
      String data = "";
      JsonObject errorObject = error.getAsJsonObject();
      if (errorObject.has("code")) code = errorObject.get("code").getAsInt();
      if (errorObject.has("message")) message = errorObject.get("message").getAsString();
      if (errorObject.has("data")) data = errorObject.get("data").getAsString();
      throw new RpcCallException(code, message, data);
    }
    return json;
  }

  /**
   * Invkokes Json Rpc.
   * Parameters are given as JsonArray.
   * Returns whole Json response.
   *
   * @param host
   * @param port
   * @param method
   * @param args
   * @param user
   * @param password
   * @return
   */
  public static JsonObject invoke(String host, int port, String method, JsonArray args, String user, String password) throws RpcCallException {
    RpcInvoker invoker = new RpcInvoker(host, port, user, password);
    return invoker.invoke(method, args);
  }


  //
  // RpcCallException
  //
  public class RpcCallException extends Exception {
    int code;
    String message;
    String data;
    RpcCallException(int code, String message, String data) {
      super(message);
      this.code = code;
      this.message = message;
      this.data = data;
    }
    RpcCallException(int code, String message, String data, Throwable t) {
      super(message, t);
      this.code = code;
      this.message = message;
      this.data = data;
    }

    public String getData() {
      return data;
    }
  }
}
