package io.mocksakov.data

import java.io.File

object MockStorage {
    private val map = hashMapOf<String, HashMap<String, String>>()

    var host: String? = null
        private set(value) {
            field = value
            map.clear()
        }

    fun newHost(host: NewHost) {
        this.host = host.host
    }

    private fun addMockAsString(
        url: String,
        method: String,
        response: String
    ) {
        map[url]
            ?.put(method, response)
            ?: map.put(url, newMap(method, response))
    }

    fun addStringMock(mock: AddMock.AddMockString) {
        addMockAsString(mock.url, mock.method, mock.response)
    }

    fun addFileMock(mock: AddMock.AddMockFile) {
        File(mock.responseFilePath).useLines {
            it.reduce { acc, s -> acc + s }
        }.let {
            addMockAsString(mock.url, mock.method, it)
        }
    }

    fun getMock(path: String) = map[path]

    private fun newMap(method: String, response: String) = hashMapOf(method to response)
}