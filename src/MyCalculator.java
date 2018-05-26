import java.util.*;
import java.util.List;

//Recursively calculate math expressions
public class MyCalculator{
    Map<String, String> namespace = new HashMap<String, String>();
    List<String> funcnamespace = new ArrayList<String>();

    String inputstring; //
    char operator;      //
    int i;              //

    public MyCalculator(String in,Map<String, String> namespace,List<String> funcnamespace){
        this.namespace=namespace;
        this.funcnamespace=funcnamespace;
        inputstring=in.trim();                  //get rid of the space
        inputstring=inputstring.substring(1,inputstring.length()-1);    //outermost '('
    }

    float compute(){
        operator=inputstring.charAt(0);
        float result=expr0();   //the entrance of recursion
        return result;
    }

    float expr0(){
        float term1=expr1();    //
        while(i<inputstring.length()){
            while(inputstring.charAt(i)==' '&&i<inputstring.length()) i++;  //skip spaces
            operator=inputstring.charAt(i);
            if((operator=='+')||operator=='-'){     //judge
                if (operator == '+') {
                    i++;
                    term1 += expr1();               //
                } else {
                    i++;
                    term1 -= expr1();
                }
            }else{
                break;
            }
        }
        return term1;
    }

    private float expr1(){
        float term2=expr2();
        while(i<inputstring.length()){
            while(inputstring.charAt(i)==' '&&i<inputstring.length()) i++;
            operator=inputstring.charAt(i);
            if(operator=='*'){
                i++;
                term2 *= expr2();
            }else if (operator=='/'){
                i++;
                float tmp=expr2();
                if(tmp<0.000001&&tmp>-0.000001){       //there is no '0' in float
                    System.out.println("Divide zero error.");
                    System.exit(-1);
                }else{
                    term2/=tmp;
                }
            }else if(operator=='%'){
                i++;
                term2 = (int)term2 % (int)expr2();  //between int and int
            }else{
                break;
            }
        }
        return term2;
    }

    private float expr2(){      //the lowest one
        float terminal=0;
        while(inputstring.charAt(i)==' '&&i<inputstring.length()) i++;
        if(inputstring.charAt(i)=='('){     //recursvie
            i++;
            terminal=expr0();
            while(inputstring.charAt(i)==' '&&i<inputstring.length()) i++;
            if(inputstring.charAt(i)==')'){
                i++;
            }else{
                System.out.println("Wrong input. right paranthesis not found.");
                System.exit(-1);
            }
        }else{
            while(inputstring.charAt(i)==' '&&i<inputstring.length()) i++;
            if((inputstring.charAt(i)>='0'&&inputstring.charAt(i)<='9')){       //if it is number
                String tmp=inputstring.charAt(i)+"";
                i++;
                while(i<inputstring.length()&&((inputstring.charAt(i)>='0'&&inputstring.charAt(i)<='9')||inputstring.charAt(i)=='.')){
                    tmp+=inputstring.charAt(i);
                    i++;
                }
                terminal=Float.parseFloat(tmp);//TODO
            }else if(inputstring.charAt(i)=='-'){       //
                String tmp="";
                i++;
                while(i<inputstring.length()&&((inputstring.charAt(i)>='0'&&inputstring.charAt(i)<='9')||inputstring.charAt(i)=='.')){
                    tmp+=inputstring.charAt(i);
                    i++;
                }
                terminal=-Float.parseFloat(tmp);
            }else if(isletter(inputstring.charAt(i))||inputstring.charAt(i)==':'){      //parse and get return
                LinkedList<String> mathcmds=new LinkedList<String >();
                Interpreter mathroot=new Interpreter(mathcmds,namespace,funcnamespace,null);
                MuaParser mathP=new MuaParser(mathcmds,mathroot);
                mathP.parse(inputstring.substring(i,inputstring.length()));     //add the leftovers
                //interpret until the first arg is number
                while((!isdigit(mathP.cmds.getFirst().charAt(0))&&!mathP.cmds.getFirst().startsWith("-"))&&!mathP.cmds.isEmpty()) mathP.Inp.interpret(); //recursive
                String stringterminal=mathP.cmds.removeFirst();
                if(stringterminal.startsWith("-")) terminal=-Float.parseFloat(stringterminal.substring(1,stringterminal.length()));
                else terminal=Float.parseFloat(stringterminal);
                String newmathline="";
                while(!mathP.cmds.isEmpty()){
                    newmathline+=" "+mathP.cmds.removeFirst();
                }
                inputstring=inputstring.substring(0,i)+newmathline;             //pin huiqu
            }
        }
        return terminal;
    }

    public boolean isletter(char c){
        if(c>='a'&&c<='z'||c>='A'&&c<='Z')return true;
        return false;
    }
    public boolean isdigit(char c) {
        if(c>='0'&&c<='9') return true;
        return false;
    }
}
