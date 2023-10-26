package com.example.demowebsiteanditstext

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {

            // onPreExecuteと同等の処理
            withContext(Dispatchers.Main) {
                showProgressDialog()
            }

            // 非同期処理
            val result = withContext(Dispatchers.IO) {
                callAPILoginAsyncTask("denis", "123456")
            }

            val jsonObject = JSONObject(result)

            Log.i("JSON RESPONSE RESULT", result)

            val responseData = Gson().fromJson(result, ResponseData::class.java)
            Log.i("Message", responseData.message)


            // onPostExecuteと同等の処理
            withContext(Dispatchers.Main) {
                cancelProgressDialog()
            }
        }
    }

    private lateinit var customProgressDialog: Dialog

    private suspend fun callAPILoginAsyncTask(userName: String, password: String): String {

        var result: String

        var connection: HttpURLConnection? = null

        try {

            // MARK: 処理
            val url = URL("https://run.mocky.io/v3/cab0fa93-350f-48f7-9331-b6d422d2ced8")
            connection = url.openConnection() as HttpURLConnection

            connection.doInput = true
            connection.doOutput = true

            connection.instanceFollowRedirects = false

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Accept", "application/json")

            connection.useCaches = false

            val writeDataOutputStream = DataOutputStream(connection.outputStream)
            val jsonRequest = JSONObject()
            jsonRequest.put("username", userName)
            jsonRequest.put("password", password)

            writeDataOutputStream.writeBytes(jsonRequest.toString())
            writeDataOutputStream.flush()
            writeDataOutputStream.close()



            val httpResult: Int = connection.responseCode

            if (httpResult == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream

                val reader = BufferedReader(InputStreamReader(inputStream))

                val stringBuilder = StringBuilder()
                var line: String?

                try {

                    while (reader.readLine().also { line = it } != null ) {
                        stringBuilder.append(line + "\n")
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {

                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    result = stringBuilder.toString()
                }
            } else {
                result = connection.responseMessage
            }
        } catch (e: SocketTimeoutException) {

            result = "Connection Timeout"

        } catch (e: Exception) {
            result = "Error: " + e.message
        } finally {
            connection?.disconnect()
        }

        return  result
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog.show()
    }

    private fun cancelProgressDialog() {
        customProgressDialog.dismiss()
    }

//    private inner class CallAPILoginAsyncTask(): AsyncTask<Any, Void, String>() {
//
//    }
}