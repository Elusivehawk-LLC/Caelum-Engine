
package com.elusivehawk.engine.render;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class RenderEngineParticles implements IRenderEngine
{
	@Override
	public void render(IRenderHUB hub)
	{
		if (hub.getScene() == null || !hub.getRenderMode().is3D())
		{
			return;
		}
		
		ParticleScene particles = hub.getScene().getParticles();
		
		if (particles == null || !particles.updateBeforeUse(hub))
		{
			return;
		}
		
		GLProgram p = particles.getProgram();
		
		if (!p.bind())
		{
			return;
		}
		
		GL.glEnable(GL.GL_DEPTH_TEST);
		GL.glDepthFunc(GL.GL_LESS);
		
		GL.glEnable(GL.GL_CULL_FACE);
		GL.glCullFace(GL.GL_BACK);
		
		GL.glDrawArrays(GL.GL_POINTS, 0, particles.getParticleCount());
		
		GL.glDisable(GL.GL_CULL_FACE);
		GL.glDisable(GL.GL_DEPTH_TEST);
		
		p.unbind();
		
	}
	
	@Override
	public int getPriority()
	{
		return 0;
	}
	
}
