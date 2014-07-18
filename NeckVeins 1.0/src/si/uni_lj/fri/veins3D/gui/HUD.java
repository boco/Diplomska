package si.uni_lj.fri.veins3D.gui;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_MODULATE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexEnvf;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class HUD {
	private final String PATH = "res/imgs/";
	private final float ELLIPSEF = 1.1180339887498948482045868343656f;

	private Texture rotationCircle;
	private Texture circleGlow;
	private Texture movementCircle;
	private Texture rotationElipse;
	private Texture movementElipse;
	private Texture ellipseGlow;
	private int clickedOn;

	public int ellipseSide = 0;
	public float clickOnCircleAngle = 0;
	public float clickToCircleDistance = 0;

	/* Position and size */
	private float lastWindowWidth;
	private float lastWindowHeight;
	public float r;
	public float x1;
	public float y1;
	public float x2;
	public float y2;
	public float f;
	public float offset;

	public HUD() {
		setHUDPositionAndSize();
		initHUDTextures();
	}

	public void setHUDPositionAndSize() {
		lastWindowWidth = VeinsWindow.settings.resWidth;
		lastWindowHeight = VeinsWindow.settings.resHeight;
		r = lastWindowWidth / 18;
		offset = r * 2 / 3;
		x1 = lastWindowWidth - offset - r;
		y1 = lastWindowHeight - lastWindowHeight / 18 - offset - r;
		x2 = lastWindowWidth - offset - r;
		y2 = lastWindowHeight - lastWindowHeight / 18 - 2 * offset - 3 * r;
		f = ELLIPSEF * r;
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	private void initHUDTextures() {
		// load textures
		try {
			rotationCircle = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "rotationCircle.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "rotationCircle.png unsuccessful");
			e.printStackTrace();
		}
		try {
			circleGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(PATH + "circleGlow.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "circleGlow.png unsuccessful");
			e.printStackTrace();
		}
		try {
			movementCircle = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "movementCircle.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "movementCircle.png unsuccessful");
			e.printStackTrace();
		}
		try {
			rotationElipse = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "rotationElipse.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "rotationElipse.png unsuccessful");
			e.printStackTrace();
		}
		try {
			movementElipse = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "movementElipse.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "movementElipse.png unsuccessful");
			e.printStackTrace();
		}
		try {
			ellipseGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(PATH + "ellipseGlow.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "ellipseGlow.png unsuccessful");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @since 0.1
	 * @version 0.1
	 */
	public void drawHUD() {
		// check if window size has changed
		if (lastWindowWidth != VeinsWindow.settings.resWidth && lastWindowHeight != VeinsWindow.settings.resHeight)
			setHUDPositionAndSize();

		startHUD();

		glColor4f(1, 1, 1, 1);
		drawRotationEllipse();
		drawMovementEllipse();
		if (clickedOn == VeinsWindow.CLICKED_ON_MOVE_ELLIPSE || clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE)
			drawEllipseGlow();

		glColor4f(1, 1, 1, 1);
		drawRotationCircle();
		drawMovementCircle();
		if (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE || clickedOn == VeinsWindow.CLICKED_ON_MOVE_CIRCLE)
			drawCircleGlow();

		endHUD();
	}

	private void startHUD() {
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0, VeinsWindow.settings.resWidth, 0, VeinsWindow.settings.resHeight, 0.2f, 2);
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();
		glDisable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		glClearColor(0f, 0f, 0f, 0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void drawRotationEllipse() {
		GL11.glBindTexture(GL_TEXTURE_2D, rotationElipse.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x1 + 1.5f * r, y1 + r, -1.0f);
		glTexCoord2f(0, 0);
		glVertex3f(x1 - 1.5f * r, y1 + r, -1.0f);
		glTexCoord2f(0, 1);
		glVertex3f(x1 - 1.5f * r, y1 - r, -1.0f);
		glTexCoord2f(1, 1);
		glVertex3f(x1 + 1.5f * r, y1 - r, -1.0f);
		glEnd();
	}

	private void drawMovementEllipse() {
		GL11.glBindTexture(GL_TEXTURE_2D, movementElipse.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x2 + 1.5f * r, y2 + r, -1.0f);
		glTexCoord2f(0, 0);
		glVertex3f(x2 - 1.5f * r, y2 + r, -1.0f);
		glTexCoord2f(0, 1);
		glVertex3f(x2 - 1.5f * r, y2 - r, -1.0f);
		glTexCoord2f(1, 1);
		glVertex3f(x2 + 1.5f * r, y2 - r, -1.0f);
		glEnd();
	}

	private void drawEllipseGlow() {
		float x = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE) ? x1 : x2;
		float y = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE) ? y1 : y2;

		glPushMatrix();
		glTranslatef(x, y, 0);
		if (ellipseSide == 0)
			glRotatef((float) (180), 0, 0, 1);
		glTranslatef(-x, -y, 0);
		GL11.glBindTexture(GL_TEXTURE_2D, ellipseGlow.getTextureID());
		glColor4f(1, 1, 1, 0.5f);
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x + 1.5f * r, y + r, -0.8f);
		glTexCoord2f(0, 0);
		glVertex3f(x - 1.5f * r, y + r, -0.8f);
		glTexCoord2f(0, 1);
		glVertex3f(x - 1.5f * r, y - r, -0.8f);
		glTexCoord2f(1, 1);
		glVertex3f(x + 1.5f * r, y - r, -0.8f);
		glEnd();
		glPopMatrix();
	}

	private void drawRotationCircle() {
		glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL_TEXTURE_2D, rotationCircle.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x1 + r, y1 + r, -0.6f);
		glTexCoord2f(0, 0);
		glVertex3f(x1 - r, y1 + r, -0.6f);
		glTexCoord2f(0, 1);
		glVertex3f(x1 - r, y1 - r, -0.6f);
		glTexCoord2f(1, 1);
		glVertex3f(x1 + r, y1 - r, -0.6f);
		glEnd();
	}

	private void drawMovementCircle() {
		GL11.glBindTexture(GL_TEXTURE_2D, movementCircle.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x2 + r, y2 + r, -0.6f);
		glTexCoord2f(0, 0);
		glVertex3f(x2 - r, y2 + r, -0.6f);
		glTexCoord2f(0, 1);
		glVertex3f(x2 - r, y2 - r, -0.6f);
		glTexCoord2f(1, 1);
		glVertex3f(x2 + r, y2 - r, -0.6f);
		glEnd();
	}

	private void drawCircleGlow() {
		float x = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? x1 : x2;
		float y = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? y1 : y2;

		glPushMatrix();
		glTranslatef(x, y, 0);
		glRotatef((float) (180 * clickOnCircleAngle / Math.PI), 0, 0, 1);
		glTranslatef(-x, -y, 0);
		GL11.glBindTexture(GL_TEXTURE_2D, circleGlow.getTextureID());
		glColor4f(1, 1, 1, (float) clickToCircleDistance);
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x + r, y + r, -0.4f);
		glTexCoord2f(0, 0);
		glVertex3f(x - r, y + r, -0.4f);
		glTexCoord2f(0, 1);
		glVertex3f(x - r, y - r, -0.4f);
		glTexCoord2f(1, 1);
		glVertex3f(x + r, y - r, -0.4f);
		glEnd();
		glPopMatrix();
	}

	private void endHUD() {
		glDisable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LIGHTING);
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
	}

	/**
	 * Sets the clickedOn so the HUD "knows" if it must response
	 * 
	 * @param clickedOn
	 */
	public void setClickedOn(int clickedOn) {
		this.clickedOn = clickedOn;
	}

}
