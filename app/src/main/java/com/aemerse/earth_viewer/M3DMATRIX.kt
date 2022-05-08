package com.aemerse.earth_viewer

import kotlin.math.cos
import kotlin.math.sin

class M3DMATRIX {
    @JvmField
	var m = Array(4) { FloatArray(4) }

     constructor()
     constructor(_m: FloatArray) {
        m[0][0] = _m[0]
        m[0][1] = _m[1]
        m[0][2] = _m[2]
        m[0][3] = _m[3]
        m[1][0] = _m[4]
        m[1][1] = _m[5]
        m[1][2] = _m[6]
        m[1][3] = _m[7]
        m[2][0] = _m[8]
        m[2][1] = _m[9]
        m[2][2] = _m[10]
        m[2][3] = _m[11]
        m[3][0] = _m[12]
        m[3][1] = _m[13]
        m[3][2] = _m[14]
        m[3][3] = _m[15]
    }

     constructor(
        _m00: Float,
        _m01: Float,
        _m02: Float,
        _m03: Float,
        _m10: Float,
        _m11: Float,
        _m12: Float,
        _m13: Float,
        _m20: Float,
        _m21: Float,
        _m22: Float,
        _m23: Float,
        _m30: Float,
        _m31: Float,
        _m32: Float,
        _m33: Float
    ) {
        m[0][0] = _m00
        m[0][1] = _m01
        m[0][2] = _m02
        m[0][3] = _m03
        m[1][0] = _m10
        m[1][1] = _m11
        m[1][2] = _m12
        m[1][3] = _m13
        m[2][0] = _m20
        m[2][1] = _m21
        m[2][2] = _m22
        m[2][3] = _m23
        m[3][0] = _m30
        m[3][1] = _m31
        m[3][2] = _m32
        m[3][3] = _m33
    }

    fun values(): FloatArray {
        val res = FloatArray(16)
        for (a in 0..3) {
            for (b in 0..3) {
                res[a * 4 + b] = m[a][b]
            }
        }
        return res
    }

