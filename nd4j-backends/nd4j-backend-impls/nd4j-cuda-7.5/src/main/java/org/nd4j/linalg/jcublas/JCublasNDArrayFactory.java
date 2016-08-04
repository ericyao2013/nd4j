/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package org.nd4j.linalg.jcublas;

import org.apache.commons.math3.util.Pair;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.LongPointer;
import org.bytedeco.javacpp.PointerPointer;
import org.nd4j.jita.allocator.impl.AllocationPoint;
import org.nd4j.jita.allocator.impl.AtomicAllocator;
import org.nd4j.jita.allocator.pointers.CudaPointer;
import org.nd4j.linalg.cache.TADManager;
import org.nd4j.jita.allocator.utils.AllocationUtils;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.complex.IComplexDouble;
import org.nd4j.linalg.api.complex.IComplexFloat;
import org.nd4j.linalg.api.complex.IComplexNDArray;
import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.BaseNDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.jcublas.blas.JcublasLapack;
import org.nd4j.linalg.jcublas.blas.JcublasLevel1;
import org.nd4j.linalg.jcublas.blas.JcublasLevel2;
import org.nd4j.linalg.jcublas.blas.JcublasLevel3;
import org.nd4j.linalg.jcublas.buffer.AddressRetriever;
import org.nd4j.linalg.jcublas.buffer.CudaDoubleDataBuffer;
import org.nd4j.linalg.jcublas.buffer.CudaFloatDataBuffer;
import org.nd4j.linalg.jcublas.buffer.CudaIntDataBuffer;
import org.nd4j.linalg.jcublas.complex.ComplexDouble;
import org.nd4j.linalg.jcublas.complex.ComplexFloat;
import org.nd4j.linalg.jcublas.complex.JCublasComplexNDArray;
import org.nd4j.linalg.jcublas.context.CudaContext;
import org.nd4j.linalg.jcublas.ops.executioner.JCudaExecutioner;
import org.nd4j.linalg.util.ArrayUtil;
import org.nd4j.nativeblas.NativeOps;
import org.nd4j.nativeblas.NativeOpsHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Jcublas ndarray factory. Handles creation of
 * jcuda.jcublas ndarrays.
 *
 * @author mjk
 */
public class JCublasNDArrayFactory extends BaseNDArrayFactory {
    private NativeOps nativeOps = NativeOpsHolder.getInstance().getDeviceNativeOps();


    public JCublasNDArrayFactory() {
    }

    public JCublasNDArrayFactory(DataBuffer.Type dtype, Character order) {
        super(dtype, order);
    }

    public JCublasNDArrayFactory(DataBuffer.Type dtype, char order) {
        super(dtype, order);
    }

    @Override
    public void createLevel1() {
        level1 = new JcublasLevel1();
    }

    @Override
    public void createLevel2() {
        level2 = new JcublasLevel2();
    }

    @Override
    public void createLevel3() {
        level3 = new JcublasLevel3();
    }

    @Override
    public void createLapack() {
        lapack = new JcublasLapack();
    }

    @Override
    public INDArray create(int[] shape, DataBuffer buffer) {
        return new JCublasNDArray(shape, buffer);
    }

    /**
     * Create float
     *
     * @param real real component
     * @param imag imag component
     * @return
     */
    @Override
    public IComplexFloat createFloat(float real, float imag) {
        return new ComplexFloat(real, imag);
    }

    /**
     * Create an instance of a complex double
     *
     * @param real the real component
     * @param imag the imaginary component
     * @return a new imaginary double with the specified real and imaginary components
     */
    @Override
    public IComplexDouble createDouble(double real, double imag) {
        return new ComplexDouble(real, imag);
    }

    /**
     * Create an ndarray with the given data layout
     *
     * @param data the data to create the ndarray with
     * @return the ndarray with the given data layout
     */
    @Override
    public INDArray create(double[][] data) {
        return new JCublasNDArray(data);
    }

    @Override
    public INDArray create(double[][] data, char ordering) {
        return new JCublasNDArray(data,ordering);
    }

