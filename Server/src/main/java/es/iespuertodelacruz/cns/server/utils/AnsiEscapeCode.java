package es.iespuertodelacruz.cns.server.utils;

public final class AnsiEscapeCode {

    private AnsiEscapeCode() {}

    public static final String CLEAR_CONSOLE = "\033[H\033[2J";

    // Text formatting
    public static final String TEXT_BOLD = "\u001B[1m";
    public static final String TEXT_DIM = "\u001B[2m";
    public static final String TEXT_ITALIC = "\u001B[3m";
    public static final String TEXT_UNDERLINE = "\u001b[4m";
    public static final String TEXT_BLINK = "\u001B[5m";
    public static final String TEXT_INVERT_COLORS = "\u001B[7m";
    public static final String HIDDEN_TEXT = "\u001B[8m";

    // Normal colors
    public final static String COLOR_RESET = "\u001B[0m";
    public static final String COLOR_VIOLET = "\033[0;35m";
    public final static String COLOR_BLACK = "\u001B[30m";
    public final static String COLOR_WHITE = "\u001B[37m";
    public final static String COLOR_RED = "\u001B[31m";
    public final static String COLOR_BLUE = "\u001B[34m";
    public final static String COLOR_CYAN = "\u001B[36m";
    public final static String COLOR_GREEN = "\u001B[32m";
    public final static String COLOR_YELLOW = "\u001B[33m";
    public final static String COLOR_MAGENTA = "\u001B[35m";

    // Bright colors
    public final static String COLOR_BRIGHT_BLACK = "\u001B[90m";
    public final static String COLOR_BRIGHT_RED = "\u001B[91m";
    public final static String COLOR_BRIGHT_GREEN = "\u001B[92m";
    public final static String COLOR_BRIGHT_YELLOW = "\u001B[93m";
    public final static String COLOR_BRIGHT_BLUE = "\u001B[94m";
    public final static String COLOR_BRIGHT_MAGENTA = "\u001B[95m";
    public final static String COLOR_BRIGHT_CYAN = "\u001B[96m";
    public final static String COLOR_BRIGHT_WHITE = "\u001B[97m";
}
