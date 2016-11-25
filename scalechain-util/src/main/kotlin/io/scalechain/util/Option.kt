package io.scalechain.util

// BUGBUG : Add a unit test.
sealed class Option<T>() {
    class Some<T>(val value : T) : Option<T>()
    class None<T> : Option<T>()
}
