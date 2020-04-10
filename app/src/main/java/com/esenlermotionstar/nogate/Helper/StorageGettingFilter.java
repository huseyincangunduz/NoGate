package com.esenlermotionstar.nogate.Helper;

import java.io.File;
import java.io.FileFilter;

public class StorageGettingFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {

        return !(pathname.getName().endsWith("emulated") || pathname.getName().endsWith("self"));
    }
}
