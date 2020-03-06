package com.ifeng.recallScheduler.bloomFilter.common;

import org.apache.hadoop.io.RawComparator;

import java.nio.ByteBuffer;

/**
 * Created by jibin on 2017/7/4.
 */
public interface IBloomFilter{
    long getKeyCount();

    long getMaxKeys();

    long getByteSize();

    byte[] createBloomKey(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

    RawComparator<byte[]> getComparator();
    boolean contains(byte[] var1, int var2, int var3, ByteBuffer var4);

    boolean supportsAutoLoading();
}
