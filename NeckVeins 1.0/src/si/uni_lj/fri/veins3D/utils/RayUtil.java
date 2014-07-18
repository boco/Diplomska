package si.uni_lj.fri.veins3D.utils;

import si.uni_lj.fri.veins3D.gui.VeinsWindow;
import si.uni_lj.fri.veins3D.gui.render.Camera;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.math.Vector;

public class RayUtil {

	/**
	 * @param x
	 * @param y
	 * @param renderer
	 * @return
	 */
	public static double[] getRaySphereIntersection(int x, int y, VeinsRenderer renderer) {
		// figure out if the click on the screen intersects the circle that
		// surrounds the veins model
		Camera cam = renderer.getCamera();
		double veinsRadius = renderer.getVeinsModel().veinsGrabRadius;

		// get the direction of the "ray" cast from the camera location
		double[] d = getRayDirection(x, y, renderer);

		// a vector representing the camera location
		double[] e = new double[] { cam.cameraX, cam.cameraY, cam.cameraZ };

		// the location of the sphere is the zero vector
		double[] c = new double[3];
		// partial calculations
		double[] eSc = Vector.subtraction(e, c);
		double dDPeSc = Vector.dotProduct(d, eSc);
		double discriminant = dDPeSc * dDPeSc - Vector.dotProduct(d, d)
				* (Vector.dotProduct(eSc, eSc) - veinsRadius * veinsRadius);

		// in this case the mouse is not pressed near the veins sphere
		if (discriminant < 0) {
			return null;
			// in this case we hold the mouse on the sphere surrounding the
			// veins model in some way
		} else {
			// partial calculation
			double[] Sd = Vector.subtraction(new double[3], d);
			// t1 and t2 are the parameter values for vor "ray" expression e+t*d
			double t1 = (Vector.dotProduct(Sd, eSc) + Math.sqrt(discriminant)) / Vector.dotProduct(d, d);
			double t2 = (Vector.dotProduct(Sd, eSc) - Math.sqrt(discriminant)) / Vector.dotProduct(d, d);

			if (t2 < 0)
				return Vector.sum(e, Vector.vScale(d, t1));
			else
				return Vector.sum(e, Vector.vScale(d, t2));
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param renderer
	 * @return
	 */
	public static double[] getRayDirection(int x, int y, VeinsRenderer renderer) {
		Camera cam = renderer.getCamera();
		double[] tempUpperLeft = cam.cameraOrientation.rotateVector3d(renderer.screenPlaneInitialUpperLeft);
		double[] tempLowerLeft = cam.cameraOrientation.rotateVector3d(renderer.screenPlaneInitialLowerLeft);
		double[] tempLowerRight = cam.cameraOrientation.rotateVector3d(renderer.screenPlaneInitialLowerRight);

		double[] leftToRight = Vector.subtraction(tempLowerRight, tempLowerLeft);
		leftToRight = Vector.vScale(leftToRight, (0.5d + x) / (double) VeinsWindow.settings.resWidth);
		double[] rayD = Vector.sum(tempLowerLeft, leftToRight);

		double[] downToUp = Vector.subtraction(tempUpperLeft, tempLowerLeft);
		downToUp = Vector.vScale(downToUp, (0.5d + y) / (double) VeinsWindow.settings.resHeight);

		rayD = Vector.sum(rayD, downToUp);
		
		return rayD;
	}

}
