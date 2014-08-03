
package com.elusivehawk.util.math;

import static com.elusivehawk.util.math.MathConst.A;
import static com.elusivehawk.util.math.MathConst.B;
import static com.elusivehawk.util.math.MathConst.C;
import static com.elusivehawk.util.math.MathConst.X;
import static com.elusivehawk.util.math.MathConst.Y;
import static com.elusivehawk.util.math.MathConst.Z;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public final class MatrixHelper
{
	private MatrixHelper(){}
	
	public static Matrix createIdentityMatrix()
	{
		return new Matrix(4, 4).setIdentity();
	}
	
	public static Matrix createHomogenousMatrix(Vector rot, Vector scl, Vector trans)
	{
		return (Matrix)createRotationMatrix(rot).mul(createScalingMatrix(scl)).mul(createTranslationMatrix(trans));
	}
	
	public static Matrix createHomogenousMatrix(Quaternion rot, Vector scl, Vector trans)
	{
		return (Matrix)createRotationMatrix(rot).mul(createScalingMatrix(scl)).mul(createTranslationMatrix(trans));
	}
	
	@SuppressWarnings("unused")//FIXME
	public static Matrix createProjectionMatrix(Vector pos, Vector rot, float fov, float aspect, float zFar, float zNear)
	{
		float[] ret = new float[16];
		
		float yScale = 1 / (float)Math.tan(MathHelper.toRadians(fov / 2f));
		float xScale = yScale / aspect;
		float frustumLength = zFar - zNear;
		
		ret[0] = xScale;
		ret[5] = yScale;
		ret[10] = -((zFar + zNear) / frustumLength);
		ret[11] = -((2 * zFar * zNear) / frustumLength);
		ret[14] = -1;
		
		return new Matrix(ret);
	}
	
	public static Matrix createRotationMatrix(Vector vec)
	{
		Matrix ret = new Matrix(4, 4);
		
		setEulerZYX(ret, vec);
		
		return ret;
	}
	
	public static Matrix createRotationMatrix(float x, float y, float z)
	{
		Matrix ret = new Matrix(4, 4);
		
		setEulerZYX(ret, x, y, z);
		
		return ret;
	}
	
	public static void setEulerZYX(Matrix mat, Vector euler)
	{
		setEulerZYX(mat, euler.get(X), euler.get(Y), euler.get(Z));
		
	}
	
	public static void setEulerZYX(Matrix mat, float eulerX, float eulerY, float eulerZ)
	{
		float cx = (float)Math.cos(eulerX);
		float cy = (float)Math.cos(eulerY);
		float cz = (float)Math.cos(eulerZ);
		float sx = (float)Math.sin(eulerX);
		float sy = (float)Math.sin(eulerY);
		float sz = (float)Math.sin(eulerZ);
		
		float cxz = cx * cz;
		float cxsz = cx * sz;
		float sxcz = sx * cz;
		float sxz = sx * sz;
		
		mat.setRow(0, cy * cz, sy * sxcz - cxsz, sy * cxz + sxz);
		mat.setRow(1, cy * sz, sy * sxz + cxz, sy * cxsz - sxcz);
		mat.setRow(2, -sy, cy * sx, cy * cx);
		
	}
	
	public static Matrix createRotationMatrix(Quaternion q)
	{
		float a = (float)Math.cos(q.get(A));
		float b = (float)Math.sin(q.get(A));
		float c = (float)Math.cos(q.get(B));
		float d = (float)Math.sin(q.get(B));
		float e = (float)Math.cos(q.get(C));
		float f = (float)Math.sin(q.get(C));
		
		float ad = a * d;
		float bd = b * d;
		
		float[] buf = new float[16];
		
		buf[0] = c * e; buf[1] = -c * f; buf[2] = d; buf[3] = 0f;
		//-------------------------------------------------
		buf[4] = bd * e + a * f; buf[5] = -bd * f + a * e; buf[6] = -b * c; buf[7] = 0f;
		//-------------------------------------------------
		buf[8] = -ad * e + b * f; buf[9] = ad * f + b * e; buf[10] = a * c; buf[11] = 0f;
		//-------------------------------------------------
		buf[12] = buf[13] = buf[14] = 0f; buf[15] = 1f;
		//-------------------------------------------------
		
		return new Matrix(buf);
	}
	
	public static Matrix createScalingMatrix(Vector vec)
	{
		return createScalingMatrix(vec.get(X), vec.get(Y), vec.get(Z));
	}
	
	public static Matrix createScalingMatrix(float x, float y, float z)
	{
		Matrix ret = createIdentityMatrix();
		
		ret.set(0, x);
		ret.set(5, y);
		ret.set(10, z);
		
		return ret;
	}
	
	public static Matrix createTranslationMatrix(Vector vec)
	{
		return createTranslationMatrix(vec.get(X), vec.get(Y), vec.get(Z));
	}
	
	public static Matrix createTranslationMatrix(float x, float y, float z)
	{
		Matrix ret = createIdentityMatrix();
		
		ret.set(3, x);
		ret.set(7, y);
		ret.set(11, z);
		
		return ret;
	}
	
}
