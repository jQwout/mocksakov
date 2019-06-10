package io.mocksakov

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import io.mocksakov.data.AddMock
import io.mocksakov.data.MockStorage
import io.mocksakov.data.NewHost

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.defaultRoute() {
        when {
            MockStorage.host == null -> call.respond(HttpStatusCode.BadRequest, "host not found")
            else -> {
                MockStorage.getMock(call.request.path())?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: client.createProxyCall(call.request, MockStorage.host!!)
            }
        }
    }

    routing {

        post {
            defaultRoute()
        }

        put {
            defaultRoute()
        }

        get {
            defaultRoute()
        }

        delete {
            defaultRoute()
        }

        put("/set_host") {
            when (val response = call.receiveOrNull<NewHost>()) {
                null -> call.respond(HttpStatusCode.BadRequest, "invalid data")
                else -> {
                    MockStorage.newHost(response)
                    call.respond(HttpStatusCode.OK, "add new host on ${response.host}")
                }
            }
        }

        put("/add_mock") {
            call.addMock(MockStorage::addStringMock)
        }

        put("/add_mock_file") {
            call.addMock(MockStorage::addFileMock)
        }
    }
}

suspend inline fun <reified T : AddMock> ApplicationCall.addMock(
    mockSetter: (T) -> Unit
) {
    val mock = receiveOrNull<T>()
    if (mock != null) {
        mockSetter(mock)
        respond(HttpStatusCode.OK, "add new mock on ${mock.url}")
    } else {
        respond(HttpStatusCode.BadRequest, "invalid data")
    }
}

suspend fun HttpClient.createProxyCall(
    request: ApplicationRequest,
    url: String
) = call(url + request.uri) {
    request.headers.forEach(headers::appendAll)
    method = request.httpMethod
}