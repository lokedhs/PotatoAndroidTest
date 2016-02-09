package com.dhsdevelopments.potato

fun <T> nlazy(getter: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getter()
    }
}
