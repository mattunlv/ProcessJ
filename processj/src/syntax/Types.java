package syntax;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author ben
 */
public enum Types {
    
    INSTANCE;
    
    //--------------------------------------------------
    //ESPECIAL TOKENS
    public final int EOF = 0;
    public final int UNKNOWN = 1;
    public final int SINGLELINE_COMMENT = 115;
    public final int MULTILINE_COMMENT = 116;
    
    //--------------------------------------------------
    //LINE FEED TOKEN
    public final int NEWLINE = -1;
    
    //--------------------------------------------------
    //KEYWORDS
    
    //Atomic Types
    public final int KEYWORD_BOOLEAN = 2;
    public final int KEYWORD_BYTE = 3;
    public final int KEYWORD_SHORT = 4;
    public final int KEYWORD_INT = 5;
    public final int KEYWORD_LONG = 6;
    public final int KEYWORD_FLOAT = 7;
    public final int KEYWORD_DOUBLE = 8;
    public final int KEYWORD_CHAR = 9;
    public final int KEYWORD_STRING = 10;
    public final int KEYWORD_VOID = 11;
    
    //Channel related keywords
    public final int KEYWORD_CHAN = 12;
    public final int KEYWORD_READ = 13;
    public final int KEYWORD_WRITE = 14;
    public final int KEYWORD_SHARED = 15;
    public final int KEYWORD_CLAIM = 16;  // No longer need
    
    //Barrier related keywords
    public final int KEYWORD_BARRIER = 17;
    public final int KEYWORD_SYNC = 18;
    public final int KEYWORD_ENROLL = 19;
    
    //Timer related keywords
    public final int KEYWORD_TIMER = 20;
    public final int KEYWORD_TIMEOUT = 21;
    
    //Silly keywords
    public final int KEYWORD_SKIP = 22;
    public final int KEYWORD_STOP = 23;
    public final int KEYWORD_IS = 24;
    
    //Control flow related keywords
    public final int KEYWORD_IF = 25;
    public final int KEYWORD_ELSE = 26;
    public final int KEYWORD_FOR = 27;
    public final int KEYWORD_WHILE = 28;
    public final int KEYWORD_SWITCH = 29;
    public final int KEYWORD_CASE = 30;
    public final int KEYWORD_DO = 31;
    public final int KEYWORD_DEFAULT = 32;
    public final int KEYWORD_BREAK = 33;
    public final int KEYWORD_CONTINUE = 34;
    public final int KEYWORD_RETURN = 35;
    
    //Other process oriented programming related keywords
    public final int KEYWORD_SEQ = 36;
    public final int KEYWORD_PAR = 37;
    public final int KEYWORD_PRI = 38;
    public final int KEYWORD_ALT = 39;
    
    public final int KEYWORD_NEW = 40;
    
    //Mobile processes related keywords
    public final int KEYWORD_RESUME = 41;
    public final int KEYWORD_SUSPEND = 42;
    public final int KEYWORD_WITH = 43;
    
    //Top level element related keywords
    public final int KEYWORD_PROC = 44;
    public final int KEYWORD_PROTOCOL = 45;
    public final int KEYWORD_RECORD = 46;
    public final int KEYWORD_EXTENDS = 47;
    public final int KEYWORD_IMPLEMENTS = 48;
    
    //Package related keywords
    public final int KEYWORD_PACKAGE = 49;
    public final int KEYWORD_IMPORT = 50;
    
    //Modifiers
    public final int KEYWORD_MOBILE = 51;
    public final int KEYWORD_NATIVE = 52;
    public final int KEYWORD_PUBLIC = 53;
    public final int KEYWORD_PRIVATE = 54;
    public final int KEYWORD_PROTECTED = 55;
    public final int KEYWORD_CONST = 56;
    public final int KEYWORD_EXTERN = 57;
    
    //Boolean Literals
    public final int BOOLEAN_LITERAL = 58;
    public final int KEYWORD_TRUE = 581;  //BOOLEAN_LITERAL
    public final int KEYWORD_FALSE = 582; //BOOLEAN_LITERAL
    
    //NULL Literal
    public final int KEYWORD_NULL = 59;
    
    //Other stuff
    public final int KEYWORD_PRAGMA = 60;
    
    //--------------------------------------------------
    //OPERATORS AND OTHER STUFF
    
    //Parentheses
    public final int LPAREN = 61;
    public final int RPAREN = 62;
    public final int LBRACE = 63;
    public final int RBRACE = 64;
    public final int LBRACK = 65;
    public final int RBRACK = 66;
    
    //Separators
    public final int SEMICOLON = 67;
    public final int COMMA = 68;
    
