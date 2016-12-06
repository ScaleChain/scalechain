package io.scalechain.util

sealed class Either<out A, out B> {
    abstract fun isLeft() : Boolean
    abstract fun isRight() : Boolean

    abstract fun left() : A?
    abstract fun right() : B?

    class Left<A>(val value: A): Either<A, Nothing>() {
        override fun isLeft() : Boolean = true
        override fun isRight() : Boolean = false
        override fun left()  = value
        override fun right() = null
    }
    class Right<B>(val value: B): Either<Nothing, B>() {
        override fun isLeft() : Boolean = true
        override fun isRight() : Boolean = false
        override fun left()  = null
        override fun right() = value
    }
}