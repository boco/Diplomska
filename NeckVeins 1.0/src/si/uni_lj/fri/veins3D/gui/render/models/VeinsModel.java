/* Author of this file: Simon Žagar, 2012, Ljubljana
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 */
package si.uni_lj.fri.veins3D.gui.render.models;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import si.uni_lj.fri.segmentation.ModelCreator;
import si.uni_lj.fri.segmentation.ModelCreatorJava;
import si.uni_lj.fri.segmentation.utils.LabelUtil;
import si.uni_lj.fri.segmentation.utils.TrianglesLabelHelper;
import si.uni_lj.fri.segmentation.utils.obj.Triangle;
import si.uni_lj.fri.segmentation.utils.obj.Vertex;
import si.uni_lj.fri.veins3D.gui.VeinsWindow;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;

/**
 * @author Simon Žagar
 * @since 0.2
 * @version 0.2
 */
public class VeinsModel {
	private final int APLICATION_SUBDIVISION_LIMIT = 3;

	private TrianglesLabelHelper labelHelper;
	protected ArrayList<Float> vertices;
	protected ArrayList<Mesh> meshes;
	public double centerx, centery, centerz;
	public float maxX, maxY, maxZ;
	public float minX, minY, minZ;
	public float threshold = 0;
	public int maxTriangels = 0;
	private int numberOfSubdivisions = 0;
	private int maxSubDepth = 0;

	public Quaternion currentOrientation;
	private Quaternion addedOrientation;

	public double[] veinsGrabbedAt;
	public double veinsGrabRadius;

	public VeinsModel() {
		meshes = new ArrayList<Mesh>();
		setDefaultOrientation();
	}

	public VeinsModel(String filepath) {
		constructVBOFromObjFile(filepath);
		setDefaultOrientation();
	}

	public VeinsModel(String filepath, double sigma, double threshold) throws LWJGLException {
		constructVBOFromRawFile(filepath, sigma, threshold);
		setDefaultOrientation();
	}

	public VeinsModel(double threshold, Quaternion currentQuaternion) throws LWJGLException {
		changeThreshold(threshold);
		this.currentOrientation = currentQuaternion;
		this.addedOrientation = new Quaternion();
	}

	public void changeThreshold(double threshold) throws LWJGLException {
		Object[] output = ModelCreator.changeModel(threshold);
		constructVBO(output);
	}

	public void changeMinTriangles(int min) {
		for (Mesh mesh : meshes) {
			mesh.deleteVBO();
		}
		boolean[] labels = LabelUtil.getValidLabels(maxTriangels, min, labelHelper);
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		int tempFaceCount = 0;
		for (Triangle t : labelHelper.getTriangles()) {
			if (labels[t.label]) {
				tempFaces.add(t.v3.index);
				tempFaces.add(t.v2.index);
				tempFaces.add(t.v1.index);
				tempFaceCount++;
			}
		}

		if (tempFaceCount > 0) {
			Mesh mesh = new Mesh(new ArrayList<String>(), tempFaces, vertices);
			meshes.add(mesh);
			System.out.println("Created a new mesh java object that will have it's own VBO.");
		}

		for (Mesh mesh : meshes) {
			mesh.constructVBO();
		}
	}

	public void deleteMesh() {
		if (meshes != null) {
			for (Mesh mesh : meshes) {
				mesh.deleteVBO();
			}
		}
	}

	/**
	 * Normals calculated on GPU are currently not used, instead Normals
	 * calculated in constructVBO are used.
	 * 
	 * @param filepath
	 */
	public void constructVBOFromRawFile(String filepath, double sigma, double threshold) throws LWJGLException {
		Object[] output = ModelCreator.createModel(filepath, sigma, threshold);
		constructVBO(output);
	}

	public void constructVBOFromRawFileSafeMode(String filepath, double sigma, double threshold) {
		Object[] output = ModelCreatorJava.createModel(filepath, sigma, threshold);
		constructVBO(output);
	}

