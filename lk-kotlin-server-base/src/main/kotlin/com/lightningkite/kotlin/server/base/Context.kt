package com.lightningkite.kotlin.server.base

typealias Context = MutableMap<String, Any?>

fun newContext() = HashMap<String, Any?>()