    //Assignment Operators
    public final int EQ = 69;
    public final int MULTEQ = 70;
    public final int DIVEQ = 71;
    public final int MODEQ = 72;
    public final int PLUSEQ = 73;
    public final int MINUSEQ = 74;
    public final int LSHIFTEQ = 75;
    public final int RSHIFTEQ = 76;
    public final int RRSHIFTEQ = 77;
    public final int ANDEQ = 78;
    public final int XOREQ = 79;
    public final int OREQ = 80;
    
    //Relational Operators
    public final int GT = 81;
    public final int LT = 82;
    public final int EQEQ = 83;
    public final int LTEQ = 84;
    public final int GTEQ = 85;
    public final int NOTEQ = 86;
    
    //Binary Operators (Some Unary: +, -)
    public final int LSHIFT = 87;
    public final int RSHIFT = 88;
    public final int RRSHIFT = 89;
    public final int ANDAND = 90;
    public final int OROR = 91;
    public final int PLUS = 92;
    public final int MINUS = 93;
    public final int MULT = 94;
    public final int DIV = 95;
    public final int AND = 96;
    public final int OR = 97;
    public final int XOR = 98;
    public final int MOD = 99;
    
    //Unary Operators
    public final int NOT = 100;
    public final int COMP = 101;
    public final int PLUSPLUS = 102;
    public final int MINUSMINUS = 103;
    
    //Other stuff
    public final int QUEST = 104;
    public final int COLONCOLON = 105;
    public final int COLON = 106;
    public final int DOT = 107;
    
    //--------------------------------------------------
    //LITERALS
    
    //Numeric literals
    public final int INTEGER_LITERAL = 108;
    public final int LONG_LITERAL = 109;
    public final int FLOAT_LITERAL = 110;
    public final int DOUBLE_LITERAL = 111;
    public final int STRING_LITERAL = 112;
    public final int CHARACTER_LITERAL = 113;
    
    //Identifiers
    public static final int IDENTIFIER = 114;
    
    //--------------------------------------------------
    //OTHER
    
    private final HashMap<Integer, String> TEXT = new HashMap<>(); //Symbol -> text
    private final HashMap<String, Integer> TABLE = new HashMap<>(); //Text -> symbol
    private final HashSet<String> KEYWORDS = new HashSet<>();
    private final HashMap<Integer, String> DESCRIPTIONS = new HashMap<>();
    
    //---------------------------------------------------
    //FIELDS AND METHODS
    
    private void addSymbols(String text, int sym) {
        TEXT.put(sym, text);
        TABLE.put(text, sym);
    }
    
    private void addKeywords(String text, int sym) {
        KEYWORDS.add(text);
        addSymbols(text, sym);
    }
    
    private void addDescription(int type, String desc) {
        DESCRIPTIONS.put(type, desc);
    }
    
    public String getText(int type) {
        if (TEXT.containsKey(type))
            return TEXT.get(type);
        return "<>";
    }
    
    public String getDescription(int type) {
        if (DESCRIPTIONS.containsKey(type))
            return DESCRIPTIONS.get(type);
        return "<>";
    }
    
