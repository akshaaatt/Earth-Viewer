package com.aemerse.forfree.earth

import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.io.File
import java.io.InputStream
import java.util.*

open class DownloadTextures(mGLES20Renderer: OpenGLES20Renderer?) : AsyncTask<String?, Void?, String>() {
    private var gles20Renderer: OpenGLES20Renderer? = null
    private var pd: ProgressDialog? = null
    private var pd_progress = 0

    fun progressDialogShow() {
        try {
            if (pd != null && this.status == Status.RUNNING) {
                pd!!.show()
            }
        } catch (e: Exception) {
            Log.e("H21lab", "Unable to create ProgressDialog " + e.message)
        }
    }

    fun progressDialogSetMax(files_to_download: Int) {
        try {
            if (pd != null) {
                pd!!.max = files_to_download
            }
        } catch (e: Exception) {
            Log.e("H21lab", "Unable to create ProgressDialog " + e.message)
        }
    }

    fun progressDialogUpdate() {
        try {
            pd_progress++
            if (pd != null) {
                pd!!.progress = pd_progress
            }
            //reloadTextures();
        } catch (e: Exception) {
            Log.e("H21lab", "Unable to create ProgressDialog " + e.message)
        }
    }

    override fun onPreExecute() {
        pd_progress = 0
        try {
            pd = ProgressDialog(gles20Renderer!!.mContext)
            if (pd != null) {
                pd!!.setMessage("Please wait...")
                pd!!.setCancelable(true)
                pd!!.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { dialog, which ->
                    gles20Renderer!!.mDownloadTextures!!.cancel(true)
                    reloadTextures()
                }
                pd!!.max = 100
                pd!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                pd!!.progress = pd_progress
                pd!!.show()
            }
        } catch (e: Exception) {
            Log.e("H21lab", "Unable to create ProgressDialog " + e.message)
        }
        super.onPreExecute()
    }

    override fun doInBackground(vararg p0: String?): String? {
        return ""
    }

    fun reloadTextures() {
        val subFiles: Array<File>
        val dir = gles20Renderer!!.mContext.filesDir
        var is2: InputStream? = null

        // clean the  storage and select actual cloadmap
        subFiles = dir.listFiles()
        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
        val epoch: Long = cal.timeInMillis
        for (f in gles20Renderer!!.mCloudMapFilename.keys) {
            // reload always after download
            val e = gles20Renderer!!.mCloudMapFilename[f]
            gles20Renderer!!.mCloudMap.remove(e)
            gles20Renderer!!.mCloudMapEpochToFilename.remove(e)
            gles20Renderer!!.mCloudMapFilename.remove(f)
        }
        if (subFiles != null) {
            for (file in subFiles) {
                if (!file.exists()) {
                    continue
                }
                if (gles20Renderer!!.mCloudMapFilename.containsKey(file.name)) {
                    continue
                }
                Log.d(
                    "H21lab",
                    "Files: " + file.name + " " + OpenGLES20Renderer.getEpochFromName(file.name) + " " + file.length()
                )
                if (OpenGLES20Renderer.getTag(file.name) == 'm') {
                    // delete older than 6h
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > (6 + 2) * 3600 * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag m: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else if (OpenGLES20Renderer.getTag(file.name) == 'h') {
                    // delete older than 2h
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > (2 + 1) * 3600 * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag h: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else if (OpenGLES20Renderer.getTag(file.name) == 'I' || OpenGLES20Renderer.getTag(
                        file.name
                    ) == 'W'
                ) {
                    // delete older than 168h
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > (168 + 6) * 3600 * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag I or W: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else if (OpenGLES20Renderer.getTag(file.name) == 'a' || OpenGLES20Renderer.getTag(
                        file.name
                    ) == 'b'
                ) {
                    // delete older than 1 year
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > (1 * (365.25 + 6) * 24 * 3600).toLong() * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag X, Y: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else if (OpenGLES20Renderer.getTag(file.name) == 'O') {
                    // delete older than 35 years
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > ((35 + 1) * 365.25 * 24 * 3600).toLong() * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag O: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else if (OpenGLES20Renderer.getTag(file.name) == 'e') {
                    // delete older than 65 years
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > ((65 + 1) * 365.25 * 24 * 3600).toLong() * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old for tag E: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                } else {
                    if (epoch - OpenGLES20Renderer.getEpochFromName(file.name) > (24 + 6) * 3600 * 1000) {
                        Log.d(
                            "H21lab",
                            "Deleting old: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                                file.name
                            )
                        )
                        file.delete()
                        continue
                    }
                }

                // delete newer files
                if (OpenGLES20Renderer.getEpochFromName(file.name) - epoch > (72 + 24) * 3600 * 1000) {
                    Log.d(
                        "H21lab",
                        "Deleting new: " + file.name + " epoch " + epoch + " file epoch " + OpenGLES20Renderer.getEpochFromName(
                            file.name
                        )
                    )
                    file.delete()
                    continue
                }
                if (OpenGLES20Renderer.getTag(file.name) != gles20Renderer!!.mTag) {
                    continue
                }
                val bi = gles20Renderer!!.loadTexture(file.name)
                if (bi != null) {
                    Log.d(
                        "H21lab",
                        "PUT file into HASH = " + " " + OpenGLES20Renderer.getEpochFromName(file.name) + " " + file.name
                    )
                    val e = OpenGLES20Renderer.getEpochFromName(file.name)
                    gles20Renderer!!.mCloudMap[e] = bi
                    gles20Renderer!!.mCloudMapFilename[file.name] = e
                    gles20Renderer!!.mCloudMapEpochToFilename[e] = file.name
                }
            }
        }
        if (gles20Renderer!!.mBitmap3 == null) {
            is2 = gles20Renderer!!.mContext.resources.openRawResource(R.raw.clouds)
            try {
                gles20Renderer!!.mBitmap3 = BitmapFactory.decodeStream(is2)
            } finally {
                try {
                    is2.close()
                } catch (e: Exception) {
                    // Ignore.
                }
            }
        }
        gles20Renderer!!.initializedShaders = false
        gles20Renderer!!._e1 = 0L
        gles20Renderer!!._e2 = 0L
        gles20Renderer!!._e3 = 0L
        gles20Renderer!!._e4 = 0L
        gles20Renderer!!.downloadedTextures = 1
        gles20Renderer!!.reloadedTextures = true
    }

    override fun onPostExecute(response1: String) {
        reloadTextures()


        //Some Code.....
        try {
            if (pd != null && pd!!.isShowing) pd!!.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        gles20Renderer = mGLES20Renderer
    }
}