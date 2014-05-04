package net.pme;

import net.pme.core.GameObject;
import net.pme.core.math.Vector3D;
import net.pme.gameloop.LoopableAttachment;
import net.pme.graphics.RenderAttachment;
import net.pme.graphics.Shader;
import org.lwjgl.opengl.GL11;

/**
 * A simple cube.
 *
 * @author Michael FÃ¼rst
 * @version 1.0
 */
public class TestCube3 extends GameObject {
    private float[] uniform = null;
    private Shader sh = null;
    private double t;

    /**
     * A Test cube.
     *
     * @param id       The id.
     * @param position The position.
     * @param front    The front axis.
     * @param up       The up axis.
     */
    public TestCube3(long id, Vector3D position, Vector3D front, Vector3D up) {
        super(id, position, front, up);

        setRenderAttachment(new RenderAttachment(this, -1) {
            @Override
            protected void specialFX() {
                GL11.glBegin(GL11.GL_QUADS);
                {
                    // Quader
                    GL11.glColor3f(0.5f, 0.5f, 0.0f);
                    GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, -1.0f);
                    GL11.glVertex3f(1.0f, -1.0f, -1.0f);

                    GL11.glColor3f(0.5f, 0.8f, 0.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, -1.0f);

                    GL11.glColor3f(0.8f, 0.5f, 0.0f);
                    GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
                    GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, -1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, -1.0f, -1.0f);

                    GL11.glColor3f(0.5f, 0.0f, 0.5f);
                    GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
                    GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, -1.0f);

                    GL11.glColor3f(0.5f, 0.5f, 0.5f);
                    GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
                    GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, -1.0f, 1.0f);

                    GL11.glColor3f(0.0f, 0.0f, 0.5f);
                    GL11.glVertex3f(1.0f, -1.0f, -1.0f);
                    GL11.glVertex3f(1.0f, -1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, 1.0f);
                    GL11.glVertex3f(1.0f, 1.0f, -1.0f);
                }
                GL11.glEnd();

            }
        });
        setLoopableAttachment(new LoopableAttachment() {
            @Override
            public void update(double elapsedTime) {
                rotateAroundPitchAxis(elapsedTime * 10.0);
                uniform[0] = (float) Math.abs(Math.sin(t));
                uniform[1] = (float) Math.abs(Math.cos(t));
                uniform[2] = 0.0f;
                uniform[3] = 1.0f;
                t += elapsedTime;
            }
        });

        sh = new Shader(null, Shaders.fsh);
        getRenderAttachment().setShader(sh);
        uniform = new float[4];
        sh.setUniform4f("color", uniform);
    }
}
