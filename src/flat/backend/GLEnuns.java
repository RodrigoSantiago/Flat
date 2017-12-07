package flat.backend;

public class GLEnuns {
    //----CLEAR BITMASK
    public static final int CB_COLOR_BUFFER_BIT = 0x00004000;
    public static final int CB_DEPTH_BUFFER_BIT = 0x00000100;
    public static final int CB_STENCIL_BUFFER_BIT = 0x00000400;

    //----DATA TYPES
    public static final int DT_BYTE = 0x1400;
    public static final int DT_SHORT = 0x1402;
    public static final int DT_INT = 0x1404;
    public static final int DT_FLOAT = 0x1406;
    public static final int DT_DOUBLE = 0x140A;

    //----HINT VALUES
    public static final int HV_DONT_CARE = 0x1100;
    public static final int HV_FASTEST = 0x1101;
    public static final int HV_NICEST = 0x1102;

    //----HINT TARGET
    public static final int HT_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B;
    /*GL*/
    public static final int HT_LINE_SMOOTH_HINT = 0x0C52;
    public static final int HT_POLYGON_SMOOTH_HINT = 0x0C53;
    public static final int HT_TEXTURE_COMPRESSION_HINT = 0x84EF;
    /*ES*/
    public static final int HT_GENERATE_MIPMAP_HINT = 0x8192;

    //----PIXEL STORE
    public static final int PS_PACK_ALIGNMENT = 0x0D05;
    public static final int PS_PACK_ROW_LENGTH = 0x0D02;
    public static final int PS_PACK_SKIP_PIXELS = 0x0D04;
    public static final int PS_PACK_SKIP_ROWS = 0x0D03;
    public static final int PS_PACK_IMAGE_HEIGHT = 0x806C;
    public static final int PS_PACK_SKIP_IMAGES = 0x806B;
    public static final int PS_UNPACK_ALIGNMENT = 0x0CF5;
    public static final int PS_UNPACK_ROW_LENGTH = 0x0CF2;
    public static final int PS_UNPACK_SKIP_PIXELS = 0x0CF4;
    public static final int PS_UNPACK_SKIP_ROWS = 0x0CF3;
    public static final int PS_UNPACK_IMAGE_HEIGHT = 0x806E;
    public static final int PS_UNPACK_SKIP_IMAGES = 0x806D;

    //----MATH FUNCTIONS
    public static final int MF_NEVER = 0x0200;
    public static final int MF_LESS = 0x0201;
    public static final int MF_EQUAL = 0x0202;
    public static final int MF_LEQUAL = 0x0203;
    public static final int MF_GREATER = 0x0204;
    public static final int MF_NOTEQUAL = 0x0205;
    public static final int MF_GEQUAL = 0x0206;
    public static final int MF_ALWAYS = 0x0207;

    //----MATH OPERATIONS
    public static final int MO_KEEP = 0x1E00;
    public static final int MO_ZERO = 0x0;
    public static final int MO_REPLACE = 0x1E01;
    public static final int MO_INCR = 0x1E02;
    public static final int MO_INCR_WRAP = 0x8507;
    public static final int MO_DECR = 0x1E03;
    public static final int MO_DECR_WRAP = 0x8508;

    //----BLEND FUNCTION
    public static final int BF_ZERO = 0x0;
    public static final int BF_ONE = 0x1;
    public static final int BF_SRC_COLOR = 0x0300;
    public static final int BF_ONE_MINUS_SRC_COLOR = 0x0301;
    public static final int BF_SRC_ALPHA = 0x0302;
    public static final int BF_ONE_MINUS_SRC_ALPHA = 0x0303;
    public static final int BF_DST_ALPHA = 0x0304;
    public static final int BF_ONE_MINUS_DST_ALPHA = 0x0305;
    public static final int BF_DST_COLOR = 0x0306;
    public static final int BF_ONE_MINUS_DST_COLOR = 0x0307;
    public static final int BF_SRC_ALPHA_SATURATE = 0x0308;
    public static final int BF_CONSTANT_COLOR = 0x8001;
    public static final int BF_ONE_MINUS_CONSTANT_COLOR = 0x8002;
    public static final int BF_CONSTANT_ALPHA = 0x8003;
    public static final int BF_ONE_MINUS_CONSTANT_ALPHA = 0x8004;

    //----BLEND EQUATION
    public static final int BE_FUNC_ADD = 0x8006;
    public static final int BE_FUNC_SUBTRACT = 0x800A;
    public static final int BE_FUNC_REVERSE_SUBTRACT = 0x800B;
    public static final int BE_MIN = 0x8007;
    public static final int BE_MAX = 0x8008;

    //----FACES (SET DATA)
    public static final int FC_FRONT = 0x0404;
    public static final int FC_BACK = 0x0405;
    public static final int FC_FRONT_AND_BACK = 0x0408;

