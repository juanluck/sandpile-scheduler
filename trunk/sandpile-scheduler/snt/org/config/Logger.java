/*
EvoGen, Evolutionary Geneura is a framework for simulating distributed evolutionary computation experiments
Copyright (C) 2008 Junta de Andalucia CICE project P06-TIC-02025

This file is part of EvoGen.

EvoGen is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

EvoGen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with EvoGen; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

contact:  http://geneura.ugr.es, https://forja.rediris.es/svn/geneura/evogen
*/
package org.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Logger {
    
	public static void append(String file, String s){
		if (file.equals("")){
			System.out.print(s);
		}else{
			try {
	    		File f = new File(file);
	    		if(f.exists()){
	    			String content = getContents(f);
	        		setContents(f, content+s);	
	    		}else{
	    			f.createNewFile();
	    			setContents(f, s);
	    		}
	    		
	    	} catch (FileNotFoundException e) {
	    		e.printStackTrace();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	
	    	}	
		}
    	
    }

    public static String getContents(File aFile) {
        //...checks on aFile are elided
        StringBuffer contents = new StringBuffer();

        //declared here only to make visible to finally clause
        BufferedReader input = null;
        try {
          //use buffering, reading one line at a time
          //FileReader always assumes default encoding is OK!
          input = new BufferedReader( new FileReader(aFile) );
          String line = null; //not declared within while loop
          /*
          * readLine is a bit quirky :
          * it returns the content of a line MINUS the newline.
          * it returns null only for the END of the stream.
          * it returns an empty String if two newlines appear in a row.
          */
          while (( line = input.readLine()) != null){
            contents.append(line);
            contents.append(System.getProperty("line.separator"));
          }
        }
        catch (FileNotFoundException ex) {
          ex.printStackTrace();
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        finally {
          try {
            if (input!= null) {
              //flush and close both "input" and its underlying FileReader
              input.close();
            }
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
        }
        return contents.toString();
      }

    private static void setContents(File aFile, String aContents)
    throws FileNotFoundException, IOException {
    if (aFile == null) {
    throw new IllegalArgumentException("File should not be null.");
    }
    if (!aFile.exists()) {
    throw new FileNotFoundException ("File does not exist: " + aFile);
    }
    if (!aFile.isFile()) {
    throw new IllegalArgumentException("Should not be a directory: " + aFile);
    }
    if (!aFile.canWrite()) {
    throw new IllegalArgumentException("File cannot be written: " + aFile);
    }

    //declared here only to make visible to finally clause; generic reference
    Writer output = null;
    try {
    //use buffering
    //FileWriter always assumes default encoding is OK!
    output = new BufferedWriter( new FileWriter(aFile) );
    output.write( aContents );
    }
    finally {
    	//flush and close both "output" and its underlying FileWriter
    	if (output != null) output.close();
    }
    }

}
