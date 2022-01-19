package com.dicsa.flasg.util

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

suspend fun <T, V> List<T>.suspendMap(transform: suspend (T) -> V): List<V> = withContext(Dispatchers.Main) {
    val deferredDestination = ArrayList<Deferred<V>>()
    val destination = ArrayList<V>()

    for (item in this@suspendMap) {
        deferredDestination.add(async { transform(item) })
    }

    for (deferredResult in deferredDestination) {
        destination.add(deferredResult.await())
    }

    destination
}