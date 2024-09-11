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

// Comments
COMMENT: [#].* -> skip;

// Properties
property: ID '=' value (',' ID '=' value)* (',')?;

// Different value types
value: STRING
     | INT
     | FLOAT
     | BOOLEAN
     | list
     | map
     | object;

// Multi-line strings block

// Lists
list: ID '=' '[' (value (',' value)*)? (',')? ']';

// Maps
map: ID '=' '{' (keyValuePair (',' keyValuePair)*)? (',')? '}';

// Multi-line string block
stringblock: ID '=' ('["\r\n]')* '|' WS? '"' (~STRING'["\r\n]')* '"' (WS? '\n' WS? '|' WS? '"' (~STRING'["\r\n]')) ;
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
WS: [ \t]+ -> skip;
