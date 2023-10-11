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
    : '#pragma' name StringLiteral? ';'
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
/** 1.4 Modifiers                                                                                                   **/
/** --------------------------------------------------------------------------------------------------------------- **/

modifier
    : 'mobile'
    | 'const'
    | 'native'
    | 'public'
    | 'private'
    | 'protected'
    ;

modifiers
    : modifier+
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
    : modifiers? type name '(' formalParameters? ')' annotations? ('implements' names)? (block | ';')
    ;

formalParameters
    : modifiers? type variableDeclarator (',' formalParameters)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 2.2 Protocol Types                                                                                              **/
/** --------------------------------------------------------------------------------------------------------------- **/

protocolTypeDeclaration
    : modifiers? 'protocol' Identifier extends? annotations? (protocolBody | ';')
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
    : modifiers? 'record' name extends? annotations? recordBody
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
/** 5.0 Constants and Variable Declarations                                                                         **/
/** --------------------------------------------------------------------------------------------------------------- **/

variableDeclaration
    : modifiers? type variableDeclarators
    ;

variableDeclarators
    : variableDeclarator (',' variableDeclarators)?
    ;

variableDeclarator
    : name (dimension)* ('=' (arrayInitializer | expression))?
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

// todo: we should be able to declare mobile channels and procedures here
statement
    : forStatement
    | ifThenStatement
    | ifThenElseStatement
    | labelledStatement
    | statementWithoutTrailingSubstatement
    | switchStatement
    | whileStatement
    ;

statementWithoutTrailingSubstatement
    : block
    | altBlockStatement
    | parBlockStatement
    | sequentialBlock
    | breakStatement
    | claimStatement
    | continueStatement
    | doStatement
    | returnStatement
    | skipStatement
    | stopStatement
    | suspendStatement
    | statementExpression ';'
    | variableDeclaration ';'
    | name '.' 'write' '(' expression ')' ';'
    | primaryExpression '.' 'write' '(' expression ')' ';'
    | ';'
    ;

parBlockStatement
    : 'par' ('enroll' barriers)? block
    ;

sequentialBlock
    : 'seq' block
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
    : 'par'? 'for' '(' forInit? ';' expression? ';' forUpdate? ')' ('enroll' '(' arguments ')')? statement
    ;

forStatementNoShortIf
    : 'par'? 'for' '(' forInit? ';' expression? ';' forUpdate? ')' ('enroll' '(' arguments ')')? statementNoShortIf
    ;

forInit
    : statementExpression
    | variableDeclaration
    ;

forUpdate
    : statementExpression (',' forUpdate)?
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.3 Claim Statement                                                                                             **/
/** --------------------------------------------------------------------------------------------------------------- **/

breakStatement
    : 'break' name? ';'
    ;

claimStatement
    : 'claim' '(' channels_ ')' statement ';'
    ;

claimStatementNoShortIf
    : 'claim' '(' channels_ ')' statementNoShortIf
    ;

continueStatement
    : 'continue' name? ';'
    ;

doStatement
    : 'do' statement 'while' '(' expression ')' ';'
    ;

returnStatement
    : 'return' expression ';'
    ;

skipStatement
    : 'skip' ';'
    ;

stopStatement
    : 'stop' ';'
    ;

suspendStatement
    : 'suspend' formalParameters? ';'
    ;

channels_
    : channel_ (',' channels_)?
    ;

channel_
    : primaryExpressionNoCreation ('.' 'read')?
    | primaryExpressionNoCreation ('.' 'write')?
    | channelType name '=' primaryExpressionNoCreation
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.4 Barrier Sync/Timeout Statement                                                                              **/
/** --------------------------------------------------------------------------------------------------------------- **/

/** --------------------------------------------------------------------------------------------------------------- **/
/** 7.6 Expression Statement                                                                                        **/
/** --------------------------------------------------------------------------------------------------------------- **/

statementExpression
    : assignmentExpression
    | preIncrementExpression
    | preDecrementExpression
    | postIncrementExpression
    | postDecrementExpression
    | primaryExpressionNoCreation
    ;

labelledStatement
    : name ':' statement
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

altBlockStatement
    : 'pri'? 'alt' ('(' forInit? ';' expression? ';' forUpdate? ')')? '{' altCases '}'
    ;

altCases
    : altCase*
    ;

altCase
    : ('(' expression ')' '&&')? guard ':' statement
    ;

// TODO: This might be flawed
guard
    : (primaryExpressionNoCreation '=')? name '(' (arguments | block)? ')'
    | 'skip'
    ;

/** --------------------------------------------------------------------------------------------------------------- **/
/** 8.0 Expressions                                                                                                 **/
/** --------------------------------------------------------------------------------------------------------------- **/

expression
    : conditionalExpression
    | assignmentExpression
    ;

assignmentExpression
    : primaryExpressionNoCreation assignmentOperator expression
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
    : multiplicativeExpression (('+'|'-') additiveExpression)?
    ;

multiplicativeExpression
    : unaryExpression (('*'|'/'|'%') multiplicativeExpression)?
    ;

unaryExpression
    : 'plus' unaryExpression
    | 'minus' unaryExpression
    | '~' unaryExpression
    | '!' unaryExpression
    | preIncrementExpression
    | preDecrementExpression
    | castExpression
    | postIncrementExpression
    | postDecrementExpression
    | primaryExpression
    | literal
    ;

preIncrementExpression
    : '++' unaryExpression
    ;

preDecrementExpression
    : '--' unaryExpression
    ;

castExpression
    : '(' typeWithoutDims ')' unaryExpression
    ;

postIncrementExpression
    : primaryExpression '++'
    ;

postDecrementExpression
    : primaryExpression '--'
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
    : name suffix?
    ;

suffix
    : arrayAccessSuffix suffix?
    | recordAccessSuffix suffix?
    | invocationSuffix suffix?
    ;

arrayAccessSuffix
    : '[' expression ']'
    ;

recordAccessSuffix
    : '.' name
    ;

invocationSuffix
    : '(' (arguments | block)? ')'
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