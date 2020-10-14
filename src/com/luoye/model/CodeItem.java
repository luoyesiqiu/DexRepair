package com.luoye.model;

import java.util.Arrays;
/**
 * @author luoyesiqiu
 */
public  class CodeItem{
    public CodeItem(long offset, byte[] byteCode) {
        this.offset = offset;
        this.byteCode = byteCode;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public byte[] getByteCode() {
        return byteCode;
    }

    public void setByteCode(byte[] byteCode) {
        this.byteCode = byteCode;
    }

    @Override
    public String toString() {
        return "CodeItem{" +
                "offset=" + offset +
                ", byteCode=" + Arrays.toString(byteCode) +
                '}';
    }

    private long offset;
    private byte[] byteCode;
}