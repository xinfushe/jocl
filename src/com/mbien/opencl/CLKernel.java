package com.mbien.opencl;

import com.sun.gluegen.runtime.BufferFactory;
import com.sun.gluegen.runtime.CPU;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static com.mbien.opencl.CLException.*;

/**
 *
 * @author Michael Bien
 */
public class CLKernel {

    public final long ID;
    public final String name;

    private final CLProgram program;
    private final CL cl;

    CLKernel(CLProgram program, long id) {
        this.ID = id;
        this.program = program;
        this.cl = program.context.cl;

        long[] longArray = new long[1];

        int ret = cl.clGetKernelInfo(ID, CL.CL_KERNEL_FUNCTION_NAME, 0, null, longArray, 0);
        checkForError(ret, "error while asking for kernel function name");

        ByteBuffer bb = ByteBuffer.allocate((int)longArray[0]).order(ByteOrder.nativeOrder());

        ret = cl.clGetKernelInfo(ID, CL.CL_KERNEL_FUNCTION_NAME, bb.capacity(), bb, null, 0);
        checkForError(ret, "error while asking for kernel function name");

        this.name = new String(bb.array(), 0, bb.capacity()).trim();

    }

    public CLKernel setArg(int argumentIndex, CLBuffer<?> value) {
        int ret = cl.clSetKernelArg(ID, argumentIndex, CPU.is32Bit()?4:8, wrap(value.ID));
        checkForError(ret, "error on clSetKernelArg");
        return this;
    }

    public CLKernel setArg(int argumentIndex, int value) {
        int ret = cl.clSetKernelArg(ID, argumentIndex, 4, wrap(value));
        checkForError(ret, "error on clSetKernelArg");
        return this;
    }

    public CLKernel setArg(int argumentIndex, long value) {
        int ret = cl.clSetKernelArg(ID, argumentIndex, 8, wrap(value));
        checkForError(ret, "error on clSetKernelArg");
        return this;
    }

    public CLKernel setArg(int argumentIndex, float value) {
        int ret = cl.clSetKernelArg(ID, argumentIndex, 4, wrap(value));
        checkForError(ret, "error on clSetKernelArg");
        return this;
    }

    public CLKernel setArg(int argumentIndex, double value) {
        int ret = cl.clSetKernelArg(ID, argumentIndex, 8, wrap(value));
        checkForError(ret, "error on clSetKernelArg");
        return this;
    }

    private final Buffer wrap(float value) {
        return BufferFactory.newDirectByteBuffer(4).putFloat(value).rewind();
    }

    private final Buffer wrap(double value) {
        return BufferFactory.newDirectByteBuffer(8).putDouble(value).rewind();
    }

    private final Buffer wrap(int value) {
        return BufferFactory.newDirectByteBuffer(4).putInt(value).rewind();
    }

    private final Buffer wrap(long value) {
        return BufferFactory.newDirectByteBuffer(8).putLong(value).rewind();
    }

    /**
     * Releases all resources of this kernel from its context.
     */
    public void release() {
        int ret = cl.clReleaseKernel(ID);
        program.onKernelReleased(this);
        checkForError(ret, "can not release kernel");
    }

    @Override
    public String toString() {
        return "CLKernel [id: " + ID
                      + " name: " + name+"]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CLKernel other = (CLKernel) obj;
        if (this.ID != other.ID) {
            return false;
        }
        if (!this.program.equals(other.program)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.ID ^ (this.ID >>> 32));
        hash = 43 * hash + (this.program != null ? this.program.hashCode() : 0);
        return hash;
    }

}
