package com.luoye.util;

import com.luoye.model.CodeItem;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author luoyesiqiu
 */
public class DexUtils {
    private static final byte[] DEX_FILE_MAGIC = { 0x64,0x65,0x78,0x0a,0x30,0x33,0x35,0x00};
    public static void repair(String dexFile, List<CodeItem> codeItems,boolean outputLog){
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
