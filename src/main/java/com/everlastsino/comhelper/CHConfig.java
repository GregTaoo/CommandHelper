package com.everlastsino.comhelper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class CHConfig {

    public static String language = "en_us";
    public static boolean playerTPAgreementRequirement = true;
    public static String sendTPRequest = "Send TP request to player %s.";
    public static String receiveTPRequest = "You received a TP request from player %s.";
    public static String targetNotFound = "Target not found!";
    public static String requestBeAgreed = "Your TP request has been agreed by %s.";
    public static String atReminding = "Player %s @@ you!";
    public static String configReloaded = "[CommandHelper] Configs reloaded.";
    public static String configReloadFailed = "[CommandHelper] Failed to reload configs.";
    public static String formatError = "[CommandHelper] FORMAT ERROR.";
    public static String languageChanged = "[CommandHelper] Language changed.";
    public static String languageChangeFailed = "[CommandHelper] Failed to change language.";
    public static String aboutThisMod = "[CommandHelper] BY GregTao. Pls follow GPL v3 license.";

    public static boolean loadConfigs() throws Exception{
        File file = new File("comhelper/comhelper.properties");
        if (!file.exists()) {
            if (((!file.getParentFile().exists() && file.getParentFile().mkdir())
                    || file.getParentFile().exists()) && file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(getDefaultConfigsFromUri());
                writer.close();
                comhelper.LOGGER.info("Generated a new config for default. Trying to reload...");
            } else {
                comhelper.LOGGER.error("Failed to create new file.");
            }
        }
        file = new File("comhelper/zh_cn.properties");
        if (!file.exists()) {
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(getZHCNLangFromUri());
                writer.close();
                comhelper.LOGGER.info("Generated a new language file for zh_cn.");
            } else {
                comhelper.LOGGER.error("Failed to create new language file.");
            }
        }
        try {
            return readConfigsFromFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean setLanguage(String language1) throws Exception {
        language = language1;

        File file = new File(String.format("comhelper/%s.properties", language1.equals("en_us") ? "comhelper" : language1));
        if (!file.exists()) return false;

        InputStreamReader stream = new InputStreamReader(new FileInputStream("comhelper/comhelper.properties"));
        Properties pro = new Properties();
        pro.load(stream);
        if (pro.getProperty("language").isEmpty()) return false;
        pro.setProperty("language", language1);
        stream.close();
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("comhelper/comhelper.properties"));
        pro.store(writer, "Hello Command World!");
        if (!loadConfigs()) return false;
        comhelper.LOGGER.info(String.format("Set mod language to '%s'", language1));
        return true;
    }

    public static boolean readConfigsFromFile() throws Exception {
        InputStreamReader stream = new InputStreamReader(new FileInputStream("comhelper/comhelper.properties"));
        Properties pro = new Properties();
        pro.load(stream);

        language = pro.getProperty("language");
        playerTPAgreementRequirement = pro.getProperty("playerTPAgreementRequirement", "true").equals("true");

        if (!language.equals("en_us")){
            stream.close();
            String fileName = String.format("comhelper/%s.properties", language);
            File file = new File(fileName);
            if (!file.exists()) {
                comhelper.LOGGER.info(String.format("Language configuration '%s' not found.", language));
                return false;
            }
            stream = new InputStreamReader(new FileInputStream(String.format("comhelper/%s.properties", language)));
            pro = new Properties();
            pro.load(stream);
        }
        sendTPRequest = pro.getProperty("sendTPRequest");
        receiveTPRequest = pro.getProperty("receiveTPRequest");
        targetNotFound = pro.getProperty("targetNotFound");
        requestBeAgreed = pro.getProperty("requestBeAgreed");
        atReminding = pro.getProperty("atReminding");
        configReloaded = pro.getProperty("configReloaded");
        configReloadFailed = pro.getProperty("configReloadedFailed");
        languageChanged = pro.getProperty("languageChanged");
        languageChangeFailed = pro.getProperty("languageChangeFailed");
        formatError = pro.getProperty("formatError");

        stream.close();

        if (language.equals("zh_cn")) {
            aboutThisMod = "[CommandHelper] GregTao创作, 请遵守GPL v3开源协议";
        }
        comhelper.LOGGER.info("Successfully loaded configurations.");
        return true;
    }

    public static String getDefaultConfigsFromUri() throws Exception {
        URL url = new URL("http://81.71.133.124/CommandHelperService/DefaultConfigurations.properties");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String tmp = reader.readLine();
        StringBuilder str = new StringBuilder();
        while (tmp != null) {
            str.append(tmp).append("\n");
            tmp = reader.readLine();
        }
        return str.toString();
    }

    public static String getZHCNLangFromUri() throws Exception {
        URL url = new URL("http://81.71.133.124/CommandHelperService/zh_cn.properties");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String tmp = reader.readLine();
        StringBuilder str = new StringBuilder();
        while (tmp != null) {
            str.append(tmp).append("\n");
            tmp = reader.readLine();
        }
        return str.toString();
    }

}
