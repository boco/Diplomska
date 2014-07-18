package si.uni_lj.fri.segmentation.utils.obj;

public class Normal {
	public float x;
	public float y;
	public float z;
	public int index;

	public Normal(float x, float y, float z, int index) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.index = index;
	}

	public String toString() {
		return "vn " + x + " " + y + " " + z;
	}
}
