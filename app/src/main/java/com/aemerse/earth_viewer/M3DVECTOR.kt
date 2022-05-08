package com.aemerse.earth_viewer

class M3DVECTOR {
    @JvmField
	var x: Float
    @JvmField
	var y: Float
    @JvmField
	var z: Float

     constructor() {
        z = 0.0f
        y = z
        x = y
    }

     constructor(d: Float) {
        z = d
        y = z
        x = y
    }

     constructor(X: Float, Y: Float, Z: Float) {
        x = X
        y = Y
        z = Z
    }

     constructor(v: M3DVECTOR) {
        x = v.x
        y = v.y
        z = v.z
    }

    fun values(): FloatArray {
        val v = FloatArray(4)
        v[0] = x
        v[1] = y
        v[2] = z
        v[3] = 1.0f
        return v
    }

    fun set(v: M3DVECTOR) {
        x = v.x
        y = v.y
        z = v.z
    }

    operator fun set(X: Float, Y: Float, Z: Float) {
        x = X
        y = Y
        z = Z
    }

    fun equals(v: M3DVECTOR): Boolean {
        return x == v.x && y == v.y && z == v.y
    }

    companion object {
        @JvmStatic
		fun ADD(A: M3DVECTOR, B: M3DVECTOR): M3DVECTOR {
            return M3DVECTOR(A.x + B.x, A.y + B.y, A.z + B.z)
        }

        @JvmStatic
		fun DIF(A: M3DVECTOR, B: M3DVECTOR): M3DVECTOR {
            return M3DVECTOR(A.x - B.x, A.y - B.y, A.z - B.z)
        }

        @JvmStatic
		fun MUL(A: M3DVECTOR, d: Float): M3DVECTOR {
            return M3DVECTOR(A.x * d, A.y * d, A.z * d)
        }

        fun SquareMagnitude(A: M3DVECTOR): Float {
            return A.x * A.x + A.y * A.y + A.z * A.z
        }

        fun Magnitude(A: M3DVECTOR): Float {
            return Math.sqrt(SquareMagnitude(A).toDouble()).toFloat()
        }

        @JvmStatic
		fun Normalize(A: M3DVECTOR): M3DVECTOR {
            val M = Magnitude(A)
            val C: M3DVECTOR
            if (M.toDouble() == 0.0) {
                C = M3DVECTOR(0.0f, 0.0f, 0.0f)
                return C
            }
            C = M3DVECTOR(A.x / M, A.y / M, A.z / M)
            return C
        }

        fun DotProduct(A: M3DVECTOR, B: M3DVECTOR): Float {
            return A.x * B.x + A.y * B.y + A.z * B.z
        }

        @JvmStatic
		fun CrossProduct(A: M3DVECTOR, B: M3DVECTOR): M3DVECTOR {
            return M3DVECTOR(A.y * B.z - A.z * B.y, A.z * B.x - A.x * B.z, A.x * B.y - A.y * B.x)
        }

        @JvmStatic
		fun POINTROTATE(B: M3DVECTOR, A: M3DVECTOR, os: M3DVECTOR, theta: Float): M3DVECTOR {
            val P: M3DVECTOR
            val osX: M3DVECTOR
            val osY: M3DVECTOR
            var BN: M3DVECTOR
            var R = M3DVECTOR(B.x - A.x, B.y - A.y, B.z - A.z)
            val t: Float

            // ci nahodou bod B nelezi na osi otocenia
            val test = CrossProduct(os, R)
            if (test.x == 0.0f && test.y == 0.0f && test.z == 0.0f) {
                return B
            }
            // nelezi
            t = DotProduct(os, R) / (os.x * os.x + os.y * os.y + os.z * os.z)
            P = M3DVECTOR(A.x + t * os.x, A.y + t * os.y, A.z + t * os.z)
            osX = M3DVECTOR(B.x - P.x, B.y - P.y, B.z - P.z)
            val L = Math.sqrt(
                Math.abs(os.x * os.x + os.y * os.y + os.z * os.z).toDouble()
            ).toFloat()
            R = M3DVECTOR(os.x / L, os.y / L, os.z / L)
            osY = CrossProduct(R, osX)
            val CS = Math.cos(theta.toDouble()).toFloat()
            val SN = Math.sin(theta.toDouble()).toFloat()
            BN =
                M3DVECTOR(CS * osX.x + SN * osY.x, CS * osX.y + SN * osY.y, CS * osX.z + SN * osY.z)
            BN = M3DVECTOR(BN.x + P.x, BN.y + P.y, BN.z + P.z)
            return BN
        }

        fun VtoView(
            vec: M3DVECTOR,
            SCREEN_WIDTH: Int,
            SCREEN_HEIGHT: Int,
            P_NPlane: Float,
            P_FPlane: Float
        ): M3DVECTOR {
            return M3DVECTOR(
                SCREEN_WIDTH / 2 + vec.x * (SCREEN_WIDTH / 2).toFloat(),
                SCREEN_HEIGHT / 2 - vec.y * (SCREEN_HEIGHT / 2).toFloat(),
                (P_FPlane - P_NPlane) / 2.0f * vec.z + (P_NPlane + P_FPlane) / 2.0f
            )
        }
    }
}