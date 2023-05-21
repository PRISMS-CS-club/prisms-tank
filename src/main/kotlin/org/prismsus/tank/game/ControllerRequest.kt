package org.prismsus.tank.game

import java.util.concurrent.CompletableFuture

data class ControllerRequest<T> @JvmOverloads constructor(
    val cid : Long,
    val returnTo : CompletableFuture<T>?,
    val requestType : ControllerRequestTypes,
    val params : Array<*>? = null,
    val timeStamp : Long = System.currentTimeMillis()
    ) : Comparable<ControllerRequest<*>> {
    override fun compareTo(other: ControllerRequest<*>): Int {
        return timeStamp.compareTo(other.timeStamp)
    }
    }