package com.vagdedes.spartan.functionality.connection.cloud;

import com.vagdedes.spartan.functionality.server.Config;
import com.vagdedes.spartan.utils.math.AlgebraUtils;
import me.vagdedes.spartan.api.API;

import java.util.Objects;

public class IDs {

    private static String
            user = "%%__USER__%%",
            file = "%%__NONCE__%%";

    public static final boolean
            hasUserIDByDefault = !user.startsWith("%%__");

    private static int platform = 0;

    static {
        if (!file.startsWith("%%__") && !AlgebraUtils.validInteger(file)) {
            file = String.valueOf(Objects.hash(file));
        }
    }

    // Setters

    static void set(int user, int nonce) {
        IDs.user = Integer.toString(user);
        IDs.file = Integer.toString(nonce);
        Config.refreshFields(false);
    }

    public static void setPlatform(int id) {
        platform = id;
    }

    // IDs

    public static String user() {
        return user;
    }

    public static String file() {
        return !JarVerification.enabled ? (CloudBase.hasToken() ? Integer.toString(CloudBase.getRawToken().hashCode()) : user) : file;
    }

    static String resource() {
        return "%%__RESOURCE__%%";
    }

    static String platform() {
        return IDs.isBuiltByBit() ? "BuiltByBit" : IDs.isPolymart() ? "Polymart" : "SpigotMC";
    }

    // Platforms

    public static boolean isBuiltByBit() {
        return platform == 2 || "%%__FILEHASH__%%".length() != 16;
    }

    public static boolean isPolymart() {
        return platform == 3 || "%%__POLYMART__%%".length() == 1;
    }

    public static String hide(String id) {
        try {
            double version = Double.parseDouble(API.getVersion().substring(6));
            double number = AlgebraUtils.cut(Integer.parseInt(id) / version, 6);
            return String.valueOf(number).replace("-", "*").replace(".", "-");
        } catch (Exception ex) {
            return "0";
        }
    }

}
