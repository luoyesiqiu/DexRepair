package com.luoye.model;

import java.util.Arrays;
/**
 * @author luoyesiqiu
 */
public  class CodeItem{
    public CodeItem(String methodName, int methodIndex, long offset, int insnsLength, byte[] insns) {
        this.methodName = methodName;
        this.methodIndex = methodIndex;
        this.offset = offset;
        this.insnsLength = insnsLength;
        this.insns = insns;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getInsnsLength() {
        return insnsLength;
    }

    public void setInsnsLength(int insnsLength) {
        this.insnsLength = insnsLength;
    }

    public byte[] getInsns() {
        return insns;
    }

    public void setInsns(byte[] insns) {
        this.insns = insns;
    }

    @Override
    public String toString() {
        return "CodeItem{" +
                "methodName='" + methodName + '\'' +
                ", methodIndex=" + methodIndex +
                ", offset=" + offset +
                ", insnsLength=" + insnsLength +
                ", insns=" + Arrays.toString(insns) +
                '}';
    }

    private String methodName;
    private int methodIndex;
    private long offset;
    private int insnsLength;
    private byte[] insns;
}