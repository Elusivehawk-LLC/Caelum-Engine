/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.elusivehawk.engine.physics.jbullet.collision.shapes;

import com.elusivehawk.engine.math.MathConst;
import com.elusivehawk.engine.math.Vector;
import cz.advel.stack.Stack;

/*
 * NOTICE: Edited by Elusivehawk
 */
/**
 * Cylinder shape around the Z axis.
 * 
 * @author jezek2
 */
public class CylinderShapeZ extends CylinderShape {

	public CylinderShapeZ(Vector halfExtents) {
		super(halfExtents, false);
		upAxis = 2;
		recalcLocalAabb();
	}

	@Override
	public Vector localGetSupportingVertexWithoutMargin(Vector vec, Vector out) {
		return cylinderLocalSupportZ(getHalfExtentsWithoutMargin(Stack.alloc(new Vector(3))), vec, out);
	}

	@Override
	public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector[] vectors, Vector[] supportVerticesOut, int numVectors) {
		for (int i = 0; i < numVectors; i++) {
			cylinderLocalSupportZ(getHalfExtentsWithoutMargin(Stack.alloc(new Vector(3))), vectors[i], supportVerticesOut[i]);
		}
	}

	@Override
	public float getRadius() {
		return getHalfExtentsWithMargin(Stack.alloc(new Vector(3))).get(MathConst.X);
	}

	@Override
	public String getName() {
		return "CylinderZ";
	}
	
}
