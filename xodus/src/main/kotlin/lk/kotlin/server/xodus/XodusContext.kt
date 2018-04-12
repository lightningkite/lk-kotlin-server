package lk.kotlin.server.xodus

import jetbrains.exodus.entitystore.PersistentEntityStore
import lk.kotlin.server.base.Context

var Context.xodus: PersistentEntityStore
    get() = this["xodus"] as PersistentEntityStore
    set(value) {
        this["xodus"] = value
    }