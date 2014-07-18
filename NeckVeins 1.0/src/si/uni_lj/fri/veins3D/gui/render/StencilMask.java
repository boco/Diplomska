package si.uni_lj.fri.veins3D.gui.render;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_REPLACE;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glStencilMask;
import static org.lwjgl.opengl.GL11.glStencilOp;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;


import org.lwjgl.opengl.GL11;

import si.uni_lj.fri.veins3D.gui.VeinsWindow;

public class StencilMask {

	public static void initStencil() {
		// Stencil
		glStencilMask(0x01);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		glEnable(GL_STENCIL_TEST);
		// disable writing colors and depth
		glColorMask(false, false, false, false);
		glDepthMask(false);
		// Make the stencil test always fail and on fail replace the appropriate
		// value in stencil buffer to 1
		glStencilFunc(GL11.GL_NEVER, 1, 0x01);
		glStencilOp(GL_REPLACE, GL_KEEP, GL_KEEP);
		glStencilMask(0x01);// enable writing to bitplanes
		// prepare ortho drawing
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0, VeinsWindow.settings.resWidth, 0, VeinsWindow.settings.resHeight, 0.2f, 3);
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();
		glDisable(GL_LIGHTING);
		glDisable(GL_DEPTH_TEST);
		// draw the lines
		glTranslatef(0.375f, 0.375f, -2);
		glColor4f(1, 1, 1, 1);
		for (int i = 0; i < VeinsWindow.settings.resHeight; i += 2) {
			glLineWidth(1);
			glBegin(GL_LINES);
			glVertex2f(0, i);
			glVertex2f(VeinsWindow.settings.resWidth, i);
			glEnd();
		}
		// exit ortho drawing
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
		glEnable(GL_DEPTH_TEST);
		// enable writing colors and depth and disable writing to bitplanes
		glColorMask(true, true, true, true);
		glDepthMask(true);
		glStencilMask(0x00);
	}
}
