package org.processj.compiler.phase.generated;

import org.processj.compiler.ast.Token;
import org.processj.compiler.utilities.syntax.Types;

%%

%class Lexer
%public
%unicode
%line
%column
%cup

%{
    /// --------------------
            /// Public Static Fields

            public static boolean   Debug       = false ;
            public static String    CurrentLine = ""    ;
            public static int       LineCount   = 0     ;
            public static int       Spaces      = 0     ;

            /// --------------
            /// Public Methods

            public void addToLine(String string, int line) {

                CurrentLine = (line != LineCount) ? string : CurrentLine + string;

                LineCount = line;

            }

            public void addLineComment() {

                final String line = "Comment, line "
                    + (yyline + 1) + " [" + (yycolumn + 1 + Spaces) + ":" + (yycolumn + yylength()) + "]";

                final Token token = (yytext().startsWith("/*"))
                    ? new Token(Types.INSTANCE.MULTILINE_COMMENT,
                        "Multi-line " + line, yyline + 1, yycolumn + 1, yycolumn + yylength())
                    : new Token(Types.INSTANCE.SINGLELINE_COMMENT,
                        "Single-line " + line, yyline + 1, yycolumn + 1, yycolumn + yylength());

            }

            public void countSpaces(int line) {

                Spaces = line;

            }

            private java_cup.runtime.Symbol token(int kind) {

                this.addToLine(yytext(), yyline+1);

                final Token token = new Token(kind, yytext(), yyline + 1, yycolumn + 1, yycolumn + yylength());

                if(Debug) System.out.println(token);

                return new java_cup.runtime.Symbol(kind, token);

            }

%}

/** --------------------------------------------------------------------------------------------------------------- **/
/** Character Class Definition                                                                                      **/
/** --------------------------------------------------------------------------------------------------------------- **/

LineTerminator          = \r|\n|\r\n
InputCharacter          = [^\r\n]
WhiteSpace              = {LineTerminator} | [ \t\f]

/** --------------------------------------------------------------------------------------------------------------- **/
/** Comment Definition                                                                                              **/
/** --------------------------------------------------------------------------------------------------------------- **/

Comment                 = {TraditionalComment} | {EndOfLineComment} | {DocumentationComment} | {EndOfFileComment}

TraditionalComment      = "/*" [^*] {CommentContent} \*+ "/"
UnterminatedComment     = "/*" [^*] {CommentContent} \** "/"?
EndOfLineComment        = "//" {InputCharacter}* {LineTerminator}
DocumentationComment    = "/**" {CommentContent} \*+ "/"
EndOfFileComment        = "//" {InputCharacter}*
CommentContent          = ( [^*] | \*+[^*/] )*

/** --------------------------------------------------------------------------------------------------------------- **/
/** Identifier Defintion                                                                                            **/
/** --------------------------------------------------------------------------------------------------------------- **/

// TODO: Check [a-zA-Z$_] | ~[\u0000-\u007F]\uD800-\uDBFF] | [\uD800-\uDBFF] [\uDC00-\uDFFF]
Identifier              = [:jletter:][:jletterdigit:]*

/** --------------------------------------------------------------------------------------------------------------- **/
/** Integral Literal Definition                                                                                     **/
/** --------------------------------------------------------------------------------------------------------------- **/

// TODO: Add underscores to decimal, hex, & octal digits
// TODO: Check specification, these should end with a digit and not an underscore
DecIntegerLiteral       = 0 | [1-9][0-9]*
DecLongLiteral          = {DecIntegerLiteral} [lL]

HexIntegerLiteral       = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral          = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit                = [0-9a-fA-F]

OctIntegerLiteral       = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral          = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit                = [0-7]

BinaryIntegerLiteral    = 0 [bB] 0* {BinaryDigit} {1, 32}
BinaryLongLiteral       = 0 [bB] 0* {BinaryDigit} {1, 64} [lL]
BinaryDigit             = [01]

