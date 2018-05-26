import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

//parser
public class MuaParser {
    LinkedList<String> cmds;
    Interpreter Inp;
    String element;

    public MuaParser(LinkedList<String> commandline, Interpreter myInp) {
        cmds = commandline;
        Inp = myInp;
    }

    public MuaParser() {
        cmds = new LinkedList<String>();
        Inp = new Interpreter(cmds, this);
        Inp.cmds=this.cmds;
    }

    boolean GetAndDoInput(){
        boolean terminate=false;            //terminate the program or not
        boolean cmdsEmpty=false;
        String line=GetInput();
        if(line.equals("exit")||line.equals("quit")){
            terminate=true;                 //terminate the program
        }
        if(!terminate){
            cmdsEmpty=addline(line);
        }
        if(!cmdsEmpty){
            System.out.println("Error. Unknown OP detected. ===> "+ cmds.getFirst());
            terminate=true;
        }
        return terminate;
    }

    String GetInput(){
        Scanner s = new Scanner(System.in);
        System.out.print(">>>");
        String line=s.nextLine();
        if(line.equals("exit")||line.equals("quit")){
            System.exit(1);
        }
//        s.close();
        return line;             //get a line from the standard input
    }

    boolean Interpretable(LinkedList<String> C,Interpreter I){
        return !I.cmds.isEmpty() && (I.isOP(I.cmds.getFirst()) || I.isinfunc(I.cmds.getFirst()) ||
                I.sys_islist(I.cmds.getFirst())|| I.sys_ismath(I.cmds.getFirst()) || (this.Inp.isWaiting>0))  ;
    }

    public boolean addline(String line){
        parse(line);
        while(Interpretable(this.cmds,this.Inp)) {
            this.Inp.interpret();
        }
        return cmds.isEmpty();
    }

    public void parse(String line) {
        Scanner sentence = new Scanner(line);
        while (sentence.hasNext()) {
            element = sentence.next();
            if (!Inp.isinfunc(element)) {
                if (element.startsWith("//")) {
                    break;
                }else if (element.startsWith(":") && element.length() > 1) {                        //seperate ':' and word
                    cmds.add(":");
                    element = "\""+element.substring(1, element.length());
                }else if (element.startsWith("[")) {                                //take out the list
                    element = parseList(element,sentence);
                }else if(element.startsWith("(")){              //take out the math line
                    element = parseMath(element,sentence);
                }
                cmds.add(element);
            }else if(Inp.isinfunc(element)){
                parseFunc(element,sentence);
            }else{
                Interpreter.error("Error. Syntax error.");
                System.exit(-1);
            }
        }
    }

    protected String parseList(String element,Scanner sentence){
        int list = 1;
        for (int i = 1; i < element.length(); i++) {        //parse the element before the first space
            if (element.charAt(i) == '[') list++;
            else if (element.charAt(i) == ']') list--;
        }
        while(list>0){                  //add the next elements into the string of list until brackets are well paired
            if(sentence.hasNext()){
                String next = sentence.next();
                element = element + " " + next;
                for (int i = 0; i < next.length(); i++) {       //calculate '[' and ']'
                    if (next.charAt(i) == '[') list++;
                    else if (next.charAt(i) == ']') list--;
                }
            }else{
                String listline=GetInput();
                element+=" "+parseList(listline,list);
                list=0;
            }
        }
        return element;
    }

    protected String parseList(String line,int mlist){    //a line of input and an empty scanner
        int list = mlist;
        Scanner mSentence=new Scanner(line);
        String element=mSentence.next();
        for (int i = 0; i < element.length(); i++) {        //parse the element before the first space
            if (element.charAt(i) == '[') list++;
            else if (element.charAt(i) == ']') list--;
        }
        while(list>0){                  //add the next elements into the string of list until brackets are well paired
            if(mSentence.hasNext()){
                String next = mSentence.next();
                element = element + " " + next;
                for (int i = 0; i < next.length(); i++) {       //calculate '[' and ']'
                    if (next.charAt(i) == '[') list++;
                    else if (next.charAt(i) == ']') list--;
                }
            }else{
                String listline=GetInput();
                element+=" "+parseList(listline,list);
                break;
            }
        }
//        if(mSentence.hasNext()){      //if remaing contents   TODO
//            sentence=mSentence;            //change the reference?
//        }
        return element;
    }

    protected String parseMath(String element,Scanner sentence) {
        int prt = 1;
        for (int i = 1; i < element.length(); i++) {
            if (element.charAt(i) == '(') prt++;
            else if (element.charAt(i) == ')') prt--;
        }
        while (prt > 0) {
            if (sentence.hasNext()) {
                String next = sentence.next();
                element = element + " " + next;
                for (int i = 0; i < next.length(); i++) {
                    if (next.charAt(i) == '(') prt++;
                    else if (next.charAt(i) == ')') prt--;
                }
            } else {
                String mathline = GetInput();
                element += " " + parseMath(mathline,prt);
                prt = 0;
            }
        }
        return element;
    }

    protected String parseMath(String line,int mPrt) {
        int prt = mPrt;
        Scanner mSentence = new Scanner(line);
        String element = mSentence.next();
        for (int i = 0; i < element.length(); i++) {
            if (element.charAt(i) == '(') prt++;
            else if (element.charAt(i) == ')') prt--;
        }
        while (prt > 0) {
            if (mSentence.hasNext()) {
                String next = mSentence.next();
                element = element + " " + next;
                for (int i = 0; i < next.length(); i++) {
                    if (next.charAt(i) == '(') prt++;
                    else if (next.charAt(i) == ')') prt--;
                }
            } else {
                String mathline = GetInput();
                element += " " + parseMath(mathline, prt);
                break;
            }
        }
        return element;
    }
//
//                if(mathsentence.hasNext()&&prt==0){            //haiyouduodejiunachulaizaiparseyiju TODO
//                    String prt_extra_line=mathsentence.nextLine();
//                    parse(prt_extra_line);
//                }

