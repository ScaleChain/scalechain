package io.scalechain.blockchain.oap.rpc;

import com.google.gson.*;
import io.scalechain.util.HttpRequester;

/**
 * Simple rpc invokder used with bitcoind.
 *
 * Created by shannon on 16. 12. 1.
 */
public class RpcInvoker {
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
    public static JsonObject invoke(String host, int port, String method, JsonArray args, String user, String password) {
        StringBuilder sb = new StringBuilder("http://").append(host).append(':').append(port).append('/');
        JsonObject request = new JsonObject();
        request.addProperty("jsonrpc", "1.0");
        request.addProperty("id", 1);
        request.addProperty("method", method);
        request.add("params", args);
//        System.out.println(request.toString());
        String response = HttpRequester.post(sb.toString(), request.toString(), user, password);
//        System.out.println(response.toString());
        JsonObject json = new JsonParser().parse(response).getAsJsonObject();
        JsonElement error = json.get("error");
        if (error != null && !(error instanceof JsonNull)) {
            System.err.println("method=" + method + ", error=" + error.toString());
        }
        return json;
    }
}
