// ~ = not
//      e. g. ~INT -> everything but an integer

/*
Stringblocks:

block1: |
 Line breaks are significant
 So a new line break will be exactly at the point
 where you do a line break in your config file

block2: >
 Just a way of structuring your strings within the config file.
 The parser will output a single coherent string without line breaks
 so that you can fold it into space later in the application.


*/

grammar BastionYML;

// Entry point
file: (line | NEWLINE)+;

// Lines in the file can be comments, properties, or various data structures
line: COMMENT
    | property
    | object
    | map
    | list
    | stringblock
    ;

//

// Properties
property: ID '=' value (',' ID '=' value)* (',')?;

// Different value types
value: STRING       # StringValue
     | INT          # IntegerValue
     | FLOAT        # FloatValue
     | BOOLEAN      # BooleanValue
     | list         # ListValue
     | map          # MapValue
     | object       # ObjectValue;

// Multi-line strings block

// Lists : supports multi line lists by default
list: '[' (value (',' value)*)? (',')? ']';

// Maps
map: ID '=' '(' (keyValuePair (',' keyValuePair)*)? (',')? ')';

// Multi-line string block
stringblock: ID '=' ('["\r\n]')* '|' WS? '"' (STRING'["\r\n]')* '"' (WS? '\n' WS? '|' WS? '"' (STRING'["\r\n]')) ;
//stringblock2: ID '=' (~'["\r\n]')* '|' WS? '"' (~'["\r\n]')* '"' (WS? '\n' WS? '|' WS? '"' (~'["\r\n]')) ;

// Key-value pairs in maps
keyValuePair: ID '=' value;

// Objects
object: ID '=' '{' (property (',' property)*)? (',')? '}';

// Tokens
ID: [a-zA-Z_][a-zA-Z_0-9]*;
STRING: '"' (~["\\] | '\\'.)* '"';
INT: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]*;
BOOLEAN: 'true' | 'false';
NEWLINE: [\r\n]+ -> skip;

// Comments & White spaces
COMMENT: '/*' .*? '*/' -> skip;
SINGLE_LINE_COMMENT: '//' ~[\r\n]* -> skip;
WS: [ \t\r\n\u000C]+ -> skip;
