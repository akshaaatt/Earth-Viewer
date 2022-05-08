package com.aemerse.earth_viewer

import android.graphics.Bitmap
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class DownloadTexturesMTSAT(mGLES20Renderer: OpenGLES20Renderer?) : DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true
        var myUri = "https://www.jma.go.jp/en/gms/imgs/6/infrared/1/"
        var tag = 'J'
        if (p0[0] == "MTSAT") {
            myUri = "https://www.jma.go.jp/en/gms/imgs/6/infrared/1/"
            tag = 'J'
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
        cal[Calendar.HOUR_OF_DAY] = 1 * (cal[Calendar.HOUR_OF_DAY] / 1)
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        val dir = gles20Renderer!!.mContext.filesDir
        var subFiles: Array<File>?
        var epoch: Long
        val current = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).timeInMillis
        val iKeys = HashMap<Int, String>()
        val eKeys = HashMap<Int, Long>()
        var files_to_download = 0
        if (iKeys.size == 0 || eKeys.size == 0) {
            try {
                val uri = "https://www.jma.go.jp/en/gms/hisjs/infrared-6.js"

                //HttpClient httpClient = new DefaultHttpClient();
                //HttpGet get = new HttpGet(uri);

                //HttpResponse response = httpClient.execute(get);
                val urlObj = URL(uri)
                val urlConnection = urlObj.openConnection() as HttpURLConnection
                Log.d("H21lab", "HTTP GET OK")

                // Build up result
                //String bodyHtml = EntityUtils.toString(response.getEntity());
                //InputStream is = urlConnection.getInputStream();
                //String bodyHtml = is.toString();

                //BufferedReader bufReader = new BufferedReader(new StringReader(bodyHtml));
                val bufReader = BufferedReader(InputStreamReader(urlConnection.inputStream))


                // <tr><td valign="top"><img src="/icons/image2.gif" alt="[IMG]"></td><td><a href="1501150545G13I04.tif">1501150545G13I04.tif</a></td><td align="right">15-Jan-2015 01:21  </td><td align="right">550K</td></tr>
                val pattern = Pattern.compile("ImageInfo\\(\\\"(\\d\\S+00-00)\\.png\\\"")

                //  <tr><td valign="top"><img src="/icons/image2.gif" alt="[IMG]"></td><td><a href="1501150545G13I04.tif">1501150545G13I04.tif</a></td><td align="right">15-Jan-2015 01:21  </td><td align="right">550K</td></tr>
                val pattern2 = Pattern.compile("ImageInfo\\(\\\"(\\d\\S+00)-00\\.png\\\"")
                var line: String?
                var i = 0
                var j = 0
                while (bufReader.readLine().also { line = it } != null) {
                    var matcher = pattern.matcher(line)
                    while (matcher.find()) {
                        Log.d("H21lab", "iKeys: " + i + " " + matcher.group(1))

                        // Filename is "201808091900-00.png". Remove suffix -00 before storeing the key.
                        iKeys[i] = matcher.group(1).trim { it <= ' ' }.replace("-00+".toRegex(), "")
                        i++
                    }
                    matcher = pattern.matcher(line)
                    while (matcher.find()) {
                        Log.d("H21lab", "eKeys: " + matcher.group(1))
                        var str = matcher.group(1)
                        str = str.trim { it <= ' ' }.replace("\\t+".toRegex(), " ")
                        str = str.trim { it <= ' ' }.replace("\\s+".toRegex(), " ")
                        str = str.trim { it <= ' ' }.replace("-00+".toRegex(), "")
                        //str += "UTC";
                        val df = SimpleDateFormat("yyyyMMddHHmm")
                        val d = df.parse(str)
                        val e = d.time
                        Log.d("H21lab", "eKeys: $str $e")
                        if (current - e <= (24 + 3) * 3600 * 1000) {
                            files_to_download++
                        }
                        Log.d("H21lab", "eKeys: $j $e")
                        eKeys[j] = e
                        j++
                    }
                }
            } catch (e3: Exception) {
                Log.e("H21lab", "Connection error " + e3.message)
                e3.printStackTrace()
            }
        }
        progressDialogSetMax(files_to_download)

        // Download the older files if possible
        epoch = cal.timeInMillis
        var h = 0
        while (h < eKeys.size) {
            if (isCancelled == true) {
                break
            }
            var exists = false
            Log.d("H21lab", "h = $h")
            if (!eKeys.containsKey(h)) {
                Log.d("H21lab", "Does not conain eKeys h = $h")
                h += 1
                continue
            }
            epoch = eKeys[h]!!


            // do not download too old data
            if (current - epoch > (24 + 3) * 3600 * 1000) {
                Log.d("H21lab", "Data fom eKeys too old h = $h")
                h += 1
                continue
            }
            filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch)
            exists = false
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
                Log.d("H21lab", "File already exists from eKeys h = $h")
                h += 1
                continue
            }
            // change filename
            Log.d("H21lab", "New filename = $filename e = $epoch")


            // download from internet
            try {
                //oiswww.eumetsat.org/IPPS/html/MSG/IMAGERY/IR108/BW/FULLDISC/IMAGESDisplay/
                url = URL(myUri + iKeys[h] + "-00.png")
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
                gles20Renderer!!.saveTexture(filename, ba, 1024, 1024)
                mis2.close()
            } catch (e: Exception) {
                if (ucon != null) {
                    Log.e("H21lab", "Unable to connect to " + ucon.url.toString() + " " + e.message)
                } else {
                    Log.e("H21lab", "Unable to connect to " + myUri + " " + e.message)
                }
            }
            progressDialogUpdate()
            h += 1
        }
        return ""
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}