    /**
     * Create a complex ndarray from the passed in indarray
     *
     * @param arr the arr to wrap
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
    @Override
    public IComplexNDArray createComplex(INDArray arr) {
        return new JCublasComplexNDArray(arr);
    }

    /**
     * Create a complex ndarray from the passed in indarray
     *
     * @param data  the data to wrap
     * @param shape
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape) {
        return new JCublasComplexNDArray(data, shape,Nd4j.getComplexStrides(shape,Nd4j.order()));
    }

    /**
     * Create a complex ndarray from the passed in indarray
     *
     * @param arrs  the arr to wrap
     * @param shape
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
    @Override
    public IComplexNDArray createComplex(List<IComplexNDArray> arrs, int[] shape) {
        return new JCublasComplexNDArray(arrs, shape);
    }

    @Override
    public INDArray create(DataBuffer data) {
        return new JCublasNDArray(data);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data) {
        return new JCublasComplexNDArray(data);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data, int rows, int columns, int[] stride, int offset) {
        return new JCublasComplexNDArray(data, new int[]{rows, columns}, stride, offset);
    }

    @Override
    public INDArray create(DataBuffer data, int rows, int columns, int[] stride, int offset) {
        return new JCublasNDArray(data, new int[]{rows, columns}, stride, offset);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data, int[] shape, int[] stride, int offset) {
        return new JCublasComplexNDArray(data, shape, stride, offset);
    }

    /**
     * Creates a complex ndarray with the specified shape
     *
     * @param data   the data to use with the ndarray
     * @param shape  the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
    @Override
    public IComplexNDArray createComplex(float[] data, int[] shape, int[] stride, int offset) {
        return new JCublasComplexNDArray(data, shape, stride, offset);
    }

    @Override
    public INDArray create(int[] shape, char ordering) {
        return new JCublasNDArray(shape, ordering);
    }

    @Override
    public INDArray createUninitialized(int[] shape, char ordering){
        return new JCublasNDArray(shape,  Nd4j.getStrides(shape, ordering), 0, ordering, false);
    }

    @Override
    public INDArray create(DataBuffer data, int[] newShape, int[] newStride, int offset, char ordering) {
        return new JCublasNDArray(data, newShape, newStride, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data, int[] newDims, int[] newStrides, int offset, char ordering) {
        return new JCublasComplexNDArray(data, newDims, newStrides, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(float[] data, Character order) {
        return new JCublasComplexNDArray(data, order);
    }

    @Override
    public INDArray create(float[] data, int[] shape, int offset, Character order) {
        return new JCublasNDArray(data, shape, offset, order);
    }

    @Override
    public INDArray create(float[] data, int rows, int columns, int[] stride, int offset, char ordering) {
        return new JCublasNDArray(data, new int[]{rows, columns}, stride, offset, ordering);
    }

    @Override
    public INDArray create(double[] data, int[] shape, char ordering) {
        return new JCublasNDArray(data, shape, ordering);
    }

    @Override
    public INDArray create(List<INDArray> list, int[] shape, char ordering) {
        return new JCublasNDArray(list, shape, ordering);
    }

    @Override
    public INDArray create(double[] data, int[] shape, int offset) {
        return new JCublasNDArray(data, shape, (char) offset);
    }

    @Override
    public INDArray create(double[] data, int[] shape, int[] stride, int offset, char ordering) {
        return new JCublasNDArray(data, shape, stride, offset, ordering);
    }


    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape, int[] stride, int offset) {
        return new JCublasComplexNDArray(data, shape, stride, offset);
    }

    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape, int[] stride, int offset, char ordering) {
        return new JCublasComplexNDArray(data, shape, stride, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape, int[] stride, char ordering) {
        return new JCublasComplexNDArray(data, shape, stride, ordering);
    }

    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape, int offset, char ordering) {
        return new JCublasComplexNDArray(data, shape, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(IComplexNumber[] data, int[] shape, char ordering) {
        return new JCublasComplexNDArray(data, shape, ordering);
    }

    /**
     * Creates an ndarray with the specified shape
     *
     * @param data
     * @param shape  the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
    @Override
    public INDArray create(float[] data, int[] shape, int[] stride, int offset) {
        return new JCublasNDArray(data, shape, stride, offset);
    }

    /**
     * Creates a complex ndarray with the specified shape
     *
     * @param data
     * @param shape  the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
    @Override
    public IComplexNDArray createComplex(double[] data, int[] shape, int[] stride, int offset) {
        return new JCublasComplexNDArray(ArrayUtil.floatCopyOf(data), shape, stride, offset);
    }

    /**
     * Creates an ndarray with the specified shape
     *
     * @param data
     * @param shape  the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
    @Override
    public INDArray create(double[] data, int[] shape, int[] stride, int offset) {
        return new JCublasNDArray(data, shape, stride, offset);
    }

    @Override
    public INDArray create(DataBuffer data, int[] shape) {
        return new JCublasNDArray(data, shape);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data, int[] shape) {
        return new JCublasComplexNDArray(data, shape);

    }

    @Override
    public IComplexNDArray createComplex(DataBuffer data, int[] shape, int[] stride) {
        return new JCublasComplexNDArray(data, shape, stride);
    }

    @Override
    public INDArray create(DataBuffer data, int[] shape, int[] stride, int offset) {
        return new JCublasNDArray(data, shape, stride, offset);
    }

    /**
     * Creates an ndarray with the specified shape
     *
     * @param list
     * @param shape the shape of the ndarray
     * @return the instance
     */
    @Override
    public INDArray create(List<INDArray> list, int[] shape) {
        if (order == FORTRAN)
            return new JCublasNDArray(list, shape, ArrayUtil.calcStridesFortran(shape));
        else
            return new JCublasNDArray(list, shape);
    }

