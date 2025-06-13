//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.videoio;



// C++: class IStreamReader
/**
 * Read data stream interface
 */
public class IStreamReader {

    protected final long nativeObj;
    protected IStreamReader(long addr) { nativeObj = addr; }

    public long getNativeObjAddr() { return nativeObj; }

    // internal usage only
    public static IStreamReader __fromPtr__(long addr) { return new IStreamReader(addr); }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }



    // native support for java finalize()
    private static native void delete(long nativeObj);

}
