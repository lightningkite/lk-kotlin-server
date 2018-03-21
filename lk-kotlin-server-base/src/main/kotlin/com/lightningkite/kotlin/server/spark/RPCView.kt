package com.lightningkite.kotlin.server.spark

import com.lightningkite.kotlin.server.base.*
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.reflectAsmConstruct
import lk.kotlin.reflect.typeInformation
import spark.Request
import spark.Response
import spark.Service

inline fun <reified T : ServerFunction<*>> Service.getFunction(
        path: String,
        context: Context,
        crossinline getUser: (Request) -> Any?,
        crossinline modifier: (Request, T) -> Unit = { _, _ -> }
) = get(path) { request, response ->
    response.processServerFunctionAndRespond(
            transaction = Transaction(context, getUser(request)),
            request = request,
            headers = request.headers().associate { it to request.headers(it) },
            function = T::class.reflectAsmConstruct().apply { modifier(request, this) }
    )
}

inline fun <reified T : ServerFunction<*>> Service.postFunction(
        path: String,
        context: Context,
        crossinline getUser: (Request) -> Any?,
        crossinline modifier: (Request, T) -> Unit = { _, _ -> }
) {
    val typeInformation = typeInformation<T>()
    post(path) { request, response ->
        request.parseProcessRespond(response, context, getUser, typeInformation, modifier)
    }
}

inline fun <reified T : ServerFunction<*>> Service.putFunction(
        path: String,
        context: Context,
        crossinline getUser: (Request) -> Any?,
        crossinline modifier: (Request, T) -> Unit = { _, _ -> }
) {
    val typeInformation = typeInformation<T>()
    put(path) { request, response ->
        request.parseProcessRespond(response, context, getUser, typeInformation, modifier)
    }
}

inline fun <reified T : ServerFunction<*>> Service.patchFunction(
        path: String,
        context: Context,
        crossinline getUser: (Request) -> Any?,
        crossinline modifier: (Request, T) -> Unit = { _, _ -> }
) {
    val typeInformation = typeInformation<T>()
    patch(path) { request, response ->
        request.parseProcessRespond(response, context, getUser, typeInformation, modifier)
    }
}

inline fun <reified T : ServerFunction<*>> Service.deleteFunction(
        path: String,
        context: Context,
        crossinline getUser: (Request) -> Any?,
        crossinline modifier: (Request, T) -> Unit = { _, _ -> }
) = delete(path) { request, response ->
    response.processServerFunctionAndRespond(
            transaction = Transaction(context, getUser(request)),
            request = request,
            headers = request.headers().associate { it to request.headers(it) },
            function = T::class.reflectAsmConstruct().apply { modifier(request, this) }
    )
}

inline fun <T : ServerFunction<*>> Request.parseProcessRespond(
        response: Response,
        context: Context,
        getUser: (Request) -> Any?,
        typeInformation: TypeInformation,
        modifier: (Request, T) -> Unit = { _, _ -> Unit }
) {
    val headers = headers().associate { it to headers(it) }
    response.processServerFunctionAndRespond(
            transaction = Transaction(context, getUser(this)),
            request = this,
            headers = headers,
            function = parseAsServerFunction<T>(typeInformation, headers).also { modifier(this, it) }
    )
}


fun <T : ServerFunction<*>> Request.parseAsServerFunction(typeInformation: TypeInformation, headers: Map<String, String>): T {
    return raw().inputStream.use {
        val type = contentTypeObject().parameterless()
        CentralContentTypeMap.parse(
                contentType = type,
                options = headers,
                type = typeInformation,
                stream = it
        )
    }
}

fun Response.processServerFunctionAndRespond(
        transaction: Transaction,
        request: Request,
        headers: Map<String, String>,
        function: ServerFunction<*>
) {
    val result = try {
        transaction.use {
            function.invoke(it)
        }
    } catch (e: Exception) {
        //TODO: Log the exception
        e.printStackTrace()
        this.status(400)
        this.body(e.message)
        return
    }

    try {
        raw().outputStream.use {
            CentralContentTypeMap.render(
                    contentTypes = request.acceptList(),
                    options = headers,
                    type = function.javaClass.kotlin.returnType,
                    data = result,
                    stream = it
            )
        }
    } catch (e: Exception) {
        //TODO: Log the exception
        e.printStackTrace()
        this.status(500)
        this.body(e.message)
        return
    }
}