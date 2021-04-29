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
            printUsage();
            return;
        }
        boolean isOutputLog = false;
        String dexPath = null;
        String binPath = null;
        switch(args.length){
            case 2:
                dexPath = args[0];
                binPath = args[1];
                break;
            case 3:
                switch (args[0]){
                    case "--log":
                        isOutputLog = true;
                        break;
                }
                dexPath = args[1];
                binPath = args[2];
                break;
            default:
                printUsage();
                return;
        }
        if(new File(dexPath).exists() && new File(binPath).exists()) {
            byte[] data = IoUtils.readFile(binPath);
            List<CodeItem> items = DexUtils.convertToCodeItems(data);
            DexUtils.patch(dexPath, items,isOutputLog);
        }
        else{
            System.err.println("Dex file or bin file not exists!");
        }
    }

    private static void printUsage(){
        System.err.println("Usage:\n\tjava -jar DexRepair.jar [--log] <DexFile> <BinFile>");
    }

}