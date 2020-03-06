package com.ifeng.recallScheduler.bloomFilter.common;

import org.apache.hadoop.io.RawComparator;

/**
 * Created by jibin on 2017/7/4.
 */
public interface IBloomFilterBase {
    long getKeyCount();

    long getMaxKeys();

    long getByteSize();

    byte[] createBloomKey(byte[] var1, int var2, int var3, byte[] var4, int var5, int var6);

    RawComparator<byte[]> getComparator();
}