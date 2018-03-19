package com.lightningkite.kotlin.server.xodus

import com.lightningkite.kotlin.server.base.Context
import jetbrains.exodus.entitystore.PersistentEntityStore
import java.util.*

val Context_xodus = WeakHashMap<Context, PersistentEntityStore>()
var Context.xodus: PersistentEntityStore
    get() = Context_xodus[this]!!
    set(value) {
        Context_xodus[this] = value
    }