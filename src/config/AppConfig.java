package config;

public final class AppConfig {
    public static final String APP_NAME = "Cuddle Cup POS";
    public static final String DB_URL =
            "jdbc:mysql://localhost:3306/java_shop?allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "";

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_ADMIN_NAME = "Cuddle Cup Admin";

    public static final String IMAGE_LOGO = "images/logo.jpeg";
    public static final String IMAGE_MENU = "images/menu.jpeg";
    public static final int LOW_STOCK_THRESHOLD = 10;

    public static final String STORE_NAME = "CUDDLE CUP";
    public static final String STORE_OPERATOR = "Operated By cuddle cup Group inc.";
    public static final String STORE_ADDRESS = "#27 Banner AVE. Fourth Estate, Paranaque city";
    public static final String VAT_REG_TIN = "009-123-456-000";
    public static final String MIN = "123456789";
    public static final String STORE_WEBSITE = "www.cuddlecup.ph";
    public static final String POS_PROVIDER = "Information Systems Inc.";
    public static final String ACCREDITATION_NO = "0300003305150000071263B";
    public static final String ACCREDITATION_DATE_ISSUED = "04/16/2007";
    public static final String ACCREDITATION_VALID_UNTIL = "07/31/2026";
    public static final double VAT_RATE = 0.12;

    private AppConfig() {
    }
}
