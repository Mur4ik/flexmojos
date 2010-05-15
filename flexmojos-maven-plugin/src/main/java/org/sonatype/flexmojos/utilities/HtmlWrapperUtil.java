/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import eu.cedarsoft.utils.ZipExtractor;

public class HtmlWrapperUtil
{
	public static void extractTemplate( MavenProject project, String templateURI, File outputDir )
		throws MojoExecutionException
	{
		outputDir.mkdirs();

        URI uri;
        try
        {
            if ( MavenUtils.isWindows() )
            {
                // Shake bars to avoid URI syntax problems
            	templateURI = templateURI.replace( '\\', '/' );
            }
            templateURI = URIUtil.encodePath( templateURI );
            uri = new URI( templateURI );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Invalid template URI.", e );
        }

        String scheme = uri.getScheme();
        if ( "embed".equals( scheme ) )
        {
            copyEmbedTemplate( uri.getSchemeSpecificPart(), outputDir );
        }
        else if ( "zip".equals( scheme ) )
        {
            copyZipTemplate( uri.getSchemeSpecificPart(), outputDir );
        }
        else if ( "folder".equals( scheme ) )
        {
            copyFolderTemplate( project, uri.getSchemeSpecificPart(), outputDir );
        }
        else
        {
            throw new MojoExecutionException( "Invalid URI scheme: " + scheme );
        }
	}
	
	private static void copyEmbedTemplate( String path, File outputDir )
		throws MojoExecutionException
	{
		URL url = HtmlWrapperUtil.class.getResource( "/templates/wrapper/" + path + ".zip" );
        File template = new File( outputDir, "template.zip" );
        try
        {
            FileUtils.copyURLToFile( url, template );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + template, e );
        }
        extractZipTemplate( outputDir, template );
        template.delete();
	}
	
	private static void extractZipTemplate( File outputDir, File template )
		throws MojoExecutionException
	{
		try
        {
            ZipExtractor ze = new ZipExtractor( template );
            ze.extract( outputDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error happens when trying to extract html-template.", e );
        }
	}
	
	private static void copyZipTemplate( String path, File outputDir )
		throws MojoExecutionException
	{
		File source = new File( path );
        if ( !source.exists() || !source.isFile() )
        {
            throw new MojoExecutionException( "Zip template doesn't exists. " + source );
        }

        extractZipTemplate( outputDir, source );
	}
	
	private static void copyFolderTemplate( MavenProject project, String path, File outputDir)
		throws MojoExecutionException
	{
		File source = new File( path );
        if ( !source.isAbsolute() )
        {
            source = new File( project.getBasedir(), path );
        }
        if ( !source.exists() || !source.isDirectory() )
        {
            throw new MojoExecutionException( "Template folder doesn't exists. " + source );
        }

        try
        {
            FileUtils.copyDirectory( source, outputDir,
                                     FileFilterUtils.makeSVNAware( FileFilterUtils.makeCVSAware( null ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + outputDir, e );
        }
	}
}
