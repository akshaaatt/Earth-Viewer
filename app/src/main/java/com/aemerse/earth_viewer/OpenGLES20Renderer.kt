package com.aemerse.earth_viewer

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.Matrix
import android.opengl.*
import android.opengl.ETC1Util.ETC1Texture
import android.util.Log
import com.aemerse.earth_viewer.M3DM.*
import com.aemerse.earth_viewer.M3DMATRIX.Companion.IdentityMatrix
import com.aemerse.earth_viewer.M3DMATRIX.Companion.VxM
import com.aemerse.earth_viewer.M3DVECTOR.Companion.POINTROTATE
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLES20Renderer(var mContext: Context) : GLSurfaceView.Renderer {
    var _t1: Long = 0
    var _t2 // used to calculate FPS
            : Long = 0
    var FPS = 30.0f
    var Tc = 1.0f / FPS // duration of 1 frame
    var Nrenderedframe = 0
    @JvmField
	var DEV: M3DM? = null
    var scene: mD3DFrame? = null
    var fearth: mD3DFrame? = null
    var earth: mD3DMesh? = null
    var tcube: mD3DTexture? = null
    var mRotation: M3DMATRIX? = null
    var mBump = 10
    @JvmField
	var mLiveLight = false
    @JvmField
	var mLightSpecular = true
    @JvmField
	var mPlay = false
    var At = M3DVECTOR(0.0f, 0.0f, 0.0f) // accelleration vector
    var Gt = M3DVECTOR(0.0f, 0.0f, 0.0f) // vertical gravity vector m/(s*s)
    var O = M3DVECTOR(0.0f, 0.0f, -1.0f)
    var U = M3DVECTOR(0.0f, 1.0f, 0.0f)
    var reloadedTextures = false
    var downloadedTextures = 0
    var preferencesChanged = false
    @JvmField
	var mDownloadTextures: DownloadTextures? = null
    var initializedShaders = false
    var initialized = false
    var mTag = 'X'
    var openGLErrorDetected = 0
    var mBitmap1: Bitmap? = null
    var mBitmap2: Bitmap? = null
    var mBitmap3: Bitmap? = null
    var mBitmap4: Bitmap? = null
    var mCloudMap = ConcurrentHashMap<Long, ETC1Texture>()
    var mCloudMapFilename = ConcurrentHashMap<String, Long>()
    var mCloudMapEpochToFilename = ConcurrentHashMap<Long, String>()
    var mCloudMapId = ConcurrentHashMap<Long, Int>()
    var mCloudMapIdFilename = ConcurrentHashMap<Int?, String?>()
    var texture: mD3DTexture? = null
    var texture1: mD3DTexture? = null
    var texture2: mD3DTexture? = null
    var texture3: mD3DTexture? = null
    var texture4: mD3DTexture? = null
    var texture5: mD3DTexture? = null
    var texture6: mD3DTexture? = null
    @JvmField
	var mTimeRotate: Long = 0
    @JvmField
	var mEpoch = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")).timeInMillis
    @JvmField
	var _e1 = 0L // nearest in past
    @JvmField
	var _e2 = 0L // second nearest in past
    @JvmField
	var _e3 = 0L // nearest in future
    @JvmField
	var _e4 = 0L // second nearest in future
    var texSizeW = 1024
    var texSizeH = 1024
    var mWidth = 0
    var mHeight = 0
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {

        // reset textures
        _e1 = 0L // nearest in past
        _e2 = 0L // second nearest in past
        _e3 = 0L // nearest in future
        _e4 = 0L // second nearest in future
        initializedShaders = false
        reloadedTextures = true
        _t2 = System.nanoTime()
        if (DEV == null) {
            initialize()
        }
        DEV!!.initializeGL()

        // textueres has been cleared by OS
        texture = null
        texture1 = null
        texture2 = null
        texture3 = null
        texture4 = null
        texture5 = null
        texture6 = null
        mCloudMapId.clear()
        mCloudMapIdFilename.clear()
        initializeShaders()
        earth!!.program = Shaders.p_cci
        earth!!.setBumpLevel(mBump.toFloat())
        initializeGLTextures()

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Log.d("H21lab", "GL_VERSION = " + GLES20.glGetString(GLES20.GL_VERSION))
        Log.d("H21lab", "GL_EXTENSIONS = " + GLES20.glGetString(GLES20.GL_EXTENSIONS))
        Log.d("H21lab", "GL_RENDERER = " + GLES20.glGetString(GLES20.GL_RENDERER))
    }

    fun setShaders() {
        if (DEV != null) {
            when (mTag) {
                'X' -> {
                    earth!!.program = Shaders.p_xplanet
                }
                'R' -> {
                    earth!!.program = Shaders.p_nrl_rainrate
                }
                'I' -> {
                    earth!!.program = Shaders.p_ssec_ir
                }
                'W' -> {
                    earth!!.program = Shaders.p_ssec_water
                }
                'F' -> {
                    earth!!.program = Shaders.p_meteosat_iodc
                }
                'G' -> {
                    earth!!.program = Shaders.p_goes_east
                }
                'H' -> {
                    earth!!.program = Shaders.p_goes_west
                }
                'J' -> {
                    earth!!.program = Shaders.p_mtsat
                }
                'C' -> {
                    earth!!.program = Shaders.p_cci
                }
                'T' -> {
                    earth!!.program = Shaders.p_cci_temp
                }
                't' -> {
                    earth!!.program = Shaders.p_cci_temp_an
                }
                'w' -> {
                    earth!!.program = Shaders.p_cci_water
                }
                'v' -> {
                    earth!!.program = Shaders.p_cci_wind
                }
                'j' -> {
                    earth!!.program = Shaders.p_cci_jet
                }
                's' -> {
                    earth!!.program = Shaders.p_cci_snow
                }
                'a' -> {
                    earth!!.program = Shaders.p_cci_temp_an_1y
                }
                'b' -> {
                    earth!!.program = Shaders.p_cci_oisst_v2
                }
                'O' -> {
                    earth!!.program = Shaders.p_cci_oisst_v2
                }
                'e' -> {
                    earth!!.program = Shaders.p_cci_oisst_v2
                }
                'm' -> {
                    earth!!.program = Shaders.p_meteosat_0_hd
                }
                else -> {
                    earth!!.program = Shaders.p_meteosat_0
                }
            }
            val p = earth!!.program
            earth!!.flushProgram()
            if (p != -1 && p != earth!!.newProgram) {
                DEV!!.DeleteProgram(p)
            }
        }
    }

    fun initializeShaders() {
        if (DEV != null) {

            // initialize Shaders
            when (mTag) {
                'X' -> {
                    Shaders.p_xplanet = DEV!!.CompileProgram(Shaders.vsc_xplanet, Shaders.fsc_xplanet)
                }
                'R' -> {
                    Shaders.p_nrl_rainrate =
                        DEV!!.CompileProgram(Shaders.vsc_nrl_rainrate, Shaders.fsc_nrl_rainrate)
                }
                'I' -> {
                    Shaders.p_ssec_ir = DEV!!.CompileProgram(Shaders.vsc_ssec_ir, Shaders.fsc_ssec_ir)
                }
                'W' -> {
                    Shaders.p_ssec_water =
                        DEV!!.CompileProgram(Shaders.vsc_ssec_water, Shaders.fsc_ssec_water)
                }
                'F' -> {
                    Shaders.p_meteosat_iodc =
                        DEV!!.CompileProgram(Shaders.vsc_meteosat_iodc, Shaders.fsc_meteosat_iodc)
                }
                'G' -> {
                    Shaders.p_goes_east =
                        DEV!!.CompileProgram(Shaders.vsc_goes_east, Shaders.fsc_goes_east)
                }
                'H' -> {
                    Shaders.p_goes_west =
                        DEV!!.CompileProgram(Shaders.vsc_goes_west, Shaders.fsc_goes_east)
                }
                'J' -> {
                    Shaders.p_mtsat = DEV!!.CompileProgram(Shaders.vsc_mtsat, Shaders.fsc_mtsat)
                }
                'C' -> {
                    Shaders.p_cci = DEV!!.CompileProgram(Shaders.vsc_cci, Shaders.fsc_cci)
                }
                'T' -> {
                    Shaders.p_cci_temp =
                        DEV!!.CompileProgram(Shaders.vsc_cci_temp, Shaders.fsc_cci_temp)
                }
                't' -> {
                    Shaders.p_cci_temp_an =
                        DEV!!.CompileProgram(Shaders.vsc_cci_temp_an, Shaders.fsc_cci_temp_an)
                }
                'w' -> {
                    Shaders.p_cci_water =
                        DEV!!.CompileProgram(Shaders.vsc_cci_water, Shaders.fsc_cci_temp)
                }
                'v' -> {
                    Shaders.p_cci_wind =
                        DEV!!.CompileProgram(Shaders.vsc_cci_wind, Shaders.fsc_cci_temp)
                }
                'j' -> {
                    Shaders.p_cci_jet = DEV!!.CompileProgram(Shaders.vsc_cci_jet, Shaders.fsc_cci_temp)
                }
                's' -> {
                    Shaders.p_cci_snow =
                        DEV!!.CompileProgram(Shaders.vsc_cci_wind, Shaders.fsc_cci_temp)
                }
                'a' -> {
                    Shaders.p_cci_temp_an_1y =
                        DEV!!.CompileProgram(Shaders.vsc_cci_temp_an_1y, Shaders.fsc_cci_temp_an_1y)
                }
                'b' -> {
                    Shaders.p_cci_oisst_v2 =
                        DEV!!.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2)
                }
                'O' -> {
                    Shaders.p_cci_oisst_v2 =
                        DEV!!.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2)
                }
                'e' -> {
                    Shaders.p_cci_oisst_v2 =
                        DEV!!.CompileProgram(Shaders.vsc_cci_oisst_v2, Shaders.fsc_cci_oisst_v2)
                }
                'm' -> {
                    Shaders.p_meteosat_0_hd =
                        DEV!!.CompileProgram(Shaders.vsc_meteosat_0_hd, Shaders.fsc_meteosat_0_hd)
                }
                else -> {
                    Shaders.p_meteosat_0 =
                        DEV!!.CompileProgram(Shaders.vsc_meteosat_0, Shaders.fsc_meteosat_0)
                }
            }
            GLES20.glFlush()
            setShaders()
            initializedShaders = true
        }
    }

    fun initializeGLTextures() {
        if (downloadedTextures == 0) {
            return
        }

        // Texture
        if (texture1 != null) {
            val textures1 = IntArray(1)
            textures1[0] = texture1!!.id
            GLES20.glDeleteTextures(1, textures1, 0)
            GLES20.glFlush()
        }
        val textures1 = IntArray(1)
        GLES20.glGenTextures(1, textures1, 0)
        texture1 = mD3DTexture()
        texture1!!.id = textures1[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1!!.id)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap2, 0)
        //


        // Texture
        if (texture2 != null) {
            val textures2 = IntArray(1)
            textures2[0] = texture2!!.id
            GLES20.glDeleteTextures(1, textures2, 0)
            GLES20.glFlush()
        }
        val textures2 = IntArray(1)
        GLES20.glGenTextures(1, textures2, 0)
        texture2 = mD3DTexture()
        texture2!!.id = textures2[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2!!.id)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap3, 0)
        //


        // Texture
        if (texture3 != null) {
            val textures3 = IntArray(1)
            textures3[0] = texture3!!.id
            GLES20.glDeleteTextures(1, textures3, 0)
            GLES20.glFlush()
        }
        val textures3 = IntArray(1)
        GLES20.glGenTextures(1, textures3, 0)
        texture3 = mD3DTexture()
        texture3!!.id = textures3[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture3!!.id)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap4, 0)

        // Texture
        if (texture != null) {
            val textures = IntArray(1)
            textures[0] = texture!!.id
            GLES20.glDeleteTextures(1, textures, 0)
            GLES20.glFlush()
        }
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        texture = mD3DTexture()
        texture!!.id = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture!!.id)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap1, 0)
        //
        earth!!.Textures = 7
        earth!!.setTexture(0, texture)
        earth!!.setTexture(1, texture1)
        earth!!.setTexture(2, texture2)
        earth!!.setTexture(3, texture3)
        earth!!.setTexture(4, texture2)
        earth!!.setTexture(5, texture2)
        earth!!.setTexture(6, texture2)


        // delete textures
        for (e in mCloudMapId.keys) {
            if (mCloudMapIdFilename.containsKey(mCloudMapId[e])) {
                if (getTag(mCloudMapIdFilename[mCloudMapId[e]]) != mTag) {
                    val t = IntArray(1)
                    t[0] = mCloudMapId[e]!!
                    GLES20.glDeleteTextures(1, t, 0)
                    mCloudMapId.remove(e)
                    mCloudMapIdFilename.remove(e as Int)
                }
            }
        }
        GLES20.glFlush()


        // load textures
        for (e in mCloudMap.keys) {
            if (mCloudMapId.contains(e)) {
                continue
            }
            val id = IntArray(1)
            GLES20.glGenTextures(1, id, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            val etc1tex = mCloudMap[e]
            if (etc1tex != null) {
                GLES20.glCompressedTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    ETC1.ETC1_RGB8_OES,
                    etc1tex.width,
                    etc1tex.height,
                    0,
                    etc1tex.data.capacity(),
                    etc1tex.data
                )
                Log.d("H21lab", "e = " + e + " " + id[0] + " " + mCloudMap[e])
                mCloudMapId[e] = id[0]
                if (mCloudMapEpochToFilename.containsKey(e)) {
                    mCloudMapIdFilename[id[0]] = mCloudMapEpochToFilename[e]!!
                } else {
                    Log.e("H21lab", "No hash key for mCloudMapEpochToFilename e = $e")
                }
            }
        }

        // find nearest epoch in past
        var e1 = 0L
        for (e in mCloudMapId.keys) {
            if (e < mEpoch && Math.abs(mEpoch - e) < Math.abs(mEpoch - e1)) {
                e1 = e
            }
        }
        if (mCloudMapId.containsKey(e1)) {
            texture = mD3DTexture()
            texture!!.id = mCloudMapId[e1]!!
            earth!!.setTexture(2, texture)
            earth!!.setTexture(4, texture)
            earth!!.setTexture(5, texture)
            earth!!.setTexture(6, texture)
        } else {
            // find nearest epoch in future
            var e3 = 0L
            for (e in mCloudMapId.keys) {
                if (e >= mEpoch && Math.abs(mEpoch - e) <= Math.abs(mEpoch - e3)) {
                    e3 = e
                }
            }
            if (mCloudMapId.containsKey(e3)) {
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e3]!!
                earth!!.setTexture(2, texture)
                earth!!.setTexture(4, texture)
                earth!!.setTexture(5, texture)
                earth!!.setTexture(6, texture)
            }
        }
        reloadedTextures = true
        downloadedTextures = 2
    }

    fun saveImage(filename: String, data: ByteArray) {
        var b = BitmapFactory.decodeByteArray(data, 0, data.size)
        b = scaleBitmap(b, texSizeW, texSizeH)

        // save file
        val outputStream: FileOutputStream
        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE)
            b.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("H21lab", "Saved Image: $filename")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveTexture(filename: String, data: ByteArray, w: Int, h: Int) {
        var b = BitmapFactory.decodeByteArray(data, 0, data.size) ?: return
        b = scaleBitmap(b, w, h)
        b = convert(b, Bitmap.Config.RGB_565)
        val size = b.rowBytes * b.height
        val inputImage = ByteBuffer.allocateDirect(size) // size is good
        inputImage.order(ByteOrder.nativeOrder())
        b.copyPixelsToBuffer(inputImage)
        inputImage.position(0)
        val encodedImageSize = ETC1.getEncodedDataSize(b.width, b.height)
        val compressedImage =
            ByteBuffer.allocateDirect(encodedImageSize).order(ByteOrder.nativeOrder())
        ETC1.encodeImage(inputImage, b.width, b.height, 2, 2 * b.width, compressedImage)
        val etc1tex = ETC1Texture(b.width, b.height, compressedImage)


        // save file
        val outputStream: FileOutputStream
        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE)
            ETC1Util.writeTexture(etc1tex, outputStream)
            outputStream.flush()
            outputStream.close()
            Log.d("H21lab", "Saved Texture: " + filename + " size: " + compressedImage.capacity())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImage(filename: String?): Bitmap? {
        if (filename == null) {
            return null
        }
        val inputStream: FileInputStream
        var b: Bitmap? = null
        try {
            inputStream = mContext.openFileInput(filename)
            b = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            Log.d("H21lab", "Loaded Image: $filename")
        } catch (e0: Exception) {
            e0.printStackTrace()
        }
        b = scaleBitmap(b, texSizeW, texSizeH)
        b = convert(b, Bitmap.Config.RGB_565)
        return b
    }

    fun loadTexture(filename: String?): ETC1Texture? {
        var etc1tex: ETC1Texture? = null
        if (filename == null) {
            return null
        }
        val inputStream: FileInputStream
        val b: Bitmap? = null
        try {
            inputStream = mContext.openFileInput(filename)
            etc1tex = ETC1Util.createTexture(inputStream)
            inputStream.close()
        } catch (e0: Exception) {
            e0.printStackTrace()
        }
        return etc1tex
    }

    fun initialize() {
        // -------------------------------------------
        DEV = M3DM()

        // create light
        DEV!!.N_Lights = 1

        // static light
        DEV!!.Light[1]!!.AR = 0.0f
        DEV!!.Light[1]!!.AG = 0.0f
        DEV!!.Light[1]!!.AB = 0.0f
        DEV!!.Light[1]!!.DR = 1.0f
        DEV!!.Light[1]!!.DG = 1.0f
        DEV!!.Light[1]!!.DB = 1.0f
        DEV!!.Light[1]!!.SR = 1.0f
        DEV!!.Light[1]!!.SG = 0.7f
        DEV!!.Light[1]!!.SB = 0.4f
        DEV!!.Light[1]!!.AT = 0.0f
        DEV!!.Light[1]!!.Pos = M3DVECTOR(1.5f, 0.7f, 3.0f)

        // live light
        DEV!!.Light[2]!!.AR = 0.0f
        DEV!!.Light[2]!!.AG = 0.0f
        DEV!!.Light[2]!!.AB = 0.0f
        DEV!!.Light[2]!!.DR = 1.0f
        DEV!!.Light[2]!!.DG = 1.0f
        DEV!!.Light[2]!!.DB = 1.0f
        DEV!!.Light[2]!!.SR = 1.0f
        DEV!!.Light[2]!!.SG = 0.7f
        DEV!!.Light[2]!!.SB = 0.4f
        DEV!!.Light[2]!!.AT = 0.0f
        DEV!!.Light[2]!!.Pos = M3DVECTOR(1.5f, 0.7f, 3.0f)
        DEV!!.Light[0]!!.AR = DEV!!.Light[1]!!.AR
        DEV!!.Light[0]!!.AG = DEV!!.Light[1]!!.AG
        DEV!!.Light[0]!!.AB = DEV!!.Light[1]!!.AB
        DEV!!.Light[0]!!.DR = DEV!!.Light[1]!!.DR
        DEV!!.Light[0]!!.DG = DEV!!.Light[1]!!.DG
        DEV!!.Light[0]!!.DB = DEV!!.Light[1]!!.DB
        DEV!!.Light[0]!!.SR = DEV!!.Light[1]!!.SR
        DEV!!.Light[0]!!.SG = DEV!!.Light[1]!!.SG
        DEV!!.Light[0]!!.SB = DEV!!.Light[1]!!.SB
        DEV!!.Light[0]!!.AT = DEV!!.Light[1]!!.AT
        DEV!!.Light[0]!!.Pos = M3DVECTOR(DEV!!.Light[1]!!.Pos)

        // create scene
        scene = mD3DFrame()

        // create object
        earth = M3DM.createEllipsoid(1.0f, 0.996f, 3, 6, 0.0f, 0.0f, 1.0f, 1.0f)
        earth!!.generateTangentsBitangets()
        earth!!.Textures = 0
        val material = M3DMATERIAL(
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            0.0f,
            0.0f,
            0.0f,
            1.0f,
            3.0f,
            0.0f,
            1.0f,
            1.0f
        )
        earth!!.setMaterial(material)
        fearth = mD3DFrame(scene!!)
        fearth!!.Position = M3DVECTOR(0.0f, 0.0f, 0.0f)
        fearth!!.Up = U
        fearth!!.Orientation = O
        fearth!!.addMesh(earth)
        mRotation = IdentityMatrix()
        // -------------------------------------------
        val `is` = mContext.resources.openRawResource(R.raw.normalmap)
        mBitmap1 = try {
            BitmapFactory.decodeStream(`is`)
        } finally {
            try {
                `is`.close()
            } catch (e: Exception) {
                // Ignore.
            }
        }
        val is1 = mContext.resources.openRawResource(R.raw.texture)
        mBitmap2 = try {
            BitmapFactory.decodeStream(is1)
        } finally {
            try {
                is1.close()
            } catch (e: Exception) {
                // Ignore.
            }
        }
        val is2 = mContext.resources.openRawResource(R.raw.earth_lights)
        mBitmap4 = try {
            BitmapFactory.decodeStream(is2)
        } finally {
            try {
                is2.close()
            } catch (e: Exception) {
                // Ignore.
            }
        }
        if (!initialized) {
            mDownloadTextures = DownloadTexturesXplanet(this)
            mDownloadTextures!!.cancel(true)
            mDownloadTextures!!.reloadTextures()
            initialized = true
        } else {
            mDownloadTextures!!.reloadTextures()
        }
    }

    override fun onDrawFrame(unused: GL10) {
        if (!initializedShaders) {
            initializeShaders()
        }
        if (downloadedTextures == 0) {
        } else if (downloadedTextures == 1) {
            initializeGLTextures()
        }

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        // Main Loop
        fearth!!.Orientation = VxM(fearth!!.Orientation, mRotation!!)
        fearth!!.Up = VxM(fearth!!.Up, mRotation!!)

        // movement attenuation
        val a = 0.95f
        mRotation!!.m[0][0] = a * mRotation!!.m[0][0] + (1.0f - a) * 1.0f
        mRotation!!.m[1][0] = a * mRotation!!.m[1][0] + (1.0f - a) * 0.0f
        mRotation!!.m[2][0] = a * mRotation!!.m[2][0] + (1.0f - a) * 0.0f
        mRotation!!.m[3][0] = a * mRotation!!.m[3][0] + (1.0f - a) * 0.0f
        mRotation!!.m[0][1] = a * mRotation!!.m[0][1] + (1.0f - a) * 0.0f
        mRotation!!.m[1][1] = a * mRotation!!.m[1][1] + (1.0f - a) * 1.0f
        mRotation!!.m[2][1] = a * mRotation!!.m[2][1] + (1.0f - a) * 0.0f
        mRotation!!.m[3][1] = a * mRotation!!.m[3][1] + (1.0f - a) * 0.0f
        mRotation!!.m[0][2] = a * mRotation!!.m[0][2] + (1.0f - a) * 0.0f
        mRotation!!.m[1][2] = a * mRotation!!.m[1][2] + (1.0f - a) * 0.0f
        mRotation!!.m[2][2] = a * mRotation!!.m[2][2] + (1.0f - a) * 1.0f
        mRotation!!.m[3][2] = a * mRotation!!.m[3][2] + (1.0f - a) * 0.0f
        mRotation!!.m[0][3] = a * mRotation!!.m[0][3] + (1.0f - a) * 0.0f
        mRotation!!.m[1][3] = a * mRotation!!.m[1][3] + (1.0f - a) * 0.0f
        mRotation!!.m[2][3] = a * mRotation!!.m[2][3] + (1.0f - a) * 0.0f
        mRotation!!.m[3][3] = a * mRotation!!.m[3][3] + (1.0f - a) * 1.0f
        fearth!!.setWorldM()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
        val now = cal.timeInMillis

        // Live light
        if (mLiveLight) {

            // sun declination calculation. see wiki
            cal.timeInMillis = mEpoch
            val N = (cal[Calendar.DAY_OF_YEAR] - 1).toDouble()
            val declination =
                -(M3DM.PI / 180.0 * 23.44) * Math.cos(M3DM.PI / 180.0 * (360.0 / 365.0) * (N + 10.0))

            // calculate sun/light realtive rotation
            cal[Calendar.HOUR_OF_DAY] = 0
            cal[Calendar.MINUTE] = 0
            cal[Calendar.SECOND] = 0
            cal[Calendar.MILLISECOND] = 0
            //long msFromMidnight  = now - cal.getTimeInMillis() + mTimeRotate;
            val msFromMidnight = mEpoch - cal.timeInMillis
            val timeShift = cal.timeZone.getOffset(mEpoch).toLong()
            DEV!!.Light[0]!!.Pos = POINTROTATE(
                M3DVECTOR(
                    0.0f, 100.0f * Math.tan(declination)
                        .toFloat(), -100.0f
                ),
                M3DVECTOR(0.0f, 0.0f, 0.0f),
                M3DVECTOR(0.0f, 1.0f, 0.0f),
                M3DM.PI - 2.0f * M3DM.PI * (msFromMidnight - timeShift).toFloat() / (1000.0f * 60.0f * 60.0f * 24.0f)
            )
            DEV!!.Light[0]!!.Pos = VxM(DEV!!.Light[0]!!.Pos, fearth!!.world)
        }

        // Time calculation
        if (mPlay) {
            // next 48h
            mTimeRotate += if (mTag == 'C' || mTag == 't' || mTag == 'T' || mTag == 'w' || mTag == 'v' || mTag == 'j' || mTag == 's') {
                (Tc * 6 * 30 * 60 * 1000).toLong()
            } else if (mTag == 'O') {
                (25 * (365.25.toLong() * Tc * 2 * 30 * 60) * 1000).toLong()
            } else if (mTag == 'e') {
                (25 * (365.25.toLong() * Tc * 2 * 30 * 60) * 1000).toLong()
            } else if (mTag == 'a' || mTag == 'b') {
                (0.3 * (365.25.toLong() * Tc * 2 * 30 * 60) * 1000).toLong()
            } else if (mTag == 'm') {
                (Tc * 2 * 30 * 60 * 1000).toLong()
            } else if (mTag == 'e') {
                (Tc * 1 * 30 * 60 * 1000).toLong()
            } else if (mTag == 'I' || mTag == 'W') {
                (Tc * 12 * 30 * 60 * 1000).toLong()
            } else {
                (Tc * 4 * 30 * 60 * 1000).toLong()
            }
        }

        // next 48h
        if (mTag == 'C' || mTag == 't' || mTag == 'T' || mTag == 'w' || mTag == 'v' || mTag == 'j' || mTag == 's') {
            while (mTimeRotate > 48 * 3600 * 1000) {
                mTimeRotate -= (48 * 3600 * 1000).toLong()
            }
            mEpoch = now + mTimeRotate
        } else if (mTag == 'O') {
            while (mTimeRotate > 35 * 365.25 * 24 * 3600 * 1000) {
                mTimeRotate -= (35 * 365.25 * 24 * 3600 * 1000).toLong()
            }
            mEpoch = now - (35 * 365.25 * 24 * 3600).toLong() * 1000 + mTimeRotate
        } else if (mTag == 'e') {
            while (mTimeRotate > 65 * 365.25 * 24 * 3600 * 1000) {
                mTimeRotate -= (65 * 365.25 * 24 * 3600 * 1000).toLong()
            }
            mEpoch = now - (65 * 365.25 * 24 * 3600).toLong() * 1000 + mTimeRotate
        } else if (mTag == 'a' || mTag == 'b') {
            while (mTimeRotate > 0.5 * 365.25 * 24 * 3600 * 1000) {
                mTimeRotate -= (0.5 * 365.25 * 24 * 3600 * 1000).toLong()
            }
            mEpoch = now - (0.5 * 365.25 * 24 * 3600).toLong() * 1000 + mTimeRotate
        } else if (mTag == 'm') {
            if (mTimeRotate < 18 * 3600 * 1000) {
                mTimeRotate = (18 * 3600 * 1000).toLong()
            }
            while (mTimeRotate > 24 * 3600 * 1000) {
                mTimeRotate -= (6 * 3600 * 1000).toLong()
            }
            mEpoch = now - 24 * 3600 * 1000 + mTimeRotate
        } else if (mTag == 'e') {
            while (mTimeRotate > 2 * 3600 * 1000) {
                mTimeRotate -= (2 * 3600 * 1000).toLong()
            }
            mEpoch = now - 2 * 3600 * 1000 + mTimeRotate
        } else if (mTag == 'I' || mTag == 'W') {
            while (mTimeRotate > 168 * 3600 * 1000) {
                mTimeRotate -= (168 * 3600 * 1000).toLong()
            }
            mEpoch = now - 168 * 3600 * 1000 + mTimeRotate
        } else {
            while (mTimeRotate > 24 * 3600 * 1000) {
                mTimeRotate -= (24 * 3600 * 1000).toLong()
            }
            mEpoch = now - 24 * 3600 * 1000 + mTimeRotate
        }

        // reset shows always current epoch
        if (mTimeRotate == 0L) {
            mEpoch = now
        }


        // Update texture
        var e1 = 1L // nearest
        var e2 = 1L // second nearest
        var e3 = 1L // nearest in future
        var e4 = 1L // second nearest in future

        // find nearest epoch in past
        for (e in mCloudMapId.keys) {
            if (e < mEpoch && Math.abs(mEpoch - e) < Math.abs(mEpoch - e1)) {
                e1 = e
            }
        }

        // find second nearest in past
        for (e in mCloudMapId.keys) {
            if (e < mEpoch && e != e1 && Math.abs(mEpoch - e) < Math.abs(mEpoch - e2)) {
                e2 = e
            }
        }

        // find nearest in future
        for (e in mCloudMapId.keys) {
            if (e >= mEpoch && Math.abs(e - mEpoch) < Math.abs(e3 - mEpoch)) {
                e3 = e
            }
        }

        // find second nearest in future
        for (e in mCloudMapId.keys) {
            if (e >= mEpoch && e != e3 && Math.abs(e - mEpoch) < Math.abs(e4 - mEpoch)) {
                e4 = e
            }
        }
        if (e1 != _e1) {
            if (mCloudMapId.containsKey(e1)) {
                Log.d("H21lab", "Changing texture e1 " + e1 + " " + _e1 + " ID " + mCloudMapId[e1])
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e1]!!
                earth!!.setTexture(2, texture)
                _e1 = e1
            }
        }
        if (e2 != _e2) {
            if (mCloudMapId.containsKey(e2)) {
                Log.d("H21lab", "Changing texture e2 " + e2 + " " + _e2 + " ID " + mCloudMapId[e2])
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e2]!!
                earth!!.setTexture(4, texture)
                _e2 = e2
            } else if (_e2 != e1 && mCloudMapId.containsKey(e1)) {
                Log.d(
                    "H21lab",
                    "Changing texture e1 no e2 " + e1 + " " + _e1 + " ID " + mCloudMapId[e1]
                )
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e1]!!
                earth!!.setTexture(4, texture)
                _e2 = e1
            }
        }
        if (e3 != _e3) {
            if (mCloudMapId.containsKey(e3)) {
                Log.d("H21lab", "Changing texture e3 " + e3 + " " + _e3 + " ID " + mCloudMapId[e3])
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e3]!!
                earth!!.setTexture(5, texture)
                _e3 = e3
            } else if (_e3 != _e1 && mCloudMapId.containsKey(e1)) {
                Log.d(
                    "H21lab",
                    "Changing texture e1 no e3 " + e1 + " " + _e1 + " ID " + mCloudMapId[e1]
                )
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e1]!!
                earth!!.setTexture(5, texture)
                _e3 = e1
            }
        }
        if (e4 != _e4) {
            if (mCloudMapId.containsKey(e4)) {
                Log.d("H21lab", "Changing texture e4 " + e4 + " " + _e4 + " ID " + mCloudMapId[e4])
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e4]!!
                earth!!.setTexture(6, texture)
                _e4 = e4
            } else if (_e4 != _e3 && mCloudMapId.containsKey(e3)) {
                Log.d(
                    "H21lab",
                    "Changing texture e3 no e4 " + e1 + " " + _e3 + " ID " + mCloudMapId[e3]
                )
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e3]!!
                earth!!.setTexture(6, texture)
                _e4 = e3
            } else if (_e4 != _e1 && _e4 != _e3 && mCloudMapId.containsKey(e1)) {
                Log.d(
                    "H21lab",
                    "Changing texture e1 no e4 " + e1 + " " + _e1 + " ID " + mCloudMapId[e1]
                )
                texture = mD3DTexture()
                texture!!.id = mCloudMapId[e1]!!
                earth!!.setTexture(6, texture)
                _e4 = e1
            }
        }

        // cloud map weights passed to shaders
        var tw1 = 1.0f
        if (_e4 - _e1 != 0L) {
            tw1 = (1.0 - (mEpoch - _e1).toDouble() / (_e4 - _e1).toDouble()).toFloat()
            if (tw1 > 1.0f) {
                tw1 = 1.0f
            }
            if (tw1 < 0.0f) {
                tw1 = 0.0f
            }
        }
        earth!!.setCustomAttribute("uTW1", tw1)
        var tw2 = 1.0f
        if (_e3 - _e2 != 0L) {
            tw2 = (1.0 - (mEpoch - _e2).toDouble() / (_e3 - _e2).toDouble()).toFloat()
            if (tw2 > 1.0f) {
                tw2 = 1.0f
            }
            if (tw2 < 0.0f) {
                tw2 = 0.0f
            }
        }
        earth!!.setCustomAttribute("uTW2", tw2)
        var tw3 = 1.0f
        if (_e3 - _e1 != 0L) {
            tw3 = (1.0 - (mEpoch - _e1).toDouble() / (_e3 - _e1).toDouble()).toFloat()
            if (tw3 > 1.0f) {
                tw3 = 1.0f
            }
            if (tw3 < 0.0f) {
                tw3 = 0.0f
            }
        }
        earth!!.setCustomAttribute("uTW3", tw3)
        DEV!!.renderFrame(scene)
        if (reloadedTextures) {
            reloadedTextures = false
            DEV!!.renderFrame(scene)
        }
        val glError = GLES20.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            Log.e("H21lab", "OpenGL error Error code $glError")
            if (openGLErrorDetected >= 0) {
                initializeShaders()
                openGLErrorDetected++
            }
        }
        /******** FPS  */
        Nrenderedframe++
        if (Nrenderedframe % 5 == 0) {
            // print time
            try {
                val act = mContext as Activity
                act.runOnUiThread {
                    val ac = mContext as Activity
                    val d = Date(mEpoch)
                    val formatter = SimpleDateFormat("E HH:mm dd-MM-yyyy Z")
                    val s = formatter.format(d)
                    ac.title = s
                }
            } catch (e: Exception) {
                Log.e("H21lab", "Unable print time" + e.message)
            }
        }
        if (Nrenderedframe % 30 == 0) {
            _t1 = _t2 // pre FPS
            _t2 = System.nanoTime()
            val _t = (_t2 - _t1).toDouble() / 1.0e9
            Tc = Tc * 0.9f + 0.1f * (_t / Nrenderedframe.toDouble()).toFloat()
            if (Tc != 0.0f) {
                FPS = 1.0f / Tc
            }
            Nrenderedframe = 0
        }
        if (openGLErrorDetected > 50) {
            val errorDialog = OpenGLErrorDialog(this)
            errorDialog.execute(
                """
                    OpenGL error detected. It may be caused by rendering device capabilities. Application will crash now, but in next dialog please help to improve the application by submitting the error report.
                    
                    OpenGL error ${GLES20.glGetError()}
                    
                    GL_VERSION = ${GLES20.glGetString(GLES20.GL_VERSION)}
                    
                    GL_EXTENSIONS = ${GLES20.glGetString(GLES20.GL_EXTENSIONS)}
                    
                    GL_RENDERER = ${GLES20.glGetString(GLES20.GL_RENDERER)}
                    
                    glGetProgramInfoLog = ${GLES20.glGetProgramInfoLog(earth!!.program)}
                    
                    
                    """.trimIndent()
            )
            openGLErrorDetected = -1
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        GLES20.glViewport(0, 0, width, height)
        DEV!!.initialize(
            width,
            height,
            1.0f,
            10.0f,
            M3DVECTOR(0.0f, 0.0f, 2.7f),
            M3DVECTOR(0.0f, 0.0f, -1.0f),
            M3DVECTOR(0.0f, 1.0f, 0.0f)
        )
    }

    companion object {
        const val TAG = "GLES20Renderer"

        // 2007006M0600
        fun getNameFromEpoch(tag: Char, epoch: Long): String {
            val c =
                Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
            c.timeInMillis = epoch
            val hour =
                String.format("%02d", c[Calendar.HOUR_OF_DAY])
            val minute =
                String.format("%02d", c[Calendar.MINUTE])
            val day =
                String.format("%03d", c[Calendar.DAY_OF_YEAR])
            return Integer.toString(c[Calendar.YEAR]) + day + tag + hour + minute
        }

        // 2007006M0600
        fun getEpochFromName(filename: String): Long {
            return try {
                val c = Calendar.getInstance()
                c.clear()
                var i: Int
                i = filename.substring(0, 4).toInt()
                c[Calendar.YEAR] = i
                i = filename.substring(4, 7).toInt()
                c[Calendar.DAY_OF_YEAR] = i
                i = filename.substring(8, 10).toInt()
                c[Calendar.HOUR] = i
                i = filename.substring(10, 12).toInt()
                c[Calendar.MINUTE] = i
                c.timeInMillis
            } catch (e: Exception) {
                0L
            }
        }

        // 2007006M0600
        fun getTag(filename: String?): Char {
            if (filename == null) {
                return ' '
            }
            return if (filename.length <= 7) {
                ' '
            } else filename[7]
        }

        fun scaleBitmap(bitmap: Bitmap?, newWidth: Int, newHeight: Int): Bitmap {
            val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            val ratioX = newWidth.toFloat() / bitmap!!.width.toFloat()
            val ratioY = newHeight.toFloat() / bitmap.height.toFloat()
            val middleX = newWidth.toFloat() / 2.0f
            val middleY = newHeight.toFloat() / 2.0f
            val scaleMatrix = Matrix()
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
            val canvas = Canvas(scaledBitmap)
            canvas.setMatrix(scaleMatrix)
            canvas.drawBitmap(
                bitmap, middleX - bitmap.width / 2, middleY - bitmap.height / 2, Paint(
                    Paint.FILTER_BITMAP_FLAG
                )
            )
            return scaledBitmap
        }

        private fun convert(bitmap: Bitmap?, config: Bitmap.Config): Bitmap {
            val convertedBitmap = Bitmap.createBitmap(bitmap!!.width, bitmap.height, config)
            val canvas = Canvas(convertedBitmap)
            val paint = Paint()
            paint.color = Color.BLACK
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return convertedBitmap
        }

        @Throws(IOException::class)
        fun readToByteBuffer(inStream: InputStream): ByteBuffer {
            val buffer = ByteArray(1024)
            val outStream = ByteArrayOutputStream(1024)
            var read: Int
            while (true) {
                read = inStream.read(buffer)
                if (read == -1) break
                outStream.write(buffer, 0, read)
            }
            return ByteBuffer.wrap(outStream.toByteArray())
        }
    }

    init {
        initialize()
    }
}