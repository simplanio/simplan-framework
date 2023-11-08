grammar StatementSplitter;

statements:
    statement (';'+ statement)*
    ;

statement:
    ALL_STRINGS*
;
ALL_STRINGS
    : ~[']
    | ~["]
    | QUOTED_STRING
    ;

BLOCK_COMMENT
    : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '--' ~[\r\n]* -> channel(HIDDEN)
    ;

SPACES
     : [ \t\r\n] -> channel(HIDDEN)
     ;

QUOTED_STRING
    : '\'' ('\'\''|~'\'')* '\''
    | '"' ('""'|~'"')* '"'
    ;