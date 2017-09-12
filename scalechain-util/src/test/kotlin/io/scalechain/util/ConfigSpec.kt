package io.scalechain.util

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import java.math.BigInteger
import java.security.SecureRandom

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(KTestJUnitRunner::class)
class ConfigSpec : FlatSpec(), Matchers {

    val PRIVATE_CONFIG_DATA =
        """
            scalechain {
              general {
                regtest=1
              }
              api {
                port = 8080
                user = "user"
                password = "pleasechangethispassword123@.@"
              }
              p2p {
                port = 7643
                peers = [
                  { address:"127.0.0.1", port:"7643" },
                  { address:"127.0.0.1", port:"7644" }
                ] 
              }
              network {
                name = "testnet"
              }
              mining {
                max_block_size = 1048576
                account = "_FOR_TEST_ONLY"
            #    address = "n3KyfQbGr6bDRbyZMTb3x69npriXotyhd3"
              }

              # If private is defined, the is a permissioned node in a private blockchain.
              private = 1

              storage {
                cassandra.ignore {
                  address = "127.0.0.1"
                  port = 9042
                }
              }
            }
        """

    val CONFIG_FILE = "testconfig/scalechain.conf"
    var config : Config? = null

    override fun beforeEach() {
        // set-up code
        //
        File("testconfig").mkdir()
        FileOutputStream(CONFIG_FILE).write(PRIVATE_CONFIG_DATA.toByteArray())
        config = Config(ConfigFactory.parseFile( File(CONFIG_FILE)))

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
        File("testconfig").deleteRecursively()
        config = null
    }

    init {
        "hasPath" should "" {
            config?.hasPath("scalechain") shouldBe true
            config?.hasPath("scalechain.general") shouldBe true

            config?.hasPath("scalechain.notexistent") shouldBe false
            config?.hasPath("notexistent") shouldBe false
        }

        "getInt" should "return a number" {
            config?.getInt("scalechain.api.port") shouldBe 8080
        }

        "getInt" should "throw an exception if unable to convert to a number" {
            shouldThrow<ConfigException.WrongType> {
                config?.getInt("scalechain.api.user")
            }
        }

        "getInt, getString, getConfigList" should "throw an exception if the path does not exist" {
            shouldThrow<ConfigException.Missing> {
                config?.getInt("scalechain.notexistent")
            }

            shouldThrow<ConfigException.Missing> {
                config?.getString("scalechain.notexistent")
            }

            shouldThrow<ConfigException.Missing> {
                config?.getConfiglistOf("scalechain.notexistent")
            }
        }

        "getString" should "return a string" {
            config?.getString("scalechain.api.user") shouldBe "user"
        }

        "getConfigList" should "return a list of configuration items" {
            val peers = config?.getConfiglistOf("scalechain.p2p.peers")!!

            peers.map{ it.toString() } shouldBe listOf(
                """Config(SimpleConfigObject({"address":"127.0.0.1","port":"7643"}))""",
                """Config(SimpleConfigObject({"address":"127.0.0.1","port":"7644"}))"""
            )
        }

        "isPrivate" should "return true if the scalechain.private exists" {
            config?.isPrivate() shouldBe true
        }

        "isPrivate" should "return false if the scalechain.private does not exist" {
            val PUBLIC_CONFIG_DATA =
                """
                    scalechain {
                        general {
                            regtest=1
                        }
                    }
                """

            File("publicconfig").mkdir()
            FileOutputStream("publicconfig/public.conf").write(PUBLIC_CONFIG_DATA.toByteArray())
            val publicConfig = Config(ConfigFactory.parseFile( File("publicconfig/public.conf")))

            publicConfig.isPrivate() shouldBe false

            File("publicconfig").deleteRecursively()
        }

        "peerAddresses" should "" {
            config?.peerAddresses() shouldBe listOf(
                PeerAddress("127.0.0.1", 7643),
                PeerAddress("127.0.0.1", 7644)
            )
        }
    }
}
