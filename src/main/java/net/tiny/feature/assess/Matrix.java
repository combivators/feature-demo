package net.tiny.feature.assess;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.function.BiFunction;

/**
 * 数值矩阵
 * 用于数值评估计算
 * @param <E>
 */
public abstract class Matrix<E extends Number> {
    /**
     * Number of height(rows) in the matrix
     */
    public abstract long getHeight();

    /**
     * Number of width(columns) in the matrix
     */
    public abstract long getWidth();

    /**
     * Returns <code>A(row,column)</code>
     */
    public abstract E get(long row, long column);

    /**
     * <code>A(row,column) = value</code>
     */
    public abstract Matrix<E>  set(long row, long column, E value);

    /**
     * <code>A(row) = values</code>
     */
    public Matrix<E>  set(long i, E[] values) {
        for (long j=0; j<values.length; j++) {
            set(i, j, values[(int)j]);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public E[] getRow(long row) {
        E tmp = get(row, 0);
        E[] array = (E[]) Array.newInstance(tmp.getClass(), (int)getWidth());
        array[0] = tmp;
        for (int j = 1; j < (int)getWidth(); j++) {
            array[j] = get(row, j);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public E[] getColumn(long column) {
        E tmp = get(0, column);
        E[] array = (E[]) Array.newInstance(tmp.getClass(), (int)getHeight());
        array[0] = tmp;
        for (long i = 1; i < getHeight(); i++) {
            array[(int)i] = get(i, column);
        }
        return array;
    }

    public void swapRows(long row1, long row2) {
        for (long j = 0; j < getWidth(); j++) {
            E tmp = get(row2, j);
            set(row2, j, get(row1, j));
            set(row1, j, tmp);
        }
    }

    public void swapColumns(long column1, long column2) {
        for (long i = 0; i < getHeight(); i++) {
            E tmp = get(i, column2);
            set(i, column2, get(i, column1));
            set(i, column1, tmp);
        }
    }

    public void reverseRows() {
        for (long i1 = 0, i2 = getHeight()-1; i1 < i2; i1++, i2--)
            swapRows(i1, i2);
    }

    public void reverseColumns() {
        for (long j1 = 0, j2 = getWidth()-1; j1 < j2; j1++, j2--)
            swapColumns(j1, j2);
    }
    /**
     * Returns true if the matrix is square
     */
    public boolean isSquare() {
        return (getHeight() == getWidth());
    }

    /**
     * Iterate over each "layer", starting from the corners swap each cell in-place.
     *
     * Layers move from the outside-in.
     *
     * [X][X][X][X]  ->  [ ][ ][ ][ ]
     * [X][ ][ ][X]  ->  [ ][X][X][ ]
     * [X][ ][ ][X]  ->  [ ][X][X][ ]
     * [X][X][X][X]  ->  [ ][ ][ ][ ]
     *
     * Positions move from each segment's start until one unit before the last,
     * as that has already been rotated.
     *
     * A <= B <= C <= D <= A
     *
     * [D][ ][ ][C]  ->  [ ][ ][C][ ]  ->  [ ][C][ ][ ]  ->  [ ][ ][ ][ ]
     * [ ][ ][ ][ ]  ->  [D][ ][ ][ ]  ->  [ ][ ][ ][B]  ->  [ ][D][C][ ]
     * [ ][ ][ ][ ]  ->  [ ][ ][ ][B]  ->  [D][ ][ ][ ]  ->  [ ][A][B][ ]
     * [A][ ][ ][B]  ->  [ ][A][ ][ ]  ->  [ ][ ][A][ ]  ->  [ ][ ][ ][ ]
     *
     */
    public void rotate() {
        long sideLength = getHeight();
        // Return early if matrix is not square or if it's empty
        if (!isSquare()) {
            return;
        }
        for (long layer = 0; layer < sideLength / 2L; layer++) {
            for (long position = layer; position < sideLength - 1L - layer; position++) {
                E temp = get(position, layer);
                set(position, layer, get((sideLength - 1 - layer), position));
                set((sideLength - 1L - layer), position, get((sideLength - 1L - position), (sideLength - 1L - layer)));
                set((sideLength - 1L - position), (sideLength - 1L - layer), get(layer, (sideLength - 1L - position)));
                set(layer, (sideLength - 1L - position),  temp);
            }
        }
    }

    /**
     * Transposes the matrix in-place.
     * In most cases, the matrix must be square for this to work.
     *
     * @return This matrix
     */
    public Matrix<E> transpose() {
        return new TransposedMatrix(this);
    }

    /**
     * Sets the tranpose of this matrix into <code>src</code>. Matrix dimensions
     * must be compatible
     *
     * @param matrix
     *            Matrix with as many rows as this matrix has columns, and as
     *            many columns as this matrix has rows
     * @return The matrix <code>B=A<sup>T</sup></code>
     */
    public Matrix<E> transpose(Matrix<E> matrix) {
        return new TransposedMatrix(matrix);
    }

    public Matrix<E> sub(long row, long column) {
        return sub(this, row, column, getHeight(), getWidth());
    }

    public Matrix<E> sub(long row1, long column1, long row2, long column2) {
        return sub(this, row1, column1, row2, column2);
    }

    public Matrix<E> sub(Matrix<E> matrix, long row1, long column1, long row2, long column2) {
        return new SubMatrix(matrix, row1, column1, row2, column2);
    }

    public Matrix<E> extend(Matrix<E> matrix) {
        return new ExtendedMatrix(this, matrix);
    }

    public Matrix<E> append(Matrix<E> a, Matrix<E> b) {
        return new ExtendedMatrix(a, b);
    }


    public double sumRow(long row) {
        double sum = 0.0d;
        long w = getWidth();
        for (long j = 0; j< w; j++) {
            sum += get(row, j).doubleValue();
        }
        return sum;
    }

    public double sumColumn(long column) {
        double sum = 0.0d;
        long h = getHeight();
        for (long i = 0; i<h; i++) {
            sum += get(i, column).doubleValue();
        }
        return sum;
    }

    public E computeRow(BiFunction<E[], E[], E> fun, long row, E[] values) {
        return fun.apply(getRow(row), values);
    }

    public E computeColumn(BiFunction<E[], E[], E> fun, long column, E[] values) {
        return fun.apply(getColumn(column), values);
    }

    @SuppressWarnings("unchecked")
    public E[] computeRows(BiFunction<E[], E[], E> fun, E[] values) {
        E tmp = get(0, 0);
        E[] array = (E[]) Array.newInstance(tmp.getClass(), (int)getHeight());
        for (int i = 0; i < (int)getHeight(); i++) {
            array[i] = computeRow(fun, i, values);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public E[] computeColumns(BiFunction<E[], E[], E> fun, E[] values) {
        E tmp = get(0, 0);
        E[] array = (E[]) Array.newInstance(tmp.getClass(), (int)getWidth());
        for (int j = 0; j < (int)getWidth(); j++) {
            array[j] = computeColumn(fun, j, values);
        }
        return array;
    }

    public Matrix<E> multiply(E[] values) {
        long w = getWidth();
        long h = getHeight();
        Matrix<E> m = of(h, w);
        for (long i = 0; i<h; i++) {
            for (long j = 0; j<w; j++) {
                E e = multiply(get(i, j), values[(int)j]);
                m.set(i, j, e);
            }
        }
        return m;
    }

    public void print(PrintStream out) {
        print(out, ",");
    }

    public void print(PrintStream out, String delim) {
        for (long i = 0; i < getHeight(); i++) {
            for (long j = 0; j < getWidth(); j++) {
                out.print(get(i, j));
                if (j < getWidth())
                    out.print(delim);
            }
            if (i < getHeight())
                out.println();
        }
    }

    @Override
    public String toString() {
        E element = get(1L, 1L);
        String type = (element != null) ? element.getClass().getSimpleName() : "";
        return String.format("%s(%dx%d)", type, getWidth(), getHeight());
    }

    class SubMatrix extends Matrix<E> {
        private final Matrix<E> base;
        private long i1, j1, i2, j2;

        SubMatrix(Matrix<E> base, long i1, long j1, long i2, long j2) {
            this.base = base;
            this.i1 = i1;
            this.j1 = j1;
            this.i2 = i2;
            this.j2 = j2;
        }

        @Override
        public long getHeight() {
            return i2 - i1;
        }

        @Override
        public long getWidth() {
            return j2 - j1;
        }

        @Override
        public E get(long i, long j) {
            return base.get(i1 + i, j1 + j);
        }

        @Override
        public Matrix<E> set(long i, long j, E value) {
            base.set(i1 + i, j1 + j, value);
            return this;
        }
    }

    class TransposedMatrix extends Matrix<E> {
        private final Matrix<E> matrix;

        TransposedMatrix(Matrix<E> matrix) {
            this.matrix = matrix;
        }

        @Override
        public E get(long i, long j) {
            return matrix.get(j, i);
        }

        @Override
        public long getHeight() {
            return matrix.getWidth();
        }

        @Override
        public long getWidth() {
            return matrix.getHeight();
        }

        @Override
        public Matrix<E> set(long i, long j, E value) {
            matrix.set(j, i, value);
            return this;
        }
    }

    class ExtendedMatrix extends Matrix<E> {
        private final Matrix<E> a, b;

        public ExtendedMatrix(Matrix<E> a, Matrix<E> b) {
            if (a.getHeight() != b.getHeight())
                throw new IllegalArgumentException(String.format("Matrix height must be same. '%d!=%d'",
                        a.getHeight(), b.getHeight()));
            this.a = a;
            this.b = b;
        }

        @Override
        public long getHeight() {
            return a.getHeight();
        }

        @Override
        public long getWidth() {
            return a.getWidth() + b.getWidth();
        }

        @Override
        public E get(long i, long j) {
            if (0 <= i && i < a.getHeight()) {
                if (0 <= j && j < a.getWidth())
                    return a.get(i, j);
                else if (a.getWidth() <= j && j < a.getWidth() + b.getWidth())
                    return b.get(i, j - a.getWidth());
                else
                    throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
            } else
                throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
        }

        @Override
        public Matrix<E> set(long i, long j, E value) {
            if (0 <= i && i < a.getHeight()) {
                if (0 <= j && j < a.getWidth()) {
                    a.set(i, j, value);
                    return this;
                } else if (a.getWidth() <= j && j < a.getWidth() + b.getWidth()) {
                    b.set(i, j - a.getHeight(), value);
                    return this;
                } else {
                    throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
                }
            } else
                throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
        }
    }

    public static <T extends Number> Matrix<T> of(final T[][] elements) {
        if(null == elements)
            return null;
        return new ArrayMatrix<T>(elements);
    }

    public static <T extends Number> Matrix<T> of(final long height, final long width) {
        return new ArrayMatrix<T>(height, width);
    }

    static class ArrayMatrix<E extends Number> extends Matrix<E> {
        private long height, width;
        private E[][] data;

        /**
         * Constructs matrix with all null references.
         * @param height
         * @param width
         */
        @SuppressWarnings("unchecked")
        public ArrayMatrix(long height, long width) {
            this.height = height;
            this.width = width;
            data = (E[][]) new Number[(int)height][(int)width];
        }

        /**
         * Constructs matrix with data from elements.
         * @param height
         * @param width
         * @param elements
         */
        public ArrayMatrix(E[][] elements) {
            this.height = elements.length;
            this.width = elements[0].length;
            data = elements;
        }

        /**
         * Constructs shallow copy of given matrix
         * @param matrix
         */
        @SuppressWarnings("unchecked")
        public ArrayMatrix(Matrix<E> matrix) {
            height = matrix.getHeight();
            width = matrix.getWidth();
            data = (E[][]) new Object[(int)height][(int)width];
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++)
                    data[i][j] = matrix.get(i, j);
        }

        @Override
        public long getHeight() {
            return height;
        }

        @Override
        public long getWidth() {
            return width;
        }

        @Override
        public E get(long i, long j) {
            try {
                return data[(int)i][(int)j];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
            }
        }

        @Override
        public Matrix<E> set(long i, long j, E value) {
            try {
                data[(int)i][(int)j] = value;
                return this;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, %d'", i, j));
            }
        }

        @Override
        public Matrix<E> set(long i, E[] values) {
            if(width != values.length) {
                throw new IllegalArgumentException("Array size must be " + width);
            }
            try {
                System.arraycopy(values, 0, data[(int)i], 0, values.length);
                return this;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(String.format("Matrix out of bounds '%d, 0'", i));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> N plus(N number, N augend) {
        Class<? extends Number> cls = number.getClass();
        if (Integer.class.equals(cls)) {
            return (N) Integer.valueOf(number.intValue() + augend.intValue());
        } else if (Short.class.equals(cls)) {
            return (N) Short.valueOf((short)(number.shortValue() + augend.shortValue()));
        } else if (Long.class.equals(cls)) {
            return (N) Long.valueOf(number.longValue() + augend.longValue());
        } else if (Float.class.equals(cls)) {
            return (N) Float.valueOf(number.floatValue() + augend.floatValue());
        } else if (Double.class.equals(cls)) {
            return (N) Double.valueOf(number.doubleValue() + augend.doubleValue());
        }
        throw new UnsupportedOperationException("Unknown class: " + cls);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> N subtract(N number, N subtrahend) {
        Class<? extends Number> cls = number.getClass();
        if (Integer.class.equals(cls)) {
            return (N) Integer.valueOf(number.intValue() - subtrahend.intValue());
        } else if (Short.class.equals(cls)) {
            return (N) Short.valueOf((short)(number.shortValue() - subtrahend.shortValue()));
        } else if (Long.class.equals(cls)) {
            return (N) Long.valueOf(number.longValue() - subtrahend.longValue());
        } else if (Float.class.equals(cls)) {
            return (N) Float.valueOf(number.floatValue() - subtrahend.floatValue());
        } else if (Double.class.equals(cls)) {
            return (N) Double.valueOf(number.doubleValue() - subtrahend.doubleValue());
        }
        throw new UnsupportedOperationException("Unknown class: " + cls);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> N multiply(N number, N multiplier) {
        Class<? extends Number> cls = number.getClass();
        if (Integer.class.equals(cls)) {
            return (N) Integer.valueOf(number.intValue() * multiplier.intValue());
        } else if (Short.class.equals(cls)) {
            return (N) Short.valueOf((short)(number.shortValue() * multiplier.shortValue()));
        } else if (Long.class.equals(cls)) {
            return (N) Long.valueOf(number.longValue() * multiplier.longValue());
        } else if (Float.class.equals(cls)) {
            return (N) Float.valueOf(number.floatValue() * multiplier.floatValue());
        } else if (Double.class.equals(cls)) {
            return (N) Double.valueOf(number.doubleValue() * multiplier.doubleValue());
        }
        throw new UnsupportedOperationException("Unknown class: " + cls);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number> N divide(N number, N divisor) {
        Class<? extends Number> cls = number.getClass();
        if (Integer.class.equals(cls)) {
            return (N) Integer.valueOf(number.intValue() / divisor.intValue());
        } else if (Short.class.equals(cls)) {
            return (N) Short.valueOf((short)(number.shortValue() / divisor.shortValue()));
        } else if (Long.class.equals(cls)) {
            return (N) Long.valueOf(number.longValue() / divisor.longValue());
        } else if (Float.class.equals(cls)) {
            return (N) Float.valueOf(number.floatValue() / divisor.floatValue());
        } else if (Double.class.equals(cls)) {
            return (N) Double.valueOf(number.doubleValue() / divisor.doubleValue());
        }
        throw new UnsupportedOperationException("Unknown class: " + cls);
    }

}
