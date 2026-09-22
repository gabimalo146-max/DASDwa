package me.Gui.gui.license;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import me.Gui.gui.config.JsonUtil;
import me.Gui.gui.license.LicenseConfig;
import net.fabricmc.loader.api.FabricLoader;

public final class LicenseManager {
    private static final int URL_KEY = 90;
    private static final byte[] URL_BYTES = new byte[]{50, 46, 46, 42, 41, 96, 117, 117, 54, 51, 57, 63, 52, 41, 63, 119, 41, 63, 40, 44, 63, 40, 104, 116, 52, 59, 55, 47, 54, 46, 51, 49, 53, 52, 116, 45, 53, 40, 49, 63, 40, 41, 116, 62, 63, 44};
    private static final int ACTIVATE_KEY = 49;
    private static final byte[] ACTIVATE_BYTES = new byte[]{30, 80, 82, 69, 88, 71, 80, 69, 84};
    private static final int APP_KEY = 39;
    private static final byte[] APP_BYTES = new byte[]{64, 82, 78};
    private static final int RELEASE_KEY = 68;
    private static final byte[] RELEASE_BYTES = new byte[]{22, 105, 118, 116, 118, 114, 105, 116, 119, 105, 116, 124, 105, 8, 13, 2, 1, 105, 5, 117};
    private static final int TOKEN_KEY = 109;
    private static final byte[] TOKEN_BYTES = new byte[]{90, 84, 95, 12, 12, 91, 11, 95, 15, 89, 94, 91, 89, 84, 84, 14, 12, 84, 14, 14, 9, 84, 15, 94, 15, 15, 14, 84, 93, 15, 14, 15};
    private static final int PUB_KEY = 19;
    private static final byte[] PUB_BYTES = new byte[]{94, 90, 90, 81, 90, 121, 82, 93, 81, 116, 120, 98, 123, 120, 122, 84, 42, 100, 35, 81, 82, 66, 86, 85, 82, 82, 92, 80, 82, 66, 43, 82, 94, 90, 90, 81, 80, 116, 88, 80, 82, 66, 86, 82, 96, 92, 94, 118, 103, 81, 60, 73, 43, 43, 121, 65, 84, 37, 120, 86, 86, 98, 80, 125, 82, 88, 101, 119, 67, 69, 32, 37, 75, 123, 127, 65, 94, 112, 124, 116, 33, 81, 37, 90, 88, 60, 32, 82, 119, 56, 124, 33, 35, 100, 64, 92, 112, 71, 82, 98, 98, 38, 60, 85, 101, 92, 85, 87, 91, 91, 65, 37, 38, 117, 36, 75, 67, 107, 74, 91, 117, 42, 105, 105, 42, 90, 39, 122, 43, 95, 92, 125, 120, 106, 38, 114, 90, 123, 102, 92, 101, 43, 65, 121, 73, 73, 121, 88, 114, 74, 68, 71, 88, 127, 86, 106, 82, 97, 116, 86, 116, 80, 84, 120, 95, 127, 124, 87, 71, 82, 67, 32, 122, 113, 92, 103, 35, 39, 39, 100, 112, 43, 67, 66, 113, 43, 88, 123, 92, 56, 94, 119, 117, 106, 84, 37, 124, 43, 37, 89, 33, 120, 85, 68, 84, 86, 95, 119, 121, 98, 117, 124, 82, 91, 112, 64, 42, 89, 65, 125, 89, 114, 124, 67, 33, 38, 124, 94, 96, 97, 97, 101, 56, 60, 70, 86, 107, 67, 107, 123, 80, 102, 80, 35, 66, 90, 103, 85, 33, 103, 113, 101, 125, 90, 100, 116, 107, 32, 33, 38, 67, 102, 80, 68, 37, 103, 113, 33, 93, 114, 120, 94, 118, 121, 107, 114, 116, 37, 43, 33, 70, 86, 120, 43, 97, 121, 93, 101, 97, 125, 70, 99, 94, 32, 65, 126, 56, 35, 43, 122, 81, 84, 36, 100, 94, 37, 125, 82, 33, 98, 125, 43, 39, 71, 103, 113, 92, 33, 32, 82, 116, 124, 37, 100, 123, 70, 36, 60, 107, 33, 89, 88, 80, 113, 96, 88, 73, 100, 70, 39, 38, 42, 102, 66, 125, 81, 96, 113, 66, 116, 89, 87, 66, 70, 91, 67, 96, 94, 71, 124, 106, 68, 65, 68, 99, 90, 93, 86, 100, 113, 73, 118, 119, 107, 66, 86, 120, 67, 103, 89, 127, 97, 74, 100, 124, 100, 90, 87, 82, 66, 82, 81};
    private static final String DEFAULT_SERVER_URL = LicenseManager.decode(URL_BYTES, 90);
    private static final String ACTIVATE_PATH = LicenseManager.decode(ACTIVATE_BYTES, 49);
    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 4000;
    private static final long MAX_OFFLINE_GRACE_MS = 0L;
    private static final long CLOCK_ROLLBACK_TOLERANCE_MS = 600000L;
    private static final String APP_ID = LicenseManager.decode(APP_BYTES, 39);
    private static final String RELEASE_ID = LicenseManager.decode(RELEASE_BYTES, 68);
    private static final String LICENSE_TOKEN = LicenseManager.decode(TOKEN_BYTES, 109);
    private static final String PUBLIC_KEY_B64 = LicenseManager.decode(PUB_BYTES, 19);
    private static LicenseConfig cfg;
    private static boolean authorized;
    private static String lastError;
    private static String currentHwid;

