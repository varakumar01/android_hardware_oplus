package android.os;

import java.io.File;

public class OplusBaseEnvironment {

    public static File getOplusCustomDirectory() {
        return new File("/my_company");
    }

    public static File getOplusEngineerDirectory() {
        return new File("/my_engineering");
    }

    public static File getOplusProductDirectory() {
        return new File("/my_product");
    }

    public static File getOplusVersionDirectory() {
        return new File("/my_version");
    }
}
