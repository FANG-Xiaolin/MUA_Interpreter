import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//To deal with the single elements
public class Interpreter {
    int isWaiting;
    MuaParser parent;
    String op = "";
    String funcname="";
    LinkedList<String> args = new LinkedList<String>();//the args of this op
    Map<String, String> namespace;
    List<String> funcnamespace;
    Map<String, String> hidden_namespace = new HashMap<>();   //constants
    List<String> hidden_funcnamespace = new LinkedList<>();      //constants and the definition of the funcion itself
    LinkedList<String> cmds = new LinkedList<String>();//
    protected List<String> onearg = new ArrayList<String>(Arrays.asList("thing", "erase",
            "isname", "print", ":", "not", "output","isnumber","isword","islist","isbool",
            "isempty","random","sqrt","int","wait","first","last","butfirst","butlast",
            "save","load"));
    protected List<String> twoargs = new ArrayList<String>(Arrays.asList("make", "add", "sub",
            "div", "mul", "mod", "eq", "gt", "lt", "and", "or", "repeat","word","sentence",
            "list","join"));
    protected List<String> OP = new ArrayList<String>(Arrays.asList("make", "thing", "erase",
            "isname", "print", "read", "readlinst", ":", "add", "sub", "div", "mul",
            "mod", "eq", "gt", "lt", "and", "or", "not", "repeat","isnumber","isword",
            "islist","isbool","isempty","random","sqrt","int","wait","erall","poall",
            "word","if","sentence","list","join","first","last","butfirst","butlast",
            "save","load"));