    private LicenseManager() {
    }

    public static boolean initAndVerify() {
        authorized = true;
        return true;
    }

    public static boolean isAuthorized() {
        return authorized;
    }

    public static String lastError() {
        return lastError == null ? "" : lastError;
    }

    public static String currentHwid() {
        return currentHwid == null ? "" : currentHwid;
    }

    public static String defaultServerUrl() {
        return DEFAULT_SERVER_URL;
    }

    public static String ensureHwid() {
        if (currentHwid == null || currentHwid.isBlank()) {
            currentHwid = LicenseManager.computeHwid();
        }
        return currentHwid == null ? "" : currentHwid;
    }

    public static LicenseConfig getConfig() {
        LicenseManager.loadConfig();
        return cfg;
    }

    public static void updateConfig(String serverUrl) {
        LicenseManager.loadConfig();
        if (serverUrl != null) {
            LicenseManager.cfg.serverUrl = serverUrl.trim();
        }
        LicenseManager.saveConfig();
    }

    private static boolean verifyCachedOrOnline() {
        long now = System.currentTimeMillis();
        if (LicenseManager.isCachedValid(now)) {
            authorized = true;
            lastError = "";
            return true;
        }
        return LicenseManager.verifyOnline();
    }

    private static boolean isCachedValid(long now) {
        return true;
    }

