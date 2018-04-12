package lk.kotlin.server.xodus

import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException
import jetbrains.exodus.entitystore.StoreTransaction

fun StoreTransaction.getEntity(id: String) = getEntity(toEntityId(id))
fun StoreTransaction.getEntityOrNull(id: String) = try {
    getEntity(toEntityId(id))
} catch (e: EntityRemovedInDatabaseException) {
    null
}
