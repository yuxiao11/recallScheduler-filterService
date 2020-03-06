package com.ifeng.recallScheduler.bloomFilter.common;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Writable;

/**
 * Created by jibin on 2017/7/4.
 */
public interface IBloomFilterWriter {
    long getKeyCount();

    long getMaxKeys();

    long getByteSize();

    byte[] createBloomKey(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

    RawComparator<byte[]> getComparator();
    void allocBloom();

    void compactBloom();

    Writable getMetaWriter();

    Writable getDataWriter();

    void add(byte[] var1, int var2, int var3);
}