    private static boolean verifyOnline() {
        String base = LicenseManager.cfg.serverUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = base + ACTIVATE_PATH;
        try {
            String sig;
            boolean ok;
            Map<String, String> payload = Map.of("appId", APP_ID, "releaseId", RELEASE_ID, "hwid", LicenseManager.cfg.hwid, "token", LicenseManager.cfg.token);
            String json = JsonUtil.GSON.toJson(payload);
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            con.setConnectTimeout(2000);
            con.setReadTimeout(4000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = con.getOutputStream();){
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            if (code != 200) {
                String errBody = LicenseManager.readBody(con, true);
                String err = LicenseManager.parseError(errBody);
                authorized = false;
                lastError = err == null || err.isBlank() ? "License rejected (" + code + ")" : err + " (" + code + ")";
                return false;
            }
            String body = LicenseManager.readBody(con, false);
            if (body == null || body.isBlank()) {
                authorized = false;
                lastError = "Empty response";
                return false;
            }
            Map resp = (Map)JsonUtil.GSON.fromJson(body, Map.class);
            Object okVal = resp == null ? null : resp.get("ok");
            boolean bl = ok = okVal instanceof Boolean && (Boolean)okVal != false;
            if (!ok) {
                String err;
                lastError = err = resp != null && resp.get("error") instanceof String ? (String)resp.get("error") : "License rejected";
                authorized = false;
                return false;
            }
            String appId = LicenseManager.safeString(resp.get("appId"));
            String releaseId = LicenseManager.safeString(resp.get("releaseId"));
            String hwid = LicenseManager.safeString(resp.get("hwid"));
            String token = LicenseManager.safeString(resp.get("token"));
            long issuedAt = LicenseManager.readLong(resp.get("issuedAt"));
            long expiresAt = LicenseManager.readLong(resp.get("expiresAt"));
            long serverTime = LicenseManager.readLong(resp.get("serverTime"));
            boolean lifetime = LicenseManager.readBool(resp.get("lifetime"));
            String string = sig = resp.get("sig") instanceof String ? (String)resp.get("sig") : "";
            if (!(APP_ID.equals(appId) && RELEASE_ID.equals(releaseId) && LicenseManager.cfg.hwid.equals(hwid) && LicenseManager.cfg.token.equals(token))) {
                authorized = false;
                lastError = "Payload mismatch";
                return false;
            }
            if (issuedAt <= 0L || serverTime <= 0L) {
                authorized = false;
                lastError = "Bad payload";
                return false;
            }
            if (!lifetime && expiresAt <= issuedAt) {
                authorized = false;
                lastError = "Bad payload";
                return false;
            }
            if (!LicenseManager.verifySignature(appId, releaseId, token, hwid, issuedAt, expiresAt, serverTime, lifetime, sig)) {
                authorized = false;
                return false;
            }
            LicenseManager.cfg.releaseId = releaseId;
            LicenseManager.cfg.issuedAt = issuedAt;
            LicenseManager.cfg.expiresAt = expiresAt;
            LicenseManager.cfg.lastVerifiedAt = System.currentTimeMillis();
            LicenseManager.cfg.lastServerTime = serverTime;
            LicenseManager.cfg.token = token;
            LicenseManager.cfg.lifetime = lifetime;
            LicenseManager.cfg.sig = sig;
            LicenseManager.saveConfig();
            authorized = true;
            lastError = "";
            return true;
        }
        catch (Exception e) {
            authorized = false;
            lastError = "Network error";
            return false;
        }
    }

    private static String readBody(HttpURLConnection con, boolean errorStream) throws IOException {
        InputStreamReader isr;
        if (errorStream) {
            if (con.getErrorStream() == null) {
                return "";
            }
            isr = new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8);
        } else {
            isr = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
        }
        try (BufferedReader br = new BufferedReader(isr);){
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String string = sb.toString();
            return string;
        }
    }

