package lk.kotlin.server.types.common

interface PointerServerFunction<T : HasId<K>, K> : ServerFunction<T> {
    var id: K
}