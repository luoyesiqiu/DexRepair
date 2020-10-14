package com.luoye;

import com.luoye.model.CodeItem;
import com.luoye.util.DexUtils;
import com.luoye.util.IoUtils;

import java.io.File;
import java.util.List;

/**
 * @author luoyesiqiu
 */
public class DexRepair {

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Usage:\n\tjava -jar DexRepair.jar <DexFile> <BinFile>");
            return;
        }
        String dexPath = args[0];
        String binPath = args[1];
        if(new File(dexPath).exists() && new File(binPath).exists()) {
            byte[] data = IoUtils.readFile(binPath);
            List<CodeItem> items = DexUtils.convertToCodeItems(data);
            DexUtils.repair(dexPath, items);
            System.out.println("success");
        }
        else{
            System.out.println("Dex file or bin file not exists!");
        }
    }

}