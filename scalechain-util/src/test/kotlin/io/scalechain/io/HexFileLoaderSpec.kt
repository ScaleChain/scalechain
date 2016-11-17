package io.scalechain.io

import io.kotlintest.*
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.HexUtil
import java.io.File
import java.io.FileOutputStream
import java.util.*

class HexFileLoaderSpec : FlatSpec(), Matchers {

    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // tear-down code
        //
    }

    val HEX_FILE_CONTENT =
"""00000000  04 00 00 00 33 52 85 50  87 a8 a9 03 78 96 a9 22  ....3R<85>P<87>¨©.x<96>©"
00000010  aa c6 ff ab c9 73 15 a5  88 a7 b2 03 00 00 00 00  ªÆÿ«És.¥<88>§².....
00000020  00 00 00 00 b1 a7 95 87  8d a6 e1 e0 92 4f 71 8d  ....±§<95><87><8d>¦áà<92>Oq<8d>
00000030  20 d2 c5 e6 5b fe f9 ce  a9 4b 54 cd e4 ff f3 88   ÒÅæ[þùÎ©KTÍäÿó<88>
00000040  06 e5 3f e3 b5 25 ab 56  f0 28 09 18 ee dc d1 c2  .å?ãµ%«Vð(..îÜÑÂ
00000050  fd 4b 02 01 00 00 00 01  00 00 00 00 00 00 00 00  ýK..............
00000060  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................
00000070  00 00 00 00 00 00 00 00  ff ff ff ff 64 03 3e 09  ........ÿÿÿÿd.>.
00000080  06 e4 b8 83 e5 bd a9 e7  a5 9e e4 bb 99 e9 b1 bc  .ä¸<83>å½©ç¥<9e>ä»<99>é±¼
00000090  c8 db cb 33 42 64 8d 94  c0 03 6b bd b4 72 20 df  ÈÛË3Bd<8d><94>À.k½´r ß
000000a0  a5 48 88 b8 a2 a6 a5 a7  80 37 d4 d1 a7 6a 21 73  ¥H<88>¸¢¦¥§<80>7ÔÑ§j!s """

    val EXPECTED_CONTENT =
        """ 04 00 00 00 33 52 85 50  87 a8 a9 03 78 96 a9 22
            aa c6 ff ab c9 73 15 a5  88 a7 b2 03 00 00 00 00
            00 00 00 00 b1 a7 95 87  8d a6 e1 e0 92 4f 71 8d
            20 d2 c5 e6 5b fe f9 ce  a9 4b 54 cd e4 ff f3 88
            06 e5 3f e3 b5 25 ab 56  f0 28 09 18 ee dc d1 c2
            fd 4b 02 01 00 00 00 01  00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00
            00 00 00 00 00 00 00 00  ff ff ff ff 64 03 3e 09
            06 e4 b8 83 e5 bd a9 e7  a5 9e e4 bb 99 e9 b1 bc
            c8 db cb 33 42 64 8d 94  c0 03 6b bd b4 72 20 df
            a5 48 88 b8 a2 a6 a5 a7  80 37 d4 d1 a7 6a 21 73 """

    init {

        "load" should "load a hex file" {

            File("hextest").mkdir()
            FileOutputStream("hextest/test.hex").write(HEX_FILE_CONTENT.toByteArray())

            var index = 0
            for ( (x,y) in HexFileLoader.load("hextest/test.hex").zip(HexUtil.bytes( EXPECTED_CONTENT ))) {
                println("[${index++}] $x == $y")
            }

            assert(
                Arrays.equals(
                    HexUtil.bytes( EXPECTED_CONTENT ).toByteArray(),
                    HexFileLoader.load("hextest/test.hex")
                )
            )

            File("hextest").deleteRecursively()
        }
    }
}