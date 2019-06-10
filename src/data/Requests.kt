package io.mocksakov.data

sealed class AddMock(
    val url: String,
    val method: String = "GET"
) {

    class AddMockString(
        url: String,
        method: String,
        val response: String
    ) : AddMock(url, method)

    class AddMockFile(
        url: String,
        method: String,
        val responseFilePath: String
    ) : AddMock(url, method)

}

class NewHost(val host: String)

