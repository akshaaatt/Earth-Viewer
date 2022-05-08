package com.aemerse.earth_viewer

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DownloadTexturesNRL(mGLES20Renderer: OpenGLES20Renderer?) :
    DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    // follow redirects
    @Throws(IOException::class)
    private fun openConnection(url: String): HttpURLConnection? {
        var url: String? = url
        if (url == null) {
            Log.e("H21lab", "url is null in method HttpURLConnection.")
            return null
        }
        var connection: HttpURLConnection
        var redirected: Boolean
        do {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.useCaches = false
            val code = connection.responseCode
            redirected =
                code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_SEE_OTHER
            if (redirected) {
                url = connection.getHeaderField("Location")
                connection.disconnect()
            }
        } while (redirected)
        return connection
    }

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true
        var myUri = "https://www.nrlmry.navy.mil/archdat/global/rain/accumulations/geo/3-hour/"
        var tag = 'R'
        if (p0[0] == "RAINRATE") {
            myUri = "https://www.nrlmry.navy.mil/archdat/global/rain/accumulations/geo/3-hour/"
            tag = 'R'
        }
        gles20Renderer!!.mTag = tag
        var is2: InputStream? = null
        val b: Bitmap? = null
        var ucon: HttpURLConnection? = null
        var url: URL? = null

        // load image from cache

        // find latest image but not older then 3 hours
        var filename: String? = null
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
        cal[Calendar.HOUR_OF_DAY] = 3 * (cal[Calendar.HOUR_OF_DAY] / 3)
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        val dir = gles20Renderer!!.mContext.filesDir
        var subFiles: Array<File>?
        var epoch: Long
        epoch = cal.timeInMillis
        progressDialogSetMax(8 - 0 + 1)
        epoch = epoch - 0 * 3600 * 1000
        var h = 0
        while (h <= 24) {
            if (isCancelled == true) {
                break
            }
            var exists = false
            epoch = epoch - 3 * 3600 * 1000
            filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch)
            Log.d("H21lab", "h = $h")
            Log.d("H21lab", "epoch = $epoch")
            Log.d("H21lab", "filename = $filename")
            subFiles = dir.listFiles()
            if (subFiles != null) {
                for (file in subFiles) {
                    if (filename == file.name) {
                        exists = true
                        break
                    }
                }
            }

            // do not download already existing
            if (exists) {
                progressDialogUpdate()
                h += 3
                continue
            }

            // download from internet
            try {
                var _filename = ""
                val c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
                c.timeInMillis = epoch
                val hour = String.format("%02d", c[Calendar.HOUR_OF_DAY])
                val day = String.format("%02d", c[Calendar.DAY_OF_MONTH])
                val month = String.format("%02d", (c[Calendar.MONTH] + 1))
                _filename =
                    Integer.toString(c[Calendar.YEAR]) + month + day + "." + hour + "00.geo.rainsum.global.3.jpg"
                url = URL(myUri + _filename)
                Log.d("H21lab", "Downloading: $url")

                //ucon = (HttpURLConnection)url.openConnection();
                //ucon.setUseCaches(false);
                //ucon.connect();

                // follow redirects
                ucon = openConnection(url.toString())
                if (ucon != null) {
                    is2 = ucon.inputStream
                    val mis2 = ByteArrayOutputStream()
                    val data = ByteArray(1024)
                    var count: Int
                    while (is2.read(data, 0, 1024).also { count = it } != -1) {
                        mis2.write(data, 0, count)
                    }
                    mis2.flush()
                    is2.close()
                    val ba = mis2.toByteArray()
                    gles20Renderer!!.saveTexture(filename, ba, 2048, 512)
                    mis2.close()
                }
            } catch (e: IOException) {
                if (ucon != null) {
                    Log.e("H21lab", "Unable to connect to " + ucon.url.toString() + " " + e.message)
                } else {
                    Log.e("H21lab", "Unable to connect to " + myUri + " " + e.message)
                }
            }
            progressDialogUpdate()
            h += 3
        }
        return ""
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}