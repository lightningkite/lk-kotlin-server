package lk.kotlin.server.base

typealias Context = MutableMap<String, Any?>

fun newContext() = HashMap<String, Any?>()