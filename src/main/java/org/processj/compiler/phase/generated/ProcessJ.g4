/**
 * <p>ProcessJ Grammar.</p>
 * <p>This grammar defines the specification for ProcessJ source code.</p>
 * @author Jan B. Pedersen
 * @author Cabel Shrestha
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version %I%, %G%
 * @since 0.1.0
 */

grammar ProcessJ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 1.0 Source                                                                                                      **/
/** --------------------------------------------------------------------------------------------------------------- **/

compilationUnit
    : pragma* packageDeclaration? importDeclaration* typeDeclaration*
    ;

qualifiedName
    : Identifier ('.' qualifiedName)?
    ;

names
    : Identifier (',' names)?
    ;

name
    : Identifier
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 1.1 Pragma                                                                                                      **/
/** --------------------------------------------------------------------------------------------------------------- **/

pragma
    : '#pragma' Identifier StringLiteral? ';'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 1.2 Package Declaration                                                                                         **/
/** --------------------------------------------------------------------------------------------------------------- **/

packageDeclaration
    : 'package' qualifiedName ';'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 1.3 Import Declaration                                                                                          **/
/** --------------------------------------------------------------------------------------------------------------- **/

importDeclaration
    : 'import' qualifiedName ('.*')? ';'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 2.0 Type Declaration                                                                                            **/
/** --------------------------------------------------------------------------------------------------------------- **/

typeDeclaration
    : procedureTypeDeclaration
    | protocolTypeDeclaration
    | recordTypeDeclaration
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 2.1 Procedure Types                                                                                             **/
/** --------------------------------------------------------------------------------------------------------------- **/

procedureTypeDeclaration
    : modifier* type Identifier '(' formalParameters? ')' annotations? ('implements' names)? (block | ';')
    ;

formalParameters
    : modifier* type variableDeclarator (',' formalParameters)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 2.2 Protocol Types                                                                                              **/
/** --------------------------------------------------------------------------------------------------------------- **/

protocolTypeDeclaration
    : modifier* 'protocol' Identifier extends? annotations? (protocolBody | ';')
    ;

protocolBody
    : '{' protocolCase+ '}'
    ;

protocolCase
    : Identifier ':' '{' recordMember* '}'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 2.3 Record Types                                                                                                **/
/** --------------------------------------------------------------------------------------------------------------- **/

recordTypeDeclaration
    : modifier* 'record' Identifier extends? annotations? recordBody
    ;

extends
    : 'extends' names
    ;

annotations
    : '[' annotation (',' annotations)? ']'
    ;

annotation
    : Identifier '=' annotation_value
    ;

annotation_value
    : Identifier
    | BooleanLiteral
    | IntegerLiteral
    | FloatingPointLiteral
    ;

recordBody
    : '{' recordMember* '}'
    ;

recordMember
    : type names ';'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 3.0 Types                                                                                                       **/
/** --------------------------------------------------------------------------------------------------------------- **/

type
    : typeWithoutDims
    | typeWithDims
    ;

typeWithoutDims
    : primitiveType
    | channelType
    | channelEndType
    | name
    ;

typeWithDims
    : typeWithoutDims ('[' ']')+
    ;

primitiveType
    : 'boolean'
    | 'char'
    | 'byte'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'string'
    | 'barrier'
    | 'timer'
    | 'void'
    ;

channelType
    : 'shared'? ('read' | 'write')? 'chan' '<' type '>'
    ;

channelEndType
    : 'shared'? ('read' | 'write')? 'chan' '<' type '>' ('.' ('read' | 'write'))?
    ;


/** --------------------------------------------------------------------------------------------------------------- **/
/** 4.0 Modifiers                                                                                                   **/
/** --------------------------------------------------------------------------------------------------------------- **/

modifier
    : 'mobile'
    | 'const'
    | 'native'
    | 'public'
    | 'private'
    | 'protected'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 5.0 Constants and Variable Declarations                                                                         **/
/** --------------------------------------------------------------------------------------------------------------- **/

