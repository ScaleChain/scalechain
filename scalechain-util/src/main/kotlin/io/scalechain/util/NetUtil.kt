package io.scalechain.util

import java.net.*

/**
 * Created by kangmo on 7/11/16.
 */
object NetUtil {
    @JvmStatic
    fun getLocalAddresses() : List<String> {
        val addresses = arrayListOf<String>()

        // better code suggetion by **b0c1** and **Janos Haber**
        for (iface in NetworkInterface.getNetworkInterfaces()) {
            for (address in iface.getInetAddresses()) {
                addresses.add( address.getHostAddress() )
            }
        }
        return addresses.toList()
/*
        // better code suggetion by **b0c1** and **Janos Haber**
        // But this code is Java 7 code. I need to use Java 6.
        val addresses = Collections.list(NetworkInterface.getNetworkInterfaces())
            .flatMap { iface ->
                Collections.list(iface.getInetAddresses()).map{ it.getHostAddress() }
            }
        return addresses
*/
    }
}
