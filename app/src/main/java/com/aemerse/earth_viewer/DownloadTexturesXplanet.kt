package com.aemerse.earth_viewer

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.util.*

class DownloadTexturesXplanet(mGLES20Renderer: OpenGLES20Renderer?) : DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true
        val tag = 'X'
        gles20Renderer!!.mTag = tag
        var is2: InputStream? = null
        val b: Bitmap? = null
        var ucon: URLConnection? = null
        var url: URL? = null

        // load image from cache

        // find latest image but not older then 3 hours
        var filename: String? = null
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
        cal[Calendar.HOUR_OF_DAY] = 24 * (cal[Calendar.HOUR_OF_DAY] / 24)
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        val dir = gles20Renderer!!.mContext.filesDir
        val subFiles: Array<File>?
        val epoch = cal.timeInMillis
        val current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).timeInMillis
        val files_to_download = 1
        progressDialogSetMax(files_to_download)
        var exists = false
        filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch)
        subFiles = dir.listFiles()
        if (subFiles != null) {
            for (file in subFiles) {
                if (filename == file.name) {
                    exists = true
                    break
                }
            }
        }
        if (exists == false) {
            // download from internet
            try {
                /* Open a connection to that URL. */
                url = URL("http://xplanetclouds.com/free/local/clouds_2048.jpg")
                Log.d("H21lab", "Downloading: $url")
                ucon = url.openConnection()
                ucon.useCaches = false
                ucon.connect()
                is2 = ucon.getInputStream()
                val mis2 = ByteArrayOutputStream()
                val data = ByteArray(1024)
                var count: Int
                while (is2.read(data, 0, 1024).also { count = it } != -1) {
                    mis2.write(data, 0, count)
                }
                mis2.flush()
                is2.close()

                // b = BitmapFactory.decodeStream(is2);
                val ba = mis2.toByteArray()
                gles20Renderer!!.saveTexture(filename, ba, 2048, 1024)
                mis2.close()
            } catch (e: Exception) {
                Log.e("H21lab", "Unable to connect to " + ucon!!.url.toString() + " " + e.message)
                if (ucon != null) {
                    try {
                        url = URL(ucon.url.toString().replace(".nyud.net:8080", ""))
                        Log.d("H21lab", "Downloading: $url")
                        ucon = url.openConnection()
                        ucon.useCaches = false
                        ucon.connect()
                        is2 = ucon.getInputStream()
                        val mis2 = ByteArrayOutputStream()
                        val data = ByteArray(1024)
                        var count: Int
                        while (is2.read(data, 0, 1024).also { count = it } != -1) {
                            mis2.write(data, 0, count)
                        }
                        mis2.flush()
                        is2.close()
                        val ba = mis2.toByteArray()
                        gles20Renderer!!.saveTexture(filename, ba, 2048, 1024)
                        mis2.close()
                    } catch (e2: Exception) {
                        if (ucon != null) {
                            Log.e(
                                "H21lab",
                                "Unable to connect to " + ucon.url.toString() + " " + e2.message
                            )
                        } else if (url != null) {
                            Log.e(
                                "H21lab",
                                "Unable to connect to " + url.toString() + " " + e2.message
                            )
                        }
                    }
                }
            }
        }
        progressDialogUpdate()
        return ""
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}