package src.Tokens;

public enum TokenT {
    //Break on
    LBracket, RBracket, LBrace, RBrace,LSquare, RSquare,
    //other Break on s
    SemiColan, Comma, Dot, Colan ,
    //Types
    Var, Int, Float, Void, Double, Char, String, Short, Boolean, Long, Array, Object,
    //Operands
    Plus, Minus, Mul, Div, Mod,
    //Builtin functions
    If , Then , Else , For, Print , Switch , Case , Default , Break , While , Try , Catch ,
    //Class stuff
    Class, Value, New , Null ,
    // Logic stuff
    Equal, EqualTo, NotEqualTo, LThan, GThan, LThanEq, GThanEq, And, Or, Not ,
    // Function stuff
    Return, FunctionCall, ArrayCall, Private , Public , Static , Throws ,
    //Value tokens
    Expression, Unknown, BoolExpression , ObjectDeclaration , ArrayDeclaration , ArrayDeclaration2 , IfThenElse , StringVal , ArrayLength ,
    //File Stuff
    Import , Const , ConstFunc, Cast
}
