package io.scalechain.blockchain.oap.env;

/**
 * provide configuration data for RpcInvokder.
 *   used for bitcond interface.
 *
 * Created by shannon on 16. 12. 1.
 */
public class OpenAssetsProtocolEnv {
    public static  String getHost() {
        return "localhost";
    }

    public static   int getPort() {
        return 8080;
    }

    public static  String getUser() {
        return "user";
    }

    public static  String getPassword() {
        return "pleasechangethispassword123@.@";
    }
}
