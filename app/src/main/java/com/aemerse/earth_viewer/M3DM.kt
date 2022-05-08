package com.aemerse.earth_viewer

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.aemerse.earth_viewer.M3DMATRIX.Companion.CreateWorldMatrix
import com.aemerse.earth_viewer.M3DMATRIX.Companion.IdentityMatrix
import com.aemerse.earth_viewer.M3DMATRIX.Companion.MUL
import com.aemerse.earth_viewer.M3DMATRIX.Companion.VxM
import com.aemerse.earth_viewer.M3DVECTOR.Companion.ADD
import com.aemerse.earth_viewer.M3DVECTOR.Companion.CrossProduct
import com.aemerse.earth_viewer.M3DVECTOR.Companion.DIF
import com.aemerse.earth_viewer.M3DVECTOR.Companion.MUL
import com.aemerse.earth_viewer.M3DVECTOR.Companion.Normalize
import com.aemerse.earth_viewer.M3DVECTOR.Companion.POINTROTATE
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

class M3DM {
    var SCREEN_WIDTH = 200
    var SCREEN_HEIGHT = 200
    var ZSORT = 1 // TODO

    /************************* CAMERA  */
    var CameraPosition: M3DVECTOR? = null
    var CameraOrientation: M3DVECTOR? = null
    var CameraUp: M3DVECTOR? = null
    var viewMatrix = FloatArray(16)
    var projectionMatrix = FloatArray(16)

    // PROJECTION
    var P_NPlane = 1.0f
    var P_FPlane = 200.0f
    var P_fov_vert = 60.0f * (PI / 180.0f) // overall
    var P_fov_horiz = SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * P_fov_vert

    // TODO not used yet
    /* For Z sort*********** */
    inner class ZSORTstruct : Comparable<ZSORTstruct> {
        var Z = 0f
        var hMesh: mD3DMesh? = null
        var World: M3DMATRIX? = null
        override fun compareTo(b: ZSORTstruct): Int {
            return if (Z > b.Z) {
                1
            } else if (Z < b.Z) {
                -1
            } else {
                0
            }
        }
    }

    var zsort: MutableList<ZSORTstruct>? = ArrayList()

    /* OpenGL */ // TODO only for 1 light and texture required
    private val vertexShaderCode = """uniform mat4 uMVPMatrix;   
uniform mat4 uMVMatrix;   	
uniform mat4 uVMatrix;   	
uniform mat4 uIMVMatrix;	
attribute vec4 aPosition; 	
attribute vec3 aNormal;  	
attribute vec2 aTex;		
varying vec3 vPosition;	
varying vec3 vNormal;		
varying vec3 vMVNormal;	
varying vec2 vTex;			
uniform vec4 uLightPos;  	
varying vec3 vLightPos;	
varying vec3 lightVector;	
varying vec3 vEye;	
varying vec3 vPositionA;	
varying vec2 shiftUV;		
void main(){              	
 gl_Position = uMVPMatrix * aPosition; 
 vec4 position = aPosition; 
 vPosition = vec3(position.x, position.y, position.z); 
 vec4 normal = vec4(aNormal, 0.0); 
 vNormal = vec3(normal.x, normal.y, normal.z); 
 normal = uMVMatrix*vec4(aNormal, 0.0); 
 vMVNormal = vec3(normal.x, normal.y, normal.z); 
 vTex = aTex; 
 vec4 lightPos = uIMVMatrix * uLightPos; 
 vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); 
 vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); 
 vEye = vec3(eye.x, eye.y, eye.z); 
 lightVector = normalize(vLightPos - vPosition);   
 vec3 _y = vec3(0.0, 1.0, 0.0); 
 vec3 _x = cross(_y, vNormal); 
 vec3 _z = vNormal; 
 shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); 
}                         
"""
    private val fragmentShaderCode = """precision mediump float;  	
varying vec3 vLightPos;  	
varying vec3 vPosition;	
varying vec3 lightVector;	
uniform vec4 uColor;  		
varying vec3 vNormal;   	
varying vec3 vMVNormal;	
varying vec2 vTex;			
uniform sampler2D uTextures[4];
varying vec3 vEye	;   	
varying vec2 shiftUV;		
uniform vec4 uLightAmbientColor;  		
uniform vec4 uLightDiffuseColor;  		
uniform vec4 uLightSpecularColor;  	
uniform float uLightAttenuation;  		
uniform vec4 uMaterialAmbientColor;  	
uniform vec4 uMaterialDiffuseColor; 	
uniform vec4 uMaterialSpecularColor;  	
uniform vec4 uMaterialEmissiveColor;  	
uniform float uMaterialShinnes;  		
uniform float uBumpLevel;				
uniform int uTexMapping;				
void main()  				
{  						
    vec3 normal2 = vNormal;	
	 vec3 normal = vec3(0.0, 0.0, 0.0);	
	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	
	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	
	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	
	 vec4 n = vec4(normal, 0.0); 
	 normal = vec3(n.x, n.y, n.z); 
	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; 
    float diffuse = max(dot(normal, lightVector), 0.0);   
    float diffuse2 = max(dot(normal2, lightVector), 0.0);   
	 vec3 E = normalize(vEye - vPosition);
	 vec3 R = normalize(-reflect(lightVector, normal));
    float sp = max(dot(R, E), 0.0);
	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);
	 spec = clamp(spec, 0.0, 1.0);
	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);
	 specW = clamp(specW, 0.0, 1.0);
	 R = normalize(-reflect(lightVector, normal2));
    sp = max(dot(R, E), 0.0);
	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);
	 specC = clamp(specC, 0.0, 1.0);
 float min = 0.0; 
 float max = 1.0; 
 float cm = clamp((texture2D(uTextures[2], vTex).g - min)/(max-min), 0.0, 1.0); 
 cm = 0.0 + 1.25*cm; 
 vec4 cmT = vec4(1.0,1.0,1.0,1.0); 
 float cmS = clamp((texture2D(uTextures[2], vTex  + uBumpLevel*0.005*shiftUV).g - min)/(max-min), 0.0, 1.0); 
 cmS = 0.0 + 1.0*cmS; 
 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; 
		gl_FragColor = 1.5*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.2*uLightSpecularColor * specC ) 						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS 						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.4*uLightSpecularColor * specW )     						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   
	 diffuse = clamp(diffuse, 0.0, 1.0); 
    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   
 float a; 
 a = abs(vMVNormal.z); 
 gl_FragColor = clamp(gl_FragColor  + 0.2 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 4.0); 
 gl_FragColor = clamp(gl_FragColor  + 10.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 0.5); 
 gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 30.0); 
 gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 if (abs(vMVNormal.z) < 0.55) { 
 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	
 }; 
}   
"""
    /** */
    /******************* STRUCTURES  */
    /************************** LIGHT  */
    var N_Lights = 0

    class LIGHT {
        @JvmField
		var Pos: M3DVECTOR = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var AR: Float
        var AG: Float
        var AB: Float
        var AA // ambient
                : Float
        var DR: Float
        var DG: Float
        var DB: Float
        var DA // diffuse
                : Float
        @JvmField
		var SR: Float
        @JvmField
		var SG: Float
        @JvmField
		var SB: Float
        var SA  = 0.0f
        var AT = 0f

        init {
            SB = SA
            SG = SB
            SR = SG
            DA = SR
            DB = DA
            DG = DB
            DR = DG
            AA = DR
            AB = AA
            AG = AB
            AR = AG
        }
    }

    @JvmField
	var Light = arrayOfNulls<LIGHT>(8)

