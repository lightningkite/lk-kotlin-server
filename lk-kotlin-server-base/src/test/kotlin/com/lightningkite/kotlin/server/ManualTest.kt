package com.lightningkite.kotlin.server

import com.lightningkite.kotlin.server.base.Context
import com.lightningkite.kotlin.server.base.ServerSettings
import com.lightningkite.kotlin.server.base.Transaction
import com.lightningkite.kotlin.server.base.respondHtml
import com.lightningkite.kotlin.server.jetty.asJettyHandler
import com.lightningkite.kotlin.server.types.*
import com.lightningkite.kotlin.server.types.annotations.GetFromID
import com.lightningkite.kotlin.server.types.annotations.PrimaryKey
import com.lightningkite.kotlin.server.types.annotations.Query
import com.lightningkite.kotlin.server.xodus.*
import jetbrains.exodus.entitystore.PersistentEntityStores
import lk.kotlin.reflect.TypeInformation
import lk.kotlin.reflect.annotations.EstimatedLength
import lk.kotlin.reflect.annotations.Hidden
import lk.kotlin.reflect.annotations.UniqueIdentifier
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.ResourceHandler
import org.junit.Test
import java.util.*

class ManualTest {
    //Data Model

    @GetFromID(GetUser::class)
    @Query(GetUsers::class)
    data class User(
            @PrimaryKey @Hidden override var id: String = "",
            var name: String = "",
            @EstimatedLength(5000) var bio: String = "",
            var created: Date = Date(),
            var role: Role = Role.Admin
    ) : XodusStorable {

        enum class Role { Admin, Poster, Other }

        override fun toString(): String {
            return name
        }
    }

    @GetFromID(GetPost::class)
    @Query(GetPosts::class)
    data class Post(
            @UniqueIdentifier @Hidden override var id: String = "",
            var postedBy: Pointer<User, String> = Pointer(),
            var title: String = "",
            @EstimatedLength(5000) var content: String = "",
            var created: Date = Date()
    ) : XodusStorable {
        override fun toString(): String {
            return title
        }
    }

    //Functionality
    class HelloWorldFunction(
            var name: String = "no-name"
    ) : ServerFunction<String> {
        override fun invoke(transaction: Transaction): String = "Hello, $name!"
    }

    data class AddUser(
            var user: User = User()
    ) : ServerFunction<User> {
        override fun invoke(transaction: Transaction): User = transaction.xodus.write(user).let { user }
    }

    data class AddPost(
            var post: Post = Post()
    ) : ServerFunction<Post> {
        override fun invoke(transaction: Transaction): Post = transaction.xodus.write(post).let { post }
    }

    data class GetUser(
            var id: String = ""
    ) : ServerFunction<User> {
        override fun invoke(transaction: Transaction): User = transaction.xodus.get<User>(id)
    }

    data class GetPost(
            var id: String = ""
    ) : ServerFunction<Post> {
        override fun invoke(transaction: Transaction): Post = transaction.xodus.get<Post>(id)
    }

    class GetUsers : ServerFunction<List<User>> {
        override fun invoke(transaction: Transaction): List<User> {
            return transaction.xodus.getAll<User>()
                    .asSequence()
                    .take(100)
                    .map { it.read<User>() }
                    .toList()
        }
    }

    class GetPosts : ServerFunction<List<Post>> {
        override fun invoke(transaction: Transaction): List<Post> {
            return transaction.xodus.getAll<Post>()
                    .asSequence()
                    .take(100)
                    .map { it.read<Post>() }
                    .toList()
        }
    }

    class BrokenFunction : ServerFunction<String> {
        override fun invoke(transaction: Transaction): String = throw IllegalArgumentException("Not allowed")
    }

    @Test
    fun main() {
        val context: Context = HashMap()
        context.xodus = PersistentEntityStores.newInstance("./xodus")

        ServerSettings.debugMode = true

        Server(8080).apply {
            handler = HandlerCollection(
                    TypedExceptionHttpRequestHandler().apply {
                        get("") {
                            respondHtml(html = "Hello!")
                        }
                        rpc(
                                url = "rpc",
                                context = context,
                                getUser = { null },
                                logger = SimpleServerFunctionLogger(),
                                functionList = listOf(
                                        HelloWorldFunction::class,
                                        AddUser::class,
                                        AddPost::class,
                                        GetUsers::class,
                                        GetPosts::class,
                                        BrokenFunction::class
                                ).map { TypeInformation(it) }
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