    //----FACES (GET DATA)
    public static final int FG_FRONT = 0x0404;
    public static final int FG_BACK = 0x0405;

    //----FRONT FACE
    public static final int FF_CW = 0x0900;
    public static final int FF_CCW = 0x0901;

    //----VERTEX MODE (DRAW ARRAYS/ELEMENTS)
    public static final int VM_POINTS = 0x0000;
    public static final int VM_LINE_STRIP = 0x0003;
    public static final int VM_LINE_LOOP = 0x0002;
    public static final int VM_LINES = 0x0001;
    public static final int VM_TRIANGLE_STRIP = 0x0005;
    public static final int VM_TRIANGLE_FAN = 0x0006;
    public static final int VM_TRIANGLES = 0x0004;

    //---- FRAMEBUFFER BIND TARGET
    public static final int FB_READ_FRAMEBUFFER = 0x8CA8;
    public static final int FB_DRAW_FRAMEBUFFER = 0x8CA9;
    public static final int FB_FRAMEBUFFER = 0x8D40;

    //----FRAMEBUFFER ATTACHMENT
    public static final int FA_MAX_COLOR_ATTACHMENTS = 0x8CDF;
    public static final int FA_COLOR_ATTACHMENT0 = 0x8CE0;
    public static final int FA_COLOR_ATTACHMENT1 = 0x8CE1;
    public static final int FA_COLOR_ATTACHMENT2 = 0x8CE2;
    public static final int FA_COLOR_ATTACHMENT3 = 0x8CE3;
    public static final int FA_COLOR_ATTACHMENT4 = 0x8CE4;
    public static final int FA_COLOR_ATTACHMENT5 = 0x8CE5;
    public static final int FA_COLOR_ATTACHMENT6 = 0x8CE6;
    public static final int FA_COLOR_ATTACHMENT7 = 0x8CE7;
    public static final int FA_COLOR_ATTACHMENT8 = 0x8CE8;
    public static final int FA_COLOR_ATTACHMENT9 = 0x8CE9;
    public static final int FA_COLOR_ATTACHMENT10 = 0x8CEA;
    public static final int FA_COLOR_ATTACHMENT11 = 0x8CEB;
    public static final int FA_COLOR_ATTACHMENT12 = 0x8CEC;
    public static final int FA_COLOR_ATTACHMENT13 = 0x8CED;
    public static final int FA_COLOR_ATTACHMENT14 = 0x8CEE;
    public static final int FA_COLOR_ATTACHMENT15 = 0x8CEF;
    public static final int FA_DEPTH_ATTACHMENT = 0x8D00;
    public static final int FA_STENCIL_ATTACHMENT = 0x8D20;
    public static final int FA_DEPTH_STENCIL_ATTACHMENT = 0x821A;

    //----FRAMEBUFFER STATUS
    public static final int FS_FRAMEBUFFER_COMPLETE = 0x8CD5;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 0x8CDB;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 0x8CDC;
    public static final int FS_FRAMEBUFFER_UNSUPPORTED = 0x8CDD;
    public static final int FS_FRAMEBUFFER_UNDEFINED = 0x8219;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56;
    public static final int FS_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS = 0x8DA8;

    //----FRAMEBUFFER ATTACHMENT OBJECT TYPE
    public static final int FO_RENDERBUFFER = 0x8D41;
    public static final int FO_TEXTURE = 0x1702;
    public static final int FO_NONE = 0x0;

    //----FRAMEBUFFER BLIT MASK
    public static final int BM_COLOR_BUFFER_BIT = 0x00004000;
    public static final int BM_DEPTH_BUFFER_BIT = 0x00000100;
    public static final int BM_STENCIL_BUFFER_BIT = 0x00000400;

    //----FRAMEBUFFER BLIT FILTER
    public static final int BF_NEAREST = 0x2600;
    public static final int BF_LINEAR = 0x2601;

    //----TEXTURE BIND TARGET
    public static final int TB_TEXTURE_2D = 0x0DE1;
    public static final int TB_TEXTURE_CUBE_MAP = 0x8513;
    public static final int TB_TEXTURE_2D_MULTISAMPLE = 0x9100;
    public static final int _TB_TEXTURE_3D = 0x806F;
    public static final int _TB_TEXTURE_2D_ARRAY = 0x8C1A;

    //----TEXTURE TARGET TYPE
    public static final int TT_TEXTURE_2D = 0x0DE1;
    public static final int TT_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
    public static final int TT_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
    public static final int TT_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
    public static final int TT_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
    public static final int TT_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;
    public static final int TT_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
    public static final int _TT_TEXTURE_3D = 0x806F;
    public static final int _TT_TEXTURE_2D_ARRAY = 0x8C1A;

