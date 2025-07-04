package flat.backend;

public class WLEnums {

    public static final int CURSOR = 0x00033001;
    public static final int STICKY_KEYS = 0x00033002;
    public static final int STICKY_MOUSE_BUTTONS = 0x00033003;

    public static final int CURSOR_NORMAL = 0x00034001;
    public static final int CURSOR_HIDDEN = 0x00034002;
    public static final int CURSOR_DISABLED = 0x00034003;

    public static final int	STANDARD_ARROW_CURSOR = 0x00036001;
    public static final int	STANDARD_IBEAM_CURSOR = 0x00036002;
    public static final int	STANDARD_CROSSHAIR_CURSOR = 0x00036003;
    public static final int	STANDARD_HAND_CURSOR = 0x00036004;
    public static final int	STANDARD_EW_RESIZE_CURSOR = 0x00036005;
    public static final int	STANDARD_NS_RESIZE_CURSOR = 0x00036006;
    public static final int	STANDARD_NWSE_RESIZE_CURSOR = 0x00036007;
    public static final int	STANDARD_NESW_RESIZE_CURSOR = 0x00036008;
    public static final int	STANDARD_RESIZE_ALL_CURSOR = 0x00036009;
    public static final int	STANDARD_NOT_ALLOWED_CURSOR = 0x0003600A;

    public static final int RELEASE = 0;
    public static final int PRESS = 1;
    public static final int REPEAT = 2;

    public static final int HAT_CENTERED = 0;
    public static final int HAT_UP = 1;
    public static final int HAT_RIGHT = 2;
    public static final int HAT_DOWN = 4;
    public static final int HAT_LEFT = 8;
    public static final int HAT_RIGHT_UP = (HAT_RIGHT | HAT_UP);
    public static final int HAT_RIGHT_DOWN = (HAT_RIGHT | HAT_DOWN);
    public static final int HAT_LEFT_UP = (HAT_LEFT | HAT_UP);
    public static final int HAT_LEFT_DOWN = (HAT_LEFT | HAT_DOWN);

    public static final int KEY_UNKNOWN = -1;

    public static final int KEY_SPACE = 32;
    public static final int KEY_APOSTROPHE = 39;
    public static final int KEY_COMMA = 44;
    public static final int KEY_MINUS = 45;
    public static final int KEY_PERIOD = 46;
    public static final int KEY_SLASH = 47;
    public static final int KEY_0 = 48;
    public static final int KEY_1 = 49;
    public static final int KEY_2 = 50;
    public static final int KEY_3 = 51;
    public static final int KEY_4 = 52;
    public static final int KEY_5 = 53;
    public static final int KEY_6 = 54;
    public static final int KEY_7 = 55;
    public static final int KEY_8 = 56;
    public static final int KEY_9 = 57;
    public static final int KEY_SEMICOLON = 59;
    public static final int KEY_EQUAL = 61;
    public static final int KEY_A = 65;
    public static final int KEY_B = 66;
    public static final int KEY_C = 67;
    public static final int KEY_D = 68;
    public static final int KEY_E = 69;
    public static final int KEY_F = 70;
    public static final int KEY_G = 71;
    public static final int KEY_H = 72;
    public static final int KEY_I = 73;
    public static final int KEY_J = 74;
    public static final int KEY_K = 75;
    public static final int KEY_L = 76;
    public static final int KEY_M = 77;
    public static final int KEY_N = 78;
    public static final int KEY_O = 79;
    public static final int KEY_P = 80;
    public static final int KEY_Q = 81;
    public static final int KEY_R = 82;
    public static final int KEY_S = 83;
    public static final int KEY_T = 84;
    public static final int KEY_U = 85;
    public static final int KEY_V = 86;
    public static final int KEY_W = 87;
    public static final int KEY_X = 88;
    public static final int KEY_Y = 89;
    public static final int KEY_Z = 90;
    public static final int KEY_LEFT_BRACKET = 91;
    public static final int KEY_BACKSLASH = 92;
    public static final int KEY_RIGHT_BRACKET = 93;
    public static final int KEY_GRAVE_ACCENT = 96;
    public static final int KEY_WORLD_1 = 161;
    public static final int KEY_WORLD_2 = 162;

