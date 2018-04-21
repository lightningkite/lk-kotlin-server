package lk.kotlin.server.types

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import lk.kotlin.jackson.jacksonToNode
import lk.kotlin.jackson.jacksonToString
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.friendlyName
import lk.kotlin.reflect.classedName
import lk.kotlin.reflect.fastMutableProperties
import lk.kotlin.reflect.typeInformation
import lk.kotlin.server.base.*
import lk.kotlin.server.types.common.ServerFunction
import lk.kotlin.server.types.log.HistoricalServerFunction
import lk.kotlin.server.types.log.ServerFunctionLogger
import kotlin.reflect.KClass

fun KClass<*>.urlName() = friendlyName.toLowerCase().replace(' ', '-').replace(Regex("-*"), "")

fun HttpRequestHandlerBuilder.rpc(
        url: String,
        context: Context,
        getUser: (HttpRequest) -> Any? = { null },
        logger: ServerFunctionLogger? = null,
        functionList: List<TypeInformation>
) {
    for (functionType in functionList) {
        val funcUrl = url + "/" + functionType.kclass.urlName()
        println("funcUrl $funcUrl")

        if (functionType.kclass.fastMutableProperties.isEmpty()) {
            get(funcUrl) {
                val user = getUser(this)
                val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
                val result = Transaction(context, user).use {
                    request.invoke(it)
                }
                logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
                @Suppress("UNCHECKED_CAST")
                respondWith(context = context, user = user, typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
            }
        } else {
            get(funcUrl) {
                val user = getUser(this)
                val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
                respondWith(context = context, user = user, typeInformation = functionType, output = request)
            }
        }
        post(funcUrl) {
            val user = getUser(this)
            val request = inputAs<ServerFunction<*>>(context = context, user = user, typeInformation = functionType)
            val result = Transaction(context, user).use {
                request.invoke(it)
            }
            logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
            @Suppress("UNCHECKED_CAST")
            respondWith(context = context, user = user, typeInformation = (functionType.kclass as KClass<out ServerFunction<*>>).returnType, output = result)
        }
    }
    get("$url/index") {
        val user = getUser(this)
        if(user == null){

        }
        respondHtml {
            append("<!DOCTYPE html>")
            append("<html>")
            append("<head>")
            append("<meta charset=\"utf-8\"/>")
            append("<link rel=\"stylesheet\" href=\"/style.css\"/>")
            append("</head>")
            append("<body>")
            append("<h1>Welcome!</h1>")
            append("<p>You are logged in as: ${getUser(this@get)}</p>")
            append("<div class = \'flexClass\'>")
            val groupedFunctions: Map<String?, List<TypeInformation>> = functionList.groupBy {
                it.kclass.friendlyName
                        .substringBeforeLast(" -","- Misc")
            }
            for((groupName, functions) in groupedFunctions){
                append("<div class = \'section\'>")
                append("<h2 style = \'text-align: center;\'>${groupName}s</h2>")
                append("<ul>")

                for(func in functions){
                    append("<li><a href=\"${func.kclass.urlName()}\">${func.kclass.simpleName}</a></li>")
                }
                append("</ul>")
                append("</div>")
            }
            append("</div>")
            append("</body>")
            append("</html>")
        }
    }
    post(url) {
        val user = getUser(this)
        val request = inputAs<ServerFunction<*>>(context = context, user = user)
        val result = Transaction(context, user).use {
            request.invoke(it)
        }
        logger?.log(HistoricalServerFunction(userIdentifier = user, call = request, result = result))
        respondWith(context = context, user = user, typeInformation = request.javaClass.kotlin.returnType, output = result)
    }
    post("$url/bulkjson") {
        val user = getUser(this)
        val requests = CentralContentTypeMap.json.mapper
                .readValue<List<ServerFunction<*>>>(input)
        val result = Transaction(context, user).use { txn ->
            requests.map { it.invoke(txn) }
        }
        //TODO log?
        respond(
                code = 200,
                contentType = ContentType.Application.Json,
                data = result.jacksonToString(CentralContentTypeMap.json.mapper).toByteArray()
        )
    }
}