    /** */
    companion object {
        @JvmStatic
		fun MUL(a: M3DMATRIX, b: M3DMATRIX): M3DMATRIX {
            val ret = M3DMATRIX()
            ret.m[0][0] =
                a.m[0][0] * b.m[0][0] + a.m[0][1] * b.m[1][0] + a.m[0][2] * b.m[2][0] + a.m[0][3] * b.m[3][0]
            ret.m[0][1] =
                a.m[0][0] * b.m[0][1] + a.m[0][1] * b.m[1][1] + a.m[0][2] * b.m[2][1] + a.m[0][3] * b.m[3][1]
            ret.m[0][2] =
                a.m[0][0] * b.m[0][2] + a.m[0][1] * b.m[1][2] + a.m[0][2] * b.m[2][2] + a.m[0][3] * b.m[3][2]
            ret.m[0][3] =
                a.m[0][0] * b.m[0][3] + a.m[0][1] * b.m[1][3] + a.m[0][2] * b.m[2][3] + a.m[0][3] * b.m[3][3]
            ret.m[1][0] =
                a.m[1][0] * b.m[0][0] + a.m[1][1] * b.m[1][0] + a.m[1][2] * b.m[2][0] + a.m[1][3] * b.m[3][0]
            ret.m[1][1] =
                a.m[1][0] * b.m[0][1] + a.m[1][1] * b.m[1][1] + a.m[1][2] * b.m[2][1] + a.m[1][3] * b.m[3][1]
            ret.m[1][2] =
                a.m[1][0] * b.m[0][2] + a.m[1][1] * b.m[1][2] + a.m[1][2] * b.m[2][2] + a.m[1][3] * b.m[3][2]
            ret.m[1][3] =
                a.m[1][0] * b.m[0][3] + a.m[1][1] * b.m[1][3] + a.m[1][2] * b.m[2][3] + a.m[1][3] * b.m[3][3]
            ret.m[2][0] =
                a.m[2][0] * b.m[0][0] + a.m[2][1] * b.m[1][0] + a.m[2][2] * b.m[2][0] + a.m[2][3] * b.m[3][0]
            ret.m[2][1] =
                a.m[2][0] * b.m[0][1] + a.m[2][1] * b.m[1][1] + a.m[2][2] * b.m[2][1] + a.m[2][3] * b.m[3][1]
            ret.m[2][2] =
                a.m[2][0] * b.m[0][2] + a.m[2][1] * b.m[1][2] + a.m[2][2] * b.m[2][2] + a.m[2][3] * b.m[3][2]
            ret.m[2][3] =
                a.m[2][0] * b.m[0][3] + a.m[2][1] * b.m[1][3] + a.m[2][2] * b.m[2][3] + a.m[2][3] * b.m[3][3]
            ret.m[3][0] =
                a.m[3][0] * b.m[0][0] + a.m[3][1] * b.m[1][0] + a.m[3][2] * b.m[2][0] + a.m[3][3] * b.m[3][0]
            ret.m[3][1] =
                a.m[3][0] * b.m[0][1] + a.m[3][1] * b.m[1][1] + a.m[3][2] * b.m[2][1] + a.m[3][3] * b.m[3][1]
            ret.m[3][2] =
                a.m[3][0] * b.m[0][2] + a.m[3][1] * b.m[1][2] + a.m[3][2] * b.m[2][2] + a.m[3][3] * b.m[3][2]
            ret.m[3][3] =
                a.m[3][0] * b.m[0][3] + a.m[3][1] * b.m[1][3] + a.m[3][2] * b.m[2][3] + a.m[3][3] * b.m[3][3]
            return ret
        }

        @JvmStatic
		fun IdentityMatrix(): M3DMATRIX {
            val mtx = M3DMATRIX()
            for (a in 0..3) {
                for (b in 0..3) {
                    mtx.m[a][b] = if (a == b) 1.0f else 0.0f
                }
            }
            return mtx
        }

        fun ZeroMatrix(): M3DMATRIX {
            val mtx = M3DMATRIX()
            for (a in 0..3) {
                for (b in 0..3) {
                    mtx.m[a][b] = 0.0f
                }
            }
            return mtx
        }

        fun POINTROTATE_MATRIX(A: M3DVECTOR, axis: M3DVECTOR, theta: Float): M3DMATRIX {
            val mtx = M3DMATRIX()
            var os = axis
            val CS = cos(theta.toDouble()).toFloat()
            val SN = sin(theta.toDouble()).toFloat()
            var temp: Float
            // normalization
            temp = M3DVECTOR.SquareMagnitude(axis)
            if (temp == 0.0f) {
                return IdentityMatrix()
            }
            if (temp != 1.0f) {
                temp = Math.sqrt(temp.toDouble()).toFloat()
                os = M3DVECTOR.MUL(os, 1.0f / temp)
            }
            mtx.m[0][0] = CS + (1 - CS) * os.x * os.x
            mtx.m[0][1] = SN * os.z + (1 - CS) * os.x * os.y
            mtx.m[0][2] = -SN * os.y + (1 - CS) * os.x * os.z
            mtx.m[0][3] = 0.0f
            mtx.m[1][0] = -SN * os.z + (1 - CS) * os.x * os.y
            mtx.m[1][1] = CS + (1 - CS) * os.y * os.y
            mtx.m[1][2] = SN * os.x + (1 - CS) * os.y * os.z
            mtx.m[1][3] = 0.0f
            mtx.m[2][0] = SN * os.y + (1 - CS) * os.x * os.z
            mtx.m[2][1] = -SN * os.x + (1 - CS) * os.y * os.z
            mtx.m[2][2] = CS + (1 - CS) * os.z * os.z
            mtx.m[2][3] = 0.0f
            // if A==(0,0,0), than 3 next rows equals zero - no translation
            mtx.m[3][0] =
                (1 - CS) * A.x - (1 - CS) * os.x * os.x * A.x - (1 - CS) * os.x * os.y * A.y - (1 - CS) * os.x * os.z * A.z - A.y * os.z * SN + (A.z
                        * os.y * SN)
            mtx.m[3][1] =
                (1 - CS) * A.y - (1 - CS) * os.y * os.y * A.y - (1 - CS) * os.x * os.y * A.x - (1 - CS) * os.y * os.z * A.z - A.z * os.x * SN + (A.x
                        * os.z * SN)
            mtx.m[3][2] =
                (1 - CS) * A.z - (1 - CS) * os.z * os.z * A.z - (1 - CS) * os.x * os.z * A.x - (1 - CS) * os.y * os.z * A.y - A.x * os.y * SN + (A.y
                        * os.x * SN)
            mtx.m[3][3] = 1.0f
            return mtx
        }

        // Multiplies vector(1x4, vec(0,3)=1.0f) with matrix 4x4
		@JvmStatic
		fun VxM(vec: M3DVECTOR, mat: M3DMATRIX): M3DVECTOR {
            val ret = M3DVECTOR()
            ret.x = vec.x * mat.m[0][0] + vec.y * mat.m[1][0] + vec.z * mat.m[2][0] + mat.m[3][0]
            ret.y = vec.x * mat.m[0][1] + vec.y * mat.m[1][1] + vec.z * mat.m[2][1] + mat.m[3][1]
            ret.z = vec.x * mat.m[0][2] + vec.y * mat.m[1][2] + vec.z * mat.m[2][2] + mat.m[3][2]
            val F: Float = vec.x * mat.m[0][3] + vec.y * mat.m[1][3] + vec.z * mat.m[2][3] + mat.m[3][3]
            ret.x /= F
            ret.y /= F
            ret.z /= F
            return ret
        }

        /******************* TRANSFORMATIONS  */
        fun CreateProjection(
            near_plane: Float,
            far_plane: Float,
            fov_vert: Float,
            fov_horiz: Float
        ): M3DMATRIX {
            val w: Float = (cos(fov_horiz * 0.5) / sin(fov_horiz * 0.5)).toFloat()
            val h: Float = (cos(fov_vert * 0.5) / sin(fov_vert * 0.5)).toFloat()
            val Q: Float = far_plane / (far_plane - near_plane)
            return M3DMATRIX(
                w,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                h,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                Q,
                -1.0f,
                0.0f,
                0.0f,
                -Q * near_plane,
                0.0f
            )
        }

        fun CreateCameraMatrix(
            position: M3DVECTOR?,
            orient: M3DVECTOR?,
            upworld: M3DVECTOR?
        ): M3DMATRIX {
            val mat = M3DMATRIX()
            var vView = M3DVECTOR()
            vView = M3DVECTOR.MUL(M3DVECTOR.Normalize(orient!!), -1.0f)
            var vRight = M3DVECTOR.CrossProduct(upworld!!, vView)
            var vUp = M3DVECTOR.CrossProduct(vView, vRight)
            vRight = M3DVECTOR.Normalize(vRight)
            vUp = M3DVECTOR.Normalize(vUp)
            mat.m[0][0] = vRight.x
            mat.m[0][1] = vUp.x
            mat.m[0][2] = vView.x
            mat.m[0][3] = 0.0f
            mat.m[1][0] = vRight.y
            mat.m[1][1] = vUp.y
            mat.m[1][2] = vView.y
            mat.m[1][3] = 0.0f
            mat.m[2][0] = vRight.z
            mat.m[2][1] = vUp.z
            mat.m[2][2] = vView.z
            mat.m[2][3] = 0.0f
            mat.m[3][0] = -M3DVECTOR.DotProduct(position!!, vRight)
            mat.m[3][1] = -M3DVECTOR.DotProduct(position, vUp)
            mat.m[3][2] = -M3DVECTOR.DotProduct(position, vView)
            mat.m[3][3] = 1.0f
            return mat
        }

        @JvmStatic
		fun CreateWorldMatrix(
            position: M3DVECTOR,
            orientation: M3DVECTOR?,
            up: M3DVECTOR?
        ): M3DMATRIX {
            val osZ = M3DVECTOR.Normalize(orientation!!)
            var osX = M3DVECTOR.CrossProduct(up!!, osZ)
            var osY = M3DVECTOR.CrossProduct(osZ, osX)
            osX = M3DVECTOR.Normalize(osX)
            osY = M3DVECTOR.Normalize(osY)
            val mat = IdentityMatrix()
            mat.m[0][0] = osX.x
            mat.m[1][0] = osY.x
            mat.m[2][0] = osZ.x
            mat.m[3][0] = position.x
            mat.m[0][1] = osX.y
            mat.m[1][1] = osY.y
            mat.m[2][1] = osZ.y
            mat.m[3][1] = position.y
            mat.m[0][2] = osX.z
            mat.m[1][2] = osY.z
            mat.m[2][2] = osZ.z
            mat.m[3][2] = position.z
            return mat
        }
    }
}