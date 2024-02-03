package sh.ouo.miband.uploader

import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class MySocketServer(
    private val port: Int,
    private val lpparam: LoadPackageParam,
    private val instance: Any
    ) {
    fun startServerInBackground() {
        Thread {
            try {
                val serverSocket = ServerSocket(port) // 选择一个端口
                Log.d("MiBand", "Server started on port: ${serverSocket.localPort}")
                while (!Thread.currentThread().isInterrupted) {
                    val clientSocket = serverSocket.accept()
                    val clientHandler = ClientHandler(clientSocket)
                    Thread(clientHandler).start()
                }
            } catch (e: Exception) {
                Log.e("MiBand", "Server Error: ${e.message}")
            }
        }.start()
    }

    @Serializable
    private data class SerializableResponse(
        val status: Int,
        val data: JsonElement
    )

    inner class ClientHandler(private val clientSocket: Socket) : Runnable {
        // 一开始我想着用 KTOR 实现 Restful API，然后又觉得太耗电，辛辛苦苦搓了个套接字。
        // 写完了之后发现我***，JavaScript 不能实现原生套接字，Obsidian 又是沙箱不能运行外部脚本
        // 得，现在变成我 Socket 手搓 HTTP 协议了，草。
        override fun run() {
            try {
                Log.d("MiBand", "Connection: $clientSocket")
                val inputStream = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val outputStream = PrintWriter(clientSocket.getOutputStream(), true)

                // 读取 HTTP 请求的第一行
                val requestLine = inputStream.readLine()
                println("Received: $requestLine")

                // 解析请求行
                val requestParts = requestLine?.split(" ")
                if (requestParts == null || requestParts.size < 3 || requestParts[0] != "GET") {
                    val resp = SerializableResponse(
                        status = 1,
                        data = JsonPrimitive("Invalid request")
                    )
                    sendSuccessResponse(outputStream, resp)
                    return
                }

                val pathWithParams = requestParts[1]
                val path = pathWithParams.split("?")[0]
                val params = parseQueryString(pathWithParams.split("?").getOrNull(1))

                when (path) {
                    "/getDailyReport" -> {
                        val type = params["type"]
                        val date = params["date"]
                        if (type == null) {
                            val resp = SerializableResponse(
                                status = 1,
                                data = JsonPrimitive("Missing 'type' parameter for /getDailyReport")
                            )
                            sendSuccessResponse(outputStream, resp)
                        } else {
                            // 处理 getDailyReport 请求
                            var resp: SerializableResponse
                            try {
                                val report = DailyReportFactory.createDailyReport(lpparam, instance, type)
                                val result = report.getDailyReport(date)
                                resp = SerializableResponse(
                                    status = 0,
                                    data = result
                                )

                            }
                            catch (e: Exception) {
                                Log.e("MiBand", "$e")
                                resp = SerializableResponse(
                                    status = 1,
                                    data = JsonPrimitive(e.message)
                                )
                            }
                            sendSuccessResponse(outputStream, resp)

                        }
                    }
                    else -> {
                        val resp = SerializableResponse(
                            status = 1,
                            data = JsonPrimitive("Unknown path: $path")
                        )
                        sendSuccessResponse(outputStream, resp)
                    }
                }
                inputStream.close()
                outputStream.close()
                clientSocket.close()
                Log.d("MiBand", "Established")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun parseQueryString(query: String?): Map<String, String> {
        val queryPairs = LinkedHashMap<String, String>()
        val pairs = query?.split("&") ?: emptyList()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx != -1) {
                val key = pair.substring(0, idx)
                val value = pair.substring(idx + 1)
                queryPairs[key] = value
            }
        }
        return queryPairs
    }
    private fun sendSuccessResponse(outputStream: PrintWriter, result: SerializableResponse) {
        val jsonResponse = Json.encodeToString(result)
        val response = """
            HTTP/1.1 200 OK
            Content-Type: application/json
            Access-Control-Allow-Origin: *
            Connection: close
            Content-Length: ${jsonResponse.toByteArray().size}
            
            $jsonResponse
        """.trimIndent()
        outputStream.println(response)
        outputStream.flush()
    }
}