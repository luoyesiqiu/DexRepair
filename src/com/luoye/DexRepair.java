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
        if(args.length < 1 || (args.length == 1 && hasLogFlag(args))){
            printUsage();
            return;
        }
        boolean isOutputLog = false;
        String dexPath = null;
        String binPath = null;
        if(hasLogFlag(args)){
            isOutputLog = true;
        }
        switch(args.length){
            case 1:
                dexPath = args[0];
                break;
            case 2:
                if(isOutputLog){
                    dexPath = args[1];
                }
                else{
                    dexPath = args[0];
                    binPath = args[1];
                }
                break;
            case 3:
                dexPath = args[1];
                binPath = args[2];
                break;
            default:
                printUsage();
                return;
        }

        if(dexPath != null && new File(dexPath).exists()) {
            DexUtils dexUtils = new DexUtils(dexPath,binPath,isOutputLog);
            dexUtils.fixDexMagic();

            if(binPath != null && new File(binPath).exists()){
                dexUtils.patch();
            }
            dexUtils.updateDexHashes();

            System.out.println("All done.");
        }
        else{
            System.err.println("Dex file or bin file not exists!");
        }
    }

    private static boolean hasLogFlag(String[] args){
        if(args == null || args.length == 0){
            return false;
        }
        return "--log".equals(args[0]);
    }


    private static void printUsage(){
        System.err.println("Usage:\n\tjava -jar DexRepair.jar [--log] <DexFile> [PatchFile]");
    }

}