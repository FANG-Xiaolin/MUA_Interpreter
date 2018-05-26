//The entrance

import java.util.Scanner;
import java.util.LinkedList;

public class Main {
    static MuaParser p = new MuaParser();
    public static void main(String[] args) {
        System.out.println("Welcome to MUA!");
        boolean Terminate=false;
        while (!Terminate){
            Terminate=p.GetAndDoInput();
        }
    }
}

//make "f [[n m][make "a (:n + :m) make "b (:n - :m) print a_is print :a print b_is print :b output (:a * :b) print a*b_is stop print test_stop]]
//print f 5.0 -2.5 //result is 18.75


//make "test [[n m][make "b add :a :n print a_is print :a print b_is print :b output :m print hi stop print sss]]
//print test 9 2


//make "f [
//[a]
// [
// make "b 10
// make "a :a+:b
// print :a]]

//make "f [[a][make "a (:a + 1) print :a]]
//f ((2* :b )--4)


//make "f [[a][make "b 0 print add :b 1 ]]
//make "f [[a][make "b 0 print (:b + 1) ]]
//f 2

//make "a [[  ][print hi]]
//make "a [[][output ((3*2 +1.5)/1.1)]]

//make "f [[a][repeat :a [print :a] f (:a - 1)]]
//f 3
