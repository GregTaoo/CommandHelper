package com.everlastsino.comhelper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class CHConfig {

    public static Version version = new Version("1.2.0");
    public static String aboutThisMod = "[CommandHelper] Version " + version + ", By GregTao. Pls follow GPL v3 license.";

    public static String language = "en_us";
    public static boolean playerTPAgreementRequirement = true;
    public static boolean playerAtAllows = true;
    public static String sendTPRequest = "[CommandHelper] Send TP request to player %s.";
    public static String receiveTPRequest = "[CommandHelper] You received a TP request from player %s.";
    public static String targetNotFound = "[CommandHelper] Target not found!";
    public static String requestBeAgreed = "[CommandHelper] Your TP request has been agreed by %s.";
    public static String atReminding = "[CommandHelper] Player %s @@ you!";
    public static String configReloaded = "[CommandHelper] Configs reloaded.";
    public static String configReloadFailed = "[CommandHelper] Failed to reload configs.";
    public static String formatError = "[CommandHelper] FORMAT ERROR.";
    public static String languageChanged = "[CommandHelper] Language changed.";
    public static String languageChangeFailed = "[CommandHelper] Failed to change language.";

    public static String help = "null";

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
            if (readConfigsFromFile()) {
                File file_help = new File("comhelper/help_" + language + ".txt");
                if (!file_help.exists()) {
                    if (((!file_help.getParentFile().exists() && file_help.getParentFile().mkdir())
                            || file_help.getParentFile().exists()) && file_help.createNewFile()) {
                        FileWriter writer = new FileWriter(file_help);
                        writer.write(getHelpTxtFromUri(language));
                        writer.close();
                        comhelper.LOGGER.info("Generated a new help text for default. Trying to reload...");
                    } else {
                        comhelper.LOGGER.error("Failed to create new file.");
                    }
                }
                help = getHelpTxtFromFile();
                return true;
            }
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
        playerAtAllows = pro.getProperty("playerAtAllows", "true").equals("true");

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
        sendTPRequest = getLangPro(pro, "sendTPRequest");
        receiveTPRequest = getLangPro(pro, "receiveTPRequest");
        targetNotFound = getLangPro(pro, "targetNotFound");
        requestBeAgreed = getLangPro(pro, "requestBeAgreed");
        atReminding = getLangPro(pro, "atReminding");
        configReloaded = getLangPro(pro, "configReloaded");
        configReloadFailed = getLangPro(pro, "configReloadedFailed");
        languageChanged = getLangPro(pro, "languageChanged");
        languageChangeFailed = getLangPro(pro, "languageChangeFailed");
        formatError = getLangPro(pro, "formatError");

        stream.close();

        if (language.equals("zh_cn")) {
            aboutThisMod = "[CommandHelper] Version " + version + ", By GregTao, 请遵守GPL v3开源协议";
        } else {
            aboutThisMod = "[CommandHelper] Version " + version + ", By GregTao. Pls follow GPL v3 license.";
        }
        comhelper.LOGGER.info("Successfully loaded configurations.");
        return true;
    }
    
    public static String getLangPro(Properties pro, String key) {
        return "[CommandHelper] " + pro.getProperty(key);
    }

    public static String getDefaultConfigsFromUri() throws Exception {
        URL url = new URL("http://?/CommandHelperService/" + version + "/comhelper.properties");
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
        URL url = new URL("http://?/CommandHelperService/" + version + "/zh_cn.properties");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String tmp = reader.readLine();
        StringBuilder str = new StringBuilder();
        while (tmp != null) {
            str.append(tmp).append("\n");
            tmp = reader.readLine();
        }
        return str.toString();
    }

    public static String getHelpTxtFromUri(String lang) throws Exception {
        URL url = new URL("http://?/CommandHelperService/" + version + "/help_" + lang + ".txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String tmp = reader.readLine();
        StringBuilder str = new StringBuilder();
        while (tmp != null) {
            str.append(tmp).append("\n");
            tmp = reader.readLine();
        }
        return str.toString();
    }

    public static String getHelpTxtFromFile() throws Exception {
        InputStreamReader stream = new InputStreamReader(new FileInputStream("comhelper/help_" + language +".txt"));
        BufferedReader reader = new BufferedReader(stream);
        String tmp = reader.readLine();
        StringBuilder str = new StringBuilder();
        while (tmp != null) {
            str.append(tmp).append("\n");
            tmp = reader.readLine();
        }
        return str.toString();
    }

}

class Version {
    public int first, second, third;

    public Version() {
        this.first = 1;
        this.second = this.third = 0;
    }

    public Version(String versionStr) {
        String[] numbers = versionStr.split("\\.");
        this.first = Integer.parseInt(numbers[0]);
        this.second = Integer.parseInt(numbers[1]);
        this.third = Integer.parseInt(numbers[2]);
    }

    public Version(int fir, int sec, int thi) {
        this.first = fir;
        this.second = sec;
        this.third = thi;
    }

    public void of(String versionStr) {
        String[] numbers = versionStr.split("\\.");
        this.first = Integer.parseInt(numbers[0]);
        this.second = Integer.parseInt(numbers[1]);
        this.third = Integer.parseInt(numbers[2]);
    }

    public void of(int fir, int sec, int thi) {
        this.first = fir;
        this.second = sec;
        this.third = thi;
    }

    public void update(int place, int delta) {
        switch (place) {
            case 1 -> this.first += delta;
            case 2 -> this.second += delta;
            case 3 -> this.third += delta;
        }
    }

    public String toString() {
        return String.format("%d.%d.%d", this.first, this.second, this.third);
    }
}