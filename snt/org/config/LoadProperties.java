package org.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties extends Properties{
	
	
	/**
	* Constructs a LoadProperties object from a parameter list.
	* The algorithm is as follows: first <code>resource</code> is used to attempt
	* loading default values from the give system resource.
	* Then pars[0] is tried if it is an
	* existing filename. If it is, reading properties from that file is
	* attempted. Then (if pars[0] was a filename then from index 0 otherwise
	* from index 1) pars is loaded as if it was a command line argument list
	* using <code>loadCommandLineDefs</code>.
	*
	* A little inconvinience is that if pars[0] is supposed to be the first
	* command line argument but it is a valid filename at the same time by
	* accident. The caller must take care of that.
	*
	* No exceptions are thrown, instead error messages are written to the
	* standard error. Users who want a finer control should use
	* the public methods of this class.
	*
	* @param pars The (probably command line) parameter list.
	* @param resource The name of the system resource that contains the
	* defaults. null if there isn't any.
	* 
	*/
	public	LoadProperties( String[] pars, String resource ) {
		
		try
		{
			if( resource != null )
			{
				loadSystemResource(resource);
				System.err.println("ConfigProperties: System resource "
				+resource+" loaded.");
			}
		}
		catch( Exception e )
		{
			System.err.println("ConfigProperties: " + e );
		}
		
		if( pars == null || pars.length == 0 ) return;
		
		try
		{
			load( pars[0] );
			System.err.println(
				"ConfigProperties: File "+pars[0]+" loaded.");
			pars[0] = "";
		}
		catch( IOException e )
		{
			System.err.println("ConfigProperties: Failed loading '"+pars[0]
			+"' as a file, interpreting it as a property.");
		}
		catch( Exception e )
		{
			System.err.println("ConfigProperties: " + e );
		}

		if( pars.length==1 && pars[0].length()==0 ) return;
		
		try
		{
			loadCommandLineDefs( pars );
			System.err.println(
				"ConfigProperties: Command line defs loaded.");
		}
		catch( Exception e )
		{
			System.err.println("ConfigProperties: " + e );
		}
	}
	

	public LoadProperties(String[] pars) {
		try
		{
			loadCommandLineDefs( pars );
			//System.err.println(
			//	"ConfigProperties: Command line defs loaded.");
		}
		catch( Exception e )
		{
			System.err.println("ConfigProperties: " + e );
		}
	}
	
	
	/**
	* Loads given file. Calls <code>Properties.load</code> with a file
	* input stream to the given file.
	*/
	public void load( String fileName ) throws IOException {

		FileInputStream fis = new FileInputStream( fileName );
		load( fis );
		fis.close();
	}
	
	// -------------------------------------------------------------------

	/**
	* Adds the properties from the given property file. Searches in the class path
	* for the file with the given name.
	*/
	public void loadSystemResource( String n ) throws IOException {
		
		ClassLoader cl = getClass().getClassLoader();
		load( cl.getResourceAsStream( n ) );
	}

	// -------------------------------------------------------------------

	
	
	private void loadCommandLineDefs( String[] cl ) throws IOException {

		StringBuffer sb = new StringBuffer();
		for(int i=0; i<cl.length; ++i) sb.append( cl[i] ).append( "\n" );
		load( new ByteArrayInputStream(sb.toString().getBytes()) );
	}
	
	
	
	
	
}
