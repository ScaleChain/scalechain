package io.scalechain.util

// BUGBUG : Add a unit test.
sealed class Option<T>() {
    class Some<T>(val value : T) : Option<T>() {
        override fun toNullable() : T? = value
    }

    class None<T> : Option<T>() {
        override fun toNullable() : T? = null
    }

    abstract fun toNullable() : T?

    companion object {
        fun<T> from(value : T?) : Option<T> {
            if (value == null) {
                return None()
            } else {
                return Some(value)
            }
        }
    }
}
