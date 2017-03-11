import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;


public class TestFile {


    public static void main(String[] args) throws IOException {
        FileReader fr = null;
        LineNumberReader lnr = null;
        String str;
        int i;

        try{
            // create new reader
            fr = new FileReader("D:\\IdeaProjects\\SeniorProject_FX\\src\\com\\myproject\\resources\\EX_Code.cs");
            lnr = new LineNumberReader(fr);

            // read lines till the end of the stream
            while((str=lnr.readLine())!=null)
            {
                i=lnr.getLineNumber();
                System.out.print("("+i+")");

                // prints string
                System.out.println(str);
            }
        }catch(Exception e){

            // if any error occurs
            e.printStackTrace();
        }finally{

            // closes the stream and releases system resources
            if(fr!=null)
                fr.close();
            if(lnr!=null)
                lnr.close();
        }

    }
}