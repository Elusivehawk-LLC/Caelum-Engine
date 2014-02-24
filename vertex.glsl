#version 330

struct Camera
{
	mat4 m;
	float zFar;
	float zNear;
	float aspectRatio;
	float fov;
	
}

uniform Camera cam;
uniform mat4 proj;
uniform mat4 model;

(location = 0) in vec3 in_position;
(location = 1) in vec2 in_texcoord;
(location = 2) in vec3 in_norm;

(location = 3) in vec3 in_scale;
(location = 4) in vec3 in_rot;
(location = 5) in vec3 in_trans;

out vec2 frag_texcoord;
out vec3 frag_norm;

void main()
{
	frag_texcoord = in_texcoord;
	frag_norm = in_norm;
	
	mat4 fin = trans(in_trans) * rotate(in_rot) * scale(in_scale) * vec4(in_position, 1.0);
	
	gl_Position = proj * cam.m * fin;
	
}

mat4 scale(vec3 scl)
{
	return mat4(scl.x, 0.0, 0.0, 0.0,
				0.0, scl.y, 0.0, 0.0,
				0.0, 0.0, scl.z, 0.0,
				0.0, 0.0, 0.0, 1.0);
}

mat4 rotate(vec3 rot)
{
	float a = cos(rot.x);
	float b = sin(rot.x);
	float c = cos(rot.y);
	float d = sin(rot.y);
	float e = cos(rot.z);
	float f = sin(rot.z);
	
	float ad = a * d;
	float bd = b * d;
	
	return mat4(c * e, -c * f, d, 0.0,
				bd * e + a * f, -bd * f + a * e, -b * c, 0.0,
				-ad * e + b * f, ad * f + b * e, a * c, 0.0,
				0.0, 0.0, 0.0, 1.0);
}

mat4 trans(vec3 trans)
{
	return mat4(1.0, 0.0, 0.0, trans.x,
				0.0, 1.0, 0.0, trans.y,
				0.0, 0.0, 1.0, trans.z,
				0.0, 0.0, 0.0, 1.0);
}
