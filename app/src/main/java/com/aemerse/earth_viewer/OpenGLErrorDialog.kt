package com.aemerse.earth_viewer

import android.app.AlertDialog
import android.os.AsyncTask
import android.util.Log

class OpenGLErrorDialog(mGLES20Renderer: OpenGLES20Renderer?) : AsyncTask<String?, Void?, String>() {
    private var gles20Renderer: OpenGLES20Renderer? = null
    var dlgAlert: AlertDialog.Builder? = null
    var errorMsg: String? = null

    override fun onPreExecute() {
        dlgAlert = AlertDialog.Builder(gles20Renderer!!.mContext)
        super.onPreExecute()
    }

    override fun doInBackground(vararg urls: String?): String? {
        errorMsg = urls[0]
        return ""
    }

    override fun onPostExecute(response1: String) {
        Log.e("H21lab", errorMsg!!)
        dlgAlert!!.setMessage(errorMsg)
        dlgAlert!!.setTitle("Rendering Device Error")
        dlgAlert!!.setNegativeButton(
            "Crash & Report"
        ) { dialog, which -> //dismiss the dialog
            throw IllegalArgumentException(errorMsg)
        }
        dlgAlert!!.setPositiveButton(
            "Ignore Error"
        ) { dialog, which ->
            //dismiss the dialog
        }
        dlgAlert!!.setCancelable(true)
        dlgAlert!!.create().show()
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}