variableDeclaration
    : modifier* type variableDeclarators
    ;

variableDeclarators
    : variableDeclarator (',' variableDeclarators)?
    ;

variableDeclarator
    : Identifier (dimension)* ('=' (arrayInitializer | expression))?
    ;

dimension
    : '[' ']'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 6.0 Arrays                                                                                                      **/
/** --------------------------------------------------------------------------------------------------------------- **/

arrayInitializer
    : '{' variableInitializers? '}'
    ;

variableInitializers
    : (arrayInitializer | expression) (',' variableInitializers)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.0 Blocks and statements                                                                                       **/
/** --------------------------------------------------------------------------------------------------------------- **/

block
    : '{' statement* '}'
    ;

// TO'do': we should be able to declare mobile channels and procedures here
statement
    : statementWithoutTrailingSubstatement
    | ifThenStatement
    | ifThenElseStatement
    | whileStatement
    | forStatement
    | switchStatement
    | labelledStatement
    ;

statementWithoutTrailingSubstatement
    : block
    | altBlock
    | 'par' ('enroll' barriers)? block
    | 'seq' block
    | doStatement
    | barrierSyncStatement
    | timeoutStatement ';'
    | 'break' Identifier? ';'
    | claimStatement ';'
    | 'continue' Identifier? ';'
    | 'return' expression ';'
    | 'skip' ';'
    | 'stop' ';'
    | 'suspend' ';'
    | statementExpression ';'
    | Identifier '.' 'write' '(' expression ')' ';'
    | primaryExpression '.' 'write' '(' expression ')' ';'
    | variableDeclaration ';'
    | ';'
    ;

barriers
    : expression (',' barriers)?
    ;

statementNoShortIf
    : statementWithoutTrailingSubstatement
    | ifThenElseStatementNoShortIf
    | whileStatementNoShortIf
    | forStatementNoShortIf
    | claimStatementNoShortIf
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.1 If statements                                                                                               **/
/** --------------------------------------------------------------------------------------------------------------- **/

ifThenStatement
    : 'if' '(' expression ')' statement
    ;

ifThenElseStatement
    : 'if' '(' expression ')' statementNoShortIf 'else' statement
    ;

ifThenElseStatementNoShortIf
    : 'if' '(' expression ')' statementNoShortIf 'else' statementNoShortIf
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.2 While/For/Do statement                                                                                      **/
/** --------------------------------------------------------------------------------------------------------------- **/

whileStatement
    : 'while' '(' expression ')' statement
    ;

whileStatementNoShortIf
    : 'while' '(' expression ')' statementNoShortIf
    ;

forStatement
    : 'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
    | 'par' 'for' '(' forInit? ';' expression? ';' forUpdate? ')' 'enroll' '(' arguments ')' statement
    | 'par' 'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
    ;

forStatementNoShortIf
    : 'for' '(' forInit? ';' expression? ';' forUpdate? ')' statementNoShortIf
    | 'par' 'for' '(' forInit? ';' expression? ';' forUpdate? ')' 'enroll' '(' arguments ')' statementNoShortIf
    | 'par' 'for' '(' forInit? ';' expression? ';' forUpdate? ')' statementNoShortIf
    ;

forInit
    : statementExpression
    | variableDeclaration
    ;

forUpdate
    : statementExpression
    ;

doStatement
    : 'do' statement 'while' '(' expression ')' ';'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.3 Claim Statement                                                                                             **/
/** --------------------------------------------------------------------------------------------------------------- **/

claimStatement
    : 'claim' '(' channels_ ')' statement
    ;

claimStatementNoShortIf
    : 'claim' '(' channels_ ')' statementNoShortIf
    ;

channels_
    : channel_ (',' channels_)?
    ;

channel_
    : leftHandSideExpression ('.' 'read')?
    | leftHandSideExpression ('.' 'write')?
    | channelType Identifier '=' primaryExpressionNoCreation
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.4 Barrier Sync/Timeout Statement                                                                              **/
/** --------------------------------------------------------------------------------------------------------------- **/

