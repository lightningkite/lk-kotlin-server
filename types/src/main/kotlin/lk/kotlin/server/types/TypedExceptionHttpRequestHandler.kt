package lk.kotlin.server.types

import lk.kotlin.reflect.TypeInformation
import lk.kotlin.server.base.HttpRequest
import lk.kotlin.server.base.HttpRequestHandler
import lk.kotlin.server.base.ServerSettings

class TypedException(val code: Int, val type: TypeInformation, val data: Any?) : Exception(data.toString())

class TypedExceptionHttpRequestHandler() : HttpRequestHandler() {
    override fun handle(request: HttpRequest): Boolean {
        val lookup = request.method.plus("|").plus(request.path).let {
            if (it.endsWith('/'))
                it.dropLast(1)
            else it
        }
        val callback = map[lookup] ?: return false
        try {
            callback.invoke(request)
        } catch (e: TypedException) {
            e.printStackTrace()
            request.respondWith(
                    code = e.code,
                    typeInformation = e.type,
                    output = e.data
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            val code = when(e){
                is IllegalArgumentException -> 400
                is IllegalAccessException -> 403
                else -> {
                    e.printStackTrace()
                    500
                }
            }
            val data:Any = if(ServerSettings.debugMode) e else e.message ?: ""

            request.respondWithImplied(
                    code = code,
                    output = data
            )
        }
        return true
    }

}