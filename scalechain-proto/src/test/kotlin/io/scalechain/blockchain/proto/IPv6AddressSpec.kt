package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith
import io.scalechain.blockchain.proto.IPv6Address
import io.scalechain.util.HexUtil.bytes

// Test case for data class IPv6Address
@RunWith(KTestJUnitRunner::class)
class IPv6AddressSpec : FlatSpec(), Matchers {
    init {
        "inetAddress" should "return InetAddress by reading big endian encoding of the adress in the address property." {
            // The IPv4 loopback address, {@code "127.0.0.1"}.
            // {@code 7f 00 00 01}
            //println("1 : " + IPv6Address( bytes( "7f 00 00 01") ).inetAddress() )
            IPv6Address( bytes( "7f 00 00 01") ).inetAddress().toString() shouldBe "/127.0.0.1"

            // The IPv6 loopback address, {@code "::1"}.
            // {@code 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01}
            //println("2 : " + IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01") ).inetAddress() )
            IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01") ).inetAddress().toString() shouldBe "/0:0:0:0:0:0:0:1"

            // From the IPv6 reserved documentation prefix ({@code 2001:db8::/32}), {@code "2001:db8::1"}.
            // {@code 20 01 0d b8 00 00 00 00 00 00 00 00 00 00 00 01}
            //println("3 : " + IPv6Address( bytes( "20 01 0d b8 00 00 00 00 00 00 00 00 00 00 00 01") ).inetAddress() )
            IPv6Address( bytes( "20 01 0d b8 00 00 00 00 00 00 00 00 00 00 00 01") ).inetAddress().toString() shouldBe "/2001:db8:0:0:0:0:0:1"

            // An IPv6 "IPv4 compatible" (or "compat") address, {@code "::192.168.0.1"}.
            // {@code 00 00 00 00 00 00 00 00 00 00 00 00 c0 a8 00 01}
            //println("4 : " + IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 00 00 c0 a8 00 01") ).inetAddress() )
            IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 00 00 c0 a8 00 01") ).inetAddress().toString() shouldBe "/0:0:0:0:0:0:c0a8:1"

            // An IPv6 "IPv4 mapped" address, {@code "::ffff:192.168.0.1"}.
            //{@code 00 00 00 00 00 00 00 00 00 00 ff ff c0 a8 00 01}
            //println("5 : " + IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 ff ff c0 a8 00 01") ).inetAddress() )
            IPv6Address( bytes( "00 00 00 00 00 00 00 00 00 00 ff ff c0 a8 00 01") ).inetAddress().toString() shouldBe "/192.168.0.1"

        }
    }
}