barrierSyncStatement
    : primaryExpression '.' 'sync' '(' ')'
    | Identifier '.' 'sync' '(' ')'
    ;

timeoutStatement
    : Identifier '.' 'timeout' '(' expression ')'
    | primaryExpression '.' 'timeout' '(' expression ')'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.6 Expression Statement                                                                                        **/
/** --------------------------------------------------------------------------------------------------------------- **/

statementExpression
    : assignmentExpression
    | preIncrementExpression
    | preDecrementExpression
    | postfixExpression
    | leftHandSideExpression // TO'do': Error on some of these options
    ;

labelledStatement
    : Identifier ':' statement
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.8 Switch Statement                                                                                            **/
/** --------------------------------------------------------------------------------------------------------------- **/

switchStatement
    : 'switch' '(' expression ')' switchBlock
    ;

switchBlock
    : '{' switchBlockStatementGroup* '}'
    ;

switchBlockStatementGroup
    : (('case' expression | 'default') ':')+ statement*
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.9 Alt Statement                                                                                               **/
/** --------------------------------------------------------------------------------------------------------------- **/

altBlock
    : 'pri'? 'alt' ('(' forInit? ';' expression? ';' forUpdate? ')')? '{' altCase* '}'
    ;

altCase
    : ('(' expression ')' '&&')? guard ':' statement
    ;

guard
    : leftHandSideExpression '=' name channelReadSuffix
    | 'skip'
    | timeoutStatement
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.0 Expressions                                                                                                 **/
/** --------------------------------------------------------------------------------------------------------------- **/

expression
    : conditionalExpression
    | assignmentExpression
    ;

assignmentExpression
    : leftHandSideExpression assignmentOperator expression
    ;

conditionalExpression
    : conditionalOrExpression ('?' expression ':' conditionalExpression)?
    ;

conditionalOrExpression
    : conditionalAndExpression ('||' conditionalOrExpression)?
    ;

conditionalAndExpression
    : inclusiveOrExpression ('&&' conditionalAndExpression)?
    ;

inclusiveOrExpression
    : exclusiveOrExpression ('|' inclusiveOrExpression)?
    ;

exclusiveOrExpression
    : andExpression ('^' exclusiveOrExpression)?
    ;

andExpression
    : equalityExpression ('&' andExpression)?
    ;

equalityExpression
    : relationalExpression (('==' | '!=') equalityExpression)?
    ;

relationalExpression
    : shiftExpression (('<'|'>'|'<='|'>=') relationalExpression)?
    | shiftExpression 'is' name
    ;

shiftExpression
    : additiveExpression (('<<'|'>>'|'>>>') shiftExpression)?
    ;

additiveExpression
    : multiplicativeExpression (('plus'|'minus') additiveExpression)?
    ;

multiplicativeExpression
    : unaryExpression (('*'|'div'|'%') multiplicativeExpression)?
    ;

unaryExpression
    : 'plus' unaryExpression
    | 'minus' unaryExpression
    | preIncrementExpression
    | preDecrementExpression
    | unaryExpressionNotPlusMinus
    ;

preIncrementExpression
    : '++' unaryExpression
    ;

preDecrementExpression
    : '--' unaryExpression
    ;

unaryExpressionNotPlusMinus
    : '~' unaryExpression
    | '!' unaryExpression
    | castExpression
    | postfixExpression
    ;

castExpression
    : '(' typeWithoutDims ')' unaryExpressionNotPlusMinus
    ;

postfixExpression
    : primaryExpression ('++'|'--')*
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.1 Primary Expressions                                                                                         **/
/** --------------------------------------------------------------------------------------------------------------- **/

primaryExpression
    : primaryExpressionNoCreation
    | newArrayExpression
    | newMobileExpression
    | newRecordExpression
    | newProtocolExpression
    ;

primaryExpressionNoCreation
    : leftHandSideExpression
    | literal
    ;

// TO'do': check for fork(), timeout(), sync(), channel.read(), channel.write()
leftHandSideExpression
    : name suffix?
    ;