    protected void parseFunc(String element,Scanner sentence){        //deal with functions
        int ArgNum;
        String funcdefine = Inp.namespace.get(element);         //the list
        FuncInterpreter funcInp=new FuncInterpreter(this);
//        FuncInterpreter funcInp = funcParser.Inp;
        funcInp.hidden_namespace.put(element, funcdefine);             //add itself in
        funcInp.hidden_funcnamespace.add(element);
        funcInp._initiate_hidden(funcInp.hidden_namespace,funcInp.hidden_funcnamespace);
        MuaFunctionParser funcParser = new MuaFunctionParser(funcInp);
//        funcInp._initiate_hidden(null,funcInp.funcnamespace);

        String ArgDefine_Sring = funcInp.GetArgDefine(funcdefine); //arglist
        ArgDefine_Sring = ArgDefine_Sring.trim();
        List<String> ArgDefine_List;
        if(ArgDefine_Sring.substring(1,ArgDefine_Sring.length()-1).trim().equals("")){     //if empty
            ArgNum=0;
            ArgDefine_List=null;
        }else{
            ArgDefine_List= new LinkedList<String>(Arrays.asList(ArgDefine_Sring.substring(1, ArgDefine_Sring.length() - 1).trim().split("\\s+")));
            ArgNum = ArgDefine_List.size();
        }
        if(ArgNum==0){
            String CmdDefine_String = funcInp.getfuncmd(funcdefine);   //fetch the body
            CmdDefine_String = CmdDefine_String.trim();
            CmdDefine_String = CmdDefine_String.substring(1, CmdDefine_String.length() - 1).trim();      //outermost '['
            funcParser.addline(CmdDefine_String);
            if (funcInp.hidden_namespace.containsKey("output")) {
                cmds.add(funcInp.hidden_namespace.get("output"));
//                Inp.interpret();
            }
        }else{
            int paranum = 0;
            LinkedList<String> parameter = new LinkedList<String>();        //actual params
            LinkedList<String> paramcmds=new LinkedList<String >();
            Interpreter paramInp=new Interpreter(paramcmds,Inp.namespace,Inp.funcnamespace,this);
            MuaParser paramParser=new MuaParser(paramcmds,paramInp);
            while(sentence.hasNext()) {
                String nextEle=sentence.next();
                boolean firstIsOP = paramParser.addline(nextEle);
                if(!firstIsOP&&paranum<ArgNum){
                    parameter.add(paramInp.cmds.removeFirst());
                    paranum++;
                }
                if(paranum==ArgNum){
                    break;
                }
            }
            if (ArgNum != parameter.size()) {
                System.out.println("Wrong input. Parameter numbers error.Need "+ArgNum+", but get "+paranum+" parameters : [ "+parameter+" ]");
                System.exit(-1);
            }
            String CmdDefine_String = funcInp.getfuncmd(funcdefine);
            CmdDefine_String = CmdDefine_String.trim();
            CmdDefine_String = CmdDefine_String.substring(1, CmdDefine_String.length() - 1).trim();
            for (int i = 0; i < ArgNum; i++) {
                funcInp.hidden_namespace.put(ArgDefine_List.get(i), parameter.get(i));
            }
            funcInp._initiate_hidden(funcInp.hidden_namespace,funcInp.hidden_funcnamespace);
            funcParser.addline(CmdDefine_String);
            if (funcInp.hidden_namespace.containsKey("output")) {
                cmds.add(funcInp.hidden_namespace.get("output"));
                Inp.interpret();
            }
        }
    }
}


class MuaFunctionParser extends MuaParser {
    FuncInterpreter Inp;

    public MuaFunctionParser(LinkedList<String> commandline, FuncInterpreter myInp) {
        cmds = commandline;
        Inp = myInp;
    }

    public MuaFunctionParser(FuncInterpreter myInp) {
        cmds = myInp.cmds;
        Inp = myInp;          //there must be an inp passed in in order to initialte the cmds
        Inp.cmds=cmds;
    }

    @Override
    boolean Interpretable(LinkedList<String> C,Interpreter I){
        return !I.cmds.isEmpty() && !I.cmds.getFirst().equals("stop") && ( I.isOP(I.cmds.getFirst()) || I.isinfunc(I.cmds.getFirst())
                || I.sys_islist(I.cmds.getFirst())|| I.sys_ismath(I.cmds.getFirst()) ||(Inp.isWaiting>0) );
    }

    @Override
    public boolean addline(String line){
        parse(line);
        while(Interpretable(cmds,Inp)) {
            Inp.interpret();
        }
        return Inp.cmds.isEmpty();
    }


    public void parse(String line) {
        Scanner sentence = new Scanner(line);
        while (sentence.hasNext()) {
            element = sentence.next();
            if (!Inp.isinfunc(element)) {
                if (element.startsWith("//")||element.startsWith("stop")) {   //if encountered '//',skip the following contents of the line.
                    break;
                }else if (element.startsWith(":") && element.length() > 1) {                        //seperate ':' and word
                    cmds.add(":");
                    element = "\""+element.substring(1, element.length());
                }else if (element.startsWith("[")) {                                //take out the list
                    element = parseList(element,sentence);
                }else if(element.startsWith("(")){              //take out the math line
                    element = parseMath(element,sentence);
                }
                cmds.add(element);
            }else{
                parseFunc(element,sentence);
            }
        }
    }

}

