package lk.kotlin.server.types.html

import jetbrains.exodus.entitystore.PersistentEntityStores
import lk.kotlin.jackson.MyJackson
import lk.kotlin.reflect.ExternalClassRegistry
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.EstimatedLength
import lk.kotlin.reflect.annotations.Hidden
import lk.kotlin.reflect.annotations.Password
import lk.kotlin.reflect.jackson.useExternalClassRegistry
import lk.kotlin.server.base.Context
import lk.kotlin.server.base.ServerSettings
import lk.kotlin.server.base.respondHtml
import lk.kotlin.server.jetty.asJettyHandler
import lk.kotlin.server.types.TypedExceptionHttpRequestHandler
import lk.kotlin.server.types.common.PointerServerFunction
import lk.kotlin.server.types.common.ServerFunction
import lk.kotlin.server.types.common.annotations.GetFromID
import lk.kotlin.server.types.common.annotations.Query
import lk.kotlin.server.types.invocation
import lk.kotlin.server.types.log.SimpleServerFunctionLogger
import lk.kotlin.server.types.rpc
import lk.kotlin.server.xodus.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import org.junit.Test
import java.util.*

class ManualTest {


    //Data Model

    @GetFromID(User.Get::class)
    @Query(User.Query::class)
    data class User(
            @Hidden override var id: String = "",
            var name: String = "",
            @Password var password: String = "",
            @EstimatedLength(5000) var bio: String = "",
            var created: Date = Date(),
            var role: Role = Role.Other
    ) : XodusStorable {

        enum class Role { Admin, Poster, Other }

        override fun toString(): String {
            return name
        }

        data class Add(
                var user: User = User()
        ) : ServerFunction<User>

        data class Get(
                override var id: String = ""
        ) : PointerServerFunction<User, String>

        class Query : ServerFunction<List<User>>
    }

    @GetFromID(Post.Get::class)
    @Query(Post.Query::class)
    data class Post(
            @Hidden override var id: String = "",
            var postedBy: User.Get? = null,
            var title: String = "",
            @EstimatedLength(5000) var content: String = "",
            var tags: List<String> = listOf(),
            var created: Date = Date()
    ) : XodusStorable {
        override fun toString(): String {
            return title
        }

        data class Add(
                var post: Post = Post()
        ) : ServerFunction<Post>

        data class Get(
                override var id: String = ""
        ) : PointerServerFunction<Post, String>

        class Query : ServerFunction<List<Post>>
    }

    class HelloWorldFunction(
            var name: String = "no-name"
    ) : ServerFunction<String>

    class BrokenFunction : ServerFunction<String>


    //Functionality

    fun setupFunctionality() {
        User.Add::class.invocation = {
            it.xodus.write(user).let { user }
        }
        User.Get::class.invocation = {
            it.xodus.get<User>(id)
        }
        User.Query::class.invocation = {
            it.xodus.getAll<User>()
                    .asSequence()
                    .take(100)
                    .map { it.read<User>() }
                    .toList()
        }
        Post.Add::class.invocation = {
            it.xodus.write(post).let { post }
        }
        Post.Get::class.invocation = {
            it.xodus.get<Post>(id)
        }
        Post.Query::class.invocation = {
            it.xodus.getAll<Post>()
                    .asSequence()
                    .take(100)
                    .map { it.read<Post>() }
                    .toList()
        }
        HelloWorldFunction::class.invocation = {
            "Hello, $name!"
        }
        BrokenFunction::class.invocation = {
            throw IllegalArgumentException("Not allowed")
        }
    }


    //Main

    @Test
    fun main() {
        val context: Context = HashMap()
        context.xodus = PersistentEntityStores.newInstance("./xodus")

        ServerSettings.debugMode = true

        HtmlConverter().apply {
            @Suppress("UNCHECKED_CAST")
            val base = defaultGenerator.invoke(User::class) as HtmlSubConverter<User>
            register(User::class, object : HtmlSubConverter<User> by base {
                override fun render(info: ItemConversionInfo<User>, to: Appendable) {
                    base.render(info, to)
                    HtmlConverter.setCookie(to, "user", info.data?.name ?: "None")
                }
            })
            setup()
        }

        setupFunctionality()
        val functions = listOf(
                HelloWorldFunction::class,
                User.Add::class,
                User.Get::class,
                User.Query::class,
                Post.Add::class,
                Post.Get::class,
                Post.Query::class,
                BrokenFunction::class
        )

        functions.forEach { ExternalClassRegistry.registerWithSubtypes(it) }
        MyJackson.mapper.useExternalClassRegistry()

        Server(8080).apply {
            handler = HandlerCollection(
                    TypedExceptionHttpRequestHandler().apply {
                        get("") {
                            respondHtml(html = "Hello!")
                        }
                        get("cookies") {
                            println("Cookies: " + cookies.entries.joinToString { it.key + " = " + it.value })
                            respondHtml(html = cookies.entries.joinToString { it.key + " = " + it.value })
                        }
                        rpc(
                                url = "rpc",
                                context = context,
                                getUser = { null },
                                logger = SimpleServerFunctionLogger(),
                                functionList = functions.map { TypeInformation(it) }
                        )
                    }.asJettyHandler(),
                    ResourceHandler().apply {
                        isDirectoriesListed = true
                        welcomeFiles = arrayOf("index.html")
                        resourceBase = "C:\\Public"
                    }
            )
            start()
            join()
        }
    }
}