    public static final int KEY_ESCAPE = 256;
    public static final int KEY_ENTER = 257;
    public static final int KEY_TAB = 258;
    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_INSERT = 260;
    public static final int KEY_DELETE = 261;
    public static final int KEY_RIGHT = 262;
    public static final int KEY_LEFT = 263;
    public static final int KEY_DOWN = 264;
    public static final int KEY_UP = 265;
    public static final int KEY_PAGE_UP = 266;
    public static final int KEY_PAGE_DOWN = 267;
    public static final int KEY_HOME = 268;
    public static final int KEY_END = 269;
    public static final int KEY_CAPS_LOCK = 280;
    public static final int KEY_SCROLL_LOCK = 281;
    public static final int KEY_NUM_LOCK = 282;
    public static final int KEY_PRINT_SCREEN = 283;
    public static final int KEY_PAUSE = 284;
    public static final int KEY_F1 = 290;
    public static final int KEY_F2 = 291;
    public static final int KEY_F3 = 292;
    public static final int KEY_F4 = 293;
    public static final int KEY_F5 = 294;
    public static final int KEY_F6 = 295;
    public static final int KEY_F7 = 296;
    public static final int KEY_F8 = 297;
    public static final int KEY_F9 = 298;
    public static final int KEY_F10 = 299;
    public static final int KEY_F11 = 300;
    public static final int KEY_F12 = 301;
    public static final int KEY_F13 = 302;
    public static final int KEY_F14 = 303;
    public static final int KEY_F15 = 304;
    public static final int KEY_F16 = 305;
    public static final int KEY_F17 = 306;
    public static final int KEY_F18 = 307;
    public static final int KEY_F19 = 308;
    public static final int KEY_F20 = 309;
    public static final int KEY_F21 = 310;
    public static final int KEY_F22 = 311;
    public static final int KEY_F23 = 312;
    public static final int KEY_F24 = 313;
    public static final int KEY_F25 = 314;
    public static final int KEY_KP_0 = 320;
    public static final int KEY_KP_1 = 321;
    public static final int KEY_KP_2 = 322;
    public static final int KEY_KP_3 = 323;
    public static final int KEY_KP_4 = 324;
    public static final int KEY_KP_5 = 325;
    public static final int KEY_KP_6 = 326;
    public static final int KEY_KP_7 = 327;
    public static final int KEY_KP_8 = 328;
    public static final int KEY_KP_9 = 329;
    public static final int KEY_KP_DECIMAL = 330;
    public static final int KEY_KP_DIVIDE = 331;
    public static final int KEY_KP_MULTIPLY = 332;
    public static final int KEY_KP_SUBTRACT = 333;
    public static final int KEY_KP_ADD = 334;
    public static final int KEY_KP_ENTER = 335;
    public static final int KEY_KP_EQUAL = 336;
    public static final int KEY_LEFT_SHIFT = 340;
    public static final int KEY_LEFT_CONTROL = 341;
    public static final int KEY_LEFT_ALT = 342;
    public static final int KEY_LEFT_SUPER = 343;
    public static final int KEY_RIGHT_SHIFT = 344;
    public static final int KEY_RIGHT_CONTROL = 345;
    public static final int KEY_RIGHT_ALT = 346;
    public static final int KEY_RIGHT_SUPER = 347;
    public static final int KEY_MENU = 348;
    public static final int KEY_LAST = KEY_MENU;

    public static final int MOD_SHIFT = 0x1;
    public static final int MOD_CONTROL = 0x2;
    public static final int MOD_ALT = 0x4;
    public static final int MOD_SUPER = 0x8;

    public static final int MOUSE_BUTTON_1 = 0;
    public static final int MOUSE_BUTTON_2 = 1;
    public static final int MOUSE_BUTTON_3 = 2;
    public static final int MOUSE_BUTTON_4 = 3;
    public static final int MOUSE_BUTTON_5 = 4;
    public static final int MOUSE_BUTTON_6 = 5;
    public static final int MOUSE_BUTTON_7 = 6;
    public static final int MOUSE_BUTTON_8 = 7;
    public static final int MOUSE_BUTTON_LAST = MOUSE_BUTTON_8;
    public static final int MOUSE_BUTTON_LEFT = MOUSE_BUTTON_1;
    public static final int MOUSE_BUTTON_RIGHT = MOUSE_BUTTON_2;
    public static final int MOUSE_BUTTON_MIDDLE = MOUSE_BUTTON_3;

