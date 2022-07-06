package src.Compiler;

import org.objectweb.asm.*;
import src.BuildingBlocks.Info.ExlClassInfo;
import src.BuildingBlocks.ToolKit;
import src.BuildingBlocks.Values.*;
import src.BuildingBlocks.parserTypes.*;
import src.BuildingBlocks.tools.Tuple;
import src.BuildingBlocks.tools.functionScope;
import src.Tokens.Token;
import src.Tokens.TokenT;

import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;

public class Compiler {

    private int onStack = 0 ;

    public ClassWriter compile(ExlClass exlclass) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V10, ACC_PUBLIC+ACC_SUPER, exlclass.getName() , null, "java/lang/Object", null);
        createFields(exlclass.getFields() , cw , exlclass.getObjects());
        ArrayList<ExlVariable> field = getField(exlclass.getFields());
        compileFunctions(exlclass.getFunctions(), cw , field , exlclass.getName() , exlclass.getObjects() );
        compileFields(exlclass.getFields() , cw , new functionScope(null , field, exlclass.getName() , exlclass.getObjects() ));
        cw.visitEnd();
        return cw;
    }

    private void createFields(ArrayList<Statement> fields , ClassWriter cw , ArrayList<ExlClassInfo> objects) {
        for(Statement s : fields) {
            if(s.getType() == StatementType.Array) {
                Tuple<Token , Integer> ar = (Tuple<Token, Integer>) s.getBody().get(0).getValue();
                StringBuilder Str = new StringBuilder();
                for(int i = 0 ; i < ar.y; i++){
                    Str.append("[");
                }
                Str.append(ToolKit.TokenToString(ar.x , objects));
                FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC | ACC_STATIC, (String) s.getBody().get(1).getValue(), Str.toString(), null, null);
                fieldVisitor.visitEnd();
            } else if(s.getType() == StatementType.Object){
                FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC | ACC_STATIC, (String) s.getBody().get(1).getValue(), "L" + ToolKit.getAddress((String) s.getBody().get(0).getValue() , objects) + ";", null, null);
                fieldVisitor.visitEnd();
            }else {
                Tuple<String, TokenT> stuff = getStuff(s.getType());
                String type = stuff.x;
                FieldVisitor fieldVisitor = cw.visitField(ACC_PUBLIC | ACC_STATIC, (String) s.getBody().get(0).getValue(), type, null, null);
                fieldVisitor.visitEnd();
            }
        }
    }

    private ArrayList<ExlVariable> getField(ArrayList<Statement> fields) {
        ArrayList<ExlVariable> vars =  new ArrayList<>();
        int pos = 0 ;
        for(Statement s : fields){
            switch (s.getType()) {
                case Boolean: case Char: case Long: case Double: case Float: case Int:
                case String: vars.add(new ExlVariable((String) s.getBody().get(0).getValue(), s.TokenType(), pos++, false, 0));break;
                case Array:
                    Tuple<Token, Integer> r = (Tuple<Token, Integer>) s.getBody().get(0).getValue();
                    if (r.x.getToken() == TokenT.Object) {vars.add(new ExlVariable((String) s.getBody().get(1).getValue(), r.x.getToken() , (String) r.x.getValue(), pos++, true, r.y));}
                    else {vars.add(new ExlVariable((String) s.getBody().get(1).getValue(), r.x.getToken(), pos++, true, r.y));}break;
                case Object:
                    vars.add(new ExlVariable((String) s.getBody().get(1).getValue(), TokenT.Object , (String) s.getBody().get(0).getValue(), pos++, false, 0));break;
            }
        }
        return vars ;
    }

    private Tuple<String , TokenT> getStuff(StatementType f){
        String type  ;
        TokenT type2 ;
        switch (f){
            case Int: type = "I";type2 = TokenT.Int ;break;
            case Boolean: type = "Z";type2 = TokenT.Boolean ;break;
            case Char: type = "C";type2 = TokenT.Char ;break;
            case Float: type = "F";type2 = TokenT.Float ;break;
            case String: type = "Ljava/lang/String;";type2 = TokenT.String;break;
            case Double: type = "D"; type2 = TokenT.Double ; break;
            case Long: type = "J"; type2 = TokenT.Long ; break;
            default: type = "O";type2 = TokenT.Unknown ;break;
        }
        return new Tuple<>(type , type2);
    }

    private void compileFields(ArrayList<Statement> fields, ClassWriter cw , functionScope fs) {
        String name ;
        String type ;
        TokenT type2 ;
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null) ;
        mv.visitCode();
        for(Statement f : fields){//TODO other than ad
            if(f.getType() == StatementType.Array) {
                Tuple<Token , Integer> x = (Tuple<Token, Integer>) f.getBody().get(0).getValue();
                switch(f.getBody().get(2).getToken()){
                    case ArrayDeclaration:
                        ArrayDeclaration ad = (ArrayDeclaration)  f.getBody().get(2).getValue();
                        doArrayDec(ad , mv , fs);
                        break;
                    case ArrayDeclaration2:
                        ArrayDeclaration2 ad2 = (ArrayDeclaration2) f.getBody().get(2).getValue();
                        doArrayDec2(ad2 , mv , fs,x);
                        break;
                }
                StringBuilder Str = new StringBuilder();
                for(int i = 0 ; i < x.y; i++){
                    Str.append("[");
                }
                Str.append(ToolKit.TokenToString(x.x , fs.getObjects()));
                mv.visitFieldInsn(PUTSTATIC, fs.getOwner(), (String) f.getBody().get(1).getValue(), Str.toString());
            } else if(f.getType() == StatementType.Object){
                name = (String) f.getBody().get(1).getValue();
                push2(f.getBody().get(2), mv,fs,TokenT.Object);
                mv.visitFieldInsn(PUTSTATIC, fs.getOwner(), name, "L" + ToolKit.getAddress((String) f.getBody().get(0).getValue() , fs.getObjects()) + ";");
            }else {
                name = (String) f.getBody().get(0).getValue();
                Tuple<String, TokenT> stuff = getStuff(f.getType());
                type = stuff.x;
                type2 = stuff.y;
                compileExpression(new Expression(f.getBody().subList(1, f.getBody().size())), mv, fs, type2);
                mv.visitFieldInsn(PUTSTATIC, fs.getOwner(), name, type);
            }
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(100 , fs.getMaxVars() + 10);
        mv.visitEnd();
    }

    public void compileFunctions(ArrayList<Function> functions, ClassWriter cw , ArrayList<ExlVariable> fields, String o, ArrayList<ExlClassInfo> obs) {
        for(Function f : functions){
            if(f.getName().equals(o)){makeConstructor(f , new functionScope(f.getVars(), fields, o, obs ) , cw );}
            else {makeFunction(cw , f , new functionScope((ArrayList<ExlVariable>) f.getVars().clone(), fields, o, obs  ) );}
        }
    }

    private void makeConstructor(Function f, functionScope fs , ClassWriter cw) {
        MethodVisitor m = cw.visitMethod(0, "<init>", f.getDesc(), null, null);
        m.visitVarInsn(ALOAD, 0);
        m.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        compileStatements(f.getStatements(),m,fs,TokenT.Void);
        m.visitInsn(RETURN);
        m.visitMaxs(100 , fs.getMaxVars() + 10);
        m.visitEnd();
    }

    private void makeFunction(ClassWriter c, Function f ,  functionScope fs )  {
        int num = getAccess(f) ;
        MethodVisitor mw ;
        if(f.getThrows() != null) {mw = c.visitMethod(num, f.getName(), f.getDesc(), null, new String[]{f.getThrows()});}
        else {mw = c.visitMethod(num, f.getName(), f.getDesc(), null, null);}
        compileStatements(f.getStatements() , mw ,fs , f.getType().getToken());
        if(f.getType().getToken() == TokenT.Void){mw.visitInsn(RETURN);}
        mw.visitMaxs(100 , fs.getMaxVars() + 10);
        mw.visitEnd();
    }

    private int getAccess(Function f) {
        int num = 0 ;
        if(f.isPrivate()){num += ACC_PRIVATE ;}
        else if(f.isPublic()){num += ACC_PUBLIC ;}
        if(f.isStatic()){num += ACC_STATIC ;}
        return num ;
    }

    private functionScope compileVar(TokenT t , Statement s , functionScope fs , MethodVisitor m ) {
        String name = (String) s.getBody().get(0).getValue();
        if( ToolKit.validVarName(name)) {
            List<Token> expression = s.getBody().subList(1, s.getBody().size());
            return makeVariable(m, name, expression, fs, t);
        } else {
            System.out.println("Variable name cannot start with a digit or ' or \" or - " + name);
            System.exit(2);
        }
        return null ;
    }

    private void compileStatements(ArrayList<Statement> statements, MethodVisitor m, functionScope fs , TokenT type) {
        for(Statement s : statements){fs = compileStatement(s , fs , m , type);}
    }

    private functionScope compileStatement(Statement s , functionScope fs , MethodVisitor m , TokenT type ){
        switch (s.getType()){
            case Int: case Float: case Double: case Short: case Char: case String:
            case Long: fs  = compileVar(s.TokenType() , s , fs , m) ;break;
            case Boolean: fs = compileBooleanVar(s.getBody(), fs, m);break;
            case While:
                Label startW = new Label();
                Label endW = new Label();
                ArrayList<ExlVariable> vars432 = (ArrayList<ExlVariable>) fs.getVars().clone();
                m.visitLabel(startW);
                visitFrame(fs , m) ;
                compileBooleanExp(s.getBody() , fs , m , startW , endW ) ;
                compileStatements(s.getInnerStatements(), m, fs , type);
                m.visitJumpInsn(GOTO, startW);
                m.visitLabel(endW);
                fs.setVars(vars432);
                visitFrame(fs , m) ;
                break;
            case For:
                ArrayList<ExlVariable> var4343 = (ArrayList<ExlVariable>) fs.getVars().clone();
                Label start = new Label();
                Label end = new Label();
                int loopPos = fs.getPos() + 1;
                Expression startV = (Expression) s.getBody().get(2).getValue();
                Expression Check = (Expression) s.getBody().get(3).getValue() ;
                Expression Adding = (Expression) s.getBody().get(4).getValue();
                fs = makeVariable(m , (String) s.getBody().get(1).getValue(), startV.tokens , fs, s.getBody().get(0).getToken());
                m.visitLabel(start) ;
                visitFrame(fs,m);
                compileBoolean(new Token(TokenT.BoolExpression , Check.tokens) , fs , m , start , end , true);
                compileStatements(s.getInnerStatements() , m, fs , type);
                if(s.getBody().get(0).getToken() == TokenT.Int && Adding.size() == 1){
                    try {
                        int l = Integer.parseInt((String) Adding.get(0).getValue());
                        m.visitIincInsn(loopPos, l);
                    } catch (Exception e){
                        loadVar(m,(String) s.getBody().get(1).getValue(),fs);
                        switch (s.getBody().get(0).getToken()){
                            case Int:
                                push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                                m.visitInsn(IADD);
                                m.visitIntInsn(ISTORE , loopPos);
                                break;
                            case Float:
                                push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                                m.visitInsn(FADD);
                                m.visitIntInsn(FSTORE , loopPos);
                                break;
                            case Double:
                                push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                                m.visitInsn(DADD);
                                m.visitIntInsn(DSTORE , loopPos);
                                break;
                            case Long:
                                push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                                m.visitInsn(LADD);
                                m.visitIntInsn(LSTORE , loopPos);
                                break;
                        }
                    }
                } else {
                    loadVar(m,(String) s.getBody().get(1).getValue(),fs);
                    switch (s.getBody().get(0).getToken()){
                        case Int:
                            push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                            m.visitInsn(IADD);
                            m.visitIntInsn(ISTORE , loopPos);
                            break;
                        case Float:
                            push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                            m.visitInsn(FADD);
                            m.visitIntInsn(FSTORE , loopPos);
                            break;
                        case Double:
                            push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                            m.visitInsn(DADD);
                            m.visitIntInsn(DSTORE , loopPos);
                            break;
                        case Long:
                            push2(s.getBody().get(4) , m, fs , s.getBody().get(0).getToken());
                            m.visitInsn(LADD);
                            m.visitIntInsn(LSTORE , loopPos);
                            break;
                    }
                }
                m.visitJumpInsn(GOTO, start);
                m.visitLabel(end);
                fs.setVars(var4343);
                visitFrame(fs , m);
                break;
            case Print:
                boolean b = true;
                Token Expression = s.getBody().get(1) ;
                Expression exp = (Expression) Expression.getValue();
                if(exp.get(0).getToken() == TokenT.Value && exp.size() == 1){
                    for(ExlVariable var : fs.getVars()){
                        if(var.getName().equals(exp.get(0).getValue()) && var.isFlat()){
                            loadVar(m, (String) exp.get(0).getValue(),fs);
                            b = false;
                            switch (var.getType()){
                                case Double: m.visitMethodInsn(INVOKEVIRTUAL, "flatDouble","print","()V",false);break;
                                case Int:  m.visitMethodInsn(INVOKEVIRTUAL, "flatInteger","print","()V",false);
                            }
                        }
                    }
                }
                if(b) {
                    m.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"); //put System.out to operand stack
                    String descriptor = (String) s.getBody().get(0).getValue();
                    TokenT type2 = ToolKit.StringToToken(ToolKit.getP(descriptor), exp.tokens.get(0).getLine()).getToken();
                    push2(Expression, m, fs, type2);
                    m.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", descriptor, false);
                }
                break;
            case TryCatch:
                ArrayList<Statement> sr = s.getInnerStatements() ;
                Label tryL = new Label();
                Label catchL = new Label();
                Label afterGOTO = new Label();
                ArrayList<ExlVariable> varss = (ArrayList<ExlVariable>) fs.getVars().clone();
                m.visitTryCatchBlock(tryL , catchL , afterGOTO , "java/lang/Exception");
                m.visitLabel(tryL);
                compileStatements(sr.get(0).getInnerStatements(), m , fs , type);
                Label afterL = new Label();
                m.visitJumpInsn(GOTO, afterL);
                m.visitLabel(afterGOTO);
                fs.setVars(varss);
                Object[] locals = getFrameTypes(fs.getVars() ,fs);
                onStack = fs.getVars().size() ;
                m.visitFrame(F_FULL, locals.length, locals, 1, new Object[] {"java/lang/Exception"});
                fs.addVar(new ExlVariable("e" , TokenT.Object , "Exception" , fs.getPos() + 1));
                m.visitVarInsn(ASTORE, fs.getPos());
                m.visitLabel(catchL);
                compileStatements( sr.get(1).getInnerStatements(), m , fs , type);
                m.visitLabel(afterL);
                fs.setVars(varss);
                visitFrame(fs,m);
                break;
            case IF:
                Label pastIf = new Label();
                Label startIf = new Label();
                ArrayList<ExlVariable> vars43 = (ArrayList<ExlVariable>) fs.getVars().clone();
                compileBooleanExp(s.getBody() , fs , m , startIf , pastIf ) ;
                m.visitLabel(startIf);
                visitFrame(fs , m) ;
                compileStatements(s.getInnerStatements(), m, fs , type);
                m.visitLabel(pastIf);
                fs.setVars(vars43);
                visitFrame(fs , m) ;
                break;
            case IF_Block:
                compileIfs(s.getInnerStatements() , fs , m  , type);
                break ;
            case Overwrite:
                if(s.getBody().get(1).getToken() == TokenT.Equal) {overwriteVar(m, (String) s.getBody().get(0).getValue(), new Token(TokenT.Expression, s.getBody().get(2).getValue()), fs);}
                else {overwriteArrayVar(m , (String) s.getBody().get(0).getValue() , (Expression) s.getBody().get(1).getValue() , new Token(TokenT.Expression , s.getBody().get(3).getValue()) , fs);}
                break;
            case FunctionCall:
                if(s.getBody().get(0).getToken() == TokenT.FunctionCall) {
                    FunctionCall fc = (FunctionCall) s.getBody().get(0).getValue();
                    doFuncCAll(fc , m , fs );
                    switch (fc.getDesc().charAt(fc.getDesc().length() - 1 )){
                        case 'J':
                        case 'D': m.visitInsn(POP2);break;
                        case 'V': break ;
                        default: m.visitInsn(POP);break;
                    }
                } else if(s.getBody().get(0).getToken() == TokenT.Object){
                    ArrayList<ObjectRef> ste = (ArrayList<ObjectRef>) s.getBody().get(0).getValue();
                    doObject( ste , m , fs);
                    switch (ste.get(ste.size() -1).getType()){
                        case "D" :
                        case "J": m.visitInsn(POP2);break;
                        case "V": break;
                        default: m.visitInsn(POP);break;
                    }
                }
                break;
            case Return:
                Expression exp2 = new Expression(ToolKit.tokensUpTo(s.getBody(), 0 , TokenT.SemiColan).x);
                compileExpression(exp2 , m , fs , type);
                doReturn(type, m );
                break;
            case Array:
                Tuple<Token , Integer> ti = (Tuple<Token, Integer>) s.getBody().get(0).getValue();
                switch (s.getBody().get(2).getToken()){
                    case ArrayDeclaration:
                        ArrayDeclaration ad = (ArrayDeclaration) s.getBody().get(2).getValue() ;
                        switch (ti.x.getToken()){
                            case Value: fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , TokenT.Object , (String) ti.x.getValue(), fs.getPos() + 1 , true , ti.y));break;
                            case Int:case Float:case Char: case Double: case Boolean: case String:
                            case Long: fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , ti.x.getToken() , fs.getPos() + 1 , true , ti.y));break;
                        }
                        doArrayDec(ad , m , fs);
                        break;
                    case ArrayDeclaration2:
                        ArrayDeclaration2 ad2 = (ArrayDeclaration2) s.getBody().get(2).getValue() ;
                        switch (ti.x.getToken()){
                            case Value: fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , TokenT.Object , (String) ti.x.getValue(), fs.getPos() + 1 , true , ti.y));break;
                            case Int:case Float:case Char: case Double: case Boolean: case String:
                            case Long: fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , ti.x.getToken() , fs.getPos() + 1 , true , ti.y));break;
                        }
                        doArrayDec2(ad2 , m , fs , ti);
                        break;
                    case ArrayCall:
                        ArrayCall ac = (ArrayCall) s.getBody().get(2).getValue() ;
                        doArrayCall(ac , m , fs);
                        Tuple<Token , Integer> i  = ToolKit.getArrayInfo(ac , fs) ;
                        if(i.y == 0){fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , i.x.getToken(), (String) ti.x.getValue(), fs.getPos() + 1 ));}
                        else {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , ti.x.getToken(), (String) ti.x.getValue(), fs.getPos() + 1 , true , i.y));}
                        break;
                    case Value:
                        push2(s.getBody().get(2) , m , fs , TokenT.Object);
                        if(ti.x.getToken() == TokenT.Value) {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(), TokenT.Object, (String) ti.x.getValue(), fs.getPos() + 1, true, ti.y));}
                        else {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(), ti.x.getToken(), "", fs.getPos() + 1, true, ti.y));}
                        break;
                    case FunctionCall:
                        FunctionCall fc = (FunctionCall) s.getBody().get(2).getValue() ;
                        doFuncCAll(fc , m , fs);
                        String ir = ToolKit.getType(new Token(TokenT.FunctionCall , fc) , fs.getObjects() , fs.getVars());
                        Token t = ToolKit.StringToToken(ir, s.getBody().get(0).getLine()) ;
                        if(t.getToken() == TokenT.Object){fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),t.getToken(), (String) t.getValue(),  fs.getPos() + 1, true, ti.y));}
                        else {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),t.getToken(), fs.getPos() + 1, true, ti.y));}
                        break;
                    case Object:
                        ArrayList<ObjectRef> ste = (ArrayList<ObjectRef>) s.getBody().get(2).getValue() ;
                        doObject(ste ,m,fs);
                        ObjectRef oR = ste.get(ste.size() - 1);
                        Token tds = ToolKit.StringToToken(oR.getType() ,s.getBody().get(0).getLine());
                        if(tds.getToken() == TokenT.Object){fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),tds.getToken(), (String) tds.getValue(),  fs.getPos() + 1, true, ti.y));}
                        else if(tds.getToken() == TokenT.Array){
                            Tuple<Token , Integer> TTI = (Tuple<Token, Integer>) tds.getValue();
                            fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , TTI.x.getToken(), (String) TTI.x.getValue(), fs.getPos() + 1 , true , TTI.y ));
                        }
                        else {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),tds.getToken(), fs.getPos() + 1, true, ti.y));}
                        break;
                    case IfThenElse:
                        IfThenElse ite = (IfThenElse) s.getBody().get(2).getValue() ;
                        doIfThenElse(ite , m ,fs ,TokenT.Object) ;
                        Token itet = ite.getType() ;
                        if(itet.getToken() == TokenT.Object){fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),itet.getToken(), (String) itet.getValue(),  fs.getPos() + 1, true, ti.y));}
                        else if(itet.getToken() == TokenT.Array){
                            Tuple<Token , Integer> TTI = (Tuple<Token, Integer>) itet.getValue();
                            fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , TTI.x.getToken(), (String) TTI.x.getValue(), fs.getPos() + 1 , true , TTI.y ));
                        }
                        else {fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue(),itet.getToken(), fs.getPos() + 1, true, ti.y));}
                        break;

                }
                m.visitVarInsn(ASTORE, fs.getPos());
                break;
            case OtherArray:
                Tuple<Token , Integer> ts = (Tuple<Token, Integer>) s.getBody().get(0).getValue();
                switch (ts.x.getToken()){
                    case Int:m.visitTypeInsn(NEW, "flatInteger");break;
                    case Double:m.visitTypeInsn(NEW, "flatDouble");break;
                }
                m.visitInsn(DUP);
                ArrayDeclaration ad = (ArrayDeclaration) s.getBody().get(2).getValue() ;
                push(m , "" + ad.getValues().size() ,fs , TokenT.Int );
                m.visitIntInsn(NEWARRAY, T_INT);
                for(int i = 0 ; i < ad.getValues().size() ; i++){
                    m.visitInsn(DUP);
                    push(m ,"" + i , fs , TokenT.Int);
                    push2(new Token(TokenT.Expression ,  new Expression((ArrayList<Token>) ad.getValues().get(i).getValue())), m , fs , TokenT.Int);
                    m.visitInsn(IASTORE);
                }
                switch (ts.x.getToken()){
                    case Int:m.visitMethodInsn(INVOKESPECIAL, "flatInteger", "<init>", "([I)V", false);break;
                    case Double:m.visitMethodInsn(INVOKESPECIAL, "flatDouble", "<init>", "([I)V", false);
                }
                fs.addVar(new ExlVariable((String) s.getBody().get(1).getValue() , ts.x.getToken(), fs.getPos() + 1 , true , true , 1));
                m.visitVarInsn(ASTORE, fs.getPos());
                break;
            case Object:
                String className = (String) s.getBody().get(0).getValue();
                String varName = (String) s.getBody().get(1).getValue() ;
                switch (s.getBody().get(2).getToken()){
                    case ObjectDeclaration:
                        ObjectDeclaration od = (ObjectDeclaration)  s.getBody().get(2).getValue() ;
                        m.visitTypeInsn(NEW, od.getAddress());
                        m.visitInsn(DUP);
                        compileParameters(od.getValues() , m, fs , od.getDesc());
                        m.visitMethodInsn(INVOKESPECIAL, od.getAddress(), "<init>", od.getDesc(), false);
                        break;
                    case Object:
                        ArrayList<ObjectRef> or = (ArrayList<ObjectRef>) s.getBody().get(2).getValue() ;
                        doObject(or , m , fs);
                        break;
                    case FunctionCall:
                        FunctionCall fc = (FunctionCall) s.getBody().get(2).getValue() ;
                        doFuncCAll(fc , m , fs );
                        break;
                    case IfThenElse:
                        IfThenElse ite = (IfThenElse) s.getBody().get(2).getValue() ;
                        doIfThenElse(ite , m , fs ,TokenT.Object);
                }
                fs.addVar(new ExlVariable(varName, TokenT.Object, className, fs.getPos() + 1));
                m.visitVarInsn(ASTORE, fs.getPos());
                break;
            case Switch:
                Token val = s.getBody().get(0);
                switch (ToolKit.getType(val ,fs.getObjects() , fs.getVars() ).charAt(0)){
                    case 'I' : push2(s.getBody().get(0), m, fs, TokenT.Int);break;
                    case 'Z' : push2(s.getBody().get(0), m, fs, TokenT.Char);break;
                    default: System.out.println("Can only be in or char");System.exit(2);
                }
                Tuple<Tuple<Label[] , Label> , int[] > r = numCases(s.getInnerStatements()) ;
                Label after = new Label() ;
                if(r.x.y == null){m.visitLookupSwitchInsn(after , r.y, r.x.x);}
                else {m.visitLookupSwitchInsn(r.x.y, r.y, r.x.x);}
                compileSwitch(s.getInnerStatements() , fs , m , r.x.x , after  , r.x.y, type);
                m.visitLabel(after);
                visitFrame(fs,m);
                break;
            default:
                System.out.println("YEs " + s.getType());
                System.exit(2);
        }
        return fs ;
    }

    private void overwriteArrayVar(MethodVisitor m, String varName , Expression ar , Token value , functionScope fs) {
        ArrayList<Token> at = ar.tokens ;
        ArrayList<ExlVariable> s = (ArrayList<ExlVariable>) fs.getVars().clone();
        s.addAll((ArrayList<ExlVariable>) fs.getFields().clone());
        for(ExlVariable var : s){
            if (var.getName().equals(varName) ) {
                if(var.isArray()){
                    if(var.isFlat()) {
                        loadVar(m, varName, fs);
                        push(m , "" + at.size(),fs , TokenT.Int );
                        m.visitIntInsn(NEWARRAY, T_INT);
                        for(int i = 0 ; i < at.size() ; i++){
                            m.visitInsn(DUP);
                            push(m ,"" + i , fs , TokenT.Int);
                            push2(new Token(TokenT.Expression ,  at.get(i).getValue()), m , fs , TokenT.Int);
                            m.visitInsn(IASTORE);
                        }
                        push2(value, m, fs, var.getType());
                        switch (var.getType()){
                            case Double:m.visitMethodInsn(INVOKEVIRTUAL, "flatDouble", "set", "([ID)V", false);break;
                            case Int:m.visitMethodInsn(INVOKEVIRTUAL, "flatInteger", "set", "([II)V", false);
                        }

                    } else {
                        loadVar(m, varName, fs);
                        while (at.size() > 1) {
                            push2(at.get(0), m, fs, TokenT.Int);
                            m.visitInsn(AALOAD);
                            at.remove(0);
                        }
                        push2(at.get(0), m, fs, TokenT.Int);
                        push2(value, m, fs, var.getType());
                        switch (var.getType()) {
                            case Int:
                            case Char:
                            case Boolean:
                                m.visitInsn(IASTORE);
                                break;
                            case Float:
                                m.visitInsn(FASTORE);
                                break;
                            case Long:
                                m.visitInsn(LASTORE);
                                break;
                            case Double:
                                m.visitInsn(DASTORE);
                                break;
                            case String:
                            case Object:
                                m.visitInsn(AASTORE);
                                break;
                        }
                    }
                } else {
                    System.out.println("Var " + varName + "is not an array");
                    System.exit(2);
                }
            }
        }
    }

    private functionScope compileBooleanVar(ArrayList<Token> body, functionScope fs, MethodVisitor m) {
        String name = (String) body.get(0).getValue();
        body = new ArrayList<>(body.subList(1 , body.size()));
        compileBooleanValue(body , fs , m);
        fs.addVar(new ExlVariable(name, TokenT.Boolean, "", fs.getPos() + 1));
        m.visitVarInsn(ISTORE, fs.getPos());
        return fs ;
    }

    private void overwriteVar(MethodVisitor m , String varName ,Token replacement , functionScope fs) {
        for (ExlVariable var : fs.getVars()) {
            if (var.getName().equals(varName)) {
                int opcode = 0;
                push2(replacement, m, fs, var.getType());
                if(var.isArray()){
                    opcode = ASTORE ;
                }else {
                    switch (var.getType()) {
                        case Char: case Boolean: case Int: opcode = ISTORE;break;
                        case Float: opcode = FSTORE;break;
                        case Object: case String: opcode = ASTORE;break;
                        case Long: opcode = LSTORE;break;
                        case Double: opcode = DSTORE;break;
                    }
                }
                m.visitVarInsn(opcode, var.getPos());
                return;
            }
        }
        for (ExlVariable var : fs.getFields()) {
            String type ;
            if (var.getName().equals(varName)) {
                if(var.getType() == TokenT.Object){type = "L" + var.getObjName() + ";" ;}
                else {type = var.getString(fs.getObjects());}
                push2(replacement, m, fs, var.getType());
                m.visitFieldInsn(PUTSTATIC, fs.getOwner(), var.getName(), type);
            }
        }
    }

    private void compileBooleanValue(ArrayList<Token> body, functionScope fs, MethodVisitor m) {
        Label zero = new Label();
        Label one = new Label();
        Label after = new Label();
        compileBooleanExp(body , fs , m , one , zero ) ;
        Object[] locals = getFrameTypes(fs.getVars() ,fs);
        m.visitLabel(one);
        visitFrame(fs , m);
        m.visitInsn(ICONST_1);
        m.visitJumpInsn(GOTO , after);
        m.visitLabel(zero);
        visitFrame(fs , m);
        m.visitInsn(ICONST_0);
        m.visitLabel(after);
        onStack = fs.getVars().size() ;
        m.visitFrame(F_FULL , fs.getVars().size() , locals, 1, new Object[] {INTEGER} );
    }

    private void compileBooleanArrayValue(ArrayList<Token> body, functionScope fs, MethodVisitor m) {
        Label zero = new Label();
        Label one = new Label();
        Label after = new Label();
        compileBooleanExp(body , fs , m , one , zero ) ;
        Object[] locals = getFrameTypes(fs.getVars() ,fs);
        m.visitLabel(one);
        onStack = fs.getVars().size() ;
        m.visitFrame(F_FULL , fs.getVars().size() , locals, 3, new Object[] {"[Z", "[Z", Opcodes.INTEGER});
        m.visitInsn(ICONST_1);
        m.visitJumpInsn(GOTO , after);
        m.visitLabel(zero);
        onStack = fs.getVars().size() ;
        m.visitFrame(F_FULL , fs.getVars().size() , locals, 3, new Object[] {"[Z", "[Z", Opcodes.INTEGER});
        m.visitInsn(ICONST_0);
        onStack = fs.getVars().size() ;
        m.visitFrame(F_FULL , fs.getVars().size() , locals, 4, new Object[] {"[Z", "[Z", Opcodes.INTEGER, Opcodes.INTEGER  });
        m.visitLabel(after);
    }

    private void doObject(ArrayList<ObjectRef> ste, MethodVisitor m , functionScope fs) {
        for(ObjectRef a : ste ){
            if( a.isLocal()) {
                switch (a.getName()){
                    case "Val":
                        String s = (String) a.getParam().get(0).getValue();
                        if(s.startsWith("\"")){push(m , s , fs, TokenT.String);}
                        else {push(m , s , fs , TokenT.Object);}
                        break;
                    case "FC":
                        FunctionCall fc = (FunctionCall) a.getParam().get(0).getValue();
                        doFuncCAll(fc , m , fs);
                        break;
                    case "OD":
                        ObjectDeclaration od = (ObjectDeclaration) a.getParam().get(0).getValue();
                        doObjectDec(od,m,fs);
                }
            } else if(! a.isFunc()){
                if(a.isStatic()) {m.visitFieldInsn(GETSTATIC, a.getOrigin(), a.getName(), a.getType());}
                else {m.visitFieldInsn(GETFIELD, a.getOrigin(), a.getName(), a.getType());}
            } else {
                compileParameters(a.getParam() , m , fs , a.getDescription());
                if(a.isStatic()) {m.visitMethodInsn(INVOKESTATIC, a.getOrigin(), a.getName(), a.getDescription(), false);}
                else {m.visitMethodInsn(INVOKEVIRTUAL, a.getOrigin(), a.getName(), a.getDescription(), false);}
            }
        }
    }

    private void compileSwitch(ArrayList<Statement> innerStatements, functionScope fs, MethodVisitor m,Label[] labels,  Label after,  Label dft,TokenT type) {
        int i = 0 ;
        ArrayList<ExlVariable> vars43 = (ArrayList<ExlVariable>) fs.getVars().clone();
        for(Statement s : innerStatements){
            ArrayList<ExlVariable> vars2 = (ArrayList<ExlVariable>) vars43.clone() ;
            fs.setVars(vars2);
            switch (s.getType()){
                case Break: m.visitJumpInsn(GOTO ,after);break;
                case Case:
                    if(s.getBody() == null) {m.visitLabel(dft);}
                    else {m.visitLabel(labels[i++]);}
                    try {visitFrame(fs,m);}
                    catch (Exception e){}break;
                default: compileStatement(s ,  fs , m, type);break;
            }
        }
        fs.setVars(vars43);
    }

    private Tuple<Tuple<Label[] , Label > , int[]> numCases(ArrayList<Statement> innerStatements) {
        ArrayList<Integer> k = new ArrayList<>();
        int i = 0;
        Label dft = null;
        for(Statement s : innerStatements){
            if(s.getType() == StatementType.Case){
                if(s.getBody() == null){
                    if(dft == null) {dft = new Label();}
                    else {
                        System.out.println("TWO DEFAULTS ONLY ONE ALLOWED");
                        System.exit(2);}}
                else {
                    String t = (String) s.getBody().get(0).getValue();
                    try {k.add(Integer.parseInt(t));}
                    catch (Exception e){
                        int c = t.charAt(1);
                        k.add(c );
                    }
                    i++;
                }
            }
        }
        Label[] labels = new Label[i] ;
        int[] tr = new int[i];
        for(int j = 0 ; j < i ; j++ ) {
            labels[j] = new Label();
            tr[j] = k.get(j);
        }
        return new Tuple<>(new Tuple<>(labels , dft ) , tr);
    }

    private void doArrayDec2(ArrayDeclaration2 ad2,  MethodVisitor m , functionScope fs , Tuple<Token , Integer> x) {
        if(x.y == 1){
            push(m , "" + ad2.getValues().size() , fs , TokenT.Int);
            int code = 0 ;
            TokenT type = x.x.getToken() ;
            switch(type) {
                case Boolean: m.visitIntInsn(NEWARRAY, T_BOOLEAN);code = BASTORE;break;
                case Int: m.visitIntInsn(NEWARRAY, T_INT);code = IASTORE ;break;
                case Char: m.visitIntInsn(NEWARRAY , T_CHAR);code = CASTORE ;break;
                case String: m.visitTypeInsn(ANEWARRAY, "java/lang/String");code = AASTORE ;break;
                case Float: m.visitIntInsn(NEWARRAY , T_FLOAT);code = FASTORE ;break;
                case Long: m.visitIntInsn(NEWARRAY , T_LONG);code = LASTORE;break;
                case Double:m.visitIntInsn(NEWARRAY , T_DOUBLE);code = DASTORE;break;
                case Value: type = TokenT.Object; case Object:String temp = ToolKit.TokenToString(x.x,fs.getObjects());m.visitTypeInsn(ANEWARRAY , temp.substring(1 ,temp.length() - 1)); code = AASTORE ; break;
                default:
                    System.out.println("Unknown Type in make array " + x.x);
                    System.exit(2);
                break;
            }
            for(int i = 0 ; i < ad2.getValues().size() ; i++){
                m.visitInsn(DUP);
                push(m ,"" + i , fs , TokenT.Int);
                if(x.x.getToken() == TokenT.Boolean){
                    ArrayList<Token> t = new ArrayList<>();
                    t.add( ad2.getValues().get(i));
                    compileBooleanArrayValue(t, fs , m);
                } else {push2(ad2.getValues().get(i), m , fs , type);}
                m.visitInsn(code);
            }
        } else {
            push(m, "" + ad2.getValues().size(), fs , TokenT.Int);
            String type = "[".repeat(Math.max(0, x.y - 1)) + ToolKit.TokenToString(x.x ,fs.getObjects());
            m.visitTypeInsn(ANEWARRAY , type);
            ArrayDeclaration2 ad21 ;
            for(int i = 0 ; i < ad2.getValues().size() ; i++){
                m.visitInsn(DUP);
                if(ad2.getValues().get(i).getToken() == TokenT.Expression){
                    Expression ex = (Expression) ad2.getValues().get(i).getValue();
                    if(ex.get(0).getToken() == TokenT.ArrayDeclaration2){
                        ad21 = (ArrayDeclaration2) ex.get(0).getValue() ;
                        push(m , "" + i , fs , TokenT.Int);
                        doArrayDec2(ad21 , m , fs , new Tuple<>(x.x , x.y - 1));
                    } else {
                        push(m , "" + i , fs , TokenT.Int);
                        push2(ad2.getValues().get(i), m , fs , TokenT.Array);
                    }
                } else if(ad2.getValues().get(i).getToken() == TokenT.ArrayDeclaration2){
                    ad21 = (ArrayDeclaration2) ad2.getValues().get(i).getValue() ;
                    push(m , "" + i , fs , TokenT.Int);
                    doArrayDec2(ad21 , m , fs , new Tuple<>(x.x , x.y - 1));
                } else {
                    push(m , "" + i , fs , TokenT.Int);
                    push2(ad2.getValues().get(i), m , fs , TokenT.Array);
                }
                m.visitInsn(AASTORE);
            }
        }
    }

    private void compileBooleanExp(ArrayList<Token> body, functionScope fs, MethodVisitor m, Label startIf, Label endIf){
        if(body.size() > 1) {
            for (int i = 0; i < body.size() - 1 ; i++) {
                if(body.get(i).getToken() != TokenT.Or) {
                    compileBoolean(body.get(i), fs, m, startIf, endIf, false);
                    i++;
                }
            }
            compileBoolean(body.get(body.size() - 1), fs, m, startIf , endIf , true);
        } else {compileBoolean(body.get(0), fs, m, startIf , endIf , true);}
    }

    private void compileBoolean(Token t, functionScope fs , MethodVisitor m , Label start , Label end , Boolean isLast){
        ArrayList<Token> expr;
        switch (t.getToken()){
            case BoolExpression:
                try {
                    expr = (ArrayList<Token>) t.getValue();
                }catch( Exception e){
                    Expression erw = (Expression) t.getValue();
                    expr = erw.tokens;
                }
                if(NoAorO(expr)){
                    if(expr.size() == 1) {
                        if(expr.get(0).getToken() == TokenT.Value){
                            compileTForVar(expr.get(0) , m, fs);
                            if(isLast){m.visitJumpInsn(IFEQ, end);}
                            else {m.visitJumpInsn(IFNE, start);}
                            return;
                        }
                        expr = (ArrayList<Token>) expr.get(0).getValue();
                    }
                    TokenT type1 = ToolKit.StringToToken(ToolKit.getType(expr.get(0), fs.getObjects() , fs.getVars()),expr.get(0).getLine()).getToken();
                    TokenT type2 = ToolKit.StringToToken(ToolKit.getType(expr.get(2) , fs.getObjects() , fs.getVars()),expr.get(0).getLine()).getToken();
                    type1 = ToolKit.getHighestType(type1 , type2 );
                    push2( expr.get(0), m, fs, type1);
                    push2( expr.get(2), m, fs, type1);
                    if(isLast) {doCompare(expr.get(1), end, m, false , type1);}
                    else {doCompare(expr.get(1), start, m, true, type1);}
                } else { doAnds(getAnds(expr) , fs , m , start , end, isLast);}
                break;
            case Value:
                compileTForVar(t , m, fs);
                if(isLast){m.visitJumpInsn(IFEQ, end);}
                else {m.visitJumpInsn(IFNE, start);}
                break;
            case ArrayCall:
            case FunctionCall:
            case Object:
            case IfThenElse:
                push2(t , m,fs,TokenT.Boolean);
                if(isLast){m.visitJumpInsn(IFEQ, end);}
                else {m.visitJumpInsn(IFNE, start);}
                break;
            default:
                System.out.println("Weird token in compile Bool " + t.getToken());
                System.exit(2);
        }
    }

    private void doAnds(ArrayList<Token> ands , functionScope fs , MethodVisitor m , Label s , Label e , boolean isLast) {
        if(!isLast) {
            Label f = new Label();
            for (int i = 0 ; i < ands.size() -1 ; i++) {
                compileBoolean(ands.get(i), fs, m, s, f, true);
            }
            compileBoolean(ands.get(ands.size() - 1), fs, m, s, e, false);
            m.visitLabel(f);
            visitFrame(fs , m);
        } else {
            for (Token and : ands) {
                compileBoolean(and, fs, m, s, e, true);
            }
        }
    }

    private void visitFrame(functionScope fs , MethodVisitor m){
        try {
            int i = fs.getVars().size() - onStack ;
            onStack = fs.getVars().size();
            Object[] locals = getFrameTypes(fs.getVars() ,fs);
            m.visitFrame(F_FULL , locals.length , locals , 0 ,null);
            /*
            if(i == 0 ){
                m.visitFrame(F_SAME , 0,null,0,null);
            } else if(i > 0 && i < 4){
                System.out.println(i);
                System.out.println(fs.getVars());
                Object[] sd = getFrameTypes(fs,i);
                for(int r = 0 ; r < sd.length ;r++ ){
                    System.out.println(">" + sd[r]);
                }
                m.visitFrame(F_APPEND, i , getFrameTypes(fs , i) , 0 , null );
            } else if(i < 0 && i > - 3){
                System.out.println(i);
                m.visitFrame(F_CHOP, i , null , 0 , null );
            } else {
                Object[] locals = getFrameTypes(fs.getVars() ,fs);
                m.visitFrame(F_FULL , locals.length , locals , 0 ,null);
            }

             */
        }catch (Exception e){
        }
    }

    private Object[] getFrameTypes(functionScope fs, int is) {
        ArrayList<ExlVariable> vars = fs.getVars();
        Object[] type = new Object[vars.size()];
        for(int i = vars.size() - is ; i < vars.size(); i++) {
            if(!vars.get(i).isArray()) {
                switch (vars.get(i).getType()) {
                    case Char:
                    case Boolean:
                    case Int: type[i] = Opcodes.INTEGER;break;
                    case Float: type[i] = Opcodes.FLOAT;break;
                    case Double: type[i] = DOUBLE;break;
                    case Long: type[i] = LONG ;break;
                    case String: type[i] = "java/lang/String";break;
                    case Object:
                        if(vars.get(i).getObjName().charAt(0) == 'L'){type[i] = vars.get(i).getObjName() ;}
                        else {
                            try {
                                type[i] = fs.getAddress(vars.get(i).getObjName());
                            } catch (Exception e) {}
                        }
                        break;
                    default:
                        System.out.println("getFrameTypes Error" + vars.get(i));
                        System.exit(2);
                }
            } else {
                StringBuilder s = new StringBuilder();
                if(vars.get(i).isFlat()){
                    if(vars.get(i).getType() == TokenT.Int){
                        type[i] = "flatInteger";
                    }else {
                        type[i] = "flatDouble";
                    }
                }else {
                    for (int j = 0; j < vars.get(i).getArraySize(); j++) {
                        s.append("[");
                    }
                    switch (vars.get(i).getType()) {
                        case Char:
                            type[i] = s + "C";
                            break;
                        case Boolean:
                            type[i] = s + "Z";
                            break;
                        case Int:
                            type[i] = s + "I";
                            break;
                        case Float:
                            type[i] = s + "F";
                            break;
                        case String:
                            type[i] = s + "Ljava/lang/String;";
                            break;
                        case Double:
                            type[i] = s + "D";
                            break;
                        case Long:
                            type[i] = s + "J";
                            break;
                        case Object:
                            if(vars.get(i).getObjName().charAt(0) == 'L'){type[i] = s + vars.get(i).getObjName() ;}
                            else {
                                if(vars.get(i).getObjName().contains("/")){
                                    type[i] = s + "L" + vars.get(i).getObjName() + ";";
                                } else {
                                    try {
                                        type[i] = s + fs.getAddress(vars.get(i).getObjName());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        default:
                            System.out.println("getFrameTypes Error " + vars.get(i).getType());
                            System.exit(2);
                    }
                }
            }
        }
        return type ;
    }


    private ArrayList<Token> getAnds(ArrayList<Token> body) {
        ArrayList<Token> ands = new ArrayList<>();
        for(int i = 1 ; i < body.size() ; i++ ){
            ands.add(body.get( i - 1));
            i++ ;
        }
        ands.add(body.get(body.size() -1));
        return ands ;
    }

    private boolean NoAorO(ArrayList<Token> expression) {
        for(Token t : expression){if(t.getToken() == TokenT.And || t.getToken() == TokenT.Or) {return false ;}}
        return true ;
    }

    private void doCompare(Token token, Label jumpTo, MethodVisitor m , Boolean T , TokenT type) {
        if(type == TokenT.Int || TokenT.Boolean == type || TokenT.Char == type) {
            if (!T) {
                switch (token.getToken()) {
                    case LThan:
                        m.visitJumpInsn(IF_ICMPGE, jumpTo);
                        break;
                    case LThanEq:
                        m.visitJumpInsn(IF_ICMPGT, jumpTo);
                        break;
                    case GThan:
                        m.visitJumpInsn(IF_ICMPLE, jumpTo);
                        break;
                    case GThanEq:
                        m.visitJumpInsn(IF_ICMPLT, jumpTo);
                        break;
                    case EqualTo:
                        m.visitJumpInsn(IF_ICMPNE, jumpTo);
                        break;
                    case NotEqualTo:
                        m.visitJumpInsn(IF_ICMPEQ, jumpTo);
                        break;
                    default:
                        System.out.println("Unknown Bop in Bool exp " + token);
                        System.exit(2);
                }
            } else {
                switch (token.getToken()) {
                    case LThan:
                        m.visitJumpInsn(IF_ICMPLT, jumpTo);
                        break;
                    case LThanEq:
                        m.visitJumpInsn(IF_ICMPLE, jumpTo);
                        break;
                    case GThan:
                        m.visitJumpInsn(IF_ICMPGT, jumpTo);
                        break;
                    case GThanEq:
                        m.visitJumpInsn(IF_ICMPGE, jumpTo);
                        break;
                    case EqualTo:
                        m.visitJumpInsn(IF_ICMPEQ, jumpTo);
                        break;
                    case NotEqualTo:
                        m.visitJumpInsn(IF_ICMPNE, jumpTo);
                        break;
                    default:
                        System.out.println("Unknown Bop in Bool exp " + token);
                        System.exit(2);
                }
            }
        } else {
            switch (type) {
                case Double:
                    if(!T) {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(DCMPG);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(DCMPG);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                        }
                    } else {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(DCMPG);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(DCMPG);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(DCMPL);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                        }
                    }
                    break;
                case Float:
                    if(!T) {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(FCMPG);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(FCMPG);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                        }
                    } else {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(FCMPG);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(FCMPG);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(FCMPL);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                        }
                    }
                    break;
                case Long:
                    if(!T) {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                        }
                    } else {
                        switch (token.getToken()) {
                            case LThan:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFLT, jumpTo);
                                break;
                            case LThanEq:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFLE, jumpTo);
                                break;
                            case GThan:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFGT, jumpTo);
                                break;
                            case GThanEq:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFGE, jumpTo);
                                break;
                            case EqualTo:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFEQ, jumpTo);
                                break;
                            case NotEqualTo:
                                m.visitInsn(LCMP);
                                m.visitJumpInsn(IFNE, jumpTo);
                                break;
                        }
                    }
                    break;
                case String:
                case Object:
                    if(!T) {
                        switch (token.getToken()) {
                            case EqualTo: m.visitJumpInsn(IF_ACMPNE, jumpTo);break;
                            case NotEqualTo: m.visitJumpInsn(IF_ACMPEQ, jumpTo);break;
                        }
                    } else {
                        switch (token.getToken()) {
                            case EqualTo: m.visitJumpInsn(IF_ACMPEQ, jumpTo);break;
                            case NotEqualTo: m.visitJumpInsn(IF_ACMPNE, jumpTo);break;
                        }
                    }

            }
        }
    }

    private void compileIfs(ArrayList<Statement> innerStatements, functionScope fs, MethodVisitor m , TokenT type) {
        boolean visitLast = false;
        ArrayList<Label> labels = new ArrayList<>();
        for(int i = 0 ; i < innerStatements.size() ; i++){
            labels.add(new Label());
        }
        for(int i = 0 ; i < innerStatements.size() ; i++){
            Statement s = innerStatements.get(i) ;
            switch (s.getType()){
                case Else_If:
                case IF:
                    Label start = new Label() ;
                    ArrayList<ExlVariable> vars43 = (ArrayList<ExlVariable>) fs.getVars().clone();
                    compileBooleanExp(s.getBody() , fs , m , start , labels.get(i) ) ;
                    m.visitLabel(start);
                    visitFrame(fs , m);
                    compileStatements(s.getInnerStatements(), m, fs , type);
                    fs.setVars(vars43);
                    if(! ToolKit.containsReturn(s.getInnerStatements())) {
                        m.visitJumpInsn(GOTO, labels.get(innerStatements.size() - 1));
                        visitFrame(fs , m);
                        visitLast = true ;
                    }else {
                        visitFrame(fs, m);
                    }
                    m.visitLabel(labels.get(i));
                    break;
                case Else:
                    ArrayList<ExlVariable> vars432 = (ArrayList<ExlVariable>) fs.getVars().clone();
                    compileStatements(s.getInnerStatements(), m, fs , type);
                    m.visitLabel(labels.get(i));
                    fs.setVars(vars432);
                    if(visitLast) {visitFrame(fs , m );}
                    break;
            }
        }
    }

    private Object[] getFrameTypes(ArrayList<ExlVariable> vars , functionScope fs) {
        Object[] type = new Object[vars.size()];
        for(int i = 0 ; i < vars.size(); i++) {
            if(!vars.get(i).isArray()) {
                switch (vars.get(i).getType()) {
                    case Char:
                    case Boolean:
                    case Int: type[i] = Opcodes.INTEGER;break;
                    case Float: type[i] = Opcodes.FLOAT;break;
                    case Double: type[i] = DOUBLE;break;
                    case Long: type[i] = LONG ;break;
                    case String: type[i] = "java/lang/String";break;
                    case Object:
                        if(vars.get(i).getObjName().charAt(0) == 'L'){type[i] = vars.get(i).getObjName() ;}
                        else {
                            try {
                                type[i] = fs.getAddress(vars.get(i).getObjName());
                            } catch (Exception e) {}
                        }
                        break;
                    default:
                        System.out.println("getFrameTypes Error" + vars.get(i));
                        System.exit(2);
                    }
            } else {
                StringBuilder s = new StringBuilder();
                if(vars.get(i).isFlat()){
                    if(vars.get(i).getType() == TokenT.Int){
                        type[i] = "flatInteger";
                    }else {
                        type[i] = "flatDouble";
                    }
                }else {
                    for (int j = 0; j < vars.get(i).getArraySize(); j++) {
                        s.append("[");
                    }
                    switch (vars.get(i).getType()) {
                        case Char:
                            type[i] = s + "C";
                            break;
                        case Boolean:
                            type[i] = s + "Z";
                            break;
                        case Int:
                            type[i] = s + "I";
                            break;
                        case Float:
                            type[i] = s + "F";
                            break;
                        case String:
                            type[i] = s + "Ljava/lang/String;";
                            break;
                        case Double:
                            type[i] = s + "D";
                            break;
                        case Long:
                            type[i] = s + "J";
                            break;
                        case Object:
                            if(vars.get(i).getObjName().charAt(0) == 'L'){type[i] = s + vars.get(i).getObjName() ;}
                            else {
                                if(vars.get(i).getObjName().contains("/")){
                                    type[i] = s + "L" + vars.get(i).getObjName() + ";";
                                } else {
                                    try {
                                        type[i] = s + fs.getAddress(vars.get(i).getObjName());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        default:
                            System.out.println("getFrameTypes Error " + vars.get(i).getType());
                            System.exit(2);
                    }
                }
            }
        }
        return type ;
    }

    private void doReturn(TokenT type2, MethodVisitor m) {
        switch (type2){
            case Char:
            case Boolean:
            case Int :   m.visitInsn(IRETURN);break;
            case Long:   m.visitInsn(LRETURN);break;
            case Double: m.visitInsn(DRETURN);break;
            case Float:  m.visitInsn(FRETURN);break;
            case Array:case Object: case String: m.visitInsn(ARETURN);break;
            case Void:m.visitInsn(RETURN);break;
            default:
                System.out.println("Return type not supported " + type2);
                System.exit(2);
        }
    }

    private ArrayList<ArrayList<Token>> getParameters(ArrayList<Token> tokens){
        ArrayList<ArrayList<Token>> ret = new ArrayList<>();
        ArrayList<Token> x = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getToken() == TokenT.Comma) {
                ret.add(x);
                x = new ArrayList<>();
            } else {x.add(token);}
        }
        ret.add(x);
        return ret;
    }

    private void doFunc(Token t , MethodVisitor m , TokenT type ){//TOOD CHANGED SOMETHING here
        switch (type){
            case Char:
            case Int:
                switch (t.getToken()){
                    case Plus: m.visitInsn(IADD);break;
                    case Mul: m.visitInsn(IMUL);break;
                    case Minus: m.visitInsn(ISUB);break;
                    case Div: m.visitInsn(IDIV);break;
                    case Mod: m.visitInsn(IREM);break;
                    default:
                        System.out.println("Unexpected token as function : " + t.getToken());
                        System.exit(2);
                        break;
                }
                break;
            case Float:
                switch (t.getToken()){
                    case Plus: m.visitInsn(FADD);break;
                    case Mul: m.visitInsn(FMUL);break;
                    case Minus: m.visitInsn(FSUB);break;
                    case Div: m.visitInsn(FDIV);break;
                    default:
                        System.out.println("Unexpected token as function : " + t.getToken());
                        System.exit(2);
                        break;
                }
                break;
            case Long:
                switch (t.getToken()){
                    case Plus: m.visitInsn(LADD);break;
                    case Mul: m.visitInsn(LMUL);break;
                    case Minus: m.visitInsn(LSUB);break;
                    case Div: m.visitInsn(LDIV);break;
                    case Mod: m.visitInsn(LREM);break;
                    default:
                        System.out.println("Unexpected token as function : " + t.getToken());
                        System.exit(2);
                        break;
                }
                break;
            case Double:
                switch (t.getToken()){
                    case Plus: m.visitInsn(DADD);break;
                    case Mul: m.visitInsn(DMUL);break;
                    case Minus: m.visitInsn(DSUB);break;
                    case Div: m.visitInsn(DDIV);break;
                    case Mod: m.visitInsn(DREM);break;
                    default:
                        System.out.println("Unexpected token as function : " + t.getToken());
                        System.exit(2);
                        break;
                }
                break;
            default:
                System.out.println(type);
                System.out.println("ONly int/char and float supported rn " +type );
                System.exit(2);
        }
    }


    private void doFuncCAll(FunctionCall fc , MethodVisitor m , functionScope fs){
        if(fc.isStatic()) {
            compileParameters2( fc.getParams() , m , fs , fc.getDesc());
            m.visitMethodInsn(INVOKESTATIC, fs.getOwner(), fc.getName(), fc.getDesc(), false);
        } else {
            m.visitVarInsn(ALOAD , 0);
            compileParameters2( fc.getParams() , m , fs , fc.getDesc());
            m.visitMethodInsn(INVOKEVIRTUAL, fs.getOwner(), fc.getName(), fc.getDesc(), false);
        }
    }

    private void compileParameters2(ArrayList<Token> params, MethodVisitor m, functionScope fs , String desc) {
        ArrayList<TokenT> types = getParameterTypes(desc);
        for (Token param : params) {
            push2(param, m, fs, types.get(0));
        }
    }

    private void compileParameters(ArrayList<Token> tokens, MethodVisitor m, functionScope fs , String description) {
        ArrayList<ArrayList<Token>> params = getParameters(tokens);
        ArrayList<TokenT> types = getParameterTypes(description);
        if(params.size() != 1 || ! params.get(0).isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                compileExpression(new Expression(params.get(i)), m, fs, types.get(i));
            }
        }
    }

    private ArrayList<TokenT> getParameterTypes(String description) {
        ArrayList<TokenT> types = new ArrayList<>();
        String s = getBetween(description);
        for(int i = 0 ; i < s.length() ; i++){
            switch (s.charAt(i)){
                case 'I': types.add(TokenT.Int);break;
                case 'F': types.add(TokenT.Float);break;
                case 'Z': types.add(TokenT.Boolean);break;
                case 'J': types.add(TokenT.Long);break;
                case 'D': types.add(TokenT.Double);break;
                case 'C': types.add(TokenT.Char);break;
                case '[':
                    while (s.charAt(i) == '['){
                        i++ ;
                        if(s.charAt(i) == 'L'){
                            while (s.charAt(i) != ';'){
                                i++ ;
                            }
                        }
                    }
                    types.add(TokenT.Object);
                    break;
                case 'L':
                    StringBuilder r = new StringBuilder();
                    while (s.charAt(i) != ';'){
                        r.append(s.charAt(i));
                        i++ ;
                    }
                    if(r.toString().equals("Ljava/lang/String;")){types.add(TokenT.String);}
                    else {types.add(TokenT.Object);}
            }
        }
        return types ;
    }

    private String getBetween(String description) {
        int start = 0 ;
        int end = description.length() - 1;
        for(int i = 0 ; i < end ; i++){
            if(description.charAt(i) == '('){start = i ;break;}
        }
        for(int i = end; i > start ; i--){
            if(description.charAt(i) == ')'){end = i ;break;}
        }
        return description.substring(start , end);
    }

    public void compileExpression(Expression exp , MethodVisitor m , functionScope fs , TokenT type){
        if(exp.size() == 0 ){}
        else if(exp.size() == 1 ) {push2(exp.get(0), m ,fs , type);}
        else if(exp.size() == 2 && exp.get(0).getToken() == TokenT.Cast) {push2(exp.get(1), m ,fs , type);doCast(exp.get(0) , m);}
        else {
            for (int i = 0; i < exp.size(); i++) {
                Token t = exp.get(i);
                if (ToolKit.isSymbol(t)) {
                    if (i % 2 == 1) {
                        push2(exp.get(i - 1), m, fs, type);
                        push2(exp.get(i + 1), m, fs, type);
                        doFunc(t, m, type);
                        Expression toks = new Expression(new ArrayList<>(exp.tokens.subList(3, exp.size())));
                        compileExpression(toks, m, fs, type);
                    } else {
                        push2(exp.get(i + 1), m, fs, type);
                        doFunc(t, m, type);
                        Expression toks = new Expression(new ArrayList<>(exp.tokens.subList(2, exp.size())));
                        compileExpression(toks, m, fs, type);
                    }
                    break;
                }
            }
        }
    }

    private void doCast(Token token, MethodVisitor m) {
        Token t = (Token) token.getValue();
        switch (t.getToken()){
            case Int: m.visitTypeInsn(CHECKCAST, "java/lang/Integer"); m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case Float: m.visitTypeInsn(CHECKCAST, "java/lang/Float"); m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case String: m.visitTypeInsn(CHECKCAST, "java/lang/String"); break;
            case Boolean: m.visitTypeInsn(CHECKCAST, "java/lang/Boolean"); m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case Char: m.visitTypeInsn(CHECKCAST, "java/lang/Character"); m.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
                break;
            case Long: m.visitTypeInsn(CHECKCAST, "java/lang/Double"); m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case Double: m.visitTypeInsn(CHECKCAST, "java/lang/Long"); m.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case Value:
                String s = (String) t.getValue();
                m.visitTypeInsn(CHECKCAST, s);
        }
    }

    private void compileTForVar(Token token , MethodVisitor m, functionScope fs) {
        if (token.getValue().equals("true") || token.getValue().equals("1")){push(m , "1" , fs,TokenT.Int);}
        else if(token.getValue().equals("false") || token.getValue().equals("0")){push(m , "0" , fs ,TokenT.Int);}
        else {
            for(ExlVariable var : fs.getVars()){
                if(var.getName().equals(token.getValue()) && var.getType() == TokenT.Boolean){
                    loadVar(m , (String) token.getValue(), fs);
                    return;
                }
            }
            throw new Error("Can't variable " + token.getValue());
        }
    }

    private void compileString(List<Token> tokens, MethodVisitor m, functionScope fs) {
        StringBuilder s = new StringBuilder();
        ArrayList<ExlVariable> bt = new ArrayList<>();
        bt.addAll(fs.getFields());
        bt.addAll(fs.getVars());
        ArrayList<String> types = new ArrayList<>();
        if(tokens.size() == 1 ){
            switch (tokens.get(0).getToken()) {
                case Value:
                    boolean b = true;
                    for (ExlVariable var : fs.getVars()) {
                        if (var.getName().equals(tokens.get(0).getValue())) {
                            b = false;
                            loadVar(m, (String) tokens.get(0).getValue(), fs);
                        }
                    }
                    if (b) {
                        String se = (String) tokens.get(0).getValue();
                        m.visitLdcInsn(se.substring(1, se.length() - 1));
                    }
                    break;
                case Expression:
                case FunctionCall:
                case ObjectDeclaration:
                case ArrayCall:
                case IfThenElse:
                case StringVal:
                case Object:
                    push2(tokens.get(0), m, fs, TokenT.String);
            }
        } else if(tokens.size() == 2 && tokens.get(0).getToken() == TokenT.Cast){
            push2(tokens.get(1), m, fs, TokenT.String);
            doCast(tokens.get(0) , m);
        }else {
            for (Token token : tokens) {
                switch (token.getToken()) {
                    case Expression:
                    case FunctionCall:
                    case ObjectDeclaration:
                    case ArrayCall:
                    case IfThenElse:
                    case StringVal:
                    case Object:
                        String sr = ToolKit.getType(token, fs.getObjects(),bt);
                        types.add(sr);
                        push2(token, m, fs, ToolKit.StringToToken(sr, token.getLine()).getToken());
                        s.append("\u0001");
                        break;
                    case Value:
                        String srs = (String) token.getValue();
                        if(srs.startsWith("\"")){
                            srs = srs.substring(1, srs.length() - 1);
                            s.append(srs);
                        } else {
                            String srrs = ToolKit.getType(token, fs.getObjects(),bt);
                            types.add(srrs);
                            push2(token, m, fs, ToolKit.StringToToken(srrs, token.getLine()).getToken());
                            s.append("\u0001");
                        }break;
                    case Plus:
                        break;
                    default:
                        System.out.println("CompileString Unknown " + token.getToken());
                        System.exit(2);
                }
            }
            StringBuilder desc = new StringBuilder("(");
            for (String type : types) {
                desc.append(type);
            }
            desc.append(")Ljava/lang/String;");
            m.visitInvokeDynamicInsn("makeConcatWithConstants", desc.toString(), new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false), s.toString());
        }
    }


    public void push2(Token t , MethodVisitor m , functionScope fs , TokenT type){
        switch (t.getToken()){
            case BoolExpression:
                try{
                    Expression expr1 = (Expression) t.getValue();
                    ArrayList<Token> s = new ArrayList<>() ;
                    s.add(new Token(TokenT.BoolExpression, expr1, expr1.tokens.get(0).getLine()));
                    compileBooleanValue(s , fs, m );
                }catch (Exception e){
                    ArrayList<Token> exp = (ArrayList<Token>) t.getValue();
                    ArrayList<Token> s = new ArrayList<>() ;
                    s.add(new Token(TokenT.BoolExpression, new Expression(exp), exp.get(0).getLine()));
                    compileBooleanValue(s , fs, m );
                }
                break;
            case Expression:
                try{
                    Expression expr1 = (Expression) t.getValue();
                    compileExpression(expr1, m , fs , type);
                }catch (Exception e){
                    //e.printStackTrace();
                    ArrayList<Token> exp = (ArrayList<Token>) t.getValue();
                    compileExpression(new Expression(exp) , m , fs , type);
                }
                break;
            case FunctionCall:
                FunctionCall fCall = (FunctionCall) t.getValue();
                doFuncCAll(fCall, m, fs);
                break;
            case Value:
                push(m, (String) t.getValue(), fs , type);
                break;
            case ArrayCall:
                ArrayCall aCall = (ArrayCall) t.getValue();
                doArrayCall(aCall , m , fs );
                break;
            case Object:
                ArrayList<ObjectRef> ste = (ArrayList<ObjectRef>) t.getValue();
                doObject( ste , m , fs);
                break;
            case ObjectDeclaration:
                ObjectDeclaration od = (ObjectDeclaration) t.getValue() ;
                doObjectDec(od ,m , fs);
                break;
            case ArrayDeclaration:
                ArrayDeclaration ad = (ArrayDeclaration) t.getValue() ;
                doArrayDec(ad , m , fs);
                break;
            case IfThenElse:
                IfThenElse ite = (IfThenElse) t.getValue() ;
                doIfThenElse(ite , m , fs , type);
                break;
            case StringVal:
                StringVal s = (StringVal)  t.getValue() ;
                compileString(s.parts , m ,fs);
                break;
            case ArrayLength:
                String name = (String) t.getValue() ;
                loadVar(m , name ,fs);
                m.visitInsn(ARRAYLENGTH);
                break;
        }
    }

    private void doIfThenElse(IfThenElse ite, MethodVisitor m, functionScope fs , TokenT type) {
        Label Else = new Label();
        Label Then = new Label();
        Label after = new Label();
        try {
            Expression e = (Expression) ite.getIf().getValue();
            compileBooleanExp(e.tokens, fs, m, Then, Else);
        } catch (Exception e){
            //e.printStackTrace();
            compileBooleanExp((ArrayList<Token>) ite.getIf().getValue(), fs, m, Then, Else);
        }
        Object[] locals = getFrameTypes(fs.getVars() ,fs);
        m.visitLabel(Then);
        visitFrame(fs , m);
        push2(ite.getThen() , m , fs , type);
        m.visitJumpInsn(GOTO , after);
        m.visitLabel(Else);
        visitFrame(fs , m);
        push2(ite.getElse() , m , fs , type);
        m.visitLabel(after);
        Object[] s = new Object[0];
        if(ite.getType() != null){
            String t = ToolKit.TokenToString(ite.getType(),fs.getObjects());
            if(ite.getType().getToken() == TokenT.Object){
                t = t.substring(1 , t.length() - 1);
                s = new Object[]{ite.getType().getValue()};
            } else {
                s = new Object[]{ ToolKit.TokenToString(ite.getType(),fs.getObjects())};
            }
        } else {
            switch (type) {
                case Int:
                case Char:
                case Boolean: s = new Object[]{INTEGER};break;
                case Double: s = new Object[]{DOUBLE};break;
                case Float: s = new Object[]{FLOAT};break;
                case String: s = new Object[]{"java/lang/String" };break;
                case Long: s = new Object[]{LONG};

            }
        }
        try {
            onStack = fs.getVars().size() ;
            m.visitFrame(F_FULL, fs.getVars().size(), locals, 1, s);
        }catch (Exception e){}
    }

    private void doArrayDec(ArrayDeclaration ad, MethodVisitor m, functionScope fs) {
        for(Token t : ad.getValues()){
            push2(t , m , fs , TokenT.Int);
        }
        if(ad.getType().charAt(1) == '['){
            int size = ad.getValues().size() ;
            if(size + 1 != ad.getType().length()) {
                String s = null;
                try {
                    s = ad.getType().substring(0, size) + "L" + fs.getAddress(ad.getType().substring(size)) + ";";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m.visitMultiANewArrayInsn(s, size);
            }
            else {m.visitMultiANewArrayInsn(ad.getType() , size);}}
        else {
            String s = ad.getType().substring(1);
            if(s.length() > 1) {
                try {
                    m.visitTypeInsn(ANEWARRAY, fs.getAddress(s));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                switch (s.charAt(0)){
                    case 'I': m.visitIntInsn(NEWARRAY, T_INT);break;
                    case 'D': m.visitIntInsn(NEWARRAY, T_DOUBLE);break;
                    case 'Z': m.visitIntInsn(NEWARRAY, T_BOOLEAN);break;
                    case 'J': m.visitIntInsn(NEWARRAY, T_LONG);break;
                    case 'F': m.visitIntInsn(NEWARRAY, T_FLOAT);break;
                }
            }
        }
    }

    private void doObjectDec(ObjectDeclaration od, MethodVisitor m, functionScope fs) {
        m.visitTypeInsn(NEW, od.getAddress());
        m.visitInsn(DUP);
        compileParameters(od.getValues() , m, fs , od.getDesc());
        m.visitMethodInsn(INVOKESPECIAL, od.getAddress(), "<init>", od.getDesc(), false);
    }

    private void doArrayCall(ArrayCall ac, MethodVisitor m, functionScope fs) {
        ArrayList<Token> values = ac.getValues();
        loadVar(m, ac.getName(), fs);
        if(ac.isBasic()) {
            int size = values.size();
            ArrayList<ExlVariable> s = (ArrayList<ExlVariable>) fs.getVars().clone();
            s.addAll((ArrayList<ExlVariable>) fs.getFields().clone());
            for (ExlVariable var : s) {
                if (var.getName().equals(ac.getName())) {
                    TokenT type = var.getType();
                    while (values.size() > 1) {
                        push2(values.get(0), m, fs, TokenT.Int);
                        m.visitInsn(AALOAD);
                        values.remove(0);
                    }
                    if (size == var.getArraySize()) {
                        push2(values.get(0), m, fs, TokenT.Int);
                        switch (type) {
                            case Int: m.visitInsn(IALOAD);break;
                            case Boolean: m.visitInsn(BALOAD);break;
                            case Char: m.visitInsn(CALOAD);break;
                            case Value:
                            case Object:
                            case String: m.visitInsn(AALOAD);break;
                            case Float: m.visitInsn(FALOAD);break;
                            case Double: m.visitInsn(DALOAD);break;
                            case Long: m.visitInsn(LALOAD);break;
                            default:
                                System.out.println("Do Array call error " + type);
                                System.exit(2);
                        }
                    } else {
                        push2(values.get(0), m, fs, TokenT.Int);
                        m.visitInsn(AALOAD);
                    }
                }
            }
        } else {
            push(m , "" + values.size(),fs , TokenT.Int );
            m.visitIntInsn(NEWARRAY, T_INT);
            for(int i = 0 ; i < values.size() ; i++){
                m.visitInsn(DUP);
                push(m ,"" + i , fs , TokenT.Int);
                push2(new Token(TokenT.Expression ,  values.get(i).getValue()), m , fs , TokenT.Int);
                m.visitInsn(IASTORE);
            }
            switch (ac.getType()){
                case "I":m.visitMethodInsn(INVOKEVIRTUAL, "flatInteger", "get", "([I)I", false);break;
                case "D":m.visitMethodInsn(INVOKEVIRTUAL, "flatDouble", "get", "([I)D", false);
            }
        }
    }

    public functionScope makeVariable(MethodVisitor m, String name, List<Token> Expression , functionScope fs, TokenT Type) {
        compileExpression(new Expression(Expression), m, fs , Type);
        fs.addVar(new ExlVariable(name , Type, "", 0));
        switch (Type){
            case Int:
            case Char:
            case Boolean: m.visitVarInsn(ISTORE, fs.getPos() );break;
            case Float: m.visitVarInsn(FSTORE, fs.getPos());break;
            case Double: m.visitVarInsn(DSTORE, fs.getPos() - 1 );break;
            case String: m.visitVarInsn(ASTORE , fs.getPos() );break;
            case Long: m.visitVarInsn(LSTORE , fs.getPos() - 1);break;
            default:
                System.out.println("WEIRD TYPE FOR A VAR " + Type);
                System.exit(2);
        }
        return fs;
    }

    private void push(MethodVisitor m, String s, functionScope fs, TokenT type) {
        switch(type){
            case Int:
                try {
                    if(s.endsWith("I")) {s = s.substring(0, s.length() - 1);}
                    int y = Integer.parseInt(s);
                    if ( (y < 0) && (y > -128) ){int z = 256 - (y * -1);m.visitIntInsn(BIPUSH , z);}
                    else if ( (y < 0) && (y > - 32768)) {int z = 65536 - (y * -1);m.visitIntInsn(SIPUSH , z);}
                    else if (y < 6) {
                        switch (y) {
                            case 0: m.visitInsn(ICONST_0);break;
                            case 1: m.visitInsn(ICONST_1);break;
                            case 2: m.visitInsn(ICONST_2);break;
                            case 3: m.visitInsn(ICONST_3);break;
                            case 4: m.visitInsn(ICONST_4);break;
                            case 5: m.visitInsn(ICONST_5);break;
                            default: System.out.println("SOMETHING WRONG");System.exit(2);break;
                        }}
                    else if (y < 128) {m.visitIntInsn(BIPUSH, y);}
                    else if (y < 32767){m.visitIntInsn(SIPUSH, y);}
                    else {m.visitLdcInsn(y);}}
                catch (Exception e){loadVar(m, s, fs);}
                break;
            case Float :
                try {
                    float y ;
                    if(s.endsWith("F")){String s2 = s.substring(0 , s.length() - 1);y = Float.parseFloat(s2);}
                    else {y = Float.parseFloat(s);}
                    m.visitLdcInsn(y);
                } catch (Exception e){loadVar(m, s, fs);}
                break;
            case Char:
                try{int is = Integer.parseInt(s) ; push(m , is+"" , fs, TokenT.Int); }
                catch (Exception e3){
                    if (s.length() != 3) {loadVar(m,s,fs);}
                    else {int c = s.charAt(1);push(m, c + "", fs, TokenT.Int);}
                }
                break;
            case String:
                if(s.charAt(0) == '\"' &&  s.charAt(s.length() -1) == '\"'){m.visitLdcInsn(s.substring(1, s.length() - 1));}
                else {loadVar(m, s, fs);}
                break;
            case Boolean:
                if(s.equals("true")){m.visitInsn(ICONST_1);}
                else if(s.equals("false")){m.visitInsn(ICONST_0);}
                else {loadVar(m,s ,fs);}
                break;
            case Double:
                try {
                    double y ;
                    if(s.endsWith("D")){String s2 = s.substring(0 , s.length() - 1);y = Double.parseDouble(s2);}
                    else {y = Double.parseDouble(s);}
                    m.visitLdcInsn(y);
                } catch (Exception e){loadVar(m, s, fs);}
                break;
            case Long:
                try {
                    long y ;
                    if(s.endsWith("L")){String s2 = s.substring(0 , s.length() - 1);y = Long.parseLong(s2);
                    }else {y = Long.parseLong(s);}
                    m.visitLdcInsn(y);
                } catch (Exception e){loadVar(m, s, fs);}
                break;
            case Array:
            case Object:
                if(s.startsWith("\"") && s.endsWith("\"")){push(m ,  s, fs , TokenT.String);break;}
                else {loadVar(m , s, fs);}
                break;
            default:
                System.out.println("push" + type + " not supported");
                System.out.println(s);
                System.exit(2);
        }
    }

    public static void loadVar(MethodVisitor m, String name , functionScope fs) {
        for (ExlVariable var : fs.getVars()) {
            if (var.getName().equals(name)) {
                int pos = var.getPos();
                if(var.isArray()){m.visitVarInsn(ALOAD , pos);return;}
                switch (var.getType()) {
                    case String:
                    case Object: m.visitVarInsn(ALOAD, pos);return;
                    case Float: m.visitVarInsn(FLOAD, pos);return;
                    case Char:
                    case Boolean:
                    case Int: m.visitVarInsn(ILOAD, pos);return;
                    case Long: m.visitVarInsn(LLOAD, pos);return;
                    case Double: m.visitVarInsn(DLOAD ,pos);return;
                    default:
                        System.out.println("UNEXPECTED VAR TYPE " + var.getType());
                        System.exit(2);
                }
            }
        }
        for (ExlVariable va : fs.getFields()) {
            if (va.getName().equals(name)) {
                String sas = va.getString(fs.getObjects());
                m.visitFieldInsn(GETSTATIC, fs.getOwner(), va.getName(), sas );
                return;
            }
        }
        System.out.println("ERROR UNKNOWN Variable : " + name );
        System.out.println(fs.getVars());
        System.out.println(fs.getFields());
        throw new Error("Unable to p");
    }
}



