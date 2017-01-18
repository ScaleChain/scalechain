package io.scalechain.blockchain.oap.env;

import io.scalechain.blockchain.transaction.ChainEnvironment;
import io.scalechain.blockchain.transaction.ChainEnvironment$;

/**
 * provide configuration data for RpcInvokder.
 *   used for bitcond interface.
 *
 * Created by shannon on 16. 12. 1.
 */
public class OpenAssetsProtocolEnv {
    public static void setEnvironmentName(String environmentName) {
        ChainEnvironment$.MODULE$.create(environmentName);
    }
    public static ChainEnvironment getEnvironment() {
        return ChainEnvironment$.MODULE$.activeEnvironmentOption().get();
    }
    public static ChainEnvironment getEnvironmentName() {
        return ChainEnvironment$.MODULE$.get();
    }

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
