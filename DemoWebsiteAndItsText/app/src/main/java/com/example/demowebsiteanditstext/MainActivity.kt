package com.example.demowebsiteanditstext

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private lateinit var customProgressDialog: Dialog

    private suspend fun callAPILoginAsyncTask() {

        try {

            // onPreExecuteと同等の処理
            withContext(Dispatchers.Main) {
                showProgressDialog()
            }


            // TODO: 処理


            // onPostExecuteと同等の処理
            withContext(Dispatchers.Main) {
                cancelProgressDialog()
            }
        } catch (e: java.lang.Exception) {


        }
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