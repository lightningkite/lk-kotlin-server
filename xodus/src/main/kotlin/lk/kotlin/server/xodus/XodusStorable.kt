package lk.kotlin.server.xodus

import lk.kotlin.server.types.common.HasId


interface XodusStorable : HasId<String> {
    override var id: String
}