    private Types() {
        //Keyboards
        addKeywords("boolean",      KEYWORD_BOOLEAN);
        addKeywords("byte",         KEYWORD_BYTE);
        addKeywords("short",        KEYWORD_SHORT);
        addKeywords("int",          KEYWORD_INT);
        addKeywords("long",         KEYWORD_LONG);
        addKeywords("float",        KEYWORD_FLOAT);
        addKeywords("double",       KEYWORD_DOUBLE);
        addKeywords("char",         KEYWORD_CHAR);
        addKeywords("string",       KEYWORD_STRING);
        addKeywords("void",         KEYWORD_VOID);
        
        addKeywords("chan",         KEYWORD_CHAN);
        addKeywords("read",         KEYWORD_READ);
        addKeywords("write",        KEYWORD_WRITE);
        addKeywords("shared",       KEYWORD_SHARED);
        addKeywords("claim",        KEYWORD_CLAIM);
        
        addKeywords("barrier",      KEYWORD_BARRIER);
        addKeywords("sync",         KEYWORD_SYNC);
        addKeywords("enroll",       KEYWORD_ENROLL);
        
        addKeywords("timer",        KEYWORD_TIMER);
        addKeywords("timeout",      KEYWORD_TIMEOUT);
        
        addKeywords("skip",         KEYWORD_SKIP);
        addKeywords("stop",         KEYWORD_STOP);
        addKeywords("is",           KEYWORD_IS);
        
        addKeywords("if",           KEYWORD_IF);
        addKeywords("else",         KEYWORD_ELSE);
        addKeywords("for",          KEYWORD_FOR);
        addKeywords("while",        KEYWORD_WHILE);
        addKeywords("switch",       KEYWORD_SWITCH);
        addKeywords("case",         KEYWORD_CASE);
        addKeywords("do",           KEYWORD_DO);
        addKeywords("default",      KEYWORD_DEFAULT);
        addKeywords("break",        KEYWORD_BREAK);
        addKeywords("continue",     KEYWORD_CONTINUE);
        addKeywords("return",       KEYWORD_RETURN);
        
        addKeywords("seq",          KEYWORD_SEQ);
        addKeywords("par",          KEYWORD_PAR);
        addKeywords("pri",          KEYWORD_PRI);
        addKeywords("alt",          KEYWORD_ALT);
        
        addKeywords("new",          KEYWORD_NEW);
        
        addKeywords("resume",       KEYWORD_RESUME);
        addKeywords("suspend",      KEYWORD_SUSPEND);
        addKeywords("with",         KEYWORD_WITH);
        
        addKeywords("proc",         KEYWORD_PROC);
        addKeywords("protocol",     KEYWORD_PROTOCOL);
        addKeywords("record",       KEYWORD_RECORD);
        addKeywords("extends",      KEYWORD_EXTENDS);
        addKeywords("implements",   KEYWORD_IMPLEMENTS);
        
        addKeywords("package",      KEYWORD_PACKAGE);
        addKeywords("import",       KEYWORD_IMPORT);
        
        addKeywords("mobile",       KEYWORD_MOBILE);
        addKeywords("native",       KEYWORD_NATIVE);
        addKeywords("public",       KEYWORD_PUBLIC);
        addKeywords("private",      KEYWORD_PRIVATE);
        addKeywords("protected",    KEYWORD_PROTECTED);
        addKeywords("const",        KEYWORD_CONST);
        addKeywords("extern",       KEYWORD_EXTERN);
        
        addKeywords("true",         KEYWORD_TRUE);
        addKeywords("false",        KEYWORD_FALSE);
        
        addKeywords("null",         KEYWORD_NULL);
        
        addKeywords("#pragma",      KEYWORD_PRAGMA);
        
        // Symbols
        addSymbols("\n",            NEWLINE);
        addSymbols("(",             LPAREN);
        addSymbols(")",             RPAREN);
        addSymbols("{",             LBRACE);
        addSymbols("}",             RBRACE);
        addSymbols("[",             RBRACK);
        addSymbols("]",             LBRACK);
        
        addSymbols(";",             SEMICOLON);
        addSymbols(",",             COMMA);
        
        addSymbols("=",             EQ);
        addSymbols("*=",            MULTEQ);
        addSymbols("/=",            DIVEQ);
        addSymbols("%=",            MODEQ);
        addSymbols("+=",            PLUSEQ);
        addSymbols("-=",            MINUSEQ);
        addSymbols("<<=",           LSHIFTEQ);
        addSymbols(">>=",           RSHIFTEQ);
        addSymbols(">>>=",          RRSHIFTEQ);
        addSymbols("&=",            ANDEQ);
        addSymbols("^=",            XOREQ);
        addSymbols("|=",            OREQ);
        
        addSymbols(">",             GT);
        addSymbols("<",             LT);
        addSymbols("==",            EQEQ);
        addSymbols("<=",            LTEQ);
        addSymbols(">=",            GTEQ);
        addSymbols("!=",            NOTEQ);
        
        addSymbols("<<",            LSHIFT);
        addSymbols(">>",            RSHIFT);
        addSymbols(">>>",           RRSHIFT);
        addSymbols("&&",            ANDAND);
        addSymbols("||",            OROR);
        addSymbols("+",             PLUS);
        addSymbols("-",             MINUS);
        addSymbols("*",             MULT);
        addSymbols("/",             DIV);
        addSymbols("&",             AND);
        addSymbols("|",             OR);
        addSymbols("^",             XOR);
        addSymbols("%",             MOD);
        
        addSymbols("!",             NOT);
        addSymbols("~",             COMP);
        addSymbols("++",            PLUSPLUS);
        addSymbols("--",            MINUSMINUS);
        
        addSymbols("?",             QUEST);
        addSymbols("::",            COLONCOLON);
        addSymbols(":",             COLON);
        addSymbols(".",             DOT);
        
        addSymbols("id",            IDENTIFIER);
        
        //Descriptions
        addDescription(NEWLINE,     "<newline>");        
        addDescription(IDENTIFIER,  "<identifier>");
    }
}
