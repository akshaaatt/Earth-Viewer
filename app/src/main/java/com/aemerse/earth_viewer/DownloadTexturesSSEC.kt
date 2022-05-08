package com.aemerse.earth_viewer

import android.graphics.Bitmap
import android.util.Log
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*

class DownloadTexturesSSEC(mGLES20Renderer: OpenGLES20Renderer?) : DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true
        var myUri = "https://www.ssec.wisc.edu/data/comp/ir/"
        var tag = 'I'
        when {
            p0[0] == "IR" -> {
                myUri = "https://www.ssec.wisc.edu/data/comp/ir/"
                tag = 'I'
            }
            p0[0] == "WATER" -> {
                myUri = "https://www.ssec.wisc.edu/data/comp/wv/"
                tag = 'W'
            }
        }
        gles20Renderer!!.mTag = tag
        var is2: InputStream? = null
        val b: Bitmap? = null
        var ucon: URLConnection? = null
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
        var epoch = cal.timeInMillis
        progressDialogSetMax(168 / 3)
        var h = 0
        while (h < 168) {
            if (isCancelled) {
                break
            }
            var exists = false
            epoch -= 3 * 3600 * 1000
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
                if (p0[0] == "IR") {
                    _filename = filename.replace("I", "M")
                } else if (p0[0] == "WATER") {
                    _filename = filename.replace("W", "WV")
                }
                url = URL("$myUri$_filename.gif")
                Log.d("H21lab", "Downloading: $url")
                ucon = url.openConnection()
                ucon.useCaches = false
                ucon.connect()

                //is2 = ucon.getInputStream();
                is2 = BufferedInputStream(ucon.getInputStream())
                val mis2 = ByteArrayOutputStream()
                val data = ByteArray(1024)
                var count: Int
                while (is2.read(data, 0, 1024).also { count = it } != -1) {
                    mis2.write(data, 0, count)
                }
                mis2.flush()
                is2.close()
                val ba = mis2.toByteArray()
                gles20Renderer!!.saveTexture(filename, ba, 1024, 512)
                mis2.close()
            } catch (e: IOException) {
                when {
                    ucon != null -> {
                        Log.e("H21lab", "Unable to connect to " + ucon.url.toString() + " " + e.message)
                    }
                    else -> {
                        Log.e("H21lab", "Unable to connect to " + myUri + " " + e.message)
                    }
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