    public static final int JOYSTICK_1 = 0;
    public static final int JOYSTICK_2 = 1;
    public static final int JOYSTICK_3 = 2;
    public static final int JOYSTICK_4 = 3;
    public static final int JOYSTICK_5 = 4;
    public static final int JOYSTICK_6 = 5;
    public static final int JOYSTICK_7 = 6;
    public static final int JOYSTICK_8 = 7;
    public static final int JOYSTICK_9 = 8;
    public static final int JOYSTICK_10 = 9;
    public static final int JOYSTICK_11 = 10;
    public static final int JOYSTICK_12 = 11;
    public static final int JOYSTICK_13 = 12;
    public static final int JOYSTICK_14 = 13;
    public static final int JOYSTICK_15 = 14;
    public static final int JOYSTICK_16 = 15;
    public static final int JOYSTICK_LAST = JOYSTICK_16;

    public static final int GAMEPAD_BUTTON_A = 0;
    public static final int GAMEPAD_BUTTON_B = 1;
    public static final int GAMEPAD_BUTTON_X = 2;
    public static final int GAMEPAD_BUTTON_Y = 3;
    public static final int GAMEPAD_BUTTON_LEFT_BUMPER = 4;
    public static final int GAMEPAD_BUTTON_RIGHT_BUMPER = 5;
    public static final int GAMEPAD_BUTTON_BACK = 6;
    public static final int GAMEPAD_BUTTON_START = 7;
    public static final int GAMEPAD_BUTTON_GUIDE = 8;
    public static final int GAMEPAD_BUTTON_LEFT_THUMB = 9;
    public static final int GAMEPAD_BUTTON_RIGHT_THUMB = 10;
    public static final int GAMEPAD_BUTTON_DPAD_UP = 11;
    public static final int GAMEPAD_BUTTON_DPAD_RIGHT = 12;
    public static final int GAMEPAD_BUTTON_DPAD_DOWN = 13;
    public static final int GAMEPAD_BUTTON_DPAD_LEFT = 14;
    public static final int GAMEPAD_BUTTON_LAST = GAMEPAD_BUTTON_DPAD_LEFT;
    public static final int GAMEPAD_BUTTON_CROSS = GAMEPAD_BUTTON_A;
    public static final int GAMEPAD_BUTTON_CIRCLE = GAMEPAD_BUTTON_B;
    public static final int GAMEPAD_BUTTON_SQUARE = GAMEPAD_BUTTON_X;
    public static final int GAMEPAD_BUTTON_TRIANGLE = GAMEPAD_BUTTON_Y;

    public static final int GAMEPAD_AXIS_LEFT_X = 0;
    public static final int GAMEPAD_AXIS_LEFT_Y = 1;
    public static final int GAMEPAD_AXIS_RIGHT_X = 2;
    public static final int GAMEPAD_AXIS_RIGHT_Y = 3;
    public static final int GAMEPAD_AXIS_LEFT_TRIGGER = 4;
    public static final int GAMEPAD_AXIS_RIGHT_TRIGGER = 5;
    public static final int GAMEPAD_AXIS_LAST = GAMEPAD_AXIS_RIGHT_TRIGGER;


    public interface WindowPosCallback {
        void handle(long window, int x, int y);
    }
    public interface WindowSizeCallback {
        void handle(long window, int width, int height);
    }
    public interface WindowCloseCallback {
        boolean handle(long window);
    }
    public interface WindowRefreshCallback {
        void handle(long window);
    }
    public interface WindowFocusCallback {
        void handle(long window, boolean focus);
    }
    public interface WindowIconifyCallback {
        void handle(long window, boolean minimized);
    }
    public interface WindowBufferSizeCallback {
        void handle(long window, int width, int height);
    }

    public interface MouseButtonCallback {
        void handle(long window, int button, int action, int mods);
    }
    public interface CursorPosCallback {
        void handle(long window, double x, double y);
    }
    public interface CursorEnterCallback {
        void handle(long window, boolean entered);
    }
    public interface KeyCallback {
        void handle(long window, int key, int scancode, int action, int mods);
    }
    public interface CharCallback {
        void handle(long window, int codepoint);
    }
    public interface CharModsCallback {
        void handle(long window, int codepoint, int mods);
    }
    public interface DropCallback {
        void handle(long window, String[] names);
    }
    public interface ScrollCallback {
        void handle(long window, double x, double y);
    }

    public interface JoyCallback {
        void handle(long window, int joy, boolean connected);
    }

    public interface ErrorCallback {
        void handle(String error);
    }
    public interface DialogCallback {
        void handle(long window, String path);
    }
}