    /************************ M3DMATERIAL  */
     class M3DMATERIAL {
        var AR: Float
        var AG: Float
        var AB: Float
        var AA: Float
        var DR: Float
        var DG: Float
        var DB: Float
        var DA // diffuse
                : Float
        var SR: Float
        var SG: Float
        var SB: Float
        var SA // specular
                : Float
        var ER: Float
        var EG: Float
        var EB: Float
        var EA // emission
                : Float
        var SH // shininess
                : Float
        var am: Float
        var dm: Float
        var sm // ambient col. index, diffuse, specular
                : Float
        var Name = CharArray(NAMELENGHT) // TODO not used

        constructor() {
            AR = 1.0f
            AG = 1.0f
            AB = 1.0f
            AA = 1.0f
            DR = 1.0f
            DG = 1.0f
            DB = 1.0f
            DA = 1.0f
            SR = 1.0f
            SG = 1.0f
            SB = 1.0f
            SA = 1.0f
            ER = 0.0f
            EG = 0.0f
            EB = 0.0f
            EA = 1.0f
            SH = 45.0f
            am = 0.0f
            dm = 1.0f
            sm = 1.0f
            Name[0] = 0.toChar()
        }

        constructor(
            ar: Float,
            ag: Float,
            ab: Float,
            aa: Float,
            dr: Float,
            dg: Float,
            db: Float,
            da: Float,
            sr: Float,
            sg: Float,
            sb: Float,
            sa: Float,
            er: Float,
            eg: Float,
            eb: Float,
            ea: Float,
            sh: Float,
            _am: Float,
            _dm: Float,
            _sm: Float
        ) {
            AR = ar
            AG = ag
            AB = ab
            AA = aa
            DR = dr
            DG = dg
            DB = db
            DA = da
            SR = sr
            SG = sg
            SB = sb
            SA = sa
            ER = er
            EG = eg
            EB = eb
            EA = ea
            SH = sh
            am = _am
            dm = _dm
            sm = _sm
            // Name=NAME;
        }
    }

    private var DEFMATERIAL = M3DMATERIAL(
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
        45.0f,
        0.0f,
        1.0f,
        1.0f
    )

    class mD3DTexture {
        var id = -1
        var Name = CharArray(NAMELENGHT) // TODO Not used

        constructor() {
            id = 0
        }

        constructor(i: Int) {
            id = i
            Name[0] = 0.toChar()
        }
    }

    var _Texture = arrayOfNulls<mD3DTexture>(8)

    /************* VERTEX  */
     class M3DVERTEX_RGB {
        var P: M3DVECTOR = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var N: M3DVECTOR = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var u: Float
        var v: Float
        var r: Float
        var g: Float
        var b: Float
        var a: Float = 0.0f

        init {
            b = a
            g = b
            r = g
            v = r
            u = v
        }
    }

     class M3DVERTEX {
        var P: M3DVECTOR = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var N: M3DVECTOR = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var u: Float
        var v: Float = 0.0f

        init {
            u = v
        }
    }

    /******************** CONSTRUCTOR  */
    constructor() {
        SCREEN_WIDTH = 0
        SCREEN_HEIGHT = 0
        for (i in 0..7) {
            _Texture[i] = mD3DTexture()
            Light[i] = LIGHT()
        }
    }

    constructor(width: Int, height: Int) {
        SCREEN_WIDTH = width
        SCREEN_HEIGHT = height
        P_fov_horiz = SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * P_fov_vert
        //	Matrix.frustumM(projectionMatrix, 0, -P_NPlane*(float)Math.tan(P_fov_horiz), P_NPlane*(float)Math.tan(P_fov_horiz), -P_NPlane*(float)Math.tan(P_fov_vert), P_NPlane*(float)Math.tan(P_fov_vert), P_NPlane, P_FPlane);
        Matrix.frustumM(
            projectionMatrix,
            0,
            -P_NPlane * SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            P_NPlane * SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            -P_NPlane * Math.tan((P_fov_vert / 2.0f).toDouble()).toFloat(),
            P_NPlane * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            P_NPlane,
            P_FPlane
        )
        for (i in 0..7) {
            _Texture[i] = mD3DTexture()
            Light[i] = LIGHT()
        }
    }

    constructor(
        width: Int,
        height: Int,
        p_nplane: Float,
        p_fplane: Float,
        cam_pos: M3DVECTOR?,
        cam_or: M3DVECTOR?,
        cam_up: M3DVECTOR?
    ) {
        initialize(width, height, p_nplane, p_fplane, cam_pos, cam_or, cam_up)
    }

    fun initializeGL() {
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private fun loadGLShader(type: Int, shaderCode: String): Int {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        var shader = GLES20.glCreateShader(type)
        if (shader != 0) {
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader $type:")
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    fun DeleteProgram(program: Int) {
        GLES20.glDeleteProgram(program)
    }

    fun CompileProgram(vsc: String, fsc: String): Int {
        val vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, vsc)
        val fragmentShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, fsc)
        val program = GLES20.glCreateProgram() // create empty OpenGL Program
        GLES20.glAttachShader(program, vertexShader) // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader) // add the fragment shader to program
        GLES20.glLinkProgram(program) // creates OpenGL program executables
        return program
    }

    fun initialize(
        width: Int,
        height: Int,
        p_nplane: Float,
        p_fplane: Float,
        cam_pos: M3DVECTOR?,
        cam_or: M3DVECTOR?,
        cam_up: M3DVECTOR?
    ) {
        SCREEN_WIDTH = width
        SCREEN_HEIGHT = height
        P_NPlane = p_nplane
        P_FPlane = p_fplane
        P_fov_horiz = SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * P_fov_vert
        CameraPosition = cam_pos
        CameraOrientation = cam_or
        CameraUp = cam_up
        Matrix.frustumM(
            projectionMatrix,
            0,
            -P_NPlane * SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            P_NPlane * SCREEN_WIDTH.toFloat() / SCREEN_HEIGHT.toFloat() * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            -P_NPlane * Math.tan((P_fov_vert / 2.0f).toDouble()).toFloat(),
            P_NPlane * Math.tan((P_fov_vert / 2.0f).toDouble())
                .toFloat(),
            P_NPlane,
            P_FPlane
        )
    }

    /************************ mD3DMesh  */
    class mD3DMesh {
        var Name = CharArray(NAMELENGHT)
        var flags: Int
        var Data: ShortArray
        var VertexStruct: Int
        var VertexSize //dimension of vertex, number of floats
                : Int
        var VertexCount: Int
        lateinit var Vertex: FloatArray
        var SrcBlend = 0
        var DestBlend = 0
        var Textures: Int
        var program = -1
            set(p) {
                newProgram = p
            }
        var newProgram = -1
        var Texture = arrayOfNulls<mD3DTexture>(8)
        var MatFlags // TODO legacy
                = 0
        var Material: M3DMATERIAL
        var bumpLevel = 20.0f
        var customAttributes = HashMap<String, Float>()
        var mMVPMatrixPrev: FloatArray? = null
        var Meshs: Int
        lateinit var Mesh: Array<mD3DMesh>
        fun addMesh(msh: mD3DMesh) {
            Mesh[Meshs] = msh
            Meshs++
        }

        fun removeMesh(msh: mD3DMesh) {
            for (A in Meshs - 1 downTo 0) {
                if (msh === Mesh[A]) {
                    for (B in A until Meshs - 1) {
                        Mesh[B] = Mesh[B + 1]
                    }
                    Meshs--
                    return
                }
            }
        }

        fun setTexture(N: Int, txt: mD3DTexture?) {
            Texture[N] = txt
        }

        @JvmName("setMaterial1")
        fun setMaterial(mat: M3DMATERIAL) {
            Material = mat
        }

        @JvmName("setBumpLevel1")
        fun setBumpLevel(bump: Float) {
            bumpLevel = bump
        }

        fun setCustomAttribute(name: String, value: Float) {
            customAttributes[name] = value
        }

        fun setVertex(N: Int, handle: FloatArray) {
            for (i in 0 until VertexSize) {
                Vertex[i] = handle[i]
            }
        }

        fun flushProgram() {
            program = newProgram
        }

        fun getVertex(N: Int): FloatArray {
            val ret = FloatArray(VertexSize)
            for (i in 0 until VertexSize) {
                ret[i] = Vertex[i]
            }
            return ret
        }

        // all normals are counted and than normalized
        fun generateNormals() {
            var NVERTEX: Int // index of just modified vertex
            var j: Int
            var k: Int
            var n: Int
            // deletion of original normals
            for (i in 0 until VertexCount) {
                j = 3
                while (j < 6) {
                    Vertex[i * VertexSize + j] = 0.0f
                    j++
                }
            }
            var N: M3DVECTOR
            var v0: M3DVECTOR
            var v1: M3DVECTOR
            var v2: M3DVECTOR
            v0 = M3DVECTOR(0.0f, 0.0f, 0.0f) // required for compilation
            val Norm = arrayOfNulls<M3DVECTOR>(VertexCount)
            j = 0
            while (j < VertexCount) {
                Norm[j] = M3DVECTOR(0.0f, 0.0f, 0.0f)
                j++
            }
            var TT: Float
            for (i in Data.indices) {
                if (i > 3) {
                    k = i
                    // get vertexs
                    NVERTEX = Data[k - 2] * VertexSize
                    v0 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    NVERTEX = Data[k - 1] * VertexSize
                    v1 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    NVERTEX = Data[k] * VertexSize
                    v2 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    N = CrossProduct(DIF(v2, v1), DIF(v2, v0))
                    TT = N.x * N.x + N.y * N.y + N.z * N.z
                    N = if (TT.toDouble() == 0.0) {
                        M3DVECTOR(0.0f, 0.0f, 0.0f)
                    } else {
                        MUL(N, 1.0f / Math.sqrt(TT.toDouble()).toFloat())
                    }

                    // Invert every 2nd face because of GL_TRIANGLE_STRIP
                    if (i % 2 == 0) {
                        N = MUL(N, -1.0f)
                    }
                    Norm[Data[k - 2].toInt()] = ADD(Norm[Data[k - 2].toInt()]!!, N)
                    Norm[Data[k - 1].toInt()] = ADD(
                        Norm[Data[k - 1].toInt()]!!, N
                    )
                    Norm[Data[k].toInt()] = ADD(Norm[Data[k].toInt()]!!, N)
                }
            }
            // normalization of normals
            for (i in Data.indices) {
                NVERTEX = Data[i] * VertexSize
                TT =
                    Norm[Data[i].toInt()]!!.x * Norm[Data[i].toInt()]!!.x + Norm[Data[i].toInt()]!!.y * Norm[Data[i].toInt()]!!.y + Norm[Data[i].toInt()]!!.z * Norm[Data[i].toInt()]!!.z
                if (TT.toDouble() == 0.0) {
                    Norm[Data[i].toInt()] = M3DVECTOR(0.0f, 0.0f, 0.0f)
                } else {
                    Norm[Data[i].toInt()] = MUL(
                        Norm[Data[i].toInt()]!!, 1.0f / Math.sqrt(TT.toDouble())
                            .toFloat()
                    )
                }
                Vertex[NVERTEX + 3] = Norm[Data[i].toInt()]!!.x
                Vertex[NVERTEX + 4] = Norm[Data[i].toInt()]!!.y
                Vertex[NVERTEX + 5] = Norm[Data[i].toInt()]!!.z
            }
        }

        fun generateTangentsBitangets() {
            var NVERTEX: Int // index of just modified vertex
            var j: Int
            var k: Int
            var n: Int
            // deletion of original tangents, bitangetns
            for (i in 0 until VertexCount) {
                j = 8
                while (j < 14) {
                    Vertex[i * VertexSize + j] = 0.0f
                    j++
                }
            }
            var N: M3DVECTOR
            var v0: M3DVECTOR
            var v1: M3DVECTOR
            var v2: M3DVECTOR
            var v0_u: Float
            var v0_v: Float
            var v1_u: Float
            var v1_v: Float
            var v2_u: Float
            var v2_v: Float
            v0 = M3DVECTOR(0.0f, 0.0f, 0.0f) // required for compilation
            v2_v = 0.0f
            v2_u = v2_v
            v1_v = v2_u
            v1_u = v1_v
            v0_v = v1_u
            v0_u = v0_v
            var TT: Float
            for (i in Data.indices) {
                if (i >= 2) {
                    k = i

                    // get vertexs
                    NVERTEX = Data[k - 2] * VertexSize
                    v0 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    v0_u = Vertex[NVERTEX + 6]
                    v0_v = Vertex[NVERTEX + 7]
                    NVERTEX = Data[k - 1] * VertexSize
                    v1 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    v1_u = Vertex[NVERTEX + 6]
                    v1_v = Vertex[NVERTEX + 7]
                    NVERTEX = Data[k] * VertexSize
                    v2 = M3DVECTOR(Vertex[NVERTEX], Vertex[NVERTEX + 1], Vertex[NVERTEX + 2])
                    v2_u = Vertex[NVERTEX + 6]
                    v2_v = Vertex[NVERTEX + 7]


                    // Edges of the triangle : position delta
                    val deltaPos1 = DIF(v1, v0)
                    val deltaPos2 = DIF(v2, v0)

                    // UV delta
                    val deltaUV1_u = v1_u - v0_u
                    val deltaUV1_v = v1_v - v0_v
                    val deltaUV2_u = v2_u - v0_u
                    val deltaUV2_v = v2_v - v0_v
                    if (deltaUV1_u * deltaUV2_v - deltaUV1_v * deltaUV2_u != 0.0f) {
                        val r = 1.0f / (deltaUV1_u * deltaUV2_v - deltaUV1_v * deltaUV2_u)
                        var tangent =
                            MUL(DIF(MUL(deltaPos1, deltaUV2_v), MUL(deltaPos2, deltaUV1_v)), r)
                        tangent = Normalize(tangent)
                        var bitangent =
                            MUL(DIF(MUL(deltaPos2, deltaUV1_u), MUL(deltaPos1, deltaUV2_u)), r)
                        bitangent = Normalize(bitangent)
                        Vertex[NVERTEX + 8] = tangent.x
                        Vertex[NVERTEX + 9] = tangent.y
                        Vertex[NVERTEX + 10] = tangent.z
                        Vertex[NVERTEX + 11] = bitangent.x
                        Vertex[NVERTEX + 12] = bitangent.y
                        Vertex[NVERTEX + 13] = bitangent.z
                    }
                }
            }
        }

        fun releasemD3DMesh(DeleteSubMeshes: Int) {
            if (VertexCount > 0) {
                Data[0] = 0
            }
            VertexCount = 0
            if (DeleteSubMeshes == 1) {
                for (i in 0 until Meshs) {
                    Mesh[i].releasemD3DMesh(1)
                }
            }
            Meshs = 0
        }

        constructor() {
            flags = 0
            Data = ShortArray(1)
            Data[0] = 0
            VertexStruct = 0
            VertexSize = 0
            VertexCount = 0
            SrcBlend = 0
            DestBlend = 0
            Textures = 0
            MatFlags = 0
            Material = M3DMATERIAL()
            Meshs = 0
        }

        constructor(V: Array<M3DVERTEX?>, D: ShortArray, D_length: Int) {
            val strsize = 14
            VertexSize = strsize
            flags = 0
            Textures = 0
            Meshs = 0
            var q = 0
            while (q < NAMELENGHT) {
                Name[q] = 0.toChar()
                q++
            }
            var j = 0
            Data = ShortArray(D_length)
            VertexCount = V.size
            j = 0
            while (j < D_length) {
                Data[j] = D[j]
                j++
            }
            VertexStruct = M3DFVF_XYZ or M3DFVF_NORMAL or M3DFVF_TEX1 or M3DFVF_TAN or M3DFVF_BITAN
            if (VertexCount > 0) {
                Vertex = FloatArray(VertexCount * strsize)
                j = 0
                while (j < VertexCount) {
                    Vertex[j * strsize] = V[j]!!.P.x
                    Vertex[j * strsize + 1] = V[j]!!.P.y
                    Vertex[j * strsize + 2] = V[j]!!.P.z
                    Vertex[j * strsize + 3] = V[j]!!.N.x
                    Vertex[j * strsize + 4] = V[j]!!.N.y
                    Vertex[j * strsize + 5] = V[j]!!.N.z
                    Vertex[j * strsize + 6] = V[j]!!.u
                    Vertex[j * strsize + 7] = V[j]!!.v
                    j++
                }
            }
            Material = M3DMATERIAL()
        }
    }
    /** */
    /************************** mD3DFrame  */
    class mD3DFrame {
        var Position: M3DVECTOR
        var Orientation: M3DVECTOR
        var Up: M3DVECTOR
        var world: M3DMATRIX
        var Frames: Int
        var Frame = arrayOfNulls<mD3DFrame>(MAXFRAMES)
        var Meshs: Int
        var Mesh = arrayOfNulls<mD3DMesh>(MAXMESHS)
        fun setWorld(P: M3DVECTOR, Or: M3DVECTOR, U: M3DVECTOR) {
            Position = P
            Orientation = Or
            Up = U
            world = CreateWorldMatrix(P, Or, U)
        }

        fun setWorldM() {
            world = CreateWorldMatrix(Position, Orientation, Up)
        }

        fun addFrame(Fr: mD3DFrame?) {
            Frame[Frames] = Fr
            Frames++
        }

        fun removeFrame(Fr: mD3DFrame) {
            for (A in Frames - 1 downTo 0) {
                if (Fr === Frame[A]) {
                    for (B in A until Frames - 1) {
                        Frame[B] = Frame[B + 1]
                    }
                    Frames--
                    return
                }
            }
        }

        fun addMesh(msh: mD3DMesh?) {
            Mesh[Meshs] = msh
            Meshs++
        }

        fun removeMesh(msh: mD3DMesh) {
            for (A in Meshs - 1 downTo 0) {
                if (msh === Mesh[A]) {
                    for (B in A until Meshs - 1) {
                        Mesh[B] = Mesh[B + 1]
                    }
                    Meshs--
                    return
                }
            }
        }

        fun releasemD3DFrame(DeleteSubMF: Int) {
            var i: Int
            if (DeleteSubMF == 1) {
                i = 0
                while (i < Frames) {
                    Frame[i]!!.releasemD3DFrame(1)
                    i++
                }
                Frames = 0
                i = 0
                while (i < Meshs) {
                    Mesh[i]!!.releasemD3DMesh(1)
                    Meshs = 0
                    i++
                }
            }
        }

        constructor() {
            Frames = 0
            Meshs = 0
            Position = M3DVECTOR(0.0f, 0.0f, 0.0f)
            Orientation = M3DVECTOR(0.0f, 0.0f, 1.0f)
            Up = M3DVECTOR(0.0f, 1.0f, 0.0f)
            world = CreateWorldMatrix(Position, Orientation, Up)
        }

        constructor(Parent: mD3DFrame) {
            Frames = 0
            Meshs = 0
            Position = M3DVECTOR(0.0f, 0.0f, 0.0f)
            Orientation = M3DVECTOR(0.0f, 0.0f, 1.0f)
            Up = M3DVECTOR(0.0f, 1.0f, 0.0f)
            world = CreateWorldMatrix(Position, Orientation, Up)
            Parent.Frame[Parent.Frames] = this
            Parent.Frames++
        }
    }
    /** */
    /*************************** RENDER  */
    fun setTexture(stage: Int, TX: mD3DTexture?) {
        _Texture[stage] = TX
    }

     class _VRX {
        var x: Int
        var y: Int
        var z: Int
        var R: Int
        var G: Int
        var B: Int
        var A: Int
        var u: Int
        var v = 0

        init {
            u = v
            z = u
            y = z
            x = y
            A = 0
            B = A
            G = B
            R = G
        }
    }

    fun sgn(x: Float): Int {
        if (x < 0.0) {
            return -1
        } else if (x > 0.0) {
            return 1
        }
        return 0
    }

    fun sgn(x: Int): Int {
        if (x < 0) {
            return -1
        } else if (x > 0) {
            return 1
        }
        return 0
    }
    /** */
    /************************ ZSORT  */
    private fun _renderMesh_zsort(mesh: mD3DMesh?, world: M3DMATRIX, rendersubmesh: Int) {
        if (mesh!!.flags and MD3DMESHF_DISABLED == MD3DMESHF_DISABLED) {
            return
        }
        if (mesh.program <= 0) {
            return
        }
        val center = M3DVECTOR(0.0f, 0.0f, 0.0f)
        var transcenter: M3DVECTOR //transformed center in to world coor.
        transcenter = VxM(center, world)
        transcenter = VxM(transcenter, M3DMATRIX(viewMatrix))
        val zs = ZSORTstruct()
        zs.Z = transcenter.z
        zs.World = world
        zs.hMesh = mesh
        zsort!!.add(zs)
        /** */
        // if sub-meshes shall be rendered
        if (rendersubmesh == 1) {
            for (i in 0 until mesh.Meshs) {
                _renderMesh_zsort(mesh.Mesh[i], world, 1)
            }
        }
    }

    private fun _renderFromBuff_zsort() {
        if (zsort != null) {
            zsort!!.sort()
            var iterator: ListIterator<ZSORTstruct> = zsort!!.listIterator()
            while (iterator.hasNext()) {
                val zs = iterator.next()
                if (zs.hMesh!!.flags and MD3DMESHF_RENDEREDFIRST == MD3DMESHF_RENDEREDFIRST) {
                    _renderMesh(zs.hMesh, zs.World, 1)
                }
            }
            iterator = zsort!!.listIterator()
            while (iterator.hasNext()) {
                val zs = iterator.next()
                if (zs.hMesh!!.flags and MD3DMESHF_RENDEREDFIRST != MD3DMESHF_RENDEREDFIRST) {
                    _renderMesh(zs.hMesh, zs.World, 1)
                }
            }
        }
    }
    /** */
    /** */
    private fun _renderMesh(mesh: mD3DMesh?, world: M3DMATRIX?, rendersubmesh: Int) {
        if (mesh!!.flags and MD3DMESHF_DISABLED == MD3DMESHF_DISABLED) {
            return
        }
        if (mesh.program == -1) {
            return
        }
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        when {
            mesh.flags and MD3DMESHF_NOCULLING == MD3DMESHF_NOCULLING -> {
                GLES20.glDisable(GLES20.GL_CULL_FACE)
            }
            mesh.flags and MD3DMESHF_FRONTCULLING == MD3DMESHF_FRONTCULLING -> {
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glCullFace(GLES20.GL_FRONT)
            }
            else -> {
                GLES20.glEnable(GLES20.GL_CULL_FACE)
                GLES20.glCullFace(GLES20.GL_BACK)
            }
        }

        // Apply a ModelView Projection transformation
        val mMVPMatrix = FloatArray(16)
        val mMVMatrix = FloatArray(16)
        var mVMatrix = FloatArray(16)
        val mIMVMatrix = FloatArray(16) // inverse model view
        val mIMVPMatrix = FloatArray(16)
        mVMatrix = viewMatrix
        Matrix.multiplyMM(mMVMatrix, 0, viewMatrix, 0, world!!.values(), 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVMatrix, 0)
        Matrix.invertM(mIMVMatrix, 0, mMVMatrix, 0)
        Matrix.invertM(mIMVPMatrix, 0, mMVPMatrix, 0)
        if (mesh.mMVPMatrixPrev == null) {
            mesh.mMVPMatrixPrev = mMVPMatrix.clone()
        }
        val mMVPMatrixPrev = mesh.mMVPMatrixPrev!!.clone()
        mesh.mMVPMatrixPrev = mMVPMatrix.clone()

        // Add program to OpenGL environment
        if (!GLES20.glIsProgram(mesh.program)) {
            return
        }
        GLES20.glUseProgram(mesh.program)
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uMVPMatrix"),
            1,
            false,
            mMVPMatrix,
            0
        )
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uMVMatrix"),
            1,
            false,
            mMVMatrix,
            0
        )
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uVMatrix"),
            1,
            false,
            mVMatrix,
            0
        )
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uIMVMatrix"),
            1,
            false,
            mIMVMatrix,
            0
        )
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uIMVPMatrix"),
            1,
            false,
            mIMVPMatrix,
            0
        )
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mesh.program, "uMVPMatrixPrev"),
            1,
            false,
            mMVPMatrixPrev,
            0
        )

        // light vector transformation into camera space
        val mTransformedLightVector = FloatArray(4)
        Matrix.multiplyMV(mTransformedLightVector, 0, mVMatrix, 0, Light[0]!!.Pos.values(), 0)
        var i: Int
        /* ALPHA */
        /* TEXTURES */
        /* MANAGMENT */
        /** */
        if (mesh.VertexCount > 0 && mesh.VertexStruct and M3DFVF_XYZ == M3DFVF_XYZ) {
            i = 0

            // initialize vertex Buffer for triangle
            // TODO add possibility to have no texture U, V in vertex
            val vertexBuffer: FloatBuffer
            val vbb = ByteBuffer.allocateDirect(mesh.Vertex.size * 4)
            vbb.order(ByteOrder.nativeOrder()) // use the device hardware's native byte order
            vertexBuffer = vbb.asFloatBuffer() // create a floating point buffer from the ByteBuffer
            vertexBuffer.put(mesh.Vertex) // add the coordinates to the FloatBuffer
            var p = GLES20.glGetAttribLocation(mesh.program, "aPosition")
            if (p >= 0) {
                vertexBuffer.position(0)
                GLES20.glEnableVertexAttribArray(p)
                GLES20.glVertexAttribPointer(
                    p,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    mesh.VertexSize * 4,
                    vertexBuffer
                )
            }
            p = GLES20.glGetAttribLocation(mesh.program, "aNormal")
            if (p >= 0) {
                vertexBuffer.position(3)
                GLES20.glEnableVertexAttribArray(p)
                GLES20.glVertexAttribPointer(
                    p,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    mesh.VertexSize * 4,
                    vertexBuffer
                )
            }
            p = GLES20.glGetAttribLocation(mesh.program, "aTex")
            if (p >= 0) {
                vertexBuffer.position(6)
                GLES20.glEnableVertexAttribArray(p)
                GLES20.glVertexAttribPointer(
                    p,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    mesh.VertexSize * 4,
                    vertexBuffer
                )
            }
            p = GLES20.glGetAttribLocation(mesh.program, "aTangent")
            if (p >= 0) {
                vertexBuffer.position(8)
                GLES20.glEnableVertexAttribArray(p)
                GLES20.glVertexAttribPointer(
                    p,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    mesh.VertexSize * 4,
                    vertexBuffer
                )
            }
            p = GLES20.glGetAttribLocation(mesh.program, "aBitangent")
            if (p >= 0) {
                vertexBuffer.position(11)
                GLES20.glEnableVertexAttribArray(p)
                GLES20.glVertexAttribPointer(
                    p,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    mesh.VertexSize * 4,
                    vertexBuffer
                )
            }

            // Light
            if (N_Lights > 0) {
                GLES20.glUniform4f(
                    GLES20.glGetUniformLocation(mesh.program, "uLightPos"),
                    mTransformedLightVector[0],
                    mTransformedLightVector[1],
                    mTransformedLightVector[2],
                    mTransformedLightVector[3]
                )
                GLES20.glUniform4f(
                    GLES20.glGetUniformLocation(mesh.program, "uLightAmbientColor"),
                    Light[0]!!.AR,
                    Light[0]!!.AG,
                    Light[0]!!.AB,
                    Light[0]!!.AA
                )
                GLES20.glUniform4f(
                    GLES20.glGetUniformLocation(mesh.program, "uLightDiffuseColor"),
                    Light[0]!!.DR,
                    Light[0]!!.DG,
                    Light[0]!!.DB,
                    Light[0]!!.DA
                )
                GLES20.glUniform4f(
                    GLES20.glGetUniformLocation(mesh.program, "uLightSpecularColor"),
                    Light[0]!!.SR,
                    Light[0]!!.SG,
                    Light[0]!!.SB,
                    Light[0]!!.SA
                )
                GLES20.glUniform1f(
                    GLES20.glGetUniformLocation(mesh.program, "uLightAttenuation"),
                    Light[0]!!.AT
                )
            }

            // Material
            GLES20.glUniform4f(
                GLES20.glGetUniformLocation(mesh.program, "uMaterialAmbientColor"),
                mesh.Material.AR,
                mesh.Material.AG,
                mesh.Material.AB,
                mesh.Material.AA
            )
            GLES20.glUniform4f(
                GLES20.glGetUniformLocation(mesh.program, "uMaterialDiffuseColor"),
                mesh.Material.DR,
                mesh.Material.DG,
                mesh.Material.DB,
                mesh.Material.DA
            )
            GLES20.glUniform4f(
                GLES20.glGetUniformLocation(mesh.program, "uMaterialSpecularColor"),
                mesh.Material.SR,
                mesh.Material.SG,
                mesh.Material.SB,
                mesh.Material.SA
            )
            GLES20.glUniform4f(
                GLES20.glGetUniformLocation(mesh.program, "uMaterialEmissiveColor"),
                mesh.Material.ER,
                mesh.Material.EG,
                mesh.Material.EB,
                mesh.Material.EA
            )
            GLES20.glUniform1f(
                GLES20.glGetUniformLocation(mesh.program, "uMaterialShinnes"),
                mesh.Material.SH
            )

            // Bump level
            val bumpLevel = mesh.bumpLevel / 100.0f
            GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.program, "uBumpLevel"), bumpLevel)

            // Custom attributes
            for ((key, value) in mesh.customAttributes) {
                GLES20.glUniform1f(GLES20.glGetUniformLocation(mesh.program, key), value)
            }

            // Texture mapping
            val texMapping = 0
            GLES20.glUniform1i(GLES20.glGetUniformLocation(mesh.program, "uTexMapping"), texMapping)

            // textures
            // TODO add multiple textures
            when (mesh.Textures) {
                1 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                }
                2 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 2)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        2,
                        intBuffer
                    )
                }
                3 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1, 2)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 3)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        3,
                        intBuffer
                    )
                }
                4 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1, 2, 3)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 4)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        4,
                        intBuffer
                    )
                }
                5 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE4)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1, 2, 3, 4)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 5)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        5,
                        intBuffer
                    )
                }
                6 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE4)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE5)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[5]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1, 2, 3, 4, 5)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 6)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        6,
                        intBuffer
                    )
                }
                7 -> {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[0]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[1]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[2]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[3]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE4)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[4]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE5)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[5]!!.id)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE6)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.Texture[6]!!.id)

                    // The {0,1} correspond to the activated textures units.
                    val textureUnits = intArrayOf(0, 1, 2, 3, 4, 5, 6)
                    val intBuffer = IntBuffer.wrap(textureUnits, 0, 7)
                    GLES20.glUniform1iv(
                        GLES20.glGetUniformLocation(mesh.program, "uTextures"),
                        7,
                        intBuffer
                    )
                }
            }
            val mNumOfTriangleIndices = mesh.Data.size
            val ibb = ByteBuffer.allocateDirect(mesh.Data.size * 2)
            ibb.order(ByteOrder.nativeOrder())
            val indexBuffer = ibb.asShortBuffer()
            indexBuffer.put(mesh.Data)
            indexBuffer.position(0)
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLE_STRIP,
                mNumOfTriangleIndices,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer
            )
        }
        GLES20.glUseProgram(0)
        /** */
        // if sub-meshes shall be rendered
        if (rendersubmesh == 1) {
            i = 0
            while (i < mesh.Meshs) {
                _renderMesh(mesh.Mesh[i], world, 1)
                i++
            }
        }
    }
    /** */
    /** */
    private fun _renderFrame(frame: mD3DFrame?, world_up: M3DMATRIX) {
        val actual = MUL(frame!!.world, world_up)
        var i = 0
        while (i < frame.Meshs) {
            if (ZSORT == 1) {
                _renderMesh_zsort(frame.Mesh[i], actual, 1)
            } else {
                _renderMesh(frame.Mesh[i], actual, 1)
            }
            i++
        }
        i = 0
        while (i < frame.Frames) {
            _renderFrame(frame.Frame[i], actual)
            i++
        }
    }

    fun renderFrame(frame: mD3DFrame?) {
        Matrix.setLookAtM(
            viewMatrix,
            0,
            CameraPosition!!.x,
            CameraPosition!!.y,
            CameraPosition!!.z,
            CameraPosition!!.x + CameraOrientation!!.x,
            CameraPosition!!.y + CameraOrientation!!.y,
            CameraPosition!!.z + CameraOrientation!!.z,
            CameraUp!!.x,
            CameraUp!!.y,
            CameraUp!!.z
        )
        if (ZSORT == 1) {
            zsort!!.clear()
        }
        _renderFrame(frame, IdentityMatrix())
        if (ZSORT == 1) {
            _renderFromBuff_zsort()
        }
    }

    // Method calculates the x, y position on screen into line in 3D space. Set as output POINT and VECT
    fun getLinefPixel(POINT: M3DVECTOR, VECT: M3DVECTOR, Xx: Float, Yy: Float) {
        var right = CrossProduct(CameraOrientation!!, CameraUp!!)
        right = Normalize(right)
        var up = CrossProduct(right, CameraOrientation!!)
        // assumption is that camera vector is normalized
        // up=Normalize(up);
        var or = CameraOrientation
        // M3DVECTOR or=Normalize(CameraOrientation);
        val w: Float
        val h: Float
        val WIDTH: Float = P_NPlane * Math.tan((P_fov_horiz / 2.0f).toDouble()).toFloat()
        val HEIGHT: Float = P_NPlane * Math.tan((P_fov_vert / 2.0f).toDouble()).toFloat()
        w = WIDTH * (Xx / SCREEN_WIDTH.toFloat() * 2.0f - 1.0f)
        h = HEIGHT * ((SCREEN_HEIGHT - Yy) / SCREEN_HEIGHT.toFloat() * 2.0f - 1.0f)
        right = MUL(right, w)
        up = MUL(up, h)
        or = MUL(or!!, P_NPlane)
        val dir: M3DVECTOR
        val f: M3DVECTOR = ADD(ADD(ADD(CameraPosition!!, or), up), right)
        dir = DIF(f, CameraPosition!!)
        POINT.set(f)
        VECT.set(dir)
    }

    companion object {
        const val TAG = "M3DM"
        const val PI = 3.1415926535f

        /************************ FLAGS  */ // TODO some parameters are legacy
        const val MAXMESHS = 20 // max # of meshs in one mesh or frame
        const val MAXFRAMES = 50 // max # of frames in one frame
        const val MAXNFACE = 50 // max # of vertex per face
        const val MAXNTX = 500 // max # of textures in MOTextureManagment
        const val MAXOBJ = 100 // max # of objects in the scene - pre Z sort
        const val MAXBOX = 100 // max # of boxes in the mesh - for collision

        // detect AABB
        const val NAMELENGHT = 16 // lenght of Name of the Mesh in chars

        /* FLAGS for Mesh */
        const val MD3DMESHF_SPHERMAP = 1 // mesh has env. mapping
        const val MD3DMESHF_DISABLED = 2 // mesh is not rendered
        const val MD3DMESHF_NOCULLING = 16 // mesh no culling
        const val MD3DMESHF_FRONTCULLING = 32 // mesh front culling
        const val MD3DMESHF_RENDEREDFIRST = 64 // mesh render first if zsort

        /* FLAGS PRE FLEXIBLE VERTEX FORMAT */
        const val M3DFVF_XYZ = 0x00000001
        const val M3DFVF_NORMAL = 0x00000002
        const val M3DFVF_COLOR = 0x00000004
        const val M3DFVF_TEX1 = 0x00000008
        const val M3DFVF_TAN = 0x00000010
        const val M3DFVF_BITAN = 0x00000020
        /*************** OBJECTS  */
        /****************** SPHERE  */
        fun createSphere(
            R: Float,
            StepRov: Int,
            StepPol: Int,
            U1: Float,
            V1: Float,
            U2: Float,
            V2: Float
        ): mD3DMesh {
            val mesh: mD3DMesh
            val xU = U2 - U1
            val xV = V2 - V1
            val VNum = (1 + 180 / StepRov) * (1 + 360 / StepPol) + 1
            val vertex = arrayOfNulls<M3DVERTEX>(VNum)
            for (t in 0 until VNum) {
                vertex[t] = M3DVERTEX()
            }
            val data = ShortArray(VNum * 5)
            for (t in 0 until VNum * 5) {
                data[t] = 0
            }
            val NRovnobeziek = 180 / StepRov + 1
            val NPoludnikov = 360 / StepPol + 1
            var be: Float
            var i: Int
            var count = 0
            val temp = M3DVECTOR(0.0f, 1.0f, 0.0f)
            var T = M3DVECTOR(0.0f, 0.0f, 0.0f)
            var axis = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val t_axis = M3DVECTOR(
                1.0f, 0.0f, 0.0f
            )
            var al = 0f
            while (al <= 360) {
                axis = POINTROTATE(
                    t_axis,
                    M3DVECTOR(0.0f, 0.0f, 0.0f),
                    M3DVECTOR(0.0f, 1.0f, 0.0f),
                    (al * (PI / 180.0f))
                )
                be = 0f
                while (be <= 180) {
                    T = POINTROTATE(temp, M3DVECTOR(0.0f, 0.0f, 0.0f), axis, (be * (PI / 180.0f)))
                    vertex[count]!!.P.x = T.x * R
                    vertex[count]!!.P.y = T.y * R
                    vertex[count]!!.P.z = T.z * R
                    vertex[count]!!.N.x = T.x
                    vertex[count]!!.N.y = T.y
                    vertex[count]!!.N.z = T.z
                    vertex[count]!!.u = U1 + al / 360.0f * xU
                    vertex[count]!!.v = V1 + be / 180.0f * xV
                    count++
                    be += StepRov.toFloat()
                }
                al += StepPol.toFloat()
            }
            count = 0
            var j = 0
            while (j < NPoludnikov - 1) {
                i = 0
                while (i < NRovnobeziek - 1) {
                    data[count + 0] = (i + (j + 1) * NRovnobeziek).toShort()
                    data[count + 1] = (i + j * NRovnobeziek).toShort()
                    count += 2
                    if (i == NRovnobeziek - 2) {
                        data[count + 0] = (i + (NPoludnikov - 2) * NRovnobeziek).toShort()
                        data[count + 1] = (i + (NPoludnikov - 2) * NRovnobeziek).toShort()
                        count += 2
                    }
                    i++
                }
                j++
            }
            mesh = mD3DMesh(vertex, data, count)
            return mesh
        }
        /****************** SPHERE  */
        /****************** SPHERE  */
        fun createEllipsoid(
            R: Float,
            V: Float,
            StepRov: Int,
            StepPol: Int,
            U1: Float,
            V1: Float,
            U2: Float,
            V2: Float
        ): mD3DMesh {
            val mesh: mD3DMesh
            val xU = U2 - U1
            val xV = V2 - V1
            val VNum = (1 + 180 / StepRov) * (1 + 360 / StepPol) + 1
            val vertex = arrayOfNulls<M3DVERTEX>(VNum)
            for (t in 0 until VNum) {
                vertex[t] = M3DVERTEX()
            }
            val data = ShortArray(VNum * 5)
            for (t in 0 until VNum * 5) {
                data[t] = 0
            }
            val NRovnobeziek = 180 / StepRov + 1
            val NPoludnikov = 360 / StepPol + 1
            var be: Float
            var i: Int
            var count = 0
            val temp = M3DVECTOR(0.0f, 1.0f, 0.0f)
            var T = M3DVECTOR(0.0f, 0.0f, 0.0f)
            var axis = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val t_axis = M3DVECTOR(
                1.0f, 0.0f, 0.0f
            )
            var al = 0f
            while (al <= 360) {
                axis = POINTROTATE(
                    t_axis,
                    M3DVECTOR(0.0f, 0.0f, 0.0f),
                    M3DVECTOR(0.0f, 1.0f, 0.0f),
                    (al * (PI / 180.0f))
                )
                be = 0f
                while (be <= 180) {
                    T = POINTROTATE(temp, M3DVECTOR(0.0f, 0.0f, 0.0f), axis, (be * (PI / 180.0f)))
                    vertex[count]!!.P.x = T.x * R
                    vertex[count]!!.P.y = T.y * V
                    vertex[count]!!.P.z = T.z * R
                    vertex[count]!!.N.x = vertex[count]!!.P.x
                    vertex[count]!!.N.y = vertex[count]!!.P.y
                    vertex[count]!!.N.z = vertex[count]!!.P.z
                    vertex[count]!!.N = Normalize(vertex[count]!!.N)
                    val u = U1.toDouble() + al / 360.0f * xU
                    val v = V1.toDouble() + be / 180.0f * xV
                    vertex[count]!!.u = u.toFloat()
                    vertex[count]!!.v = v.toFloat()
                    count++
                    be += StepRov.toFloat()
                }
                al += StepPol.toFloat()
            }
            count = 0
            i = 0
            var j = 0
            while (j < NPoludnikov - 1) {
                i = 0
                while (i < NRovnobeziek - 1) {
                    data[count + 0] = (i + (j + 1) * NRovnobeziek).toShort()
                    data[count + 1] = (i + j * NRovnobeziek).toShort()
                    count += 2
                    if (i == NRovnobeziek - 2) {
                        data[count + 0] = (i + (NPoludnikov - 2) * NRovnobeziek).toShort()
                        data[count + 1] = (i + (NPoludnikov - 2) * NRovnobeziek).toShort()
                        count += 2
                    }
                    i++
                }
                j++
            }
            mesh = mD3DMesh(vertex, data, count)
            return mesh
        }

        /****************** ELLIPSOID  */
        fun createCube(a: Float): mD3DMesh {
            val mesh: mD3DMesh
            val vertex = arrayOfNulls<M3DVERTEX>(24)
            val data = ShortArray(4) //short[31];

            // front
            vertex[0] = M3DVERTEX()
            vertex[0]!!.P.x = -a
            vertex[0]!!.P.y = -a
            vertex[0]!!.P.z = a
            vertex[0]!!.u = 0.0f
            vertex[0]!!.v = 1.0f
            vertex[1] = M3DVERTEX()
            vertex[1]!!.P.x = -a
            vertex[1]!!.P.y = a
            vertex[1]!!.P.z = a
            vertex[1]!!.u = 0.0f
            vertex[1]!!.v = 0.0f
            vertex[2] = M3DVERTEX()
            vertex[2]!!.P.x = a
            vertex[2]!!.P.y = a
            vertex[2]!!.P.z = a
            vertex[2]!!.u = 1.0f
            vertex[2]!!.v = 0.0f
            vertex[3] = M3DVERTEX()
            vertex[3]!!.P.x = a
            vertex[3]!!.P.y = -a
            vertex[3]!!.P.z = a
            vertex[3]!!.u = 1.0f
            vertex[3]!!.v = 1.0f

            // right
            vertex[4] = M3DVERTEX()
            vertex[4]!!.P.x = a
            vertex[4]!!.P.y = -a
            vertex[4]!!.P.z = a
            vertex[4]!!.u = 0.0f
            vertex[4]!!.v = 1.0f
            vertex[5] = M3DVERTEX()
            vertex[5]!!.P.x = a
            vertex[5]!!.P.y = a
            vertex[5]!!.P.z = a
            vertex[5]!!.u = 0.0f
            vertex[5]!!.v = 0.0f
            vertex[6] = M3DVERTEX()
            vertex[6]!!.P.x = a
            vertex[6]!!.P.y = a
            vertex[6]!!.P.z = -a
            vertex[6]!!.u = 1.0f
            vertex[6]!!.v = 0.0f
            vertex[7] = M3DVERTEX()
            vertex[7]!!.P.x = a
            vertex[7]!!.P.y = -a
            vertex[7]!!.P.z = -a
            vertex[7]!!.u = 1.0f
            vertex[7]!!.v = 1.0f

            // back
            vertex[8] = M3DVERTEX()
            vertex[8]!!.P.x = a
            vertex[8]!!.P.y = -a
            vertex[8]!!.P.z = -a
            vertex[8]!!.u = 0.0f
            vertex[8]!!.v = 1.0f
            vertex[9] = M3DVERTEX()
            vertex[9]!!.P.x = a
            vertex[9]!!.P.y = a
            vertex[9]!!.P.z = -a
            vertex[9]!!.u = 0.0f
            vertex[9]!!.v = 0.0f
            vertex[10] = M3DVERTEX()
            vertex[10]!!.P.x = -a
            vertex[10]!!.P.y = a
            vertex[10]!!.P.z = -a
            vertex[10]!!.u = 1.0f
            vertex[10]!!.v = 0.0f
            vertex[11] = M3DVERTEX()
            vertex[11]!!.P.x = -a
            vertex[11]!!.P.y = -a
            vertex[11]!!.P.z = -a
            vertex[11]!!.u = 1.0f
            vertex[11]!!.v = 1.0f

            // left
            vertex[12] = M3DVERTEX()
            vertex[12]!!.P.x = -a
            vertex[12]!!.P.y = -a
            vertex[12]!!.P.z = -a
            vertex[12]!!.u = 0.0f
            vertex[12]!!.v = 1.0f
            vertex[13] = M3DVERTEX()
            vertex[13]!!.P.x = -a
            vertex[13]!!.P.y = a
            vertex[13]!!.P.z = -a
            vertex[13]!!.u = 0.0f
            vertex[13]!!.v = 0.0f
            vertex[14] = M3DVERTEX()
            vertex[14]!!.P.x = -a
            vertex[14]!!.P.y = a
            vertex[14]!!.P.z = a
            vertex[14]!!.u = 1.0f
            vertex[14]!!.v = 0.0f
            vertex[15] = M3DVERTEX()
            vertex[15]!!.P.x = -a
            vertex[15]!!.P.y = -a
            vertex[15]!!.P.z = a
            vertex[15]!!.u = 1.0f
            vertex[15]!!.v = 1.0f

            // top
            vertex[16] = M3DVERTEX()
            vertex[16]!!.P.x = -a
            vertex[16]!!.P.y = a
            vertex[16]!!.P.z = a
            vertex[16]!!.u = 0.0f
            vertex[16]!!.v = 1.0f
            vertex[17] = M3DVERTEX()
            vertex[17]!!.P.x = -a
            vertex[17]!!.P.y = a
            vertex[17]!!.P.z = -a
            vertex[17]!!.u = 0.0f
            vertex[17]!!.v = 0.0f
            vertex[18] = M3DVERTEX()
            vertex[18]!!.P.x = a
            vertex[18]!!.P.y = a
            vertex[18]!!.P.z = -a
            vertex[18]!!.u = 1.0f
            vertex[18]!!.v = 0.0f
            vertex[19] = M3DVERTEX()
            vertex[19]!!.P.x = a
            vertex[19]!!.P.y = a
            vertex[19]!!.P.z = a
            vertex[19]!!.u = 1.0f
            vertex[19]!!.v = 1.0f

            // bottom
            vertex[20] = M3DVERTEX()
            vertex[20]!!.P.x = -a
            vertex[20]!!.P.y = -a
            vertex[20]!!.P.z = -a
            vertex[20]!!.u = 1.0f
            vertex[20]!!.v = 0.0f
            vertex[21] = M3DVERTEX()
            vertex[21]!!.P.x = -a
            vertex[21]!!.P.y = -a
            vertex[21]!!.P.z = a
            vertex[21]!!.u = 1.0f
            vertex[21]!!.v = 1.0f
            vertex[22] = M3DVERTEX()
            vertex[22]!!.P.x = a
            vertex[22]!!.P.y = -a
            vertex[22]!!.P.z = a
            vertex[22]!!.u = 0.0f
            vertex[22]!!.v = 1.0f
            vertex[23] = M3DVERTEX()
            vertex[23]!!.P.x = a
            vertex[23]!!.P.y = -a
            vertex[23]!!.P.z = -a
            vertex[23]!!.u = 0.0f
            vertex[23]!!.v = 0.0f
            data[0] = 8
            data[1] = 9
            data[2] = 11
            data[3] = 10
            for (i in vertex.indices) {
                vertex[i]!!.N = Normalize(vertex[i]!!.P)
            }
            mesh = mD3DMesh(vertex, data, 4)
            return mesh
        }

        fun createRingOneSidedTop(R1: Float, R2: Float, Step: Int): mD3DMesh {
            val mesh: mD3DMesh
            val VNum = 2 * (360 / Step) + 2
            val vertex = arrayOfNulls<M3DVERTEX>(VNum)
            for (t in 0 until VNum) {
                vertex[t] = M3DVERTEX()
            }
            val data = ShortArray(VNum * 5)
            for (t in 0 until VNum * 5) {
                data[t] = 0
            }
            var al: Float
            var be: Float
            var i: Int
            var j: Int
            var count = 0
            val temp = M3DVECTOR(0.0f, 1.0f, 0.0f)
            var T = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val axis = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val t_axis = M3DVECTOR(1.0f, 0.0f, 0.0f)
            al = 0f
            while (al <= 360) {
                T = POINTROTATE(
                    t_axis,
                    M3DVECTOR(0.0f, 0.0f, 0.0f),
                    M3DVECTOR(0.0f, 1.0f, 0.0f),
                    (al * (PI / 180.0f))
                )
                vertex[count]!!.P.x = T.x * R1
                vertex[count]!!.P.y = 0.0f
                vertex[count]!!.P.z = T.z * R1
                vertex[count]!!.N.x = 0.0f
                vertex[count]!!.N.y = 1.0f
                vertex[count]!!.N.z = 0.0f
                vertex[count]!!.N = Normalize(vertex[count]!!.N)
                vertex[count]!!.u = 0.0f
                vertex[count]!!.v = 0.0f
                count++
                vertex[count]!!.P.x = T.x * R2
                vertex[count]!!.P.y = 0.0f
                vertex[count]!!.P.z = T.z * R2
                vertex[count]!!.N.x = 0.0f
                vertex[count]!!.N.y = 1.0f
                vertex[count]!!.N.z = 0.0f
                vertex[count]!!.N = Normalize(vertex[count]!!.N)
                vertex[count]!!.u = 1.0f
                vertex[count]!!.v = 0.0f
                count++
                al += Step.toFloat()
            }
            count = 0
            al = 0f
            while (al <= 360 - Step) {
                data[count + 3] = ((count / 4) * 2 + 1).toShort()
                data[count + 1] = ((count / 4) * 2 + 0).toShort()
                data[count + 2] = ((count / 4) * 2 + 2).toShort()
                data[count + 0] = ((count / 4) * 2 + 3).toShort()
                count += 4
                al += Step.toFloat()
            }
            mesh = mD3DMesh(vertex, data, count)
            return mesh
        }

        fun createRingOneSidedBottom(R1: Float, R2: Float, Step: Int): mD3DMesh {
            val mesh: mD3DMesh
            val VNum = 2 * (360 / Step) + 2
            val vertex = arrayOfNulls<M3DVERTEX>(VNum)
            for (t in 0 until VNum) {
                vertex[t] = M3DVERTEX()
            }
            val data = ShortArray(VNum * 5)
            for (t in 0 until VNum * 5) {
                data[t] = 0
            }
            var al: Float
            var be: Float
            var i: Int
            var j: Int
            var count = 0
            val temp = M3DVECTOR(0.0f, 1.0f, 0.0f)
            var T = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val axis = M3DVECTOR(0.0f, 0.0f, 0.0f)
            val t_axis = M3DVECTOR(1.0f, 0.0f, 0.0f)
            al = 0f
            while (al <= 360) {
                T = POINTROTATE(
                    t_axis,
                    M3DVECTOR(0.0f, 0.0f, 0.0f),
                    M3DVECTOR(0.0f, 1.0f, 0.0f),
                    (al * (PI / 180.0f))
                )
                vertex[count]!!.P.x = T.x * R1
                vertex[count]!!.P.y = 0.0f
                vertex[count]!!.P.z = T.z * R1
                vertex[count]!!.N.x = 0.0f
                vertex[count]!!.N.y = -1.0f
                vertex[count]!!.N.z = 0.0f
                vertex[count]!!.N = Normalize(vertex[count]!!.N)
                vertex[count]!!.u = 0.0f
                vertex[count]!!.v = 0.0f
                count++
                vertex[count]!!.P.x = T.x * R2
                vertex[count]!!.P.y = 0.0f
                vertex[count]!!.P.z = T.z * R2
                vertex[count]!!.N.x = 0.0f
                vertex[count]!!.N.y = -1.0f
                vertex[count]!!.N.z = 0.0f
                vertex[count]!!.N = Normalize(vertex[count]!!.N)
                vertex[count]!!.u = 1.0f
                vertex[count]!!.v = 0.0f
                count++
                al += Step.toFloat()
            }
            count = 0
            al = 0f
            while (al <= 360 - Step) {
                data[count + 3] = ((count / 4) * 2 + 0).toShort()
                data[count + 1] = ((count / 4) * 2 + 1).toShort()
                data[count + 2] = ((count / 4) * 2 + 3).toShort()
                data[count + 0] = ((count / 4) * 2 + 2).toShort()
                count += 4
                al += Step.toFloat()
            }
            mesh = mD3DMesh(vertex, data, count)
            return mesh
        }
    }
}