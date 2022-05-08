package com.aemerse.earth_viewer

object Shaders {
    var p_meteosat_0 = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_meteosat_0 = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " 	vTexM[0] = 1.0 - (vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(vPosition.x*vPosition.x + vPosition.y*vPosition.y); \n" +
            "   float sx = sqrt(vPosition.x*vPosition.x); \n" +
            "   float sy = sqrt(vPosition.y*vPosition.y); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
            " 	vTexM[0] = -0.1 + (1.0 + 0.2 )*vTexM[0]; \n" +
            " 	vTexM[1] = -0.09 + (1.0 + 0.18 )*vTexM[1]; \n" +
            "}                         \n"
    const val fsc_meteosat_0 = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " if (vPosition.y < -0.92) { \n" +
            " 	_vTexM[1] =  0.05 + (0.92 - (vPosition.y+0.92)) ; \n" +
            " 	_vTexM[0] =  1.5*(-0.37 + (1.0 - (vPosition.x+1.0)/2.0)) ; \n" +
            " } \n" +
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.3 + 0.7*cm; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.0 + 1.0*cmS; \n" +
            " if (vPosition.z > 0.0) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +  //" a = pow(abs(vMVNormal.z), 0.5); \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_meteosat_0_hd = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_meteosat_0_hd = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " 	vTexM[0] = 1.0 - (vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(vPosition.x*vPosition.x + vPosition.y*vPosition.y); \n" +
            "   float sx = sqrt(vPosition.x*vPosition.x); \n" +
            "   float sy = sqrt(vPosition.y*vPosition.y); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.16*pow(s,1.8)); \n" +
            " 	vTexM[0] = -0.1 + (1.0 + 0.2 )*vTexM[0]; \n" +
            " 	vTexM[1] = -0.09 + (1.0 + 0.18 )*vTexM[1]; \n" +
            "}                         \n"
    const val fsc_meteosat_0_hd = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " if (vPosition.y < -0.92) { \n" +
            " 	_vTexM[1] =  0.05 + (0.92 - (vPosition.y+0.92)) ; \n" +
            " 	_vTexM[0] =  0.3*(-0.37 + (1.0 - (vPosition.x+1.0)/2.0)) ; \n" +
            " } \n" +
            " vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.3 + 0.7*cm; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.0 + 1.0*cmS; \n" +
            " if (vPosition.z > 0.0) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_meteosat_iodc = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_meteosat_iodc = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec4 _vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " mat4 m = mat4( \n" +
            " cos(3.1415*(41.50/180.0)), 0.0, sin(3.1415*(57.5/180.0)), 0.0, \n" +  // first column
            " 0.0, 1.0, 0.0, 0.0, \n" +  // second column
            " -sin(3.1415*(41.50/180.0)), 0.0, cos(3.1415*(57.5/180.0)), 0.0, \n" +  // third column
            " 0.0, 0.0, 0.0, 1.0  \n" +  // forth column
            " ); \n" +
            "_vPosition = m*aPosition;	\n" +
            " 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
            "   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
            "   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
            " 	vTexM[0] = 0.006 + vTexM[0]; \n" +
            " 	vTexM[1] = 0.0025 + vTexM[1]; \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 + 0.15); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.21); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.125*pow(s,2.1)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.165*pow(s,1.85)); \n" +
            "}                         \n"
    const val fsc_meteosat_iodc = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec4 _vPosition;	\n" +
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " if (_vPosition.y < -0.92) { \n" +
            " 	_vTexM[1] =  0.05 + (0.92 - (_vPosition.y+0.92)) ; \n" +
            " 	_vTexM[0] =  1.5*(-0.37 + (1.0 - (_vPosition.x+1.0)/2.0)) ; \n" +
            " } \n" +
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.3 + 0.7*cm; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.0 + 1.0*cmS; \n" +
            " if (_vPosition.z > 0.0) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_xplanet = 0
    var vsc_xplanet = """uniform mat4 uMVPMatrix;   
uniform mat4 uMVMatrix;   	
uniform mat4 uVMatrix;   	
varying mat4 vMVMatrix;   	
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
    var fsc_xplanet = """precision mediump float;  	
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
 cm = 0.0 + 1.0*cm; 
 vec4 cmT = vec4(1.0,1.0,1.0,1.0); 
 float cmS = clamp((texture2D(uTextures[2], vTex  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0); 
 cmS = 0.0 + 0.9*cmS; 
 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; 
		gl_FragColor = 1.0*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) 						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS 						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   
	 diffuse = clamp(diffuse, 0.0, 1.0); 
    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   
 float a; 
 a = abs(vMVNormal.z); 
 gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 4.0); 
 gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 0.5); 
 gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 30.0); 
 gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 gl_FragColor.a = 1.0;	
 if (abs(vMVNormal.z) < 0.55) { 
 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	
 }; 
}   
"""
    var p_ssec_water = 0
    var vsc_ssec_water = """uniform mat4 uMVPMatrix;   