suffix
    : arrayAccessSuffix suffix?
    | invocationSuffix suffix?
    | recordAccessSuffix suffix?
    | channelReadSuffix suffix?
    | channelWriteSuffix suffix?
    ;

arrayAccessSuffix
    : '[' expression ']'
    ;

recordAccessSuffix
    : '.' Identifier
    ;

channelReadSuffix
    : '.' 'read' '(' block? ')'
    ;

channelWriteSuffix
    : '.' 'write' '(' expression ')'
    ;

invocationSuffix
    : '(' arguments? ')'
    ;

arguments
    : expression (',' arguments)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.2 Array Creation Expression                                                                                   **/
/** --------------------------------------------------------------------------------------------------------------- **/

newArrayExpression
    : 'new' typeWithoutDims dimExpression+ dims? arrayInitializer?
    ;

dimExpression
    : '[' expression ']'
    ;

dims
    : ('[' ']')+
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.3 Record Creation Expression                                                                                  **/
/** --------------------------------------------------------------------------------------------------------------- **/

newRecordExpression
    : 'new' name '{' newRecordExpressionArguments '}'
    ;

newRecordExpressionArguments
    : Identifier '=' expression (',' newRecordExpressionArguments)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.4 Protocol Creation Expression                                                                                **/
/** --------------------------------------------------------------------------------------------------------------- **/

newProtocolExpression
    : 'new' name '{' Identifier ':' newRecordExpressionArguments? '}'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.5 Mobile Creation Expression                                                                                  **/
/** --------------------------------------------------------------------------------------------------------------- **/

newMobileExpression
    : 'new' 'mobile' '(' name ')'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 9.0 Literals                                                                                                    **/
/** --------------------------------------------------------------------------------------------------------------- **/

literal
    : IntegerLiteral
    | FloatingPointLiteral
    | BooleanLiteral
    | StringLiteral
    | CharacterLiteral
    | NullLiteral
    ;

assignmentOperator
    : '='
    | '*='
    | '/='
    | '%='
    | '+='
    | '-='
    | '<<='
    | '>>='
    | '>>>='
    | '&='
    | '^='
    | '|='
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Boolean Literal                                                                                                 **/
/** --------------------------------------------------------------------------------------------------------------- **/

BooleanLiteral
    : 'true'
    | 'false'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Null Literal                                                                                                    **/
/** --------------------------------------------------------------------------------------------------------------- **/

NullLiteral
    : 'null'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Identifier Definition                                                                                           **/
/** --------------------------------------------------------------------------------------------------------------- **/

Identifier
    : JavaLetter JavaLetterOrDigit*
    ;

