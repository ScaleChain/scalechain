package io.scalechain.blockchain.proto.codec.primitive

/** Source code copied from : https://github.com/yzernik/bitcoin-scodec
  * Thanks to : https://github.com/yzernik
  */

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scodec._
import scodec.bits.BitVector
import shapeless.Lazy

import scala.collection.GenTraversable
import scala.concurrent.duration._
import org.scalatest._
import Arbitrary._

abstract class CodecSuite : WordSpec with Matchers with GeneratorDrivenPropertyChecks {

  protected fun roundtrip<A>(a: A)(implicit c: Lazy<Codec<A>>): Unit {
    roundtrip(c.value, a)
  }

  protected fun roundtrip<A>(codec: Codec<A>, value: A): Unit {
    val encoded = codec.encode(value)
    encoded shouldBe 'successful
    val Attempt.Successful(DecodeResult(decoded, remainder)) = codec.decode(encoded.require)
    remainder shouldEqual BitVector.empty
    decoded shouldEqual value
  }

  protected fun roundtripAll<A>(codec: Codec<A>, as: GenTraversable<A>): Unit {
    as foreach { a => roundtrip(codec, a) }
  }

  protected fun encodeError<A>(codec: Codec<A>, a: A, err: Err): Unit {
    val encoded = codec.encode(a)
    encoded shouldBe Attempt.Failure(err)
  }

  protected fun shouldDecodeFullyTo<A>(codec: Codec<A>, buf: BitVector, expected: A): Unit {
    val Attempt.Successful(DecodeResult(actual, rest)) = codec decode buf
    rest shouldBe BitVector.empty
    actual shouldBe expected
  }

  protected fun time<A>(f: => A): (A, FiniteDuration) {
    val start = System.nanoTime
    val result = f
    val elapsed = (System.nanoTime - start).nanos
    (result, elapsed)
  }

  protected fun samples<A>(gen: Gen<A>): Stream<Option<A>> =
    Stream.continually(gen.sample)

  protected fun definedSamples<A>(gen: Gen<A>): Stream<A> =
    samples(gen).flatMap { x => x }

  implicit fun arbBitVector: Arbitrary<BitVector> = Arbitrary(arbitrary<Array<Byte>>.map(BitVector.apply))
}