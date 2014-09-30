package com.camber.ndrutils.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static com.camber.ndrutils.NdrApp.getInstance;

/**
 * Created by CamberCreak on 20/04/2014.
 */
public class PropUtil {

    private static final String CACHE_FOLDER = "/cache/XZDualRecovery";
    private static Properties xzdrProperties = new Properties();

    private static String locateXzdrPropFolder() {
        if (Boolean.valueOf(RootUtil.getRoot().ShellForString("[ -d '" + CACHE_FOLDER + "' ]&&echo 'true'||echo 'false'"))) {
            return CACHE_FOLDER;
        }
        String secondary_storage = System.getenv("SECONDARY_STORAGE");
        String external_storage_sd = System.getenv("EXTERNAL_STORAGE_SD");
        String external_storage = System.getenv("EXTERNAL_STORAGE");
        String removable_storage;
        if (secondary_storage != null) {
            removable_storage = secondary_storage;
        } else if (external_storage_sd != null) {
            removable_storage = external_storage_sd;
        } else {
            removable_storage = external_storage;
        }
        File mSecondaryStorage = new File(removable_storage);
        if (mSecondaryStorage.canRead()) {
            return removable_storage + "/XZDualRecovery";
        }
        return null;
    }

    private static void readXzdrPropFile() {
        String PROP_FOLDER = locateXzdrPropFolder();
        int uid = getInstance().getApplicationInfo().uid;
        String APP_CACHE_FOLDER = getInstance().getCacheDir().toString();
        RootUtil.getRoot().ShellForCode("cp " + PROP_FOLDER + "/XZDR.prop " + APP_CACHE_FOLDER + "/XZDR.prop");
        RootUtil.getRoot().ShellForCode("chown " + uid + ":" + uid + " " + APP_CACHE_FOLDER + "/XZDR.prop");
        File xzdrPropFile = new File(APP_CACHE_FOLDER + "/XZDR.prop");
        try {
            if (xzdrPropFile.exists()) {
                FileInputStream systemPropFileInputStream = new FileInputStream(xzdrPropFile);
                xzdrProperties.load(systemPropFileInputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSystemProp(String propKey) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + propKey);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString().replace("\n", "").replace("\r", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getXzdrProp(String propKey) {
        readXzdrPropFile();
        return xzdrProperties.getProperty(propKey, "");
    }

    public static int readPropFile() {
        String prop = getXzdrProp("dr.recovery.boot");
        if (!prop.isEmpty()) {
            if (prop.contains("cwm")) {
                return 2;
            } else if (prop.contains("twrp")) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    private static void setXzdrProp(String propKey, String propValue) {
        if (xzdrProperties.isEmpty()) {
            readXzdrPropFile();
        }
        xzdrProperties.setProperty(propKey, propValue);
        try {
            String PROP_FOLDER = locateXzdrPropFolder();
            String APP_CACHE_FOLDER = getInstance().getCacheDir().toString();
            FileOutputStream xzdrPropFileOutputStream = new FileOutputStream(APP_CACHE_FOLDER + "/XZDR.prop");
            xzdrProperties.store(xzdrPropFileOutputStream, "Updated by XZDualRecovery app");
            RootUtil.getRoot().ShellForCode("cp " + APP_CACHE_FOLDER + "/XZDR.prop "
                    + PROP_FOLDER + "/XZDR.prop && rm " + APP_CACHE_FOLDER
                    + "/XZDR.prop");
            if (PROP_FOLDER.matches(CACHE_FOLDER)) {
                RootUtil.getRoot().ShellForCode("chown root:root " + PROP_FOLDER + "/XZDR.prop");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updatePropFile(int rcvMode) {
        switch (rcvMode) {
            case 2:
                setXzdrProp("dr.recovery.boot", "cwm");
                break;
            case 1:
                setXzdrProp("dr.recovery.boot", "twrp");
                break;
            default:
                setXzdrProp("dr.recovery.boot", "philz");
        }
    }
}
