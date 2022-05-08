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

class DownloadTexturesEumetsat(mGLES20Renderer: OpenGLES20Renderer?) : DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true
        var tWidth = 1024
        var tHeight = 1024
        var hBack = 24
        var myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLDISC"
        var tag = 'M'
        when {
            p0[0] == "AIRMASS" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLDISC"
                tag = 'M'
            }
            p0[0] == "AIRMASS_HD" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/AIRMASS/FULLRESOLUTION"
                tag = 'm'
                tWidth = 4096
                tHeight = 4096
                hBack = 3
            }
            p0[0] == "NATURALCOLOR" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/RGB/NATURALCOLOR/FULLDISC"
                tag = 'N'
            }
            p0[0] == "IR108_BW" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/IR108/BW/FULLDISC"
                tag = 'B'
            }
            p0[0] == "VIS006_BW" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/VIS006/BW/FULLDISC"
                tag = 'C'
            }
            p0[0] == "WV062_BW" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/IMAGERY/WV062/BW/FULLDISC"
                tag = 'D'
            }
            p0[0] == "MPE" -> {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/PRODUCTS/H03B/FULLDISC"
                tag = 'E'
            } /* else if (urls[0].equals("MPE_HD")) {
                myUri = "https://eumetview.eumetsat.int/static-images/MSG/PRODUCTS/MPE/FULLRESOLUTION";
                tag = 'e';
                tWidth = 4096;
                tHeight = 4096;
                hBack = -1;
            }*/
            p0[0] == "IODC" -> {
                //myUri = "http://oiswww.eumetsat.org/IPPS/html/MTP/PRODUCTS/MPE/FULLDISC";
                myUri =
                    "https://eumetview.eumetsat.int/static-images/MSGIODC/IMAGERY/IR108/BW/FULLDISC/"
                tag = 'F'
            }
        }
        gles20Renderer!!.mTag = tag
        var is2: BufferedInputStream? = null
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

                //HttpClient httpClient = new DefaultHttpClient();
                //HttpGet get = new HttpGet(myUri + "/index.htm");

                //HttpResponse response = httpClient.execute(get);
                val urlObj = URL("$myUri/index.htm")
                val urlConnection = urlObj.openConnection() as HttpURLConnection
                Log.d("H21lab", "HTTP GET OK")

                // Build up result
                //String bodyHtml = EntityUtils.toString(response.getEntity());
                //InputStream is = urlConnection.getInputStream();
                //String bodyHtml = is.toString();

                //BufferedReader bufReader = new BufferedReader(new StringReader(bodyHtml));
                val bufReader = BufferedReader(InputStreamReader(urlConnection.inputStream))

                // array_nom_imagen[0]="PTwhQddyHWGUL"
                val pattern = Pattern.compile("array_nom_imagen\\[(\\d+)\\]\\s*=\\s*\\\"(\\S+)\\\"")

                //  <option value="0">13/01/15   11:00 UTC</option>
                val pattern2 =
                    Pattern.compile("\\<option value=\\\"(\\d+)\\\"\\>(.*)\\<\\/option\\>")
                var line: String?
                while (bufReader.readLine().also { line = it } != null) {
                    var matcher = pattern.matcher(line)
                    while (matcher.find()) {
                        Log.d(
                            "H21lab",
                            "iKeys: " + "array_nom_imagen[" + matcher.group(1) + "] = " + matcher.group(
                                2
                            )
                        )
                        iKeys[matcher.group(1).toInt()] = matcher.group(2)
                    }
                    matcher = pattern2.matcher(line)
                    while (matcher.find()) {
                        Log.d("H21lab", "eKeys: " + matcher.group(1) + " " + matcher.group(2))
                        var str = matcher.group(2)
                        str = str.trim { it <= ' ' }.replace("\\t+".toRegex(), " ")
                        str = str.trim { it <= ' ' }.replace("\\s+".toRegex(), " ")
                        val df = SimpleDateFormat("dd/MM/yy HH:mm zzz")
                        val d = df.parse(str)
                        val e = d.time
                        if (current - e <= (hBack + 3) * 3600 * 1000) {
                            files_to_download++
                        }
                        eKeys[matcher.group(1).toInt()] = e
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
                progressDialogUpdate()
                Log.d("H21lab", "Does not conain eKeys h = $h")
                h += 1
                continue
            }
            epoch = eKeys[h]!!


            // do not download too old data
            if (current - epoch > (hBack + 3) * 3600 * 1000) {
                progressDialogUpdate()
                Log.d("H21lab", "Data from eKeys too old h = $h")
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
                Log.e("H21lab", "!!!!!!!!!!!!!!!!!!!!")

                //oiswww.eumetsat.org/IPPS/html/MSG/IMAGERY/IR108/BW/FULLDISC/IMAGESDisplay/
                url = URL(myUri + "/IMAGESDisplay/" + iKeys[h])
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
                    Log.d("H21lab", Integer.toString(data.toString().length))
                    mis2.write(data, 0, count)
                }
                mis2.flush()
                is2.close()
                val ba = mis2.toByteArray()
                gles20Renderer!!.saveTexture(filename, ba, tWidth, tHeight)
                mis2.close()
            } catch (e: Exception) {
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
            h += 1
        }
        return ""
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}