package org.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadProperties extends Properties{

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
	
	
	
	private void loadCommandLineDefs( String[] cl ) throws IOException {

		StringBuffer sb = new StringBuffer();
		for(int i=0; i<cl.length; ++i) sb.append( cl[i] ).append( "\n" );
		load( new ByteArrayInputStream(sb.toString().getBytes()) );
	}
	
	
	
	
}
