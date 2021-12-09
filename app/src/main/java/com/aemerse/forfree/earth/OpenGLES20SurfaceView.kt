package com.aemerse.forfree.earth

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

class OpenGLES20SurfaceView(context: Context) : GLSurfaceView(context) {
    val openGLES20Renderer: OpenGLES20Renderer?
    private var mPreviousX = -1.0f
    private var mPreviousY = -1.0f
    private var mPreviousX2 = -1.0f
    private var mPreviousY2 = -1.0f
    private var mPreviousxtheta = 0.0f
    private var mPreviousytheta = 0.0f
    private var previousPointerCount = 0
    var gestureDetector: GestureDetector
    var test = 0f

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // event when double tap occurs
        override fun onDoubleTap(e: MotionEvent): Boolean {

            // play/stop
            openGLES20Renderer!!.mPlay = !openGLES20Renderer.mPlay
            return true
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(e)

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        val x: Float
        val y: Float
        val x2: Float
        val y2: Float
        if (openGLES20Renderer != null && openGLES20Renderer.initialized) {
            try {
                when (e.action) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                        mPreviousX = -1.0f
                        mPreviousY = -1.0f
                        mPreviousX2 = -1.0f
                        mPreviousY2 = -1.0f
                    }
                    MotionEvent.ACTION_MOVE -> if (e.pointerCount == 1) {
                        if (previousPointerCount >= 2) {
                            mPreviousX = -1.0f
                            mPreviousY = -1.0f
                            mPreviousX2 = -1.0f
                            mPreviousY2 = -1.0f
                        }
                        x = e.getX(0)
                        y = e.getY(0)
                        if (mPreviousX == -1.0f) {
                            mPreviousX = x
                        }
                        if (mPreviousY == -1.0f) {
                            mPreviousY = y
                        }
                        val ytheta =
                            0.5f * mPreviousytheta + 0.5f * (1.0f * (y - mPreviousY) / openGLES20Renderer.DEV!!.SCREEN_HEIGHT.toFloat())
                        val xtheta =
                            0.5f * mPreviousxtheta + 0.5f * (1.0f * (x - mPreviousX) / openGLES20Renderer.DEV!!.SCREEN_WIDTH.toFloat())
                        openGLES20Renderer.mRotation = M3DMATRIX.POINTROTATE_MATRIX(
                            M3DVECTOR(0.0f, 0.0f, 0.0f),
                            M3DVECTOR(0.0f, 1.0f, 0.0f),
                            xtheta
                        )
                        openGLES20Renderer.mRotation = M3DMATRIX.MUL(
                            openGLES20Renderer.mRotation!!,
                            M3DMATRIX.POINTROTATE_MATRIX(
                                M3DVECTOR(0.0f, 0.0f, 0.0f),
                                M3DVECTOR(1.0f, 0.0f, 0.0f),
                                ytheta
                            )
                        )
                        mPreviousX = x
                        mPreviousY = y
                        mPreviousX2 = -1.0f
                        mPreviousY2 = -1.0f
                        mPreviousxtheta = xtheta
                        mPreviousytheta = ytheta
                        previousPointerCount = 1
                    } else if (e.pointerCount >= 2) {
                        x = e.getX(0)
                        y = e.getY(0)
                        x2 = e.getX(1)
                        y2 = e.getY(1)
                        if (mPreviousX == -1.0f) {
                            mPreviousX = x
                        }
                        if (mPreviousY == -1.0f) {
                            mPreviousY = y
                        }
                        if (mPreviousX2 == -1.0f) {
                            mPreviousX2 = x2
                        }
                        if (mPreviousY2 == -1.0f) {
                            mPreviousY2 = y2
                        }
                        var _x = (x - x2) / openGLES20Renderer.DEV!!.SCREEN_WIDTH.toFloat()
                        var _y = (y - y2) / openGLES20Renderer.DEV!!.SCREEN_HEIGHT.toFloat()
                        var __x =
                            (mPreviousX - mPreviousX2) / openGLES20Renderer.DEV!!.SCREEN_WIDTH.toFloat()
                        var __y =
                            (mPreviousY - mPreviousY2) / openGLES20Renderer.DEV!!.SCREEN_HEIGHT.toFloat()
                        if (_x != 0.0f && __x != 0.0f && _y != 0.0f && __y != 0.0f) {
                            var _angle = Math.atan(_y.toDouble() / _x).toFloat()
                            if (x2 < x) {
                                _angle -= M3DM.PI / 1.0f
                            }
                            var __angle = Math.atan(__y.toDouble() / __x)
                                .toFloat()
                            if (mPreviousX2 < mPreviousX) {
                                __angle -= M3DM.PI / 1.0f
                            }
                            val angle = (_angle - __angle) * 1.0f
                            openGLES20Renderer.fearth!!.Orientation = M3DVECTOR.POINTROTATE(
                                openGLES20Renderer.fearth!!.Orientation,
                                M3DVECTOR(0.0f, 0.0f, 0.0f),
                                openGLES20Renderer.DEV!!.CameraOrientation!!,
                                angle
                            )
                            openGLES20Renderer.fearth!!.Up = M3DVECTOR.POINTROTATE(
                                openGLES20Renderer.fearth!!.Up,
                                M3DVECTOR(0.0f, 0.0f, 0.0f),
                                openGLES20Renderer.DEV!!.CameraOrientation!!,
                                angle
                            )
                            openGLES20Renderer.fearth!!.setWorldM()
                        }


                        // scale
                        _x = x - x2
                        _y = y - y2
                        __x = mPreviousX - mPreviousX2
                        __y = mPreviousY - mPreviousY2
                        val _r = Math.sqrt((_x * _x + _y * _y).toDouble())
                            .toFloat()
                        val __r = Math.sqrt((__x * __x + __y * __y).toDouble())
                            .toFloat()
                        val scale = Math.sqrt((__r / _r).toDouble()).toFloat()
                        openGLES20Renderer.DEV!!.P_fov_horiz *= scale
                        if (openGLES20Renderer.DEV!!.P_fov_horiz > M3DM.PI - 1.0f) {
                            openGLES20Renderer.DEV!!.P_fov_horiz = M3DM.PI - 1.0f
                        } else if (openGLES20Renderer.DEV!!.P_fov_horiz <= 0.0f) {
                            openGLES20Renderer.DEV!!.P_fov_horiz = 0.01f
                        }
                        openGLES20Renderer.DEV!!.P_fov_vert *= scale
                        if (openGLES20Renderer.DEV!!.P_fov_vert > M3DM.PI - 1.7f) {
                            openGLES20Renderer.DEV!!.P_fov_vert = M3DM.PI - 1.7f
                        } else if (openGLES20Renderer.DEV!!.P_fov_vert <= 0.0f) {
                            openGLES20Renderer.DEV!!.P_fov_vert = 0.01f
                        }
                        val left =
                            -openGLES20Renderer.DEV!!.P_NPlane * (openGLES20Renderer.DEV!!.SCREEN_WIDTH.toFloat() / openGLES20Renderer.DEV!!.SCREEN_HEIGHT.toFloat()) * Math.tan(
                                (openGLES20Renderer.DEV!!.P_fov_vert / 2.0f).toDouble()
                            )
                                .toFloat()
                        val right =
                            openGLES20Renderer.DEV!!.P_NPlane * (openGLES20Renderer.DEV!!.SCREEN_WIDTH.toFloat() / openGLES20Renderer.DEV!!.SCREEN_HEIGHT.toFloat()) * Math.tan(
                                (openGLES20Renderer.DEV!!.P_fov_vert / 2.0f).toDouble()
                            )
                                .toFloat()
                        val bottom =
                            -openGLES20Renderer.DEV!!.P_NPlane * Math.tan((openGLES20Renderer.DEV!!.P_fov_vert / 2.0f).toDouble())
                                .toFloat()
                        val top =
                            openGLES20Renderer.DEV!!.P_NPlane * Math.tan((openGLES20Renderer.DEV!!.P_fov_vert / 2.0f).toDouble())
                                .toFloat()
                        Matrix.frustumM(
                            openGLES20Renderer.DEV!!.projectionMatrix,
                            0,
                            left,
                            right,
                            bottom,
                            top,
                            openGLES20Renderer.DEV!!.P_NPlane,
                            openGLES20Renderer.DEV!!.P_FPlane
                        )
                        mPreviousX = x
                        mPreviousY = y
                        mPreviousX2 = x2
                        mPreviousY2 = y2
                        previousPointerCount = 2
                    } else {
                        mPreviousX = -1.0f
                        mPreviousY = -1.0f
                        mPreviousX2 = -1.0f
                        mPreviousY2 = -1.0f
                        previousPointerCount = 0
                    }
                }
            } catch (exception: Exception) {
                Log.e("H21lab", "Exception detected: " + exception.message)
            }
        }
        return true
    }

    init {

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)
        // Set the Renderer for drawing on the GLSurfaceView
        openGLES20Renderer = OpenGLES20Renderer(context)
        setRenderer(openGLES20Renderer)
        gestureDetector = GestureDetector(context, GestureListener())
    }
}