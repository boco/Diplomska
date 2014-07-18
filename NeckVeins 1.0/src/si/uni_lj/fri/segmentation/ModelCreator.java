package si.uni_lj.fri.segmentation;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

import si.uni_lj.fri.segmentation.utils.CLUtils;
import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.Utils;

/**
 * TODO FIX execFindMax, REFACTOR
 * 
 * 
 */
public class ModelCreator {
	private static final int MATRIX_DATA = 0;
	private static final int DIMENSIONS_DATA = 1;

	// private static final int FIND_MAX_LOCAL_SIZE = 512;
	private static final int HISTOGRAM_LOCAL_SIZE = 64;

	// OpenCL variables
	private static CLContext context;
	private static CLPlatform platform;
	private static List<CLDevice> devices;
	private static CLCommandQueue queue;
	private static CLProgram program;
	private static int matrixSize = 0;
	private static float max = 0;
	private static CLMem[] staticMemory;

	public ModelCreator() {
	}

	/**
	 * Called when loading from file
	 * 
	 * @param fileName
	 * @param sigma
	 * @param threshold
	 * @return
	 * @throws LWJGLException
	 */
	public static Object[] createModel(String fileName, double sigma, double threshold) throws LWJGLException {
		clearOldProgram();
		initializeCL();
		program = CLUtils.createProgram("/opencl/segmentation.cls", context, devices);
		initStaticData(fileName, sigma);
		execGauss3D(sigma);
		long t = Utils.startTime();
		max = execFindMax();
		Utils.endTime(t, "Max");
		System.out.println(max);
		threshold = execOtsuThreshold((float) threshold);
		Object[] output = execMarchingCubes((float) threshold);
		return output;
	}

	/**
	 * Called when changing threshold in main window
	 * 
	 * NOTE: because we are changing threshold of already loaded model, data is
	 * already on GPU
	 * 
	 * @param threshold
	 * @return
	 * @throws LWJGLException
	 */
	public static Object[] changeModel(double threshold) throws LWJGLException {
		System.out.println("Marching cubes on GPU...");
		return execMarchingCubes((float) threshold);
	}

