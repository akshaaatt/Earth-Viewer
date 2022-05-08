package com.aemerse.earth_viewer

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.*

class DownloadTexturesCCI(mGLES20Renderer: OpenGLES20Renderer?) : DownloadTextures(mGLES20Renderer) {
    private var gles20Renderer: OpenGLES20Renderer? = null

    override fun doInBackground(vararg p0: String?): String {
        gles20Renderer!!.downloadedTextures = 0
        gles20Renderer!!.reloadedTextures = true

        //String myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
        // Format: https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/2018-08-08-00z/00.png
        var myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp/"
        var tag = 'C'
        when {
            p0[0] == "CLOUDS" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PRCP/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PRCP/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/prcp-tcld-topo/"
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/prcp-tcld-topo/";
                tag = 'C'
            }
            p0[0] == "TEMP" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2/"
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/t2/";
                tag = 'T'
            }
            p0[0] == "TEMP_AN" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/t2anom/"
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/t2anom/";
                tag = 't'
            }
            p0[0] == "WATER" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/PWTR/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/PWTR/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/pwtr/"
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/pwtr/";
                tag = 'w'
            }
            p0[0] == "WIND" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS10/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS10/";
                //myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws10/";
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/ws10-mslp/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws10-mslp/"
                tag = 'v'
            }
            p0[0] == "JET" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/WS250/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/WS250/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/ws250-mslp/"
                tag = 'j'
            }
            p0[0] == "SNOW" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
                myUri = "https://climatereanalyzer.org/wx_frames/gfs/world-ced/snowd-mslp/"
                //myUri = "https://pamola.um.maine.edu/wx_frames/gfs/world-ced/snowd-mslp/";
                tag = 's'
            }
            p0[0] == "TEMP_AN_1Y" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
                //myUri = "https://pamola.um.maine.edu/wxrmaps/clim_frames/t2anom/world-ced/";
                myUri =
                    "https://climatereanalyzer.org/reanalysis/daily_maps/clim_frames/t2anom/world-ced/"
                tag = 'a'
            }
            p0[0] == "OISST_V2_1Y" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/T2_anom/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/T2_anom/";
                //myUri = "https://pamola.um.maine.edu/wxrmaps/clim_frames/sstanom/world-ced2/";
                myUri =
                    "https://climatereanalyzer.org/reanalysis/daily_maps/clim_frames/sstanom/world-ced2/"
                tag = 'b'
            }
            p0[0] == "OISST_V2" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
                //myUri = "https://pamola.um.maine.edu/cr/clim/sst/frames/oisst2/world-ced2/sstanom/";
                myUri = "https://climatereanalyzer.org/clim/sst/frames/oisst2/world-ced2/sstanom/"
                tag = 'O'
            }
            p0[0] == "ERSST_V5" -> {
                //myUri = "http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/SNOW/";
                //myUri = "http://traveler.um.maine.edu/fcst_frames/GFS-025deg/WORLD-CED/SNOW/";
                //myUri = "https://pamola.um.maine.edu/cr/clim/sst/frames/ersst5/world-ced2/sstanom/";
                myUri = "https://climatereanalyzer.org/clim/sst/frames/ersst5/world-ced2/sstanom/"
                tag = 'e'
            }
        }
        gles20Renderer!!.mTag = tag
        var is2: InputStream? = null
        var ucon: URLConnection? = null
        var url: URL? = null

        // load image from cache

        // find latest image but not older then 3 hours
        var filename: String? = null
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        val dir = gles20Renderer!!.mContext.filesDir
        var subFiles: Array<File>?
        var epoch: Long
        val current_real = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).timeInMillis
        var data_generated_epoch: Long
        val iKeys = HashMap<Int, String>()
        val eKeys = HashMap<Int, Long>()
        val mKeys = HashMap<Int, Long>()
        var files_to_download: Int

        // last 35 years
        if (tag == 'O') {
            files_to_download = 35
        }
        // last 65 years
        files_to_download = when (tag) {
            'e' -> {
                65
            }
            'a', 'b' -> {
                365 / 2 - 15
            }
            else -> {
                48 / 3
            }
        }
        progressDialogSetMax(files_to_download)


        // Download the older files if possible
        epoch = cal.timeInMillis

        // assume that if is less than 9:00 am UTC, the images are old from previous day
        var reload = 0L
        var c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))

        // last 35 years or last 65 years
        if (tag == 'O' || tag == 'e') {
            c[Calendar.HOUR_OF_DAY] = 9
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 0
            c[Calendar.MILLISECOND] = 0

            // lets day, that can be safely iterated
            c[Calendar.DAY_OF_YEAR] = 100
            reload = c.timeInMillis
        } else if (tag == 'a' || tag == 'b') {
            c[Calendar.HOUR_OF_DAY] = 9
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 0
            c[Calendar.MILLISECOND] = 0
            reload = c.timeInMillis
        } else {
            if (c[Calendar.HOUR_OF_DAY] < 9) {
                epoch -= (24 * 3600 * 1000).toLong()
                c[Calendar.HOUR_OF_DAY] = 9
                c[Calendar.MINUTE] = 0
                c[Calendar.SECOND] = 0
                c[Calendar.MILLISECOND] = 0
                reload = c.timeInMillis
                reload -= (24 * 3600 * 1000).toLong()
            } else {
                c[Calendar.HOUR_OF_DAY] = 9
                c[Calendar.MINUTE] = 0
                c[Calendar.SECOND] = 0
                c[Calendar.MILLISECOND] = 0
                reload = c.timeInMillis
            }
        }

        // Historical data
        if (tag == 'O' || tag == 'e' || tag == 'a' || tag == 'b') {
            epoch = c.timeInMillis
            data_generated_epoch = current_real
        } else {
            // Download the older files if possible
            epoch = cal.timeInMillis

            // if is less than 8:00 am UTC, assume the images are old from previous day
            c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            data_generated_epoch = current_real
            if (c[Calendar.HOUR_OF_DAY] < 8) {
                epoch -= (24 * 3600 * 1000).toLong()
                data_generated_epoch -= (24 * 3600 * 1000).toLong()
            }
        }

        // last 35 years
        if (tag == 'O') {
            // start loading from beginning and not backward
            epoch -= 35 * (365.25 * 24 * 3600 * 1000).toLong()
        } else if (tag == 'e') {
            // start loading from beginning and not backward
            epoch -= 65 * (365.25 * 24 * 3600 * 1000).toLong()
        } else if (tag == 'a' || tag == 'b') {
            // start loading from beginning and not backward
            epoch -= (0.5 * 365.25 * 24 * 3600 * 1000).toLong()
        }
        var h = 0
        while (h < files_to_download) {
            if (isCancelled) {
                break
            }
            var exists = false

            // last 35 years or last 65 years
            epoch = if (tag == 'O' || tag == 'e') {
                // each 1 year
                epoch + (365.25 * 24 * 3600 * 1000).toLong()
            } else if (tag == 'a' || tag == 'b') {
                // each 1 day
                epoch + 24 * 3600 * 1000
            } else {
                // each 3h
                epoch + 3 * 3600 * 1000
            }
            Log.d("H21lab", "h = $h")


            // last 35 years
            if (tag == 'O') {
                // do not download too new data
                if (epoch - current_real > (-1).toLong() * 365.25 * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys too new h = $h")
                    h += 1
                    continue
                }
                // do not download old data
                if (epoch - current_real < (-35).toLong() * 365.25 * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys old h = $h")
                    files_to_download++
                    h += 1
                    continue
                }
            } else if (tag == 'e') {
                // do not download too new data
                if (epoch - current_real > (-1).toLong() * 365.25 * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys too new h = $h")
                    h += 1
                    continue
                }
                // do not download old data
                if (epoch - current_real < (-65).toLong() * 365.25 * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys old h = $h")
                    files_to_download++
                    h += 1
                    continue
                }
            } else if (tag == 'a' || tag == 'b') {
                // do not download too new data
                if (epoch - current_real > (-15).toLong() * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys too new h = $h")
                    h += 1
                    continue
                }
                // do not download old data
                if (epoch - current_real < (-(0.5 * 365.25)).toLong() * 24 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys old h = $h")
                    files_to_download++
                    h += 1
                    continue
                }
            } else {
                // do not download too new data
                if (epoch - current_real > 48 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys too new h = $h")
                    h += 1
                    continue
                }
                // do not download old data
                if (epoch - current_real < -3 * 3600 * 1000) {
                    Log.d("H21lab", "Data fom eKeys old h = $h")
                    files_to_download++
                    h += 1
                    continue
                }
            }
            filename = OpenGLES20Renderer.getNameFromEpoch(tag, epoch)
            exists = false
            subFiles = dir.listFiles()
            if (subFiles != null) {
                for (file in subFiles) {
                    // last 35 years or last 65 years
                    if (tag == 'O' || tag == 'e') {
                        // file exist
                        if (filename == file.name) {
                            exists = true
                            break
                        }
                    } else if (tag == 'a' || tag == 'b') {
                        // file exist
                        if (filename == file.name) {
                            exists = true
                            break
                        }
                    } else {
                        // file exist and is not newer file
                        if (filename == file.name && file.lastModified() - reload > 0) {
                            exists = true
                            break
                        }
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


                // last 35 years
                url = when (tag) {
                    'O' -> {
                        URL(
                            myUri + "/"
                                    + getOISSTV2NameFromEpoch(epoch) + ".png"
                        )
                    }
                    'e' -> {
                        URL(
                            myUri + "/"
                                    + getERSSTV5NameFromEpoch(epoch) + ".png"
                        )
                    }
                    'a' -> {
                        URL(
                            myUri + "/"
                                    + getTEMP_AN_1Y_NameFromEpoch(epoch) + ".png"
                        )
                    }
                    'b' -> {
                        URL(
                            myUri + "/"
                                    + getOISSTV2_1Y_NameFromEpoch(epoch) + ".png"
                        )
                    }
                    else -> {
                        URL(
                            myUri
                                    + getDirectoryNameFromEpoch(data_generated_epoch) + "/"
                                    + String.format("%02d", h) + ".png"
                        )
                    }
                }
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

                // delete previous files
                subFiles = dir.listFiles()
                if (subFiles != null) {
                    for (file in subFiles) {
                        if (file.name.startsWith(filename)) {
                            gles20Renderer!!.mContext.deleteFile(file.name)
                        }
                    }
                }

                //save texture
                gles20Renderer!!.saveTexture(filename, ba, 1024, 1024)
                mis2.close()
            } catch (e1: MalformedURLException) {
                Log.e("H21lab", "Connection error " + "MalformedURLException " + e1.message)
            } catch (e2: Exception) {
                if (ucon != null) {
                    Log.e(
                        "H21lab",
                        "Unable to connect to " + ucon.url.toString() + " " + e2.message
                    )
                } else {
                    Log.e("H21lab", "Unable to connect to " + myUri + " " + e2.message)
                }
            }
            progressDialogUpdate()
            h += 1
        }
        return ""
    }

    companion object {
        // 2018-08-07-00z
        fun getDirectoryNameFromEpoch(epoch: Long): String {
            val c =
                Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val year =
                String.format("%04d", c[Calendar.YEAR])
            val month =
                String.format("%02d", (c[Calendar.MONTH] + 1))
            val day =
                String.format("%02d", c[Calendar.DAY_OF_MONTH])
            return c[Calendar.YEAR].toString() + "-" + month + "-" + day + "-00z"
        }

        // 2019/t2anom_world-ced_2019_d151.png
        fun getTEMP_AN_1Y_NameFromEpoch(epoch: Long): String {
            val c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val year = String.format("%04d", c[Calendar.YEAR])
            val day = String.format("%03d", c[Calendar.DAY_OF_YEAR])
            return year + "/" + "t2anom_world-ced_" + year + "_d" + day
        }

        // 2019/sstanom_world-ced2_2019_d152.png
        fun getOISSTV2_1Y_NameFromEpoch(epoch: Long): String {
            val c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val year = String.format("%04d", c[Calendar.YEAR])
            val day = String.format("%03d", c[Calendar.DAY_OF_YEAR])
            return year + "/" + "sstanom_world-ced2_" + year + "_d" + day
        }

        // oisst2_world-ced2_sstanom_2019-03.png
        fun getOISSTV2NameFromEpoch(epoch: Long): String {
            val c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val year = String.format("%04d", c[Calendar.YEAR])
            //String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
            //String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));
            return "oisst2_world-ced2_sstanom_" + c[Calendar.YEAR].toString() + "-" + "13"
        }

        // ersst5_world-ced2_sstanom_1886-13.png
        fun getERSSTV5NameFromEpoch(epoch: Long): String {
            val c = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val year = String.format("%04d", c[Calendar.YEAR])
            //String month = String.format("%02d", (int)(c.get(Calendar.MONTH) + 1));
            //String day = String.format("%02d", (int)(c.get(Calendar.DAY_OF_MONTH)));
            return "ersst5_world-ced2_sstanom_" + c[Calendar.YEAR].toString() + "-" + "13"
        }
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}