    public Interpreter(LinkedList<String> cmds, Map<String, String> namespace, List<String> funcnamespace,MuaParser parent) {
        this.cmds = cmds;
        this.namespace = namespace;
        this.funcnamespace=funcnamespace;
        this.parent=parent;
        _initiate_hidden(namespace,funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public Interpreter(Map<String, String> namespace, List<String> funcnamespace) {
        this.cmds = new LinkedList<String>();
        this.namespace = namespace;
        this.funcnamespace=funcnamespace;
        parent=new MuaParser(cmds,this);
        _initiate_hidden(namespace,funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public Interpreter(LinkedList<String> cmds, MuaParser parent) {
        this.cmds = cmds;
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        this.parent=parent;
        _initiate_hidden(namespace,funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public Interpreter(LinkedList<String> cmds) {
        this.cmds = cmds;
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        parent=new MuaParser(cmds,this);
        _initiate_hidden(namespace,funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public Interpreter() {
        this.cmds = new LinkedList<String>();
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        parent=new MuaParser(cmds,this);
        _initiate_hidden(namespace,funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    void _initiate_hidden(Map<String,String> HID_NAME,List<String> HID_FUNC){
        try{
            this.namespace.putAll(HID_NAME);
            this.funcnamespace.addAll(HID_FUNC);
        }catch (Exception ignore){
            //ignore
        }
        return;
    }



    public String interpret() {                      //
        if(!cmds.isEmpty()){
            if(isOP(cmds.getFirst())) {
                GetOPArgs();
                //Arguments preparation done. Time to carry out specific operation.
                String ret = null;
                ret=doOP();
                if (ret != null && !ret.equals("")) {
                    cmds.addFirst(ret);
                }
            }else if(!cmds.isEmpty()&&sys_islist(cmds.getFirst())){
                String newline=cmds.removeFirst();
                newline=newline.substring(1,newline.length()-1);
                Interpreter tmpInp=new Interpreter(namespace,funcnamespace);
                MuaParser tmpParser=tmpInp.parent;
                tmpParser.addline(newline);
                tmpParser.cmds.addAll(cmds);
                parent.cmds.clear();
                parent.cmds.addAll(tmpParser.cmds);
                parent.Inp.interpret();
            }else if(!cmds.isEmpty()&&sys_ismath(cmds.getFirst())){     //math line
                MyCalculator cal=new MyCalculator(cmds.removeFirst(),namespace,funcnamespace);
                cmds.addFirst(cal.compute()+"");
            }else if(isinfunc(cmds.getFirst())){
                funcname=cmds.removeFirst();
                FuncInterpreter funcInp= new FuncInterpreter(cmds,parent);
                funcInp.namespace.put(funcname,namespace.get(funcname));        //to implement recursive procedure
                if(!funcInp.funcnamespace.contains(funcname)){
                    funcInp.funcnamespace.add(funcname);
                }
                funcInp.interpret();
            }else{
                System.out.println("???");
                return null;
            }
        }
        args.clear();
        return null;
    }

    void GetOPArgs() {
        if(isWaiting==0){
            this.op = cmds.removeFirst();
            if (onearg.contains(op)) {
                isWaiting = 1;
            } else if (twoargs.contains(op)) {
                isWaiting = 2;
            } else if(op.equals("if")){
                isWaiting = 3;
            } else {
                isWaiting = 0;
            }
        }
        while (isWaiting > 0) {
            if (cmds.isEmpty() && !op.equals("read") && !op.equals("readlinst")) {
                String line = parent.GetInput();
                parent.parse(line);
            }
            String element;
            if(!cmds.isEmpty()){
                element=cmds.getFirst();
            }
            else{
                break;
            }

            if (isOP(element)||isinfunc(element)) {
                Interpreter nextlevel = new Interpreter(cmds, namespace, funcnamespace, parent);
                nextlevel.interpret();
            }else if(sys_ismath(element)){
                MyCalculator cal=new MyCalculator(cmds.removeFirst(),namespace,funcnamespace);
                cmds.addFirst(cal.compute()+"");
            }
            if (!cmds.isEmpty()) {
                args.add(cmds.removeFirst());
                isWaiting--;
            }
        }
    }

    String doOP() {
        String ret="";
        switch (op) {
            case "print":
                print(args);
                break;
            case "make":
                make(args);
                break;
            case "thing":
                ret = thing(args);
                break;
            case ":":
                ret = colon(args);
                break;
            case "erase":
                ret = erase(args);
                break;
            case "isname":
                ret = isname(args);
                break;
            case "read":
                ret = read(args);
                break;
            case "readlinst":
                ret = readlinst(args);
                break;
            case "add":
                ret = add(args);
                break;
            case "sub":
                ret = sub(args);
                break;
            case "mul":
                ret = mul(args);
                break;
            case "div":
                ret = div(args);
                break;
            case "mod":
                ret = mod(args);
                break;
            case "and":
                ret = and(args);
                break;
            case "or":
                ret = or(args);
                break;
            case "not":
                ret = not(args);
                break;
            case "eq":
                ret = eq(args);
                break;
            case "gt":
                ret = gt(args);
                break;
            case "lt":
                ret = lt(args);
                break;
            case "repeat":
                ret = repeat(args);
                break;
            case "isnumber":
                ret = isnumber(args);
                break;
            case "isword":
                ret = isword(args);
                break;
            case "islist":
                ret = islist(args);
                break;
            case "isbool":
                ret = isbool(args);
                break;
            case "isempty":
                ret = isempty(args);
                break;
            case "random":
                ret = random(args);
                break;
            case "sqrt":
                ret = sqrt(args);
                break;
            case "int":
                ret = mua_int(args);
                break;
            case "wait":
                ret = wait(args);
                break;
            case "erall":
                ret = erall(args);
                break;
            case "poall":
                ret = poall(args);
                break;
            case "word":
                ret = word(args);
                break;
            case "if":
                ret = mua_if(args);
                break;
            case "sentence":
                ret = sentence(args);
                break;
            case "list":
                ret = list(args);
                break;
            case "join":
                ret = join(args);
                break;
            case "first":
                ret = first(args);
                break;
            case "last":
                ret = last(args);
                break;
            case "butfirst":
                ret = butfirst(args);
                break;
            case "butlast":
                ret = butlast(args);
                break;
            case "save":
                ret = save(args);
                break;
            case "load":
                ret = load(args);
                break;
        }
        return ret;
    }

    String print(LinkedList<String> args) {
        System.out.println(args.getFirst());
        return null;
    }

    String make(LinkedList<String> args) {
        if (!sys_isword(args.getFirst())) {
            error("Syntax error.The first argument has to be a word.");
            System.exit(-1);
        } else if (isOP(args.getFirst().substring(1,args.getFirst().length()))) {
            error("Syntax error.The first argument should not be an OPname.");
            System.exit(-1);
        } else if(isfunc(args.getLast())) {
            String word = getword(args.getFirst());
            if(!funcnamespace.contains(word)){
                funcnamespace.add(word);
            }
            namespace.put(word,args.getLast());
        } else {
            String word = getword(args.getFirst());
            if(hidden_namespace.containsKey(word)){
                namespace.put(word, args.getLast());
                hidden_namespace.remove(word);
            }else {
                namespace.put(word, args.getLast());
            }
        }
        return null;
    }

    String thing(LinkedList<String> args) {
        if (!sys_isword(args.getFirst()) || isOP(args.getFirst())) {
            error("Syntax error.The argument has to be a word, but get [" + args.getFirst() + "]");
            System.exit(-1);
        } else if (!isinnamespace(args.getFirst())&&!isinfunc(args.getFirst())) {
            error("The word " + args.getFirst()+" is not bounded with any value or function.");
            System.exit(-1);
        } else {
            String word = getword(args.getFirst());
            return namespace.get(word);
        }
        return null;
    }

    String thing(String keyname) {
        if (!sys_isword(keyname) || isOP(keyname)) {
            error("Syntax error.The argument has to be a word, but get [" + args.getFirst() + "]");
            System.exit(-1);
        } else if (!isinnamespace(keyname)&&!isinfunc(keyname)) {
            error("The word " + keyname +" is not bounded with any value or function.");
            System.exit(-1);
        } else {
            return namespace.get(keyname);
        }
        return null;
    }

    String colon(LinkedList<String> args) {
//        if (!sys_isword(args.getFirst()) || isOP(args.getFirst())) {
//            error("Syntax error.The argument has to be a word, but get [" + args.getFirst() + "]");
//            System.exit(-1);
//        }else if (!isinnamespace(args.getFirst())&&!isinfunc(args.getFirst())){
//            error("The word " + args.getFirst()+" is not bounded with any value or function.");
//            System.exit(-1);
//        }else {
//            String word = getword(args.getFirst());
//            return namespace.get(word);
//        }
        return thing(args);
    }

    String erase(LinkedList<String> args) {
        if (!isinnamespace(args.getFirst())) {
            error("The word [ " + args.getFirst() + " ] is not bounded with any value.");
        } else if (isOP(args.getFirst())) {
            error("OPnames can't be bounded with any value.");
        } else {
            String word = getword(args.getFirst());
            namespace.remove(word);
            while(funcnamespace.contains(word)){
                funcnamespace.remove(word);
            }
        }
        return null;
    }


    String erase(String keyname) {
        if (!isinnamespace(keyname)) {
            error("The word [ " + keyname + " ] is not bounded with any value.");
        } else if (isOP(keyname)) {
            error("OPnames can't be bounded with any value.");
        } else {
            namespace.remove(keyname);
            if(funcnamespace.contains(keyname)){
                funcnamespace.remove(keyname);
            }
        }
        return null;
    }

    String isname(LinkedList<String> args) {
        if (isinnamespace(args.getFirst())||isinfunc(args.getFirst())) {
            return "true";
        } else {
            return "false";
        }
    }

    String read(LinkedList<String> args) {
        Scanner s = new Scanner(System.in);
        System.out.print(">");
        String line = s.next();
        return line;
    }

    String readlinst(LinkedList<String> args) {
        Scanner s = new Scanner(System.in);
        System.out.print(">");
        String line=s.nextLine();
        return "[" + line + "]";
    }

    String add(LinkedList<String> args) {
        if (!sys_isnumber(args.getFirst()) || !sys_isnumber(args.getLast())) {
            error("Arguments should be numbers.");
            System.exit(1);
        } else {
            String s = getnumber(args.getFirst()) + getnumber(args.getLast()) + "";
            return s;
        }
        return null;
    }

    String sub(LinkedList<String> args) {
        if (!sys_isnumber(args.getFirst()) || !sys_isnumber(args.getLast())) {
            error("Arguments should be numbers.");
            System.exit(1);
        } else {
            String s = getnumber(args.getFirst()) - getnumber(args.getLast()) + "";
            return s;
        }
        return null;
    }

    String mul(LinkedList<String> args) {
        if (!sys_isnumber(args.getFirst()) || !sys_isnumber(args.getLast())) {
            error("Arguments should be numbers.");
            System.exit(1);
        } else {
            String s = getnumber(args.getFirst()) * getnumber(args.getLast()) + "";
            return s;
        }
        return null;
    }

    String div(LinkedList<String> args) {
        if (!sys_isnumber(args.getFirst()) || !sys_isnumber(args.getLast())) {
            error("Arguments should be numbers.");
            System.exit(1);
        } else {
            float f1 = getnumber(args.getFirst());
            float f2 = getnumber(args.getLast());
            if(f2<1e-4){
                error("Div zero error.");
                System.exit(1);
            }
            float f=f1/f2;
            String s=f+"";
            return s;
        }
        return null;
    }

    String mod(LinkedList<String> args) {
        if (!sys_isnumber(args.getFirst()) || !sys_isnumber(args.getLast())) {
            error("Arguments should be numbers.");
            System.exit(1);
        } else {
            String s = getnumber(args.getFirst()) % getnumber(args.getLast()) + "";
            return s;
        }
        return null;
    }

    String and(LinkedList<String> args) {
        if (!sys_isbool(args.getFirst()) || !sys_isbool(args.getLast())) {
            error("Arguments should be boolean.");
            System.exit(1);
        } else {
            boolean b = Boolean.parseBoolean(args.getFirst())&&Boolean.parseBoolean(args.getLast());
            return b+"";
        }
        return null;
    }

    String or(LinkedList<String> args) {
        if (!sys_isbool(args.getFirst()) || !sys_isbool(args.getLast())) {
            error("Arguments should be boolean.");
            System.exit(1);
        } else {
            boolean b = Boolean.parseBoolean(args.getFirst())||Boolean.parseBoolean(args.getLast());
            return b+"";
        }
        return null;
    }

    String not(LinkedList<String> args) {
        if (!sys_isbool(args.getFirst())) {
            error("Argument should be boolean.");
            System.exit(1);
        } else {
            boolean b = !Boolean.parseBoolean(args.getFirst());
            return b+"";
        }
        return null;
    }

    String eq(LinkedList<String> args) {
        String arg1=args.getFirst();
        String arg2=args.getLast();
        if(sys_isnumber(arg1)&&sys_isnumber(arg2)){
            float a1=Float.parseFloat(arg1);
            float a2=Float.parseFloat(arg2);
            return a1==a2?"true":"false";
        }else if(arg1.equals(arg2)){
            return "true";
        } else {
            return "false";
        }
    }

    String gt(LinkedList<String> args) {
        String arg1=args.getFirst();
        String arg2=args.getLast();
        if(sys_isnumber(arg1)&&sys_isnumber(arg2)){
            float a1=Float.parseFloat(arg1);
            float a2=Float.parseFloat(arg2);
            return a1>a2?"true":"false";
        } else {
            error("'gt' should be carried out between two numbers.");
            System.exit(1);
        }
        return null;
    }

    String lt(LinkedList<String> args) {
        String arg1=args.getFirst();
        String arg2=args.getLast();
        if(sys_isnumber(arg1)&&sys_isnumber(arg2)){
            float a1=Float.parseFloat(arg1);
            float a2=Float.parseFloat(arg2);
            return a1<a2?"true":"false";
        } else {
            error("'lt' should be carried out between two numbers.");
            System.exit(1);
        }
        return null;
    }

    String repeat(LinkedList<String> args) {        //the arguments passed in is the two actual terminals
        String arg1=args.getFirst();
        String arg2=args.getLast();
        float a1=0;
        if(sys_isnumber(arg1)){         //第一个参数为重复次数
            a1=Float.parseFloat(arg1);
        } else {
            error("The first argument should be a number. (repeat times)");
            System.exit(1);
        }
        if(a1<=0) return null;
        String repeatcmd=arg2.substring(1,arg2.length()-1);
        String retline="";
        for(int i=0;i<a1;i++){      //
            retline+=repeatcmd+" ";
        }
        return "[ "+ retline + " ]";    //The interpreter will automatically interprete a [xxx] list
    }

    String isnumber(LinkedList<String> args){
        if(sys_isnumber(args.getFirst())) return "true";
        else return "false";
    }

    String isword(LinkedList<String> args) {
        if(sys_isword(args.getFirst())) return "true";
        else return "false";
    }

    String islist(LinkedList<String> args) {
        if(sys_islist(args.getFirst())) return "true";
        else return "false";
    }

    String isbool(LinkedList<String> args) {
        if(sys_isbool(args.getFirst())) return "true";
        else return "false";
    }

    String isempty(LinkedList<String> args){
        String element=args.getFirst();
        boolean res=false;
        if(sys_islist(element)){
            element=element.trim();
            element=element.substring(1,element.length()-1); //remove the '[' and ']'
            res=(element.trim().isEmpty());
        }else if(sys_isword(element)){
            res=element.substring(1,element.length()).isEmpty();
        }else{
            error("The arguments for 'isempty' should either be a word or a list.");
            System.exit(-1);
        }
        if(res) return "true";
        else return "false";
    }

    String random(LinkedList<String> args) {
        String element = args.getFirst();
        String ret = "";
        if (!sys_isnumber(element)) {
            error("Error.The argument should be a number.");
            System.exit(-1);
        } else {
            float range = Float.parseFloat(element);
            float randseed = (float) (Math.random());
            ret = randseed * range + "";
        }
        return ret;
    }

    String sqrt(LinkedList<String> args){
        String element=args.getFirst();
        String ret="";
        if(!sys_isnumber(element)){
            error("Error.The argument should be a number.");
            System.exit(-1);
        }else{
            double num=Double.parseDouble(element);
            ret=Math.sqrt(num*num)+"";
        }
        return ret;
    }

    String mua_int(LinkedList<String> args){
        String element=args.getFirst();
        String ret="";
        if(!sys_isnumber(element)){
            error("Error.The argument should be a number.");
            System.exit(-1);
        }else{
            double num=Double.parseDouble(element);
            ret=(int)Math.floor(num)+"";
        }
        return ret;
    }

    String wait(LinkedList<String> args){
        String element=args.getFirst();
        if(!sys_isnumber(element)){
            error("Error.The argument should be a number.");
            System.exit(-1);
        }else{
            double num=Double.parseDouble(element);
            num=Math.floor(num);
            long msec=(long)num;
            try{
                Thread.sleep(msec);
            }catch (Exception e){
                error("Error. Exception when thread trying to sleep.");
                System.exit(-1);
            }
        }
        return null;
    }

    String erall(LinkedList<String> args){
        Iterator iterator=namespace.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object keyname = entry.getKey();
            if(hidden_namespace.containsKey(keyname)){
                continue;
            }
            iterator.remove();
            if(funcnamespace.contains(keyname)){
                funcnamespace.remove(keyname);
            }
        }
        return null;
    }

    String poall(LinkedList<String> args){
        System.out.println("Name list\n"+"=====================");
        for (String keyname:
                this.namespace.keySet()) {
            if(this.hidden_namespace.containsKey(keyname)||this.hidden_funcnamespace.contains(keyname)){
                continue;
            }
            System.out.println(keyname);
        }
        return null;
    }

    String word(LinkedList<String> args){           //TODO
        String arg1=args.getFirst();
        String arg2=args.getLast();
        if(!sys_isword(arg1)){
            error("'word' should be carried out between word and other type of arguments. <word> <word|number|bool> ");
            System.exit(-1);
        }
        arg1=arg1.substring(1,arg1.length()); // get rid of the "
        if(sys_isword(arg2)){
            arg2=arg2.substring(1,arg2.length());
        }else if(!(sys_isnumber(arg2)||sys_isbool(arg2))){
            error("'word' should be carried out between these typed of arguments. <word> <word|number|bool> ");
            System.exit(-1);
        }
        return arg1+arg2;
    }

    String mua_if(LinkedList<String> args){
        String arg1=args.get(0);
        String arg2=args.get(1);
        String arg3=args.get(2);
        if(!sys_isbool(arg1)){
            error("The first argument should be a boolean value. Either 'true' or 'false'(case sensitive).");
            System.exit(-1);
        }
        if(!sys_islist(arg2)||!sys_islist(arg3)){
            error("The second and third argument should be a list.");
            System.exit(-1);
        }
        if(arg1.equals("true")){
            return arg2;
        }else{
            return arg3;
        }
    }

    String sentence(LinkedList<String> args){
        String arg1=args.get(0);
        String arg2=args.get(1);
        if(sys_islist(arg1)){               //take out the actual value
            arg1=arg1.trim();
            arg1=arg1.substring(1,arg1.length()-1);     //get rid of []
        }else if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());       //get rid of "
        }
        if(sys_islist(arg2)){               //take out the actual value
            arg2=arg2.trim();
            arg2=arg2.substring(1,arg2.length()-1);     //get rid of []
        }else if(sys_isword(arg2)){
            arg2=arg2.substring(1,arg2.length());       //get rid of "
        }
        return "["+arg1+" "+arg2+"]";
    }

    String list(LinkedList<String> args){
        String arg1=args.get(0);
        String arg2=args.get(1);
        if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());       //get rid of "
        }
        if(sys_isword(arg2)){
            arg2=arg2.substring(1,arg2.length());       //get rid of "
        }
        return "["+arg1+" "+arg2+"]";
    }

    String join(LinkedList<String> args){
        String arg1=args.get(0);
        String arg2=args.get(1);
        if(!sys_islist(arg1)){
            error("The first argument should be a list.");
            System.exit(-1);
        }
        arg1=arg1.trim();
        arg1=arg1.substring(1,arg1.length()-1);
        if(sys_isword(arg2)){
            arg2=arg2.substring(1,arg2.length());       //get rid of "
        }
        return "["+arg1+" "+arg2+"]";
    }

    String first(LinkedList<String> args){
        String ret="";
        String arg1=args.get(0);
        if(sys_islist(arg1)){
            arg1=arg1.trim();
            arg1=arg1.substring(1,arg1.length()-1);
            arg1=arg1.trim();
            if(arg1.equals("")){
                error("Error. The list is empty. It is illegal in this operation.");
                System.exit(-1);
            }else{
                ret=arg1.split("\\s+")[0];
            }
        }else if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());
            try{
                ret=arg1.charAt(0)+"";
            }catch (Exception e){
                error("Error. The word is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else{
            error("Error. The argument should either be a word or a list.");
            System.exit(-1);
        }
        return ret;
    }

    String last(LinkedList<String> args){
        String ret="";
        String arg1=args.get(0);
        if(sys_islist(arg1)){
            arg1=arg1.trim();
            arg1=arg1.substring(1,arg1.length()-1);
            try{
                ret=arg1.split("\\s+")[arg1.split("\\s+").length-1];
            }catch (Exception e){
                error("Error. The list is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());
            try{
                ret=arg1.charAt(arg1.length()-1)+"";
            }catch (Exception e){
                error("Error. The word is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else{
            error("Error. The argument should either be a word or a list.");
            System.exit(-1);
        }
        return ret;
    }

    String butfirst(LinkedList<String> args){
        String ret="";
        String arg1=args.get(0);
        if(sys_islist(arg1)){
            arg1=arg1.trim();
            arg1=arg1.substring(1,arg1.length()-1);
            arg1=arg1.trim();
            try{
                ret=Arrays.asList(arg1.split("\\s+")).subList(1,arg1.split("\\s+").length)+"";
            }
            catch (Exception e){
                error("Error. The list is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());
            try{
                ret=arg1.substring(1,arg1.length());
            }catch (Exception e){
                error("Error. The word is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else{
            error("Error. The argument should either be a word or a list.");
            System.exit(-1);
        }
        return ret;
    }

    String butlast(LinkedList<String> args){
        String ret="";
        String arg1=args.get(0);
        if(sys_islist(arg1)){
            arg1=arg1.trim();
            arg1=arg1.substring(1,arg1.length()-1);
            arg1=arg1.trim();
            try{
                ret="["+Arrays.asList(arg1.split("\\s+")).remove(arg1.split("\\s+").length-1)+"]";
            }catch (Exception e){
                error("Error. The list is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else if(sys_isword(arg1)){
            arg1=arg1.substring(1,arg1.length());
            try{
                ret=arg1.substring(0,arg1.length()-1);
            }catch (Exception e){
                error("Error. The word is empty. It is illegal in this operation.");
                System.exit(-1);
            }
        }else{
            error("Error. The argument should either be a word or a list.");
            System.exit(-1);
        }
        return ret;
    }

    String save(LinkedList<String> args){
        String arg1=args.get(0);
        if(!sys_isword(arg1)){
            error("Error. The argument should be a word. Save \"a means save to the file named a" );
            System.exit(-1);
        }else{
            arg1=arg1.substring(1,arg1.length());
            try{
                FileWriter fw=new FileWriter(arg1);
                for (String keyname:namespace.keySet()
                     ) {
                    if(hidden_namespace.containsKey(keyname)){
                        continue;
                    }
                    fw.write(keyname+" "+namespace.get(keyname)+" ");
                }
                fw.close();
            }catch(IOException e){
                error("Error. IO Exception occured.");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return null;
    }

    String load(LinkedList<String> args){
        String arg1=args.get(0);
        if(!sys_isword(arg1)){
            error("Error. The argument should be a word. Save \"a means save to the file named a" );
            System.exit(-1);
        }else{
            arg1=arg1.substring(1,arg1.length());
            try{
                BufferedReader fr=new BufferedReader(new FileReader(arg1));
                String line=fr.readLine();
                fr.close();
                LinkedList<String> tmp=new LinkedList<String>(Arrays.asList(line.split("\\s+")));
                int i=0;
                while(i<tmp.size()-1){
                    namespace.put(tmp.get(i),tmp.get(i+1));
                    i+=2;
                }
            }catch(IOException e){
                error("Error. IO Exception occured.");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return null;
    }





    public boolean isOP(String element) {
        return OP.contains(element);
    }

    protected boolean sys_isnumber(String s) {
        return s.matches("^[-]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    protected boolean sys_isword(String s) {
        return (s.startsWith("\"")&&(s.charAt(1)=='_'||Character.isLetter(s.charAt(1)))&&!s.contains("[")&&!s.contains("]")&&!s.contains(":"));
    }

    protected boolean sys_isbool(String s) {
        if(s.equals("true")||s.equals("false")) return true;
        return false;
    }

    protected boolean isinnamespace(String s) {
        if (s.startsWith("\"") && namespace.containsKey(s.substring(1, s.length()))) return true;
        return false;
    }

    public boolean isinfunc(String s){
        if(funcnamespace.contains(s)) return true;
        return false;
    }

    public boolean sys_islist(String s) {
        if(s.startsWith("[")&&s.endsWith("]")) return true;
        return false;
    }

    public boolean sys_ismath(String s){
        return (s.startsWith("(")&&s.endsWith(")"));
    }

    boolean isoperator(String s){
        if(s.contains("+")||s.contains("-")||s.contains("*")||s.contains("/")||s.contains("%")||s.contains("(")||s.contains(")")) return true;
        return  false;
    }

    public boolean isfunc(String s){
        if(!sys_islist(s)) return false;
        int left,right,pair;
        left=right=pair=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='[') left++;
            else if(s.charAt(i)==']'){
                right++;
                if(left-right==1) pair++;
            }
        }
        if(left==right&&pair==2) return true;
        return false;
    }

    public String GetArgDefine(String s){
        int left,right;
        left=right=0;
        String ret="";
        for(int i=1;i<s.length();i++){
            if(s.charAt(i)=='[') left++;
            else if(s.charAt(i)==']')right++;
            if(left>0){
                if(s.charAt(i)==' '){
                    while(s.charAt(i)==' '&&i<s.length()-1){
                        i++;
                    }
                    i--;
                    ret+=" ";
                }
                if(left!=right) {
                    if(s.charAt(i)=='['||s.charAt(i)==']') ret+=" ";
                    ret=ret+s.charAt(i);
                    if(s.charAt(i)=='['||s.charAt(i)==']') ret+=" ";
                }
                else break;
            }
        }
        return ret+" ]";
    }

    public String getfuncmd(String s){
        int left,right,pair;
        left=right=pair=0;
        String ret="";
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)==' '){
                while(s.charAt(i)==' '&&i<s.length()-1){
                    i++;
                }
                ret+=" ";
            }
            if(pair==1){
                if(s.charAt(i)=='['||s.charAt(i)==']') ret+=" ";
                ret=ret+s.charAt(i);
                if(s.charAt(i)=='['||s.charAt(i)==']') ret+=" ";
            }
            if(s.charAt(i)=='[') {
                left++;
                if(left >=2 &&((left-right)==1)) {
                    pair++;
                }
            }
            else if(s.charAt(i)==']') {
                right++;
                if(left >=2 &&((left-right)==1)) {
                    pair++;
                }
            }
        }
        return ret;
    }

    protected String getword(String s) {
        return s.substring(1, s.length());
    }

    protected static float getnumber(String s) {
        return Float.parseFloat(s);
    }

    public static LinkedList<String> getlist(String s){
        String element;
        LinkedList<String> queue=new LinkedList<String>();
        Scanner ret=new Scanner(s.substring(1,s.length()-1));//throw away the '[' and ']'
        while(ret.hasNext()){
            element=ret.next();
            queue.add(element);
        }
        return queue;
    }


    protected static void error(String err) {
        System.out.println(err);
        System.exit(-1);
    }
}

class FuncInterpreter extends Interpreter{
    List<String> OP = new ArrayList<String>(Arrays.asList("make", "thing", "erase",
            "isname", "print", "read", "readlinst", ":", "add", "sub", "div", "mul",
            "mod", "eq", "gt", "lt", "and", "or", "not", "repeat","output","stop",
            "isnumber","isword","islist","isbool","isempty","random","sqrt","int","wait",
            "erall","poall","export","word","if","sentence","list","join","first","last",
            "butfirst","butlast","save","load"));

    public FuncInterpreter(LinkedList<String> cmds, Map<String, String> namespace, List<String> funcnamespace,MuaParser parent) {
        this.cmds = cmds;
        this.namespace = namespace;
        this.funcnamespace=funcnamespace;
        this.parent=parent;
        _initiate_hidden(this.namespace,this.funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public FuncInterpreter(LinkedList<String> cmds, Map<String, String> namespace, List<String> funcnamespace) {
        this.cmds = cmds;
        this.namespace = namespace;
        this.funcnamespace=funcnamespace;
        parent=new MuaParser(cmds,this);
        _initiate_hidden(this.namespace,this.funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public FuncInterpreter(LinkedList<String> cmds, MuaParser parent) {
        this.cmds = cmds;
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        this.parent=parent;
        _initiate_hidden(this.namespace,this.funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public FuncInterpreter(LinkedList<String> cmds) {
        this.cmds = cmds;
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        parent=new MuaParser(cmds,this);
        _initiate_hidden(this.namespace,this.funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    public FuncInterpreter(MuaParser parent) {
//        this.cmds = parent.cmds;
        cmds=new LinkedList<>();
        this.namespace=new HashMap<String, String>();
        this.funcnamespace =new ArrayList<String>();
        this.parent=parent;
        _initiate_hidden(this.namespace,this.funcnamespace);
        namespace.put("pi","3.14159");
        namespace.put("run","[[][]]");
        funcnamespace.add("run");
    }

    @Override
    void _initiate_hidden(Map<String,String> HID_NAME,List<String> HID_FUNC){
        try{
            this.namespace.putAll(HID_NAME);
            this.funcnamespace.addAll(HID_FUNC);
        }catch (Exception ignore){
            //ignore
        }
        return;
    }

    @Override
    String doOP() {
        String ret="";
        switch (op) {
            case "output":
                ret = output(args);
                break;
            case "export":
                ret=export(args);
                break;
            case"stop":
                ret=null;
                break;
            default:
                ret=super.doOP();
                break;
        }
        return ret;
    }

    @Override
    public String interpret() {
        if(cmds.isEmpty()) return null;
        if(isinfunc(cmds.getFirst())){
            funcname=cmds.removeFirst();
            FuncInterpreter funcInp= new FuncInterpreter(cmds,parent);
            funcInp.namespace.put(funcname,namespace.get(funcname));        //to implement recursive procedure
            if(!funcInp.funcnamespace.contains(funcname)){
                funcInp.funcnamespace.add(funcname);
            }
            funcInp.interpret();
        }else if (!cmds.isEmpty()) {
//            String first=cmds.getFirst();
            if (isOP(cmds.getFirst())) {
                GetOPArgs();
                //Arguments preparation done. Time to carry out specific operation.
                String ret = null;
                ret=doOP();
                if (ret != null && !ret.equals("")) {
                    cmds.addFirst(ret);
                }
            }else if(!cmds.isEmpty()&&sys_islist(cmds.getFirst())){
                String newline=cmds.removeFirst();
                newline=newline.substring(1,newline.length()-1);
                MuaParser tmpParser=new MuaParser();
                tmpParser.addline(newline);
                tmpParser.cmds.addAll(cmds);
                cmds=tmpParser.cmds;
                if(!cmds.isEmpty()) interpret();
            }else if(!cmds.isEmpty()&&sys_ismath(cmds.getFirst())){
                MyCalculator cal=new MyCalculator(cmds.removeFirst(),namespace,funcnamespace);
                cmds.addFirst(cal.compute()+"");
            } else {
                return null;
            }
        }
        args.clear();
        return null;
    }

    @Override
    void GetOPArgs() {
        if(isWaiting==0){
            this.op = cmds.removeFirst();
            if (onearg.contains(op)) {
                isWaiting = 1;
            } else if (twoargs.contains(op)) {
                isWaiting = 2;
            } else if(op.equals("if")){
                isWaiting = 3;
            } else {
                isWaiting = 0;
            }
        }
        while (isWaiting > 0) {
            if (cmds.isEmpty() && !op.equals("read") && !op.equals("readlinst")) {
                String line = parent.GetInput();
                parent.parse(line);
            }
            String element = cmds.getFirst();
            if (isOP(element)) {
                Interpreter nextlevel = new Interpreter(cmds, namespace, funcnamespace, parent);
                nextlevel.interpret();
            }else if(sys_ismath(element)){
                MyCalculator cal=new MyCalculator(cmds.removeFirst(),namespace,funcnamespace);
                cmds.addFirst(cal.compute()+"");
            }
            if (!cmds.isEmpty()) {
                args.add(cmds.removeFirst());
                isWaiting--;
            }
        }
    }

    @Override
    public boolean isOP(String element) {
        return this.OP.contains(element);
    }

    @Override
    String poall(LinkedList<String> args){
        System.out.println("Name list\n"+"=====================");
        for (String keyname:
                this.namespace.keySet()) {
            if(this.hidden_namespace.containsKey(keyname)||this.hidden_funcnamespace.contains(keyname)){
                continue;
            }
            System.out.println(keyname);
        }
        return null;
    }

    String output(LinkedList<String> args) {
        hidden_namespace.put("output",args.getFirst());
        return null;
    }


    String export(LinkedList<String> args){
        for(String key :namespace.keySet()) {
            if(hidden_namespace.containsKey(key)){    //output and other params are not included
                continue;
            }else{
                parent.Inp.namespace.put(key,namespace.get(key));
//                parent.Inp.namespace.put(key,namespace.get(key));
            }
        }
        for(String funcname:funcnamespace){
            if(!parent.Inp.funcnamespace.contains(funcname)){
                parent.Inp.funcnamespace.add(funcname);
            }
        }
        return null;
    }
}