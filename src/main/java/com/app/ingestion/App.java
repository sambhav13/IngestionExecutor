package com.app.ingestion;



import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {

        File f1 = new File("src/data/input/file3.txt");
        File f2 = new File("src/data/output/file3.txt");
        InputStream in = new FileInputStream(new File("src/data/input/file3.txt"));
        OutputStream out = new FileOutputStream(new File("src/data/output/file3.txt"));


        IOUtils.copy(in,out);
        //FileUtils.moveFile(f1,f2);
       in.close();
        out.close();


        System.out.println( "Hello World!" );
    }
}
