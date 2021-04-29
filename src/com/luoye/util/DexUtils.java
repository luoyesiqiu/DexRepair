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
    private static final byte[] DEX_FILE_MAGIC = { 0x64,0x65,0x78,0x0a,0x30,0x33,0x35,0x00};

    public static void patch(String dexFile, List<CodeItem> codeItems, boolean outputLog) {

        String outFile = dexFile.endsWith(".dex") ? dexFile.replaceAll("\\.dex$","_repair.dex") : dexFile + "_repair.dex";
        File outDexFile = new File(outFile);
        if(outDexFile.exists()){
            outDexFile.delete();
        }
        byte[] dexData = IoUtils.readFile(dexFile);
        IoUtils.writeFile(outFile,dexData);
        File inDexFile = new File(dexFile);
        Map<Integer, byte[]> map = codeItems.stream().collect(Collectors.toMap(CodeItem::getMethodIndex, CodeItem::getInsns));
        RandomAccessFile randomAccessFile = null;
        int patchCount = 0;
        try {
            randomAccessFile = new RandomAccessFile(outFile, "rw");
            Dex dex = new Dex(inDexFile);
            fixDexMagic(randomAccessFile);
            Iterable<ClassDef> classDefs = dex.classDefs();
            int classIdx = 0;
            for (Iterator<ClassDef> it = classDefs.iterator(); it.hasNext(); ) {
                ClassDef classDef = it.next();
                ClassData classData = null;
                try {
                    classData = dex.readClassData(classDef);
                }catch (Exception e){
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
                    }
                    catch (Exception e){

                    }
                    byte[] bytes = map.get(method.getMethodIndex());
                    if (bytes != null) {
                        int writeLen = Math.min(insSize * 2, bytes.length);
                        if(outputLog) {
                            System.out.println(Arrays.toString(bytes));
                            System.out.printf("Patching %d bytes to method_idx %d of class_idx %d...\n",writeLen,method.getMethodIndex(),classIdx);
                        }
                        randomAccessFile.seek(offsetInstructions);
                        randomAccessFile.write(bytes, 0, writeLen);
                        patchCount++;
                    }
                }
                classIdx++;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            IoUtils.close(randomAccessFile);
        }
        System.out.printf("Patched %d method(s).\n",patchCount);
    }

    @Deprecated
    public static void repair(String dexFile, List<CodeItem> codeItems, boolean outputLog){
        RandomAccessFile randomAccessFile = null;
        String outFile = dexFile.endsWith(".dex") ? dexFile.replaceAll("\\.dex$","_repair.dex") : dexFile + "_repair.dex";
        //copy dex
        byte[] dexData = IoUtils.readFile(dexFile);
        int dexSize = dexData.length;
        IoUtils.writeFile(outFile,dexData);
        try{
            randomAccessFile = new RandomAccessFile(outFile,"rw");
            fixDexMagic(randomAccessFile);
            for(int i = 0 ; i < codeItems.size();i++){
                CodeItem codeItem = codeItems.get(i);
                long offset = codeItem.getOffset();
                if(offset > dexSize){
                    if(outputLog) {
                        System.err.printf("Skip invalid offset %d corresponding method : '%s'.\n", offset, codeItem.getMethodName());
                    }
                    continue;
                }
                if(outputLog) {
                    System.out.printf("Patch method : %s \n", codeItem.getMethodName());
                }
                randomAccessFile.seek(offset);
                randomAccessFile.write(codeItem.getInsns());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            IoUtils.close(randomAccessFile);
        }
    }

    private static void fixDexMagic(RandomAccessFile dexRandomAccessFile) throws Exception{
        int firstPartOfMagic = dexRandomAccessFile.readInt();
        int secondPartOfMagic = dexRandomAccessFile.readInt();

        if(firstPartOfMagic == 0 || secondPartOfMagic == 0){
            dexRandomAccessFile.seek(0);
            dexRandomAccessFile.write(DEX_FILE_MAGIC);
        }
    }
    /**
     * .bin file item sample
     * {name:android.view.View com.luoye.helper.ui.databinding.FragmentMineBinding.getRoot(),method_idx:3962,offset:1474972,code_item_len:26,ins:AgABAAEAAABPUxMABQAAAG4QeQ8BAAwAEQA=}
     * @param bytes
     * @return
     */
    public static List<CodeItem> convertToCodeItems(byte[] bytes){
        String input = new String(bytes);

        List<CodeItem> codeItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{name:(.+?),method_idx:(\\d+),offset:(\\d+),code_item_len:(\\d+),ins:(.+?)\\}");
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
            String methodName = matcher.group(1);
            int methodIndex = Integer.parseInt(matcher.group(2));
            int offset = Integer.parseInt(matcher.group(3));
            int insLength = Integer.parseInt(matcher.group(4));
            String insBase64 = matcher.group(5);
            byte[] ins = null;
            try {
                ins = Base64.getDecoder().decode(insBase64);
            }
            catch (Exception e){
                System.err.printf("Error to decode \"%s\"\n",insBase64);
                continue;
            }
            CodeItem codeItem = new CodeItem(methodName,methodIndex,offset,insLength,ins);
            codeItems.add(codeItem);
        }

        return codeItems;
    }

}
