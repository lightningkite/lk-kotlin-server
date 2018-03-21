package com.lightningkite.kotlin.server.xodus

import com.lightningkite.kotlin.server.base.Context
import jetbrains.exodus.entitystore.PersistentEntityStore

var Context.xodus: PersistentEntityStore
    get() = this["xodus"] as PersistentEntityStore
    set(value) {
        this["xodus"] = value
    }