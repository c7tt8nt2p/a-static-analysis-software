import myproject.Symbol;

import java.io.*;
import java.nio.charset.Charset;


public class TestFile {


    public static void main(String[] args) throws IOException {
        int lineOfCode = 0;
        String line;
        try (InputStream fileInputStream = new FileInputStream("D:\\IdeaProjects\\a-static-analysis-software\\src\\main\\java\\myproject\\resources\\EX_Code.cs");
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll("\\s+", "");
                if ((line.length() == 0) ||
                        line.length() == 1 && line.equals(Symbol.LB.getSymbolCharacter()))
                    continue;
                ++lineOfCode;
                System.out.println(line.length());
                System.out.println(line);

            }
            System.out.println("LOC : " + lineOfCode);
        }
    }
}