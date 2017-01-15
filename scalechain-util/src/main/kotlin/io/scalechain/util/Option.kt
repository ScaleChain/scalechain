package io.scalechain.util

sealed class Option<T>() {
    class Some<T>(val value : T) : Option<T>() {
        override fun toNullable() : T? = value
        override fun hashCode() : Int = value?.hashCode() ?: 0
        override fun equals(other: Any?): Boolean {
            if (other is Some<*>) {
                return value == other.value
            } else {
                return false
            }
        }
    }

    class None<T> : Option<T>() {
        override fun toNullable() : T? = null
        // BUGBUG : Currently None<TypeA> equals None<TypeB>
        override fun hashCode() : Int = 1258712095
        override fun equals(other: Any?): Boolean {
            return other is None<*>
        }
    }

    abstract fun toNullable() : T?

    companion object {
        @JvmStatic
        fun<T> from(value : T?) : Option<T> {
            if (value == null) {
                return None()
            } else {
                return Some(value)
            }
        }
    }
}