/** --------------------------------------------------------------------------------------------------------------- **/
/** Floating Point Literal Definition                                                                               **/
/** --------------------------------------------------------------------------------------------------------------- **/

// TODO: Hexadecimal Floating-Point Literals?
FloatLiteral            = ({FLit1}|{FLit2}|{FLit3}|{FLit4}) [fF]*
DoubleLiteral           = ({FLit1}|{FLit2}|{FLit3}|{FLit4}) [dD]

FLit1                   = [0-9]+ \. [0-9]* {Exponent}?
FLit2                   = \. [0-9]+ {Exponent}?
FLit3                   = [0-9]+ {Exponent}
FLit4                   = [0-9]+ {Exponent}?

Exponent                = [eE] [+\-]? [0-9]+

/** --------------------------------------------------------------------------------------------------------------- **/
/** String Literal Defintion                                                                                        **/
/** --------------------------------------------------------------------------------------------------------------- **/

StringCharacter         = [^\r\n\"\\]
SingleCharacter         = [^\r\n\'\\]
StringEscape            = \\([btnfr\"\'\\]|[0-3]?{OctDigit}?{OctDigit}|u{HexDigit}{HexDigit}{HexDigit}{HexDigit})

%%

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Modifiers                                                                                                   **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "mobile"    { return token(sym.MOBILE)                                                                           ;}
    "native"    { return token(sym.NATIVE)                                                                           ;}
    "public"    { return token(sym.PUBLIC)                                                                           ;}
    "private"   { return token(sym.PRIVATE)                                                                          ;}
    "protected" { return token(sym.PROTECTED)                                                                        ;}
    "const"     { return token(sym.CONST)                                                                            ;}
    "extern"    { return token(sym.EXTERN)                                                                           ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Keywords                                                                                                    **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "break"      { return token(sym.BREAK)                                                                           ;}
    "case"       { return token(sym.CASE)                                                                            ;}
    "continue"   { return token(sym.CONTINUE)                                                                        ;}
    "default"    { return token(sym.DEFAULT)                                                                         ;}
    "do"         { return token(sym.DO)                                                                              ;}
    "else"       { return token(sym.ELSE)                                                                            ;}
    "extends"    { return token(sym.EXTENDS)                                                                         ;}
    "for"        { return token(sym.FOR)                                                                             ;}
    "if"         { return token(sym.IF)                                                                              ;}
    "implements" { return token(sym.IMPLEMENTS)                                                                      ;}
    "import"     { return token(sym.IMPORT)                                                                          ;}
    "is"         { return token(sym.IS)                                                                              ;}
    "new"        { return token(sym.NEW)                                                                             ;}
    "package"    { return token(sym.PACKAGE)                                                                         ;}
    "return"     { return token(sym.RETURN)                                                                          ;}
    "switch"     { return token(sym.SWITCH)                                                                          ;}
    "while"      { return token(sym.WHILE)                                                                           ;}
    "#pragma"    { return token(sym.PRAGMA)                                                                          ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Process Keywords                                                                                            **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "alt"       { return token(sym.ALT)                                                                              ;}
    "chan"      { return token(sym.CHAN)                                                                             ;}
    "claim"     { return token(sym.CLAIM)                                                                            ;}
    "enroll"    { return token(sym.ENROLL)                                                                           ;}
    "fork"      { return token(sym.FORK)                                                                             ;}
    "par"       { return token(sym.PAR)                                                                              ;}
    "pri"       { return token(sym.PRI)                                                                              ;}
    "proc"      { return token(sym.PROC)                                                                             ;}
    "protocol"  { return token(sym.PROTOCOL)                                                                         ;}
    "read"      { return token(sym.READ)                                                                             ;}
    "record"    { return token(sym.RECORD)                                                                           ;}
    "resume"    { return token(sym.RESUME)                                                                           ;}
    "seq"       { return token(sym.SEQ)                                                                              ;}
    "shared"    { return token(sym.SHARED)                                                                           ;}
    "skip"      { return token(sym.SKIP)                                                                             ;}
    "stop"      { return token(sym.STOP)                                                                             ;}
    "sync"      { return token(sym.SYNC)                                                                             ;}
    "suspend"   { return token(sym.SUSPEND)                                                                          ;}
    "timeout"   { return token(sym.TIMEOUT)                                                                          ;}
    "with"      { return token(sym.WITH)                                                                             ;}
    "write"     { return token(sym.WRITE)                                                                            ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /* Separators                                                                                                   **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "("         { return token(sym.LPAREN)                                                                           ;}
    ")"         { return token(sym.RPAREN)                                                                           ;}
    "["         { return token(sym.LBRACK)                                                                           ;}
    "]"         { return token(sym.RBRACK)                                                                           ;}
    "{"         { return token(sym.LBRACE)                                                                           ;}
    "}"         { return token(sym.RBRACE)                                                                           ;}
    ";"         { return token(sym.SEMICOLON)                                                                        ;}
    ","         { return token(sym.COMMA)                                                                            ;}
    "."         { return token(sym.DOT)                                                                              ;}
    "?"         { return token(sym.QUEST)                                                                            ;}
    ":"         { return token(sym.COLON)                                                                            ;}
    "::"        { return token(sym.COLONCOLON)                                                                       ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Unary Operators                                                                                             **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "!"         { return token(sym.NOT)                                                                              ;}
    "~"         { return token(sym.COMP)                                                                             ;}
    "++"        { return token(sym.PLUSPLUS)                                                                         ;}
    "--"        { return token(sym.MINUSMINUS)                                                                       ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Bitwise Operators                                                                                           **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "&"         { return token(sym.AND)                                                                              ;}
    "^"         { return token(sym.XOR)                                                                              ;}
    "|"         { return token(sym.OR)                                                                               ;}
    "<<"        { return token(sym.LSHIFT)                                                                           ;}
    ">>"        { return token(sym.RSHIFT)                                                                           ;}
    ">>>"       { return token(sym.RRSHIFT)                                                                          ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Logical Operators                                                                                           **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "&&"        { return token(sym.ANDAND)                                                                           ;}
    "||"        { return token(sym.OROR)                                                                             ;}
    "=="        { return token(sym.EQEQ)                                                                             ;}
    "!="        { return token(sym.NOTEQ)                                                                            ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Relational Operators                                                                                        **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    ">"         { return token(sym.GT)                                                                               ;}
    "<"         { return token(sym.LT)                                                                               ;}
    "<="        { return token(sym.LTEQ)                                                                             ;}
    ">="        { return token(sym.GTEQ)                                                                             ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Arithmetic Operators                                                                                        **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "+"         { return token(sym.PLUS)                                                                             ;}
    "-"         { return token(sym.MINUS)                                                                            ;}
    "*"         { return token(sym.MULT)                                                                             ;}
    "/"         { return token(sym.DIV)                                                                              ;}
    "%"         { return token(sym.MOD)                                                                              ;}
    "="         { return token(sym.EQ)                                                                               ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Compound Assignment Operator                                                                                **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "&="        { return token(sym.ANDEQ)                                                                            ;}
    "^="        { return token(sym.XOREQ)                                                                            ;}
    "|="        { return token(sym.OREQ)                                                                             ;}
    "<<="       { return token(sym.LSHIFTEQ)                                                                         ;}
    ">>="       { return token(sym.RSHIFTEQ)                                                                         ;}
    ">>>="      { return token(sym.RRSHIFTEQ)                                                                        ;}
    "+="        { return token(sym.PLUSEQ)                                                                           ;}
    "-="        { return token(sym.MINUSEQ)                                                                          ;}
    "*="        { return token(sym.MULTEQ)                                                                           ;}
    "/="        { return token(sym.DIVEQ)                                                                            ;}
    "%="        { return token(sym.MODEQ)                                                                            ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Types                                                                                                       **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "boolean"   { return token(sym.BOOLEAN)                                                                          ;}
    "byte"      { return token(sym.BYTE)                                                                             ;}
    "char"      { return token(sym.CHAR)                                                                             ;}
    "double"    { return token(sym.DOUBLE)                                                                           ;}
    "float"     { return token(sym.FLOAT)                                                                            ;}
    "int"       { return token(sym.INT)                                                                              ;}
    "long"      { return token(sym.LONG)                                                                             ;}
    "short"     { return token(sym.SHORT)                                                                            ;}
    "string"    { return token(sym.STRING)                                                                           ;}
    "void"      { return token(sym.VOID)                                                                             ;}
    "barrier"   { return token(sym.BARRIER)                                                                          ;}
    "timer"     { return token(sym.TIMER)                                                                            ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Boolean Literals                                                                                            **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "true"      { return token(sym.BOOLEAN_LITERAL)                                                                  ;}
    "false"     { return token(sym.BOOLEAN_LITERAL)                                                                  ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Null Literal                                                                                                **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    "null"      { return token(sym.NULL_LITERAL)                                                                     ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Numeric Literals                                                                                            **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    {DecIntegerLiteral}            { return token(sym.INTEGER_LITERAL)                                               ;}
    {DecLongLiteral}               { return token(sym.LONG_LITERAL)                                                  ;}

    {HexIntegerLiteral}            { return token(sym.INTEGER_LITERAL)                                               ;}
    {HexLongLiteral}               { return token(sym.LONG_LITERAL)                                                  ;}

    {OctIntegerLiteral}            { return token(sym.INTEGER_LITERAL)                                               ;}
    {OctLongLiteral}               { return token(sym.LONG_LITERAL)                                                  ;}

    {FloatLiteral}                 { return token(sym.FLOAT_LITERAL)                                                 ;}
    {DoubleLiteral}                { return token(sym.DOUBLE_LITERAL)                                                ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Character Literals                                                                                          **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    \'{SingleCharacter}\'          { return token(sym.CHARACTER_LITERAL)                                             ;}
    \'{StringEscape}\'             { return token(sym.CHARACTER_LITERAL)                                             ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** String Literals                                                                                             **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    \"({StringCharacter})*\"       { return token(sym.STRING_LITERAL)                                                ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Identifier                                                                                                  **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    {Identifier}                   { return token(sym.IDENTIFIER)                                                    ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Comments                                                                                                    **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    {Comment}                      { addToLine(yytext(), yyline+1); addLineComment()                                 ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Whitespace                                                                                                  **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    {WhiteSpace}                   { addToLine(yytext(), yyline+1)                                                   ;}

    /** ----------------------------------------------------------------------------------------------------------- **/
    /** Error Fallback                                                                                              **/
    /** ----------------------------------------------------------------------------------------------------------- **/

    {UnterminatedComment}                   { throw new RuntimeException("Unterminated comment at EOF at line "+(yyline+1)+", column "+(yycolumn+1))                            ;}
    \"({StringCharacter})*{LineTerminator}  { throw new RuntimeException("Unterminated string at end-of-line \""+yytext()+"\" at line "+(yyline+1)+", column "+(yycolumn+1))    ;}
    \'{SingleCharacter}?{LineTerminator}    { throw new RuntimeException("Unterminated character at end-of-line \""+yytext()+"\" at line "+(yyline+1)+", column "+(yycolumn+1)) ;}
    \'{StringEscape}?{LineTerminator}       { throw new RuntimeException("Unterminated character at end-of-line \""+yytext()+"\" at line "+(yyline+1)+", column "+(yycolumn+1)) ;}
    [^]                                     { throw new RuntimeException("Illegal character \""+yytext()+"\" at line "+(yyline+1)+", column "+(yycolumn+1))                     ;}