fragment JavaLetter
    : [a-zA-Z$_]
    | ~[\u0000-\u007F\uD800-\uDBFF]
      {Character.isJavaIdentifierStart(_input.LA(-1))}?
    | [\uD800-\uDBFF] [\uDC00-\uDFFF]
      {Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

fragment JavaLetterOrDigit
    : [a-zA-Z0-9$_]
    | ~[\u0000-\u007F\uD800-\uDBFF]
        {Character.isJavaIdentifierPart(_input.LA(-1))}?
    | [\uD800-\uDBFF] [\uDC00-\uDFFF]
        {Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** String Literal                                                                                                  **/
/** --------------------------------------------------------------------------------------------------------------- **/

StringLiteral
    : '"' StringCharacters? '"'
    ;

fragment StringCharacters
    : StringCharacter+
    ;

fragment StringCharacter
    : ~["\\]
    | EscapeSequence
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | OctalEscape
    | UnicodeEscape
    ;

fragment OctalEscape
    : '\\' OctalDigit
    | '\\' OctalDigit OctalDigit
    | '\\' [0-3] OctalDigit OctalDigit
    ;

fragment UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Integer Literal                                                                                                 **/
/** --------------------------------------------------------------------------------------------------------------- **/

FloatingPointLiteral
	:	DecimalFloatingPointLiteral
	|	HexadecimalFloatingPointLiteral
	;

fragment DecimalFloatingPointLiteral
	:	Digits '.' Digits? ExponentPart? FloatTypeSuffix?
	|	'.' Digits ExponentPart? FloatTypeSuffix?
	|	Digits ExponentPart FloatTypeSuffix?
	|	Digits FloatTypeSuffix
	;

fragment ExponentPart
	:	ExponentIndicator SignedInteger
	;

fragment ExponentIndicator
	:	[eE]
	;

fragment SignedInteger
	:	Sign? Digits
	;

fragment Sign
	:	[+-]
	;

fragment FloatTypeSuffix
	:	[fFdD]
	;

fragment HexadecimalFloatingPointLiteral
	:	HexSignificand BinaryExponent FloatTypeSuffix?
	;

fragment HexSignificand
	:	HexNumeral '.'?
	|	'0' [xX] HexDigits? '.' HexDigits
	;

fragment BinaryExponent
	:	BinaryExponentIndicator SignedInteger
	;

fragment BinaryExponentIndicator
	:	[pP]
	;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Character Literal                                                                                               **/
/** --------------------------------------------------------------------------------------------------------------- **/

CharacterLiteral
	:	'\'' SingleCharacter '\''
	|	'\'' EscapeSequence '\''
	;

fragment SingleCharacter
	:	~['\\]
	;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Integer Literal                                                                                                 **/
/** --------------------------------------------------------------------------------------------------------------- **/

IntegerLiteral
    : DecimalIntegerLiteral
    | HexIntegerLiteral
    | OctalIntegerLiteral
    | BinaryIntegerLiteral
    ;

fragment DecimalIntegerLiteral
    : DecimalNumeral [lL]?
    ;

fragment HexIntegerLiteral
    : HexNumeral [lL]?
    ;

fragment OctalIntegerLiteral
    : OctalNumeral [lL]?
    ;

fragment BinaryIntegerLiteral
    : BinaryNumeral [lL]?
    ;

fragment DecimalNumeral
    : '0'
    | [1-9] (Digits? | Underscores Digits)
    ;

fragment Digits
    : Digit (DigitsAndUnderscores? Digit)?
    ;

fragment Digit
    : '0'
    | [1-9]
    ;

fragment DigitsAndUnderscores
    : DigitOrUnderscore+
    ;

fragment DigitOrUnderscore
    : Digit
    | '_'
    ;

fragment Underscores
    : '_'+
    ;

fragment HexNumeral
    : '0' [xX] HexDigits
    ;

fragment HexDigits
    : HexDigit (HexDigitsAndUnderscores? HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment HexDigitsAndUnderscores
    : HexDigitOrUnderscore+
    ;

fragment HexDigitOrUnderscore
    : HexDigit
    | '_'
    ;

fragment OctalNumeral
    : '0' Underscores? OctalDigits
    ;

fragment OctalDigits
    : OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
    ;

fragment OctalDigit
    : [0-7]
    ;

fragment OctalDigitsAndUnderscores
    : OctalDigitOrUnderscore+
    ;

fragment OctalDigitOrUnderscore
    : OctalDigit
    | '_'
    ;

fragment BinaryNumeral
    : '0' [bB] BinaryDigits
    ;

fragment BinaryDigits
    : BinaryDigit (BinaryDigitsAndUnderscores? BinaryDigit)?
    ;

fragment BinaryDigit
    : [01]
    ;

fragment BinaryDigitsAndUnderscores
    : BinaryDigitOrUnderscore+
    ;

fragment BinaryDigitOrUnderscore
    : BinaryDigit
    | '_'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** Whitespace & Comments                                                                                           **/
/** --------------------------------------------------------------------------------------------------------------- **/

Whitespace
    :  [ \t\r\n\u000C]+ -> channel(HIDDEN)
    ;

Comment
    :   '/*' .*? '*/' -> channel(HIDDEN)
    ;

LineComment
    :   '//' ~[\r\n]* -> channel(HIDDEN)
    ;