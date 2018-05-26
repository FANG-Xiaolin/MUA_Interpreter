//import java.util.LinkedList;
//
//public abstract class Type {
//    String content;
////    public boolean equals(Type t);
//    public abstract void print();
//}
//
//class Word extends Type{
//    String content;
//    public Word(String content){
//        this.content=content;
//    }
////    @Override
//    public boolean equals(Word w){
//        return content.equals(w);
//    }
//    @Override
//    public void print(){
//        System.out.print("\"");
//        System.out.println(content);
//    }
//}
//
//class Number extends Type{
//    String content;
//    public Number(String content){
//        this.content=content;
//    }
////    @Override
//    public boolean equals(Number n){
//        float f1=Float.parseFloat(content);
//        float f2=Float.parseFloat(n.content);
//        return (f1-f2<1e-6);
//    }
//    @Override
//    public void print(){
//        System.out.println(content);
//    }
//}
//
//class List extends Type{
//    LinkedList<Type> content;
//    @Override
//    public void print(){
//        System.out.println(content);
//    }
//}
////boolean 