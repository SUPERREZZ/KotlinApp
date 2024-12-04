package com.example.kotlinapp1.api
import com.example.kotlinapp1.model.Students
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {

    fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return connection.inputStream.bufferedReader()
            .use { it.readText() }
    }

    fun post(url: String, jsonBody: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val outputStream = connection.outputStream
        outputStream.write(jsonBody.toByteArray())
        outputStream.flush()

        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun put(url: String, jsonBody: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val outputStream = connection.outputStream
        outputStream.write(jsonBody.toByteArray())
        outputStream.flush()
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun delete(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"

        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}
 fun test(){
     val students : Students = Students(1,"Naruto","L")
     val (id, nama, kelamin) = students
 }