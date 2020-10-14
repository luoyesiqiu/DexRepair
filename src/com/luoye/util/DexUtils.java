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
    public static void repair(String dexFile, List<CodeItem> codeItems){
        RandomAccessFile randomAccessFile = null;
        String outFile = dexFile.endsWith(".dex") ? dexFile.replaceAll("\\.dex","_repair.dex") : dexFile + "_repair.dex";
        //copy dex
        byte[] dexData = IoUtils.readFile(dexFile);
        IoUtils.writeFile(outFile,dexData);
        try{
            randomAccessFile = new RandomAccessFile(outFile,"rw");
            for(int i = 0 ; i < codeItems.size();i++){
                CodeItem codeItem = codeItems.get(i);
                randomAccessFile.seek(codeItem.getOffset());
                randomAccessFile.write(codeItem.getByteCode());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            IoUtils.close(randomAccessFile);
        }
    }
    /**
     * .bin file item sample
     * {name:android.view.View com.yunke.helper.ui.databinding.FragmentMineBinding.getRoot(),method_idx:3962,offset:1474972,code_item_len:26,ins:AgABAAEAAABPUxMABQAAAG4QeQ8BAAwAEQA=}
     * @param bytes
     * @return
     */
    public static List<CodeItem> convertToCodeItems(byte[] bytes){
        String input = new String(bytes);

        List<CodeItem> codeItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{name:(.+?),method_idx:(\\d+),offset:(\\d+),code_item_len:(\\d+),ins:(.+?)\\}");
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()){
            int offset = Integer.parseInt(matcher.group(3));
            String insBase64 = matcher.group(5);
            byte[] ins = Base64.getDecoder().decode(insBase64);
            CodeItem codeItem = new CodeItem(offset,ins);
            codeItems.add(codeItem);
        }

        return codeItems;
    }

}