	private static void execGauss3D(double sigma) {
		if (sigma < 0.1)
			return;

		// Init kernels
		CLKernel gaussX = CL10.clCreateKernel(program, "gaussX", null);
		CLKernel gaussY = CL10.clCreateKernel(program, "gaussY", null);
		CLKernel gaussZ = CL10.clCreateKernel(program, "gaussZ", null);

		// MATRIX
		CLMem kernel = CLUtils.locateMemory(Utils.getGauss1DKernel(sigma), CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem tmpMemory = CLUtils.copyMemory(staticMemory[MATRIX_DATA], queue, context);
		Util.checkCLError(CL10.clFinish(queue));

		// GAUSS 3D
		gaussX.setArg(0, tmpMemory);
		gaussX.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussX.setArg(2, kernel);
		gaussX.setArg(3, staticMemory[MATRIX_DATA]);
		CLUtils.enqueueKernel(gaussX, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz }, queue);
		Util.checkCLError(CL10.clFinish(queue));
		gaussY.setArg(0, staticMemory[MATRIX_DATA]);
		gaussY.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussY.setArg(2, kernel);
		gaussY.setArg(3, tmpMemory);
		CLUtils.enqueueKernel(gaussY, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz }, queue);
		Util.checkCLError(CL10.clFinish(queue));
		gaussZ.setArg(0, tmpMemory);
		gaussZ.setArg(1, staticMemory[DIMENSIONS_DATA]);
		gaussZ.setArg(2, kernel);
		gaussZ.setArg(3, staticMemory[MATRIX_DATA]);
		CLUtils.enqueueKernel(gaussZ, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz }, queue);
		Util.checkCLError(CL10.clFinish(queue));

		CLUtils.cleanCLResources(new CLMem[] { tmpMemory, kernel }, new CLKernel[] { gaussX, gaussY, gaussZ }, null);
		Util.checkCLError(CL10.clFinish(queue));
	}

	private static float execFindMax() {
		// Get device info
		ByteBuffer deviceInfoBuff = BufferUtils.createByteBuffer(8);
		CL10.clGetDeviceInfo(devices.get(0), CL10.CL_DEVICE_MAX_COMPUTE_UNITS, deviceInfoBuff, null);
		final int MAX_COMPUTE_UNITS = (int) deviceInfoBuff.getLong(0);
		deviceInfoBuff.clear();
		CL10.clGetDeviceInfo(devices.get(0), CL10.CL_DEVICE_LOCAL_MEM_SIZE, deviceInfoBuff, null);
		final int LOCAL_MEM_SIZE = (int) deviceInfoBuff.getLong(0);
		deviceInfoBuff.clear();
		CL10.clGetDeviceInfo(devices.get(0), CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE, deviceInfoBuff, null);
		final int MAX_WORK_GROUP_SIZE = (int) deviceInfoBuff.getLong(0);

		int nLocalWorkItems = MAX_WORK_GROUP_SIZE;
		int nWorkGroups = LOCAL_MEM_SIZE * (MAX_COMPUTE_UNITS + 2) / (nLocalWorkItems * 4);
		int nGlobalWorkItems = nLocalWorkItems * nWorkGroups;

		CLKernel findMax = CL10.clCreateKernel(program, "findMax", null);
		CLMem maxMemory = CLUtils.locateMemory(nWorkGroups * 4, CL10.CL_MEM_WRITE_ONLY, context);

		findMax.setArg(0, staticMemory[MATRIX_DATA]);
		findMax.setArg(1, staticMemory[DIMENSIONS_DATA]);
		CL10.clSetKernelArg(findMax, 2, nLocalWorkItems);
		findMax.setArg(3, maxMemory);

		CLUtils.enqueueKernel(findMax, new int[] { nGlobalWorkItems }, new int[] { nLocalWorkItems }, queue);
		Util.checkCLError(CL10.clFinish(queue));

		FloatBuffer buffer = BufferUtils.createFloatBuffer(nWorkGroups);
		CL10.clEnqueueReadBuffer(queue, maxMemory, CL10.CL_TRUE, 0, buffer, null, null);
		float max = 0;
		for (int i = 0; i < buffer.capacity(); i++) {
			if (buffer.get(i) > max)
				max = buffer.get(i);
		}

		Util.checkCLError(CL10.clReleaseKernel(findMax));
		Util.checkCLError(CL10.clReleaseMemObject(maxMemory));
		Util.checkCLError(CL10.clFinish(queue));

		return max;
	}

	private static double execOtsuThreshold(double threshold) {
		if (threshold > 0.1)
			return threshold;
		IntBuffer histogram = execOtsuHistogram();
		return Graytresh.thresholdFromHistogram(histogram, MHDReader.Nx * MHDReader.Ny * MHDReader.Nz);
	}

	private static IntBuffer execOtsuHistogram() {
		int localGroupSize = HISTOGRAM_LOCAL_SIZE;

		CLKernel otsuHistogram = CL10.clCreateKernel(program, "otsuHistogram", null);
		CLMem maxValueMemory = CLUtils.locateMemory(new float[] { max }, CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem otsuHistogramMemory = CLUtils.locateMemory(new int[256], CL10.CL_MEM_READ_WRITE, queue, context);
		Util.checkCLError(CL10.clFinish(queue));

		otsuHistogram.setArg(0, staticMemory[MATRIX_DATA]);
		otsuHistogram.setArg(1, staticMemory[DIMENSIONS_DATA]);
		otsuHistogram.setArg(2, maxValueMemory);
		otsuHistogram.setArg(3, otsuHistogramMemory);

		CLUtils.enqueueKernel(otsuHistogram, new int[] { localGroupSize * 10000 }, new int[] { localGroupSize }, queue);
		Util.checkCLError(CL10.clFinish(queue));

		IntBuffer histogram = BufferUtils.createIntBuffer(256);
		CL10.clEnqueueReadBuffer(queue, otsuHistogramMemory, CL10.CL_TRUE, 0, histogram, null, null);
		Util.checkCLError(CL10.clReleaseKernel(otsuHistogram));
		Util.checkCLError(CL10.clReleaseMemObject(maxValueMemory));
		Util.checkCLError(CL10.clReleaseMemObject(otsuHistogramMemory));
		Util.checkCLError(CL10.clFinish(queue));

		return histogram;
	}

	private static Object[] execMarchingCubes(float threshold) {
		CLKernel marchingKernel = CL10.clCreateKernel(program, "marchingCubes", null);
		CLMem maxThreshMemory = CLUtils.locateMemory(new float[] { max, threshold }, CL10.CL_MEM_READ_WRITE, queue,
				context);
		CLMem trianglesMemory = CLUtils.locateMemory((int) (matrixSize / 1.25f), CL10.CL_MEM_WRITE_ONLY, context);
		CLMem normalsMemory = CLUtils.locateMemory((int) (matrixSize / 1.25f), CL10.CL_MEM_WRITE_ONLY, context);
		CLMem nTrianglesMemory = CLUtils.locateMemory(new int[1], CL10.CL_MEM_READ_WRITE, queue, context);
		Util.checkCLError(CL10.clFinish(queue));

		// Set the kernel parameters
		marchingKernel.setArg(0, staticMemory[MATRIX_DATA]);
		marchingKernel.setArg(1, staticMemory[DIMENSIONS_DATA]);
		marchingKernel.setArg(2, maxThreshMemory);
		marchingKernel.setArg(3, trianglesMemory);
		marchingKernel.setArg(4, normalsMemory);
		marchingKernel.setArg(5, nTrianglesMemory);
		CLUtils.enqueueKernel(marchingKernel, new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz }, queue);
		Util.checkCLError(CL10.clFinish(queue));

		IntBuffer nTrianglesBuff = BufferUtils.createIntBuffer(1);
		CL10.clEnqueueReadBuffer(queue, nTrianglesMemory, CL10.CL_TRUE, 0, nTrianglesBuff, null, null);
		FloatBuffer trianglesBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		FloatBuffer normalsBuff = BufferUtils.createFloatBuffer(nTrianglesBuff.get(0) * 9);
		CL10.clEnqueueReadBuffer(queue, trianglesMemory, CL10.CL_TRUE, 0, trianglesBuff, null, null);
		CL10.clEnqueueReadBuffer(queue, normalsMemory, CL10.CL_TRUE, 0, normalsBuff, null, null);

		CLMem[] memObj = { maxThreshMemory, trianglesMemory, normalsMemory, nTrianglesMemory };
		CLKernel[] kernelObj = { marchingKernel };
		CLUtils.cleanCLResources(memObj, kernelObj, null);

		return new Object[] { nTrianglesBuff, trianglesBuff, normalsBuff, threshold };

	}

	/**
	 * @throws LWJGLException
	 */
	public static void initializeCL() throws LWJGLException {
		IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
		CL.create();
		platform = CLPlatform.getPlatforms().get(0);
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
		context = CLContext.create(platform, devices, errorBuf);
		queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
		Util.checkCLError(errorBuf.get(0));
	}

	/**
	 * 
	 */
	private static void clearOldProgram() {
		if (staticMemory != null) {
			CLUtils.cleanCLResources(staticMemory, null, new CLProgram[] { program });
			CLUtils.destroyCL(queue, context);
		}
	}

	/**
	 * @param fileName
	 * @param sigma
	 */
	private static void initStaticData(String fileName, double sigma) {
		float[] matrix = FileUtils.readFile(fileName);
		matrixSize = matrix.length;
		int gaussSize = (int) (2 * Math.ceil(3 * sigma / MHDReader.dx) + 1);
		int[] dimensions = new int[] { MHDReader.Nx, MHDReader.Ny, MHDReader.Nz, gaussSize };
		CLMem dimensionsMemory = CLUtils.locateMemory(dimensions, CL10.CL_MEM_READ_ONLY, queue, context);
		CLMem matrixMemory = CLUtils.locateMemory(matrix, CL10.CL_MEM_READ_WRITE, queue, context);
		staticMemory = new CLMem[] { matrixMemory, dimensionsMemory };
	}

}