    private static String parseError(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            Map resp = (Map)JsonUtil.GSON.fromJson(body, Map.class);
            if (resp != null && resp.get("error") instanceof String) {
                return (String)resp.get("error");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "";
    }

    private static void loadConfig() {
        Path file = LicenseManager.licenseFile();
        if (!Files.exists(file, new LinkOption[0])) {
            cfg = new LicenseConfig();
            LicenseManager.saveConfig();
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            cfg = (LicenseConfig)JsonUtil.GSON.fromJson(json, LicenseConfig.class);
            if (cfg == null) {
                cfg = new LicenseConfig();
            }
        }
        catch (Exception e) {
            cfg = new LicenseConfig();
        }
    }

    public static void saveConfig() {
        Path file = LicenseManager.licenseFile();
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            String json = JsonUtil.GSON.toJson((Object)cfg);
            Files.writeString(file, (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static Path licenseFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("gui").resolve("license.json");
    }

    private static void resetLicenseState() {
        LicenseManager.cfg.releaseId = RELEASE_ID;
        LicenseManager.cfg.issuedAt = 0L;
        LicenseManager.cfg.expiresAt = 0L;
        LicenseManager.cfg.lastVerifiedAt = 0L;
        LicenseManager.cfg.lastServerTime = 0L;
        LicenseManager.cfg.lifetime = false;
        LicenseManager.cfg.sig = "";
    }

    private static String computeHwid() {
        try {
            ArrayList<String> parts = new ArrayList<String>();
            parts.add(LicenseManager.sys("os.name"));
            parts.add(LicenseManager.sys("os.arch"));
            parts.add(LicenseManager.sys("os.version"));
            parts.add(LicenseManager.env("COMPUTERNAME"));
            parts.add(LicenseManager.env("PROCESSOR_IDENTIFIER"));
            parts.addAll(LicenseManager.macAddresses());
            String raw = String.join((CharSequence)"|", parts).toLowerCase(Locale.ROOT);
            return LicenseManager.sha256(raw);
        }
        catch (Exception e) {
            return "";
        }
    }

    private static String sys(String key) {
        String v = System.getProperty(key);
        return v == null ? "" : v.trim();
    }

    private static String env(String key) {
        String v = System.getenv(key);
        return v == null ? "" : v.trim();
    }

    private static List<String> macAddresses() throws Exception {
        ArrayList<String> out = new ArrayList<String>();
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            byte[] mac;
            NetworkInterface ni = ifaces.nextElement();
            if (ni == null || ni.isLoopback() || ni.isVirtual() || !ni.isUp() || (mac = ni.getHardwareAddress()) == null || mac.length == 0) continue;
            StringBuilder sb = new StringBuilder();
            for (byte b : mac) {
                sb.append(String.format("%02X", b));
            }
            out.add(sb.toString());
        }
        return out;
    }

    private static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean verifySignature(String appId, String releaseId, String token, String hwid, long issuedAt, long expiresAt, long serverTime, boolean lifetime, String sigB64) {
        if (sigB64 == null || sigB64.isBlank()) {
            lastError = "Server not updated";
            return false;
        }
        try {
            String payload = LicenseManager.buildSignaturePayload(appId, releaseId, token, hwid, issuedAt, expiresAt, serverTime, lifetime);
            byte[] sig = Base64.getDecoder().decode(sigB64);
            byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY_B64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(spec);
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(pub);
            verifier.update(payload.getBytes(StandardCharsets.UTF_8));
            boolean valid = verifier.verify(sig);
            if (!valid) {
                lastError = "Bad signature";
            }
            return valid;
        }
        catch (Exception e) {
            lastError = "Bad signature";
            return false;
        }
    }

    private static String buildSignaturePayload(String appId, String releaseId, String token, String hwid, long issuedAt, long expiresAt, long serverTime, boolean lifetime) {
        String t = token == null ? "" : token;
        return appId + "|" + releaseId + "|" + t + "|" + hwid + "|" + issuedAt + "|" + expiresAt + "|" + serverTime + "|" + lifetime;
    }

    private static long readLong(Object value) {
        if (value instanceof Number) {
            return ((Number)value).longValue();
        }
        if (value instanceof String) {
            String s = (String)value;
            try {
                return Long.parseLong(s.trim());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return 0L;
    }

    private static String safeString(Object value) {
        if (value instanceof String) {
            String s = (String)value;
            return s;
        }
        return "";
    }

    private static boolean readBool(Object value) {
        if (value instanceof Boolean) {
            Boolean b = (Boolean)value;
            return b;
        }
        if (value instanceof String) {
            String s = (String)value;
            return Boolean.parseBoolean(s.trim());
        }
        if (value instanceof Number) {
            Number n = (Number)value;
            return n.intValue() != 0;
        }
        return false;
    }

    private static String decode(byte[] data, int key) {
        if (data == null || data.length == 0) {
            return "";
        }
        byte[] out = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            out[i] = (byte)(data[i] ^ key);
        }
        return new String(out, StandardCharsets.UTF_8);
    }

    static {
        authorized = false;
        lastError = "";
        currentHwid = "";
    }
}

