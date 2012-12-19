grammar Expr;
options { language = Apex; }

@header {
//import java.util.HashMap;
}

@members {
/** Map variable name to Integer object holding value */
Map<String,Integer> memory = new Map<String,Integer>();
}

prog:   stat+ ;
                
stat:   expr NEWLINE {System.out.println($expr.value);}
    |   ID '=' expr NEWLINE
        {memory.put($ID.text, new Integer($expr.value));}
    |   NEWLINE
    ;

expr returns [int value]
    :   e=multExpr {$value = $e.value;}
        (   '+' e=multExpr {$value += $e.value;}
        |   '-' e=multExpr {$value -= $e.value;}
        )*
    ;

multExpr returns [int value]
    :   e=atom {$value = $e.value;} ('*' e=atom {$value *= $e.value;})*
    ; 

atom returns [int value]
    :   UINT {$value = Integer.parseInt($UINT.text);}
    |   ID
        {
        Integer v = (Integer)memory.get($ID.text);
        if ( v!=null ) $value = v.intValue();
        else System.debug('undefined variable '+$ID.text);
        }
    |   '(' expr ')' {$value = $expr.value;}
    ;

ID  :   ('a'..'z'|'A'..'Z')+ ;
UINT :  '0'..'9'+ ;
NEWLINE:'\r'? '\n' ;
WS  :   (' '|'\t')+ {skip();} ;
