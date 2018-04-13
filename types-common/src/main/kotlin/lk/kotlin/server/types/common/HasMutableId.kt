package lk.kotlin.server.types.common

interface HasMutableId<T> : HasId<T> {
    override var id: T
}