    @Override
    public IComplexNDArray createComplex(double[] data, int[] shape, int[] stride, int offset, char ordering) {
        return new JCublasComplexNDArray(ArrayUtil.floatCopyOf(data), shape, stride, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(double[] data, int[] shape, int offset, char ordering) {
        return new JCublasComplexNDArray(ArrayUtil.floatCopyOf(data), shape, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer buffer, int[] shape, int offset, char ordering) {
        return new JCublasComplexNDArray(buffer, shape, offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(double[] data, int[] shape, int offset) {
        return new JCublasComplexNDArray(ArrayUtil.floatCopyOf(data), shape, offset);
    }

    @Override
    public IComplexNDArray createComplex(DataBuffer buffer, int[] shape, int offset) {
        return new JCublasComplexNDArray(buffer, shape, offset);
    }

    @Override
    public INDArray create(float[] data, int[] shape, int offset) {
        return new JCublasNDArray(data, shape, offset);
    }

    @Override
    public IComplexNDArray createComplex(float[] data, int[] shape, int offset, char ordering) {
        return new JCublasComplexNDArray(data, shape, Nd4j.getComplexStrides(shape, ordering), offset, ordering);
    }

    @Override
    public IComplexNDArray createComplex(float[] data, int[] shape, int offset) {
        return new JCublasComplexNDArray(data, shape, offset);
    }

    @Override
    public IComplexNDArray createComplex(float[] data, int[] shape, int[] stride, int offset, char ordering) {
        return new JCublasComplexNDArray(data, shape, stride, offset, ordering);
    }

    @Override
    public INDArray create(float[][] floats) {
        return new JCublasNDArray(floats);
    }

    @Override
    public INDArray create(float[][] data, char ordering) {
        return new JCublasNDArray(data,ordering);
    }

    @Override
    public IComplexNDArray createComplex(float[] dim) {
        if (dim.length % 2 != 0)
            throw new IllegalArgumentException("Complex nd array buffers must have an even number of elements");
        IComplexNDArray ret = Nd4j.createComplex(dim.length / 2);
        int count = 0;
        for (int i = 0; i < dim.length - 1; i += 2) {
            ret.putScalar(count++, Nd4j.createDouble(dim[i], dim[i + 1]));
        }
        return ret;
    }

    @Override
    public INDArray create(float[] data, int[] shape, int[] stride, int offset, char ordering) {
        return new JCublasNDArray(data, shape, stride, offset, ordering);
    }

    @Override
    public INDArray create(DataBuffer buffer, int[] shape, int offset) {
        return new JCublasNDArray(buffer, shape, offset);
    }


    @Override
    public INDArray toFlattened(Collection<INDArray> matrices) {
        return this.toFlattened(order(),matrices);
    }

    @Override
    public INDArray toFlattened(char order, Collection<INDArray> matrices) {
        int length = 0;
        for (INDArray m : matrices)
            length += m.length();
        INDArray ret = Nd4j.create(new int[]{1,length},order);
        int linearIndex = 0;

        AtomicAllocator allocator = AtomicAllocator.getInstance();


        for(INDArray m : matrices) {

            CudaContext context =  allocator.getFlowController().prepareAction(ret, m);

            if(m.ordering() == order && ret.elementWiseStride() == m.elementWiseStride() && ret.elementWiseStride() == 1) {
                // do memcpy in proper direction and forget about that
                allocator.memcpyAsync(ret.data(),new CudaPointer(allocator.getHostPointer(m).address()), AllocationUtils.getRequiredMemory(AllocationUtils.buildAllocationShape(m)), linearIndex * (m.data().dataType() == DataBuffer.Type.DOUBLE ? 8 : m.data().dataType() == DataBuffer.Type.FLOAT ? 4 : 2));
                linearIndex += m.length();
            } else {
                Pointer hostYShapeInfo = AddressRetriever.retrieveHostPointer(m.shapeInfoDataBuffer());

                PointerPointer extras = new PointerPointer(
                        AddressRetriever.retrieveHostPointer(ret.shapeInfoDataBuffer()),
                        context.getOldStream(),
                        allocator.getDeviceIdPointer(),
                        context.getBufferAllocation(),
                        context.getBufferReduction(),
                        context.getBufferScalar(),
                        context.getBufferSpecial(),
                        hostYShapeInfo,
                        AddressRetriever.retrieveHostPointer(ret.shapeInfoDataBuffer())
                );

                if (m.data().dataType() == DataBuffer.Type.DOUBLE) {
                    nativeOps.flattenDouble(
                            extras,
                            linearIndex,
                            order,
                            allocator.getPointer(ret, context),
                            allocator.getPointer(ret.shapeInfoDataBuffer(), context),
                            allocator.getPointer(m, context),
                            allocator.getPointer(m.shapeInfoDataBuffer(), context));
                } else if (m.data().dataType() == DataBuffer.Type.FLOAT) {
                    nativeOps.flattenFloat(
                            extras,
                            linearIndex,
                            order,
                            allocator.getPointer(ret, context),
                            allocator.getPointer(ret.shapeInfoDataBuffer(), context),
                            allocator.getPointer(m, context),
                            allocator.getPointer(m.shapeInfoDataBuffer(), context));

                } else {
                    nativeOps.flattenHalf(
                            extras,
                            linearIndex,
                            order,
                            allocator.getPointer(ret, context),
                            allocator.getPointer(ret.shapeInfoDataBuffer(), context),
                            allocator.getPointer(m, context),
                            allocator.getPointer(m.shapeInfoDataBuffer(), context));
                }



                //Works for all cases...

               /* NdIndexIterator iter = new NdIndexIterator(order, m.shape());
                while (iter.hasNext()) {
                    ret.putScalar(linearIndex++, m.getDouble(iter.next()));
                }*/

                linearIndex += m.length();
            }

            if (ret != null) allocator.registerAction(context, ret, m);
        }
        return ret;
    }

    @Override
    public INDArray concat(int dimension, INDArray... toConcat) {
        if (toConcat.length == 1)
            return toConcat[0];

        int sumAlongDim = 0;
        for (int i = 0; i < toConcat.length; i++) {
            sumAlongDim += toConcat[i].size(dimension);
        }

        int[] outputShape = ArrayUtil.copy(toConcat[0].shape());

        outputShape[dimension] = sumAlongDim;

        INDArray ret = Nd4j.createUninitialized(outputShape,Nd4j.order());

        AtomicAllocator allocator = AtomicAllocator.getInstance();

        CudaContext context =  allocator.getFlowController().prepareAction(ret, toConcat);

        long[] shapeInfoPointers = new long[toConcat.length];
        long[] dataPointers = new long[toConcat.length];
        long[] tadPointers = new long[toConcat.length];
        long[] offsetsPointers = new long[toConcat.length];
        long[] hostShapeInfoPointers = new long[toConcat.length];

        TADManager tadManager = ((JCudaExecutioner) Nd4j.getExecutioner()).getTadManager();
        for(int i = 0; i < toConcat.length; i++) {
            shapeInfoPointers[i] = AddressRetriever.retrieveDeviceAddress(toConcat[i].shapeInfoDataBuffer(), context);
            dataPointers[i] = AtomicAllocator.getInstance().getPointer(toConcat[i], context).address();
            hostShapeInfoPointers[i] = AtomicAllocator.getInstance().getHostPointer(toConcat[i].shapeInfoDataBuffer()).address();

            sumAlongDim += toConcat[i].size(dimension);
            for(int j = 0; j < toConcat[i].rank(); j++)
                if(j != dimension && toConcat[i].size(j) != outputShape[j]) {
                    throw new IllegalArgumentException("Illegal concatneation at array " + i + " and shape element "  + j);
                }

            Pair<DataBuffer, DataBuffer> tadBuffers = tadManager.getTADOnlyShapeInfo(toConcat[i], new int[]{dimension});

            long devTadShapeInfo = AtomicAllocator.getInstance().getPointer(tadBuffers.getFirst(), context).address();

            DataBuffer offsets = tadBuffers.getSecond();
            long devTadOffsets = AtomicAllocator.getInstance().getPointer(offsets, context).address();

            tadPointers[i] = devTadShapeInfo;
            offsetsPointers[i] = devTadOffsets;

        }

        //System.out.println("shapePointers: " + Arrays.toString(shapeInfoPointers));

        Pointer dZ = AtomicAllocator.getInstance().getPointer(ret, context);
        Pointer dZShapeInfo = AddressRetriever.retrieveDevicePointer(ret.shapeInfoDataBuffer(), context);



        CudaDoubleDataBuffer tempData = new CudaDoubleDataBuffer(toConcat.length);
        CudaDoubleDataBuffer tempShapes = new CudaDoubleDataBuffer(toConcat.length);
        CudaDoubleDataBuffer tempTAD = new CudaDoubleDataBuffer(toConcat.length);
        CudaDoubleDataBuffer tempOffsets = new CudaDoubleDataBuffer(toConcat.length);

        AtomicAllocator.getInstance().memcpyBlocking(tempData, new LongPointer(dataPointers), dataPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempShapes, new LongPointer(shapeInfoPointers), shapeInfoPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempTAD, new LongPointer(tadPointers), tadPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempOffsets, new LongPointer(offsetsPointers), offsetsPointers.length * 8, 0);

        Pointer dataPointer = AtomicAllocator.getInstance().getPointer(tempData, context);
        Pointer shapesPointer = AtomicAllocator.getInstance().getPointer(tempShapes, context);
        Pointer tadPointer = AtomicAllocator.getInstance().getPointer(tempTAD, context);
        Pointer offsetPointer = AtomicAllocator.getInstance().getPointer(tempOffsets, context);


       // System.out.println("ShapesPointer after conversion: " + shapesPointer);

        PointerPointer extras = new PointerPointer(
                AddressRetriever.retrieveHostPointer(ret.shapeInfoDataBuffer()),
                context.getOldStream(),
                allocator.getDeviceIdPointer(),
                context.getBufferAllocation(),
                context.getBufferReduction(),
                context.getBufferScalar(),
                context.getBufferSpecial(),
                AddressRetriever.retrieveHostPointer(toConcat[0].shapeInfoDataBuffer()),
                AddressRetriever.retrieveHostPointer(ret.shapeInfoDataBuffer()),
                new LongPointer(hostShapeInfoPointers)
        );

        if(ret.data().dataType() == DataBuffer.Type.DOUBLE) {
            nativeOps.concatDouble(
                    extras,
                    dimension,
                    toConcat.length,
                    new PointerPointer(new Pointer[] {dataPointer}),
                    new PointerPointer(new Pointer[] {shapesPointer}),
                    dZ,
                    dZShapeInfo,
                    new PointerPointer(new Pointer[] {tadPointer}),
                    new PointerPointer(new Pointer[] {offsetPointer}));
        } else if(ret.data().dataType() == DataBuffer.Type.FLOAT)  {
            nativeOps.concatFloat(
                    extras,
                    dimension,
                    toConcat.length,
                    new PointerPointer(new Pointer[] {dataPointer}),
                    new PointerPointer(new Pointer[] {shapesPointer}),
                    dZ,
                    dZShapeInfo,
                    new PointerPointer(new Pointer[] {tadPointer}),
                    new PointerPointer(new Pointer[] {offsetPointer}));

        }
        else {
            nativeOps.concatHalf(
                    extras,
                    dimension,
                    toConcat.length,
                    new PointerPointer(new Pointer[] {dataPointer}),
                    new PointerPointer(new Pointer[] {shapesPointer}),
                    dZ,
                    dZShapeInfo,
                    new PointerPointer(new Pointer[] {tadPointer}),
                    new PointerPointer(new Pointer[] {offsetPointer}));

        }

        allocator.registerAction(context, ret, toConcat);

        return ret;
        //return super.concat(dimension, toConcat);
    }

    /**
     * This method produces concatenated array, that consist from tensors, fetched from source array, against some dimension and specified indexes
     *
     * @param source          source tensor
     * @param sourceDimension dimension of source tensor
     * @param indexes         indexes from source array
     * @return
     */
    @Override
    public INDArray pullRows(INDArray source, int sourceDimension, int[] indexes) {
        int vectorLength = source.shape()[sourceDimension];
        INDArray ret = Nd4j.createUninitialized(new int[]{indexes.length, vectorLength}, order());

        AtomicAllocator allocator = AtomicAllocator.getInstance();
        CudaContext context =  allocator.getFlowController().prepareAction(ret, source);

        Pointer x = AtomicAllocator.getInstance().getPointer(source, context);
        Pointer xShape = AtomicAllocator.getInstance().getPointer(source.shapeInfoDataBuffer(), context);
        Pointer z = AtomicAllocator.getInstance().getPointer(ret, context);
        Pointer zShape = AtomicAllocator.getInstance().getPointer(ret.shapeInfoDataBuffer(), context);

        PointerPointer extras = new PointerPointer(
                AddressRetriever.retrieveHostPointer(ret.shapeInfoDataBuffer()),
                context.getOldStream(),
                allocator.getDeviceIdPointer()
        );

        CudaFloatDataBuffer tempIndexes = new CudaFloatDataBuffer(indexes.length);
        AtomicAllocator.getInstance().memcpyBlocking(tempIndexes, new IntPointer(indexes), indexes.length * 4, 0);

        Pointer pIndex = AtomicAllocator.getInstance().getPointer(tempIndexes, context);

        TADManager tadManager = ((JCudaExecutioner) Nd4j.getExecutioner()).getTadManager();

        Pair<DataBuffer, DataBuffer> tadBuffers = tadManager.getTADOnlyShapeInfo(source, new int[]{sourceDimension});

        Pointer tadShapeInfo = AtomicAllocator.getInstance().getPointer(tadBuffers.getFirst(), context);

        DataBuffer offsets = tadBuffers.getSecond();
        Pointer tadOffsets = AtomicAllocator.getInstance().getPointer(offsets, context);

        if(ret.data().dataType() == DataBuffer.Type.DOUBLE) {
            nativeOps.pullRowsDouble(
                    extras,
                    x,
                    xShape,
                    z,
                    zShape,
                    indexes.length,
                    pIndex,
                    tadShapeInfo,
                    tadOffsets
            );
        } else if(ret.data().dataType() == DataBuffer.Type.FLOAT) {
            nativeOps.pullRowsFloat(
                    extras,
                    x,
                    xShape,
                    z,
                    zShape,
                    indexes.length,
                    pIndex,
                    tadShapeInfo,
                    tadOffsets
            );
        } else {
            nativeOps.pullRowsHalf(
                    extras,
                    x,
                    xShape,
                    z,
                    zShape,
                    indexes.length,
                    pIndex,
                    tadShapeInfo,
                    tadOffsets
            );
        }

        allocator.registerAction(context, ret, source);

        return ret;
    }

    @Override
    public INDArray average(INDArray target, INDArray[] arrays) {
        if (arrays == null || arrays.length == 0)
            throw new RuntimeException("Input arrays are missing");

        if (arrays.length == 1)
            return target.assign(arrays[0]);

        long len = target.lengthLong();

        AtomicAllocator allocator = AtomicAllocator.getInstance();

        CudaContext context =  allocator.getFlowController().prepareAction(target);

        PointerPointer extras = new PointerPointer(
                null, // not used
                context.getOldStream(),
                allocator.getDeviceIdPointer()
        );



        Pointer z = AtomicAllocator.getInstance().getPointer(target, context);

        long[] xPointers = new long[arrays.length];

        for (int i = 0; i < arrays.length; i++) {
            if (arrays[i].lengthLong() != len)
                throw new RuntimeException("All arrays should have equal length for averaging");

            AllocationPoint point = allocator.getAllocationPoint(arrays[i]);
            xPointers[i] = point.getPointers().getDevicePointer().address();
            point.tickDeviceWrite();
        }

        CudaDoubleDataBuffer tempX = new CudaDoubleDataBuffer(arrays.length);

        allocator.memcpyBlocking(tempX, new LongPointer(xPointers), xPointers.length * 8, 0);

        Pointer x = AtomicAllocator.getInstance().getPointer(tempX, context);

        if (target.data().dataType() == DataBuffer.Type.DOUBLE) {
            nativeOps.averageDouble(extras, x, z, arrays.length, len, true);
        } else if (target.data().dataType() == DataBuffer.Type.FLOAT) {
            nativeOps.averageFloat(extras, x, z, arrays.length, len, true);
        } else {
            nativeOps.averageHalf(extras, x, z, arrays.length, len, true);
        }

        allocator.getFlowController().registerAction(context, target);

        return target;
    }

    @Override
    public INDArray average(Collection<INDArray> arrays) {
        return average(arrays.toArray(new INDArray[0]));
    }


    /**
     * This method averages input arrays, and returns averaged array
     *
     * @param arrays
     * @return
     */
    @Override
    public INDArray average(INDArray[] arrays) {
        if (arrays == null || arrays.length == 0)
            throw new RuntimeException("Input arrays are missing");

        // we assume all arrays have equal length,
        INDArray ret = Nd4j.createUninitialized(arrays[0].shape(), arrays[0].ordering());

        return average(ret, arrays);
    }

    /**
     * This method averages input arrays, and returns averaged array
     *
     * @param target
     * @param arrays
     * @return
     */
    @Override
    public INDArray average(INDArray target, Collection<INDArray> arrays) {
        return average(target, arrays.toArray(new INDArray[0]));
    }

    /**
     * In place shuffle of an ndarray
     * along a specified set of dimensions
     *
     * @param array     the ndarray to shuffle
     * @param dimension the dimension to do the shuffle
     * @return
     */
    @Override
    public void shuffle(INDArray array, int... dimension) {
        shuffle(Collections.singletonList(array), dimension);
    }

    /**
     * Symmetric in place shuffle of an ndarray
     * along a specified set of dimensions. All arrays
     *
     * @param sourceArrays     the ndarray to shuffle
     * @param dimension the dimension to do the shuffle
     * @return
     */
    @Override
    public void shuffle(Collection<INDArray> sourceArrays, int... dimension) {
        // no dimension - no shuffle
        if (dimension == null || dimension.length == 0)
            throw new RuntimeException("Dimension can't be null or 0-length");

        if (sourceArrays == null || sourceArrays.size() ==0)
            throw new RuntimeException("No input arrays provided");

        List<INDArray> arrays = new ArrayList<>(sourceArrays);

        // first we build TAD for input array and dimensions

        AtomicAllocator allocator = AtomicAllocator.getInstance();

        CudaContext context =  allocator.getFlowController().prepareAction(arrays.get(0));

        for (int x = 1; x < arrays.size(); x++ ){
            allocator.getFlowController().prepareAction(arrays.get(x));
        }

        int tadLength = 1;
        for (int i = 0; i < dimension.length; i++) {
            tadLength *= arrays.get(0).shape()[dimension[i]];
        }

        int numTads = arrays.get(0).length() / tadLength;

        int[] map = ArrayUtil.buildHalfVector(numTads / 2, numTads);

        CudaIntDataBuffer shuffle = new CudaIntDataBuffer(map);

        Pointer shuffleMap = allocator.getPointer(shuffle, context);

        PointerPointer extras = new PointerPointer(
                null, // not used
                context.getOldStream(),
                allocator.getDeviceIdPointer()
        );


        long[] xPointers = new long[arrays.size()];
        long[] xShapes = new long[arrays.size()];
        long[] tadShapes = new long[arrays.size()];
        long[] tadOffsets = new long[arrays.size()];

        for (int i = 0; i < arrays.size(); i++) {
            INDArray array = arrays.get(i);

            Pointer x = AtomicAllocator.getInstance().getPointer(array, context);
            Pointer xShapeInfo = AtomicAllocator.getInstance().getPointer(array.shapeInfoDataBuffer(), context);


            TADManager tadManager = ((JCudaExecutioner) Nd4j.getExecutioner()).getTadManager();

            Pair<DataBuffer, DataBuffer> tadBuffers = tadManager.getTADOnlyShapeInfo(array, dimension);

            Pointer tadShapeInfo = AtomicAllocator.getInstance().getPointer(tadBuffers.getFirst(), context);

            DataBuffer offsets = tadBuffers.getSecond();
            Pointer tadOffset = AtomicAllocator.getInstance().getPointer(offsets, context);

            xPointers[i] = x.address();
            xShapes[i] = xShapeInfo.address();
            tadShapes[i] = tadShapeInfo.address();
            tadOffsets[i] = tadOffset.address();
        }


        CudaDoubleDataBuffer tempX = new CudaDoubleDataBuffer(arrays.size());
        CudaDoubleDataBuffer tempShapes = new CudaDoubleDataBuffer(arrays.size());
        CudaDoubleDataBuffer tempTAD = new CudaDoubleDataBuffer(arrays.size());
        CudaDoubleDataBuffer tempOffsets = new CudaDoubleDataBuffer(arrays.size());

        AtomicAllocator.getInstance().memcpyBlocking(tempX, new LongPointer(xPointers), xPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempShapes, new LongPointer(xShapes), xPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempTAD, new LongPointer(tadShapes), xPointers.length * 8, 0);
        AtomicAllocator.getInstance().memcpyBlocking(tempOffsets, new LongPointer(tadOffsets), xPointers.length * 8, 0);


        if (Nd4j.dataType() == DataBuffer.Type.DOUBLE) {
            nativeOps.shuffleDouble(
                    extras,
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    arrays.size(),
                    shuffleMap,
                    allocator.getPointer(tempTAD, context),
                    allocator.getPointer(tempOffsets, context)
            );
        } else if (Nd4j.dataType() == DataBuffer.Type.FLOAT) {
            nativeOps.shuffleFloat(
                    extras,
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    arrays.size(),
                    shuffleMap,
                    allocator.getPointer(tempTAD, context),
                    allocator.getPointer(tempOffsets, context)
            );
        } else {
            // HALFs
            nativeOps.shuffleHalf(
                    extras,
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    allocator.getPointer(tempX, context),
                    allocator.getPointer(tempShapes, context),
                    arrays.size(),
                    shuffleMap,
                    allocator.getPointer(tempTAD, context),
                    allocator.getPointer(tempOffsets, context)
            );
        }


        for (int f = 0; f < arrays.size(); f++ ){
            allocator.getFlowController().registerAction(context, arrays.get(f));
        }


        // just to keep reference
        shuffle.address();

        tempX.dataType();
        tempShapes.dataType();
        tempOffsets.dataType();
        tempTAD.dataType();
    }
}