    //----TEXTURE FORMAT
    public static final int TF_RGB = 0x1907;                    //form = rgb,  type = GL_UNSIGNED_BYTE
    public static final int TF_RGBA = 0x1908;                   //form = rgba, type = GL_UNSIGNED_BYTE
    public static final int TF_DEPTH_COMPONENT32F = 0x8CAC;     //form = dc,   type = GL_FLOAT
    public static final int TF_DEPTH_COMPONENT24 = 0x81A6;      //form = dc,   type = GL_UNSIGNED_INT
    public static final int TF_DEPTH_COMPONENT16 = 0x81A5;      //form = dc,   type = GL_UNSIGNED_SHORT
    public static final int TF_DEPTH32F_STENCIL8 = 0x8CAD;      //form = ds,   type = GL_FLOAT_32_UNSIGNED_INT_24_8_REV
    public static final int TF_DEPTH24_STENCIL8 = 0x88F0;       //form = ds    type = UNSIGNED_INT_24_8

    //----IMAGE FILTERS
    public static final int IF_NEAREST = 0x2600;
    public static final int IF_LINEAR = 0x2601;
    public static final int IF_NEAREST_MIPMAP_NEAREST = 0x2700;
    public static final int IF_LINEAR_MIPMAP_NEAREST = 0x2701;
    public static final int IF_NEAREST_MIPMAP_LINEAR = 0x2702;
    public static final int IF_LINEAR_MIPMAP_LINEAR = 0x2703;

    //----IMAGE WRAP MODE
    public static final int IW_CLAMP_TO_EDGE = 0x812F;
    public static final int IW_CLAMP_TO_BORDER = 0x812D;
    public static final int IW_MIRRORED_REPEAT = 0x8370;
    public static final int IW_REPEAT = 0x2901;

    //----IMAGE COMPARE MODE
    public static final int CM_COMPARE_REF_TO_TEXTURE = 0x884E;
    public static final int CM_NONE = 0x0;

    //----COLOR CHANEL
    public static final int CC_RED = 0x1903;
    public static final int CC_GREEN = 0x1904;
    public static final int CC_BLUE = 0x1905;
    public static final int CC_ALPHA = 0x1906;
    public static final int CC_ZERO = 0x0;
    public static final int CC_ONE = 0x1;

    //----BUFFER BIND TYPE
    public static final int BB_ARRAY_BUFFER = 0x8892;
    public static final int BB_COPY_READ_BUFFER = 0x8F36;
    public static final int BB_COPY_WRITE_BUFFER = 0x8F37;
    public static final int BB_ELEMENT_ARRAY_BUFFER = 0x8893;
    public static final int BB_PIXEL_PACK_BUFFER = 0x88EB;
    public static final int BB_PIXEL_UNPACK_BUFFER = 0x88EC;
    public static final int BB_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E;
    public static final int BB_UNIFORM_BUFFER = 0x8A11;

    //----USAGE TYPE
    public static final int UT_STREAM_DRAW = 0x88E0;
    public static final int UT_STREAM_READ = 0x88E1;
    public static final int UT_STREAM_COPY = 0x88E2;
    public static final int UT_STATIC_DRAW = 0x88E4;
    public static final int UT_STATIC_READ = 0x88E5;
    public static final int UT_STATIC_COPY = 0x88E6;
    public static final int UT_DYNAMIC_DRAW = 0x88E8;
    public static final int UT_DYNAMIC_READ = 0x88E9;
    public static final int UT_DYNAMIC_COPY = 0x88EA;

    //----ACESS BITMASK
    public static final int AM_MAP_READ_BIT = 0x0001;
    public static final int AM_MAP_WRITE_BIT = 0x0002;
    public static final int AM_MAP_INVALIDATE_RANGE_BIT = 0x0004;
    public static final int AM_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
    public static final int AM_MAP_FLUSH_EXPLICIT_BIT = 0x0010;
    public static final int AM_MAP_UNSYNCHRONIZED_BIT = 0x0020;

    //----SHADER TYPE
    public static final int ST_FRAGMENT_SHADER = 0x8B30;
    public static final int ST_VERTEX_SHADER = 0x8B31;
    public static final int _ST_GEOMETRY_SHADER = 0x8DD9;

    //----TRANSFORM FEEDBACK POLYGON TYPE
    public static final int FP_POINTS = 0x0000;
    public static final int FP_LINES = 0x0001;
    public static final int FP_TRIANGLES = 0x0004;

    //----TRANSFORM FEEDBACK BUFFER MODE
    public static final int FM_INTERLEAVED_ATTRIBS = 0x8C8C;
    public static final int FM_SEPARATE_ATTRIBS = 0x8C8D;
}