	private void constructVBO(Object[] output) {
		IntBuffer nTrianglesBuff = (IntBuffer) output[0];
		FloatBuffer trianglesBuff = (FloatBuffer) output[1];
		// FloatBuffer normalsBuff = (FloatBuffer) output[2];
		labelHelper = new TrianglesLabelHelper(nTrianglesBuff.get(0));
		LabelUtil.createVertexList(nTrianglesBuff.get(0), trianglesBuff, labelHelper);
		this.threshold = (Float) output[3];
		this.maxTriangels = nTrianglesBuff.get(0);

		vertices = new ArrayList<Float>();
		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		int tempFaceCount = 0;

		for (Vertex v : labelHelper.getVertTriMap().keySet()) {
			vertices.add(x = v.x);
			vertices.add(y = v.y);
			vertices.add(z = v.z);
			centerx += x;
			centery += y;
			centerz += z;
			if (x < minX)
				minX = x;
			if (y < minY)
				minY = y;
			if (z < minZ)
				minZ = z;
			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;
			if (z > maxZ)
				maxZ = z;
		}
		for (Triangle t : labelHelper.getTriangles()) {

			tempFaces.add(t.v3.index);
			tempFaces.add(t.v2.index);
			tempFaces.add(t.v1.index);
			tempFaceCount++;

		}

		if (tempFaceCount > 0) {
			Mesh mesh = new Mesh(groups, tempFaces, vertices);
			meshes.add(mesh);
			System.out.println("Created a new mesh java object that will have it's own VBO.");
		} else {
			System.out.println("One \"g\" holding 0 elements discarted.");
		}

		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes) {
			mesh.constructVBO();
		}
	}

	public void constructVBOFromObjFile(String filepath) {
		vertices = new ArrayList<Float>();
		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		boolean newG = false;
		int tempFaceCount = 0;

		File file = new File(filepath);
		Scanner scanner;
		try {
			scanner = new Scanner(file);
			String type;
			String line;
			while (scanner.hasNext()) {
				line = scanner.nextLine();
				StringTokenizer strTokenizer = new StringTokenizer(line);
				type = strTokenizer.nextToken();
				if (type.equalsIgnoreCase("v")) {
					vertices.add(x = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(y = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(z = Float.parseFloat(strTokenizer.nextToken()));
					centerx += x;
					centery += y;
					centerz += z;
					if (x < minX)
						minX = x;
					if (y < minY)
						minY = y;
					if (z < minZ)
						minZ = z;
					if (x > maxX)
						maxX = x;
					if (y > maxY)
						maxY = y;
					if (z > maxZ)
						maxZ = z;
				} else if (type.equalsIgnoreCase("f")) {
					int a, b, c;
					StringTokenizer tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					a = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					b = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					c = Integer.parseInt(tok.nextToken());

					tempFaces.add(c);
					tempFaces.add(b);
					tempFaces.add(a);

					tempFaceCount++;
				} else if (type.equalsIgnoreCase("g")) {
					if (tempFaceCount > 0) {
						// It seems that since last starting a new group, there
						// have been faces stored
						// Here I create a new mesh
						Mesh mesh = new Mesh(groups, tempFaces, vertices);
						meshes.add(mesh);
						// After the whole file will be read, each mesh object's
						// faces will be stored as VBOs (Vertex Buffer Objects).
						System.out.println("Created a new mesh java object that will have it's own VBO.");
					} else if (newG)
						System.out.println("One \"g\" holding 0 elements discarted.");
					// start a new group
					newG = true;
					groups = new ArrayList<String>();
					tempFaces = new ArrayList<Integer>();
					tempFaceCount = 0;
					while (strTokenizer.hasMoreTokens()) {
						groups.add(strTokenizer.nextToken());
					}
				}
			}
			if (tempFaceCount > 0) {
				// It seems that since last starting a new group, there have
				// been faces stored
				// Here I create a new mesh
				Mesh mesh = new Mesh(groups, tempFaces, vertices);
				meshes.add(mesh);
				// After the whole file will be read, each mesh object's faces
				// will be stored as VBOs (Vertex Buffer Objects).
				System.out.println("Created a new mesh java object that will have it's own VBO.");
			} else {
				System.out.println("One \"g\" holding 0 elements discarted.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// The file has been read.
		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes) {
			mesh.constructVBO();
		}
	}

	public void increaseSubdivisionDepth() {
		numberOfSubdivisions = Math.min(APLICATION_SUBDIVISION_LIMIT, numberOfSubdivisions + 1);
		if (maxSubDepth < numberOfSubdivisions) {
			for (Mesh mesh : meshes)
				mesh.increaseMaxSubdivision();
			maxSubDepth++;
		}
	}

	public void decreaseSubdivisionDepth() {
		numberOfSubdivisions = Math.max(0, numberOfSubdivisions - 1);
	}

	public void render() {
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		/* Apply orientation (add rotations) */
		Quaternion compositeOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		FloatBuffer fb = compositeOrientation.getRotationMatrix(false);
		GL11.glMultMatrix(fb);

		/* Translate and render */
		glTranslatef(-(float) centerx, -(float) centery, -(float) centerz);
		for (Mesh vbo : meshes) {
			vbo.render(numberOfSubdivisions);
		}

		glPopMatrix();
	}

	/**
	 * Change added orientation (rotations) of the model
	 */
	public void changeAddedOrientation(VeinsRenderer renderer) {
		double[] veinsHeldAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);

		if (veinsHeldAt != null) {
			double[] rotationAxis = Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
			if (Vector.length(rotationAxis) > 0) {
				rotationAxis = Vector.normalize(rotationAxis);
				rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);
				double angle = Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt)
						/ (Vector.length(veinsGrabbedAt) * Vector.length(veinsHeldAt)));
				addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle, rotationAxis);
			}
		}
	}

	private void setDefaultOrientation() {
		double angle1 = Math.toRadians(-90); // Math.PI * -90 / 180;
		double angle2 = Math.toRadians(180); // Math.PI * 180 / 180;
		currentOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle1, new double[] { 1, 0, 0 });
		double[] v = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(new double[] { 0, 1, 0 });
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation,
				Quaternion.quaternionFromAngleAndRotationAxis(angle2, v));
		addedOrientation = new Quaternion();
	}

	public void normalizeCurrentOrientation() {
		currentOrientation = Quaternion.quaternionNormalization(currentOrientation);
	}

	public void normalizeAddedOrientation() {
		addedOrientation = Quaternion.quaternionNormalization(addedOrientation);
	}

	/**
	 * Saves the current orientation of the model in currenOrientation
	 */
	public void saveCurrentOrientation() {
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
	}

	public void setCurrentOrientation(Quaternion q) {
		currentOrientation = q;
	}

	public void setAddedOrientation(Quaternion q) {
		addedOrientation = q;
	}

	public void deleteMeshes() {
		if (meshes != null) {
			for (Mesh m : meshes) {
				m.deleteVBO();
			}
		}
	}

	public void rotateModel3D(double[] rot, VeinsRenderer renderer) {

		double[] centerVector = RayUtil.getRayDirection((int) VeinsWindow.settings.resWidth / 2,
				(int) VeinsWindow.settings.resHeight / 2, renderer);

		Quaternion temp = new Quaternion();
		double[] rotationAxis;

		rotationAxis = Vector.crossProduct(centerVector, new double[] { 0, 1, 0 });
		rotationAxis = Vector.normalize(rotationAxis);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(rot[0], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

		rotationAxis = Vector.crossProduct(centerVector, new double[] { 1, 0, 0 });
		rotationAxis = Vector.normalize(rotationAxis);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(rot[2], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

		rotationAxis = Vector.crossProduct(centerVector, new double[] { 0, 0, 1 });
		rotationAxis = Vector.normalize(centerVector);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(-rot[1], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

	}

}
