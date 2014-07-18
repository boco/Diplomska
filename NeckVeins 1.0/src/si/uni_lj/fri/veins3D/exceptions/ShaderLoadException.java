package si.uni_lj.fri.veins3D.exceptions;

public class ShaderLoadException extends Exception {
	private static final long serialVersionUID = 1L;

	public ShaderLoadException() {
		super();
	}

	public ShaderLoadException(String message) {
		super(message);
	}

	public ShaderLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShaderLoadException(Throwable cause) {
		super(cause);
	}

}
