package com.luoye.util;

import com.android.dex.ClassData;
import com.android.dex.ClassDef;
import com.android.dex.Dex;
import com.luoye.model.CodeItem;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author luoyesiqiu
 */
public class DexUtils {
    private static final byte[] DEX_FILE_MAGIC = {0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00};

    private RandomAccessFile mRandomOutAccessFile = null;
    private String mInDexFile;
    private String mOutDexFile;
    private List<CodeItem> mCodeItems;
    private byte[] mDexData;
    private boolean openLog;
    public DexUtils(String dexFile,String patchFile,boolean openLog){
        try{
            this.mInDexFile = dexFile;
            File inDexFile = new File(dexFile);
            this.mOutDexFile = dexFile.endsWith(".dex") ? dexFile.replaceAll("\\.dex$", "_repair.dex") : dexFile + "_repair.dex";
            File outDexFile = new File(mOutDexFile);

            this.mDexData = IoUtils.readFile(inDexFile.getAbsolutePath());
            IoUtils.writeFile(mOutDexFile, mDexData);

            this.mRandomOutAccessFile = new RandomAccessFile(outDexFile, "rw");

            if(patchFile != null) {
                this.mCodeItems = convertToCodeItems(patchFile);
            }
            this.openLog = openLog;

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void patch() {
        if(mCodeItems == null || mDexData == null ){
            System.out.println("Do not patch.\n");

            return;
        }

        System.out.printf("Start patch to file : %s\n", mInDexFile);

        Map<Integer, byte[]> map = mCodeItems.stream().collect(Collectors.toMap(CodeItem::getMethodIndex, CodeItem::getInsns));
        int patchCount = 0;
        try {
            Dex dex = new Dex(mDexData, false);
            Iterable<ClassDef> classDefs = dex.classDefs();
            int classIdx = 0;
            for (Iterator<ClassDef> it = classDefs.iterator(); it.hasNext(); ) {
                ClassDef classDef = it.next();
                ClassData classData = null;
                try {
                    classData = dex.readClassData(classDef);
                } catch (Exception e) {
                    classIdx++;
                    continue;
                }
                ClassData.Method[] methods = classData.allMethods();
                for (int i = 0; i < methods.length; i++) {
                    ClassData.Method method = methods[i];
                    int offsetInstructions = method.getCodeOffset() + 16;
                    int insSize = 0;
                    try {
                        insSize = dex.readCode(method).getInstructions().length;
                    } catch (Exception e) {
                        continue;
                    }
                    byte[] bytes = map.get(method.getMethodIndex());
                    if (bytes != null) {
                        int min = Math.min(insSize * 2, bytes.length);
                        int max = Math.max(insSize * 2, bytes.length);
                        if (openLog) {
                            System.out.printf("Patching %d bytes to method_idx %d of class_idx %d...\n", min, method.getMethodIndex(), classIdx);
                        }
                        mRandomOutAccessFile.seek(offsetInstructions);
                        mRandomOutAccessFile.write(bytes, max - (insSize * 2) , min);
                        patchCount++;
                    }
                }
                classIdx++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            IoUtils.close(mRandomOutAccessFile);
        }
        System.out.printf("Patched %d method(s).\n", patchCount);
        System.out.printf("Patched file : %s\n", mOutDexFile);

    }

    public void fixDexMagic() {
        try {
            int firstPartOfMagic = mRandomOutAccessFile.readInt();
            int secondPartOfMagic = mRandomOutAccessFile.readInt();

            if (firstPartOfMagic == 0 || secondPartOfMagic == 0) {
                mRandomOutAccessFile.seek(0);
                mRandomOutAccessFile.write(DEX_FILE_MAGIC);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateDexHashes() {
        try {
            byte[] dexData = IoUtils.readFile(mOutDexFile);
            Dex dex = new Dex(dexData, false);
            dex.writeHashes();
            dex.writeTo(new File(mOutDexFile));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * .bin文件条目样例：
     * {name:android.view.View com.luoye.helper.ui.databinding.FragmentMineBinding.getRoot(),method_idx:3962,offset:1474972,code_item_len:26,ins:AgABAAEAAABPUxMABQAAAG4QeQ8BAAwAEQA=}
     */
    private List<CodeItem> convertToCodeItems(String patchFileContent) {
        String input = patchFileContent;

        List<CodeItem> codeItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{name:(.+?),method_idx:(\\d+),offset:(\\d+),code_item_len:(\\d+),ins:(.+?)\\}");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            int methodIndex = Integer.parseInt(matcher.group(2));
            int offset = Integer.parseInt(matcher.group(3));
            int insLength = Integer.parseInt(matcher.group(4));
            String insBase64 = matcher.group(5);
            byte[] ins = null;
            try {
                ins = Base64.getDecoder().decode(insBase64);
            } catch (Exception e) {
                System.err.printf("Error to decode \"%s\"\n", insBase64);
                continue;
            }
            CodeItem codeItem = new CodeItem(methodName, methodIndex, offset, insLength, ins);
            codeItems.add(codeItem);
        }

        return codeItems;
    }

}
