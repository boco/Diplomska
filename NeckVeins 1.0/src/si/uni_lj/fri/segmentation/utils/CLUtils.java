package si.uni_lj.fri.segmentation.utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
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

public class CLUtils {

	/**
	 * @param platform
	 * @param devices
	 * @param context
	 * @param queue
	 * @throws LWJGLException
	 */
	public static void initializeCL(CLPlatform platform, List<CLDevice> devices, CLContext context, CLCommandQueue queue)
			throws LWJGLException {
		IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
		CL.create();
		platform = CLPlatform.getPlatforms().get(0);
		devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
		context = CLContext.create(platform, devices, errorBuf);
		queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
		Util.checkCLError(errorBuf.get(0));
	}

	/**
	 * @param kernelFile
	 * @param context
	 * @param devices
	 * @return
	 */
	public static CLProgram createProgram(String kernelFile, CLContext context, List<CLDevice> devices) {
		CLProgram program = CL10.clCreateProgramWithSource(context, FileUtils.loadText(kernelFile), null);
		int error = CL10.clBuildProgram(program, devices.get(0), "", null);
		checkProgramError(error, program, devices);
		return program;
	}

	/**
	 * @param error
	 * @param program
	 * @param devices
	 */
	private static void checkProgramError(int error, CLProgram program, List<CLDevice> devices) {
		// Print sintax errors
		if (error == CL10.CL_BUILD_PROGRAM_FAILURE) {
			PointerBuffer bbuff_size = BufferUtils.createPointerBuffer(1);
			bbuff_size.put(0, 10000);
			ByteBuffer bbuff = BufferUtils.createByteBuffer(10000);
			CL10.clGetProgramBuildInfo(program, devices.get(0), CL10.CL_PROGRAM_BUILD_LOG, bbuff, bbuff_size);
			bbuff.rewind();
			byte[] bytearr = new byte[bbuff.remaining()];
			bbuff.get(bytearr);
			String s = new String(bytearr);
			System.out.println(s);
		}
		// Check other errors
		Util.checkCLError(error);
	}

	/**
	 * @param memObj
	 * @param kernelObj
	 * @param programObj
	 */
	public static void cleanCLResources(CLMem[] memObj, CLKernel[] kernelObj, CLProgram[] programObj) {
		if (kernelObj != null) {
			for (int i = 0; i < kernelObj.length; i++) {
				CL10.clReleaseKernel(kernelObj[i]);
			}
		}

		if (memObj != null) {
			for (int i = 0; i < memObj.length; i++) {
				CL10.clReleaseMemObject(memObj[i]);
			}
		}

		if (programObj != null) {
			for (int i = 0; i < programObj.length; i++) {
				CL10.clReleaseProgram(programObj[i]);
			}
		}
	}

	/**
	 * @param queue
	 * @param context
	 */
	public static void destroyCL(CLCommandQueue queue, CLContext context) {
		CL10.clReleaseCommandQueue(queue);
		CL10.clReleaseContext(context);
		CL.destroy();
	}

	/**
	 * @param data
	 * @param flags
	 * @param queue
	 * @param context
	 * @return
	 */
	public static CLMem locateMemory(float[] data, int flags, CLCommandQueue queue, CLContext context) {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		CLMem memory = CL10.clCreateBuffer(context, flags, buffer.capacity() * 4, errorBuff);
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));

		// Dereference buffer
		buffer = null;

		return memory;
	}

	/**
	 * @param data
	 * @param flags
	 * @param queue
	 * @param context
	 * @return
	 */
	public static CLMem locateMemory(int[] data, int flags, CLCommandQueue queue, CLContext context) {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.rewind();
		CLMem memory = CL10.clCreateBuffer(context, flags, buffer.capacity() * 4, errorBuff);
		CL10.clEnqueueWriteBuffer(queue, memory, CL10.CL_TRUE, 0, buffer, null, null);
		Util.checkCLError(errorBuff.get(0));

		// Dereference buffer
		buffer = null;

		return memory;
	}

	/**
	 * @param nBytes
	 * @param flags
	 * @param context
	 * @return
	 */
	public static CLMem locateMemory(int nBytes, int flags, CLContext context) {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		CLMem memory = CL10.clCreateBuffer(context, flags, nBytes, errorBuff);
		Util.checkCLError(errorBuff.get(0));
		return memory;
	}

	/**
	 * @param src
	 * @param queue
	 * @param context
	 * @return
	 */
	public static CLMem copyMemory(CLMem src, CLCommandQueue queue, CLContext context) {
		IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
		int memSize = (int) src.getInfoSize(CL10.CL_MEM_SIZE);
		CLMem dst = locateMemory(memSize, CL10.CL_MEM_READ_WRITE, context);
		CL10.clEnqueueCopyBuffer(queue, src, dst, 0, 0, memSize, null, null);
		Util.checkCLError(errorBuff.get(0));
		return dst;
	}

	/**
	 * @param kernel
	 * @param dimensions
	 * @param queue
	 */
	public static void enqueueKernel(CLKernel kernel, int[] dimensions, CLCommandQueue queue) {
		final int dim = dimensions.length;
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dim);
		for (int i = 0; i < dim; i++) {
			globalWorkSize.put(i, dimensions[i]);
		}
		CL10.clEnqueueNDRangeKernel(queue, kernel, dim, null, globalWorkSize, null, null, null);
	}

	/**
	 * @param kernel
	 * @param globalWorkDimensions
	 * @param localWorkDimension
	 * @param queue
	 */
	public static void enqueueKernel(CLKernel kernel, int[] globalWorkDimensions, int[] localWorkDimension,
			CLCommandQueue queue) {
		final int dim = globalWorkDimensions.length;
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dim);
		for (int i = 0; i < dim; i++) {
			globalWorkSize.put(i, globalWorkDimensions[i]);
		}
		PointerBuffer localWorkSize = BufferUtils.createPointerBuffer(localWorkDimension.length);
		for (int i = 0; i < localWorkDimension.length; i++) {
			localWorkSize.put(i, localWorkDimension[i]);
		}
		CL10.clEnqueueNDRangeKernel(queue, kernel, dim, null, globalWorkSize, localWorkSize, null, null);
	}

}