uniform mat4 uMVMatrix;   	
uniform mat4 uVMatrix;   	
varying mat4 vMVMatrix;   	
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
    var fsc_ssec_water = """precision mediump float;  	
varying vec3 vLightPos;  	
varying vec3 vPosition;	
varying vec3 lightVector;	
uniform vec4 uColor;  		
varying vec3 vNormal;   	
varying vec3 vMVNormal;	
varying vec2 vTex;			
uniform sampler2D uTextures[7];
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
uniform float uTW1;					
uniform float uTW2;					
uniform float uTW3;					
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
	 vec2 vTexM; 
 	float psi = 3.14159265*(vTex[1] - 0.5); 
 	for (int i = 0; i < 4; i++) { 
 		psi = psi - (2.0*psi + sin(2.0*psi) - 3.14159265*sin(vTex[1]*3.14159265 - 3.14159265*0.5))/(2.0 + 2.0*cos(2.0*psi)); 
 	} 
 	vTexM[0] = (2.0*vTex[0] - 1.0)*cos(psi); 
 	vTexM[1] = sin(psi); 
 	vTexM[0] = 0.5 + 0.5*vTexM[0]; 
 	vTexM[1] = 0.5 + 0.5*vTexM[1]; 
 	vTexM[0] = 0.015 + (1.0 - 0.015 - 0.015)*vTexM[0]; 
 	vTexM[1] = 0.0525 + (1.0 - 0.055 - 0.05)*vTexM[1]; 
 float min = 0.0; 
 float max = 1.0; 
 float cm = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) + 			 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM).g - min)/(max-min), 0.0, 1.0))) + 			 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))); 
 cm = 0.0 + 1.2*cm; 
 vec4 cmT = vec4(0.8,0.95,1.0,1.0); 
 float cmS = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) +			  0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) +           0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))); 
 cmS = 0.0 + 1.0*cmS; 
 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; 
		gl_FragColor = 0.95*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) 						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS 						+ 0.5*clamp(1.0 - 0.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   
	 diffuse = clamp(diffuse, 0.0, 1.0); 
    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   
 float a; 
 a = abs(vMVNormal.z); 
 gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5)*(1.0 - 1.0*cm), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 4.0); 
 gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 0.5); 
 gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 30.0); 
 gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 gl_FragColor.a = 1.0;	
 if (abs(vMVNormal.z) < 0.55) { 
 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	
 }; 
}   
"""
    var p_ssec_ir = 0
    var vsc_ssec_ir = """uniform mat4 uMVPMatrix;   
uniform mat4 uMVMatrix;   	
uniform mat4 uVMatrix;   	
varying mat4 vMVMatrix;   	
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
    var fsc_ssec_ir = """precision mediump float;  	
varying vec3 vLightPos;  	
varying vec3 vPosition;	
varying vec3 lightVector;	
uniform vec4 uColor;  		
varying vec3 vNormal;   	
varying vec3 vMVNormal;	
varying vec2 vTex;			
uniform sampler2D uTextures[7];
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
uniform float uTW1;					
uniform float uTW2;					
uniform float uTW3;					
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
	 vec2 vTexM; 
 	float psi = 3.14159265*(vTex[1] - 0.5); 
 	for (int i = 0; i < 4; i++) { 
 		psi = psi - (2.0*psi + sin(2.0*psi) - 3.14159265*sin(vTex[1]*3.14159265 - 3.14159265*0.5))/(2.0 + 2.0*cos(2.0*psi)); 
 	} 
 	vTexM[0] = (2.0*vTex[0] - 1.0)*cos(psi); 
 	vTexM[1] = sin(psi); 
 	vTexM[0] = 0.5 + 0.5*vTexM[0]; 
 	vTexM[1] = 0.5 + 0.5*vTexM[1]; 
 	vTexM[0] = 0.015 + (1.0 - 0.015 - 0.015)*vTexM[0]; 
 	vTexM[1] = 0.0525 + (1.0 - 0.055 - 0.05)*vTexM[1]; 
 float min = 0.25; 
 float max = 0.9; 
 float cm = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) 			+ 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM).g - min)/(max-min), 0.0, 1.0))) 			+ 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM).g - min)/(max-min), 0.0, 1.0) 						+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM).g - min)/(max-min), 0.0, 1.0))) ; 
 cm = 0.0 + 1.25*cm; 
 vec4 cmT = vec4(1.0,1.0,1.0,1.0); 
 float cmS = 0.05*(uTW3*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW3)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0)))			 + 0.475*(uTW1*clamp((texture2D(uTextures[2], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW1)*(clamp((texture2D(uTextures[6], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0)))           + 0.475*(uTW2*clamp((texture2D(uTextures[4], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0) 		+ (1.0 - uTW2)*(clamp((texture2D(uTextures[5], vTexM  + 0.0005*shiftUV).g - min)/(max-min), 0.0, 1.0))) ; 
 cmS = 0.0 + 1.0*cmS; 
 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; 
		gl_FragColor = 1.0*cm*cmT*( 0.15 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) 						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS 						+ clamp(1.0 - 1.0*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   
	 diffuse = clamp(diffuse, 0.0, 1.0); 
    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   
 float a; 
 a = abs(vMVNormal.z); 
 gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 4.0); 
 gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 0.5); 
 gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 a = pow(abs(1.0 - vMVNormal.z), 30.0); 
 gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	
 gl_FragColor.a = 1.0;	
 if (abs(vMVNormal.z) < 0.55) { 
 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	
 }; 
}   
"""
    var p_goes_east = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_goes_east = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec4 _vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " mat4 m = mat4( \n" +
            " cos(3.1415*(-75.324/180.0)), 0.0, sin(3.1415*(-75.324/180.0)), 0.0, \n" +  // first column 
            " 0.0, 1.0, 0.0, 0.0, \n" +  // second column
            " -sin(3.1415*(-75.324/180.0)), 0.0, cos(3.1415*(-75.324/180.0)), 0.0, \n" +  // third column
            " 0.0, 0.0, 0.0, 1.0  \n" +  // forth column
            " ); \n" +
            "_vPosition = m*aPosition;	\n" +
            " 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
            "   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
            "   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
            " 	vTexM[0] = -0.048 + vTexM[0]; \n" +
            " 	vTexM[1] = -0.015 + vTexM[1]; \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.15); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.05); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.1*pow(s,5.0)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.20*pow(s,5.0)); \n" +
            "}                         \n"
    const val fsc_goes_east = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec4 _vPosition;	\n" +
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " vec4 cmT = vec4(0.75,0.75,0.75,1.0); \n" +
            " vec4 cmTS = vec4(0.75,0.75,0.75,1.0); \n" +
            " float cm = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM).g) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM).g) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM).g) ) "
            + "; \n" +
            " cm = clamp(-0.3 + cm/0.7, 0.0, 1.0); \n" +
            " float cmS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV).g) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
            + "; \n" +
            " cmS = clamp(-0.3 + cmS/0.7, 0.0, 1.0); \n" +
            " if (_vPosition.z > 0.0) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 2.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_goes_west = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_goes_west = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec4 _vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " mat4 m = mat4( \n" +
            " cos(3.1415*(-135.0/180.0)), 0.0, sin(3.1415*(-135.0/180.0)), 0.0, \n" +  // first column 
            " 0.0, 1.0, 0.0, 0.0, \n" +  // second column
            " -sin(3.1415*(-135.0/180.0)), 0.0, cos(3.1415*(-135.0/180.0)), 0.0, \n" +  // third column
            " 0.0, 0.0, 0.0, 1.0  \n" +  // forth column
            " ); \n" +
            "_vPosition = m*aPosition;	\n" +
            " 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
            "   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
            "   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
            " 	vTexM[0] = -0.048 + vTexM[0]; \n" +
            " 	vTexM[1] = -0.035 + vTexM[1]; \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.15); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.05); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.1*pow(s,9.0)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.20*pow(s,5.0)); \n" +
            "}                         \n"
    var p_mtsat = 0

    /* OpenGL */ // TODO only for 1 light and texture required
    const val vsc_mtsat = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec4 _vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " mat4 m = mat4( \n" +
            " cos(3.1415*(140.0/180.0)), 0.0, sin(3.1415*(140.0/180.0)), 0.0, \n" +  // first column 
            " 0.0, 1.0, 0.0, 0.0, \n" +  // second column
            " -sin(3.1415*(140.0/180.0)), 0.0, cos(3.1415*(140.0/180.0)), 0.0, \n" +  // third column
            " 0.0, 0.0, 0.0, 1.0  \n" +  // forth column
            " ); \n" +
            "_vPosition = m*aPosition;	\n" +
            " 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
            " 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
            "   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
            "   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
            "   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.117*pow(s,4.5)); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.065*pow(s,4.8)); \n" +
            " 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 + 0.12); \n" +
            " 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.09); \n" +
            " 	vTexM[1] = -0.008 + vTexM[1]; \n" +
            "}                         \n"
    const val fsc_mtsat = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec4 _vPosition;	\n" +
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " vec4 cmT = vec4(0.75,0.75,0.75,1.0); \n" +
            " vec4 cmTS = vec4(0.75,0.75,0.75,1.0); \n" +
            " float cm = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM).g) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM).g) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM).g) ) "
            + "; \n" +
            " float cmS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV).g) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV).g) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV).g)						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV).g) ) "
            + "; \n" +
            " if (_vPosition.z > 0.0) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 2.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_cci = 0
    const val vsc_cci = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1100x779
            // [41,60]
            // [1081,683]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1081.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/779.0) + (vTex[1])*((683.0-60.0)/779.0); \n" +
            "}                         \n"
    const val fsc_cci = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +  /*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.025 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +
			
			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.05 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
            " vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.5; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.5; \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_cci_temp = 0
    const val vsc_cci_temp = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1100x763
            // [41,60]
            // [1081,684]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1081.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/763.0) + (vTex[1])*((684.0-60.0)/763.0); \n" +
            "}                         \n"
    const val fsc_cci_temp = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +  /*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.025 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +

			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.05 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.5; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.5; \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_cci_temp_an = 0
    const val vsc_cci_temp_an = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1172x764
            // [41,60]
            // [1081,683]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1172.0) + (vTex[0])*((1081.0-41.0)/1172.0); \n" +
            " 	vTexM[1] =  (60.0/764.0) + (vTex[1])*((683.0-60.0)/764.0); \n" +
            "}                         \n"
    const val fsc_cci_temp_an = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +  /*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.03 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +
			
			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.19 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.5; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.5; \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_cci_water = 0
    const val vsc_cci_water = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1100x758
            // [41,61]
            // [1082,684]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1082.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (61.0/758.0) + (vTex[1])*((684.0-61.0)/758.0); \n" +
            "}                         \n"
    var p_cci_wind = 0
    const val vsc_cci_wind = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1100x760
            // [41,60]
            // [1081,684]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1081.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/760.0) + (vTex[1])*((684.0-60.0)/760.0); \n" +
            "}                         \n"
    var p_cci_jet = 0
    const val vsc_cci_jet = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1100x764
            // [41,60]
            // [1082,683]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1082.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/764.0) + (vTex[1])*((683.0-60.0)/764.0); \n" +
            "}                         \n"
    var p_cci_snow = 0
    var p_cci_temp_an_1y = 0
    const val vsc_cci_temp_an_1y = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1172x764
            // [41,60]
            // [1081,683]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTex[0])*((1080.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/827.0) + (vTex[1])*((683.0-60.0)/827.0); \n" +
            "}                         \n"
    const val fsc_cci_temp_an_1y = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +  /*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.03 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +

			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.19 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.5; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.5; \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_cci_oisst_v2 = 0
    const val vsc_cci_oisst_v2 = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +  // 1172x764
            // [41,60]
            // [1081,683]
            //" 	vTexM[0] = vTex[0]; \n" +
            //" 	vTexM[1] = vTex[1]; \n" +
            // repeat texture
            "   vTexM[0] = vTex[0] + 0.25; \n" +
            " 	if (vTexM[0] < 0.0) { vTexM[0] = vTexM[0] + 1.0; } \n" +
            " 	if (vTexM[0] > 1.0) { vTexM[0] = vTexM[0] - 1.0; } \n" +
            " 	vTexM[0] =  (41.0/1100.0) + (vTexM[0])*((1080.0-41.0)/1100.0); \n" +
            " 	vTexM[1] =  (60.0/764.0) + (vTex[1])*((683.0-60.0)/764.0); \n" +  // repeat texture
            //" 	if (vTexM[0] < (41.0/1100.0)) { vTexM[0] = vTexM[0] + (1080.0-41.0)/1100.0; } \n" +
            //" 	if (vTexM[0] > (1080.0/1100.0)) { vTexM[0] = vTexM[0] - (1080.0-41.0)/1100.0; } \n" +
            /*" mat4 m = mat4( \n" +
					" cos(3.1415*(-75.324/180.0)), 0.0, sin(3.1415*(-75.324/180.0)), 0.0, \n" +  	// first column
					" 0.0, 1.0, 0.0, 0.0, \n" +  												// second column
					" -sin(3.1415*(-75.324/180.0)), 0.0, cos(3.1415*(-75.324/180.0)), 0.0, \n" +  	// third column
					" 0.0, 0.0, 0.0, 1.0  \n" +  												// forth column
					" ); \n" +
					"_vPosition = m*aPosition;	\n" +

					" 	vTexM[0] = 1.0 - (_vPosition.x+1.0)/2.0; \n" +
					" 	vTexM[1] = 1.0 - (_vPosition.y+1.0)/2.0; \n" +
					"   float s = sqrt(_vPosition.x*_vPosition.x + _vPosition.y*_vPosition.y); \n" +
					"   float sx = sqrt(_vPosition.x*_vPosition.x); \n" +
					"   float sy = sqrt(_vPosition.y*_vPosition.y); \n" +
					" 	vTexM[0] = -0.048 + vTexM[0]; \n" +
					" 	vTexM[1] = -0.015 + vTexM[1]; \n" +
					" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.15); \n" +
					" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 + 0.05); \n" +
					" 	vTexM[0] = 0.5 + (vTexM[0] - 0.5)*(1.0 - 0.1*pow(s,5.0)); \n" +
					" 	vTexM[1] = 0.5 + (vTexM[1] - 0.5)*(1.0 - 0.20*pow(s,5.0)); \n" +*/
            "}                         \n"
    const val fsc_cci_oisst_v2 = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +  /*	" if (vPosition.y > 0.95) { \n" +
			" 	_vTexM[1] =  -0.03 + 0.1 - 2.0*(vPosition.y-0.95) ; \n" +
			" 	_vTexM[0] =  1.6*_vTexM[0] ; \n" +
			" } \n" +

			" if (vPosition.y < -0.9) { \n" +
			" 	_vTexM[1] =  -0.19 + 0.9 - 1.5*(0.9 + vPosition.y - 0.01) ; \n" +
			" } \n" +
		*/
            " vec4 cmT = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 0.2*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.4*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.8; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.8; \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
    var p_nrl_rainrate = 0
    const val vsc_nrl_rainrate = "uniform mat4 uMVPMatrix;   \n" +
            "uniform mat4 uMVMatrix;   	\n" +
            "uniform mat4 uVMatrix;   	\n" +
            "varying mat4 vMVMatrix;   	\n" +
            "uniform mat4 uIMVMatrix;	\n" +
            "attribute vec4 aPosition; 	\n" +
            "attribute vec3 aNormal;  	\n" +
            "attribute vec2 aTex;		\n" +
            "varying vec3 vPosition;	\n" +
            "varying vec3 vNormal;		\n" +
            "varying vec3 vMVNormal;		\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform vec4 uLightPos;  	\n" +
            "varying vec3 vLightPos;	\n" +
            "varying vec3 lightVector;	\n" +
            "varying vec3 vEye;	\n" +  // Atmosphere
            "varying vec3 vPositionA;	\n" +  // clouds shadow shift
            "varying vec2 shiftUV;		\n" +
            "void main(){              	\n" +
            " gl_Position = uMVPMatrix * aPosition; \n" +
            " vec4 position = aPosition; \n" +
            " vPosition = vec3(position.x, position.y, position.z); \n" +  // only rotate the normals 
            " vec4 normal = vec4(aNormal, 0.0); \n" +
            " vNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " normal = uMVMatrix*vec4(aNormal, 0.0); \n" +
            " vMVNormal = vec3(normal.x, normal.y, normal.z); \n" +
            " vTex = aTex; \n" +
            " vec4 lightPos = uIMVMatrix * uLightPos; \n" +
            " vLightPos = vec3(lightPos.x, lightPos.y, lightPos.z); \n" +
            " vec4 eye = uIMVMatrix * vec4(0.0, 0.0, 0.0, 1.0); \n" +
            " vEye = vec3(eye.x, eye.y, eye.z); \n" +
            " lightVector = normalize(vLightPos - vPosition);   \n" +  // clouds shadow
            " vec3 _y = vec3(0.0, 1.0, 0.0); \n" +
            " vec3 _x = cross(_y, vNormal); \n" +
            " vec3 _z = vNormal; \n" +
            " shiftUV = vec2( dot(lightVector, _x), -dot(lightVector, _y) ); \n" +
            " vTexM = vTex; \n" +
            " 	vTexM[0] = + 0.5 + (vTex[0] - 0.5)*(1.0); \n" +
            " 	vTexM[1] = + 0.5 + (vTex[1] - 0.5)*(1.5); \n" +
            "}                         \n"
    const val fsc_nrl_rainrate = ("precision mediump float;  	\n" +
            "varying vec3 vLightPos;  	\n" +
            "varying vec3 vPosition;	\n" +  // Interpolated position for this fragment.
            "varying vec3 lightVector;	\n" +
            "uniform vec4 uColor;  		\n" +  // This is the color from the vertex shader interpolated across the triangle per fragment.
            "varying vec3 vNormal;   	\n" +  // Interpolated normal for this fragment.
            "varying vec3 vMVNormal;	\n" +
            "varying vec2 vTex;			\n" +
            "varying vec2 vTexM;		\n" +
            "uniform sampler2D uTextures[7];\n" +
            "varying vec3 vEye	;   	\n" +
            "varying vec2 shiftUV;		\n" +  // clouds shadow shift
            "uniform vec4 uLightAmbientColor;  		\n" +
            "uniform vec4 uLightDiffuseColor;  		\n" +
            "uniform vec4 uLightSpecularColor;  	\n" +
            "uniform float uLightAttenuation;  		\n" +
            "uniform vec4 uMaterialAmbientColor;  	\n" +
            "uniform vec4 uMaterialDiffuseColor; 	\n" +
            "uniform vec4 uMaterialSpecularColor;  	\n" +
            "uniform vec4 uMaterialEmissiveColor;  	\n" +
            "uniform float uMaterialShinnes;  		\n" +
            "uniform float uBumpLevel;				\n" +
            "uniform int uTexMapping;				\n" +
            "uniform float uTW1;					\n" +  // cloud map texture weight
            "uniform float uTW2;					\n" +  // cloud map texture weight
            "uniform float uTW3;					\n" +  // cloud map texture weight
            // The entry point for our fragment shader.
            "void main()  				\n" +
            "{  						\n" +  // phong shading
            //"    vec3 normal2 = normalize(vNormal);	\n" +
            // gouraud shading
            "    vec3 normal2 = vNormal;	\n" +  // bumpmapping
            // lookup normal from normal map, move from [0,1] to  [-1, 1] range
            "	 vec3 normal = vec3(0.0, 0.0, 0.0);	\n" +
            "	 normal.x = -(2.0 * texture2D (uTextures[0], vTex).b - 1.0);	\n" +
            "	 normal.y = 2.0 * texture2D (uTextures[0], vTex).g - 1.0;	\n" +
            "	 normal.z = -(2.0 * texture2D (uTextures[0], vTex).r - 1.0);	\n" +
            "	 vec4 n = vec4(normal, 0.0); \n" +
            "	 normal = vec3(n.x, n.y, n.z); \n" +
            "	 normal = uBumpLevel*normal + (1.0 - uBumpLevel)*normal2; \n" +  // diffuse
            "    float diffuse = max(dot(normal, lightVector), 0.0);   \n" +  // diffuse 2
            "    float diffuse2 = max(dot(normal2, lightVector), 0.0);   \n" +  // specular
            "	 vec3 E = normalize(vEye - vPosition);\n" +  // we are in Eye Coordinates, so EyePos is (0,0,0)
            "	 vec3 R = normalize(-reflect(lightVector, normal));\n" +
            "    float sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 spec = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, uMaterialShinnes);\n" +
            "	 spec = clamp(spec, 0.0, 1.0);\n" +  // specular watter
            "	 vec4 specW = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 15.0);\n" +
            "	 specW = clamp(specW, 0.0, 1.0);\n" +  // specular clouds 
            "	 R = normalize(-reflect(lightVector, normal2));\n" +
            "    sp = max(dot(R, E), 0.0);\n" +
            "	 vec4 specC = vec4(1.0, 1.0, 1.0, 1.0) * pow(sp, 1.5);\n" +
            "	 specC = clamp(specC, 0.0, 1.0);\n" +  // text in the bottom
            " 	vec2 _vTexM =  vTexM; \n" +
            " vec4 cmT = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM)) ) "
            + "; \n" +
            " vec4 cmTS = 1.0*(sin(uTW3*0.5*3.1415)*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - sin(uTW3*0.5*3.1415))*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW1*(texture2D(uTextures[2], _vTexM + 0.001*shiftUV)) 						+ (1.0 - uTW1)*(texture2D(uTextures[6], _vTexM + 0.001*shiftUV)) ) "
            + "			+ 0.0*( uTW2*(texture2D(uTextures[4], _vTexM + 0.001*shiftUV))						+ (1.0 - uTW2)*(texture2D(uTextures[5], _vTexM + 0.001*shiftUV)) ) "
            + "; \n" +
            " float min = 0.3; \n" +
            " float max = 1.0; \n" +
            " cmT = vec4((cmT.r - min)/(max-min),(cmT.g - min)/(max-min),(cmT.b - min)/(max-min),1.0); \n" +
            " cmTS = vec4((cmTS.r - min)/(max-min),(cmTS.g - min)/(max-min),(cmTS.b - min)/(max-min),1.0); \n" +
            " float cm = (cmT.r + cmT.g + cmT.b)/3.0; \n" +
            " cm = 0.7 + 0.3*cm; \n" +  //" cm = 1.0; \n" +
            " float cmS = (cmTS.r + cmTS.g + cmTS.b)/3.0; \n" +
            " cmS = 0.0 + 0.3*cmS; \n" +
            " if (vPosition.y > 0.87) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " if (vPosition.y < -0.87) { \n" +
            " 	cmT = vec4(0.0,0.0,0.0,1.0); \n" +
            " 	cm = 0.0; \n" +
            " 	cmS = 0.0; \n" +
            " } \n" +
            " 		float d = (1.0 - texture2D(uTextures[1], vTex).r)*(1.0 - texture2D(uTextures[1], vTex).g)*texture2D(uTextures[1], vTex).b; \n" +  // cloudmap
            "		gl_FragColor = 1.0*cm*cmT*( 0.4 + 1.0*uLightAmbientColor + 0.9*uLightDiffuseColor * diffuse2 + 0.1*uLightSpecularColor * specC ) " // clouds shadow
            + "						+ clamp(-1.0*vec4(0.4, 0.4, 0.4, 1.0)*cmS " // texture
            + "						+ clamp(1.0 - 0.5*cm, 0.0, 1.0)*texture2D(uTextures[1], vTex) * (uMaterialEmissiveColor + uLightAmbientColor * uMaterialAmbientColor + uLightDiffuseColor * uMaterialDiffuseColor * diffuse  + 0.3*uLightSpecularColor * specW )     " // specular light on watter
            + "						+ 5.0*d*(1.0 - 1.0*cm)*1.0*vec4(1.0, 1.0, 1.0, 1.0)*uLightSpecularColor * specW , 0.0, 1.0)  ;   \n" +  // night light
            //" if (diffuse < 2.0) { \n" +
            "	 diffuse = clamp(diffuse, 0.0, 1.0); \n" +
            "    gl_FragColor =  gl_FragColor + 1.0*(1.0*clamp(1.0 - 2.25*cm, 0.0, 1.0))*pow(1.0 - diffuse, 10.0)*texture2D(uTextures[3], vTex);   \n" +  //"}   \n" +
            // antialias edges
            " float a; \n" +
            " a = abs(vMVNormal.z); \n" +  // additional specular
            " gl_FragColor = clamp(gl_FragColor  + 0.1 * uLightSpecularColor * (a) * (sp*sp * 0.5), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 4.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 7.0*vec4(1.0, 1.0, 1.0, 1.0) * uLightSpecularColor  * (a) * (diffuse * 5.0), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 0.5); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 0.2*vec4(0.3, 0.6, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " a = pow(abs(1.0 - vMVNormal.z), 30.0); \n" +
            " gl_FragColor = clamp(gl_FragColor  + 20.0*vec4(0.0, 0.4, 0.9, 1.0) * (a) * (1.3), 0.0, 1.0);	\n" +
            " gl_FragColor.a = 1.0;	\n" +
            " if (abs(vMVNormal.z) < 0.55) { \n" +
            " 	gl_FragColor = clamp(gl_FragColor * pow(abs(vMVNormal.z)/0.55 - 0.0, 5.0), 0.0, 1.0);	\n" +
            " }; \n" +
            "}   \n")
}