/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * @author jspacco
 *
 */
public class ParseWebXml
{
    public static final String FILTER_NAME= "filter-name";
    public static final String FILTER_URL_PATTERN= "url-pattern";
    public static final String FILTER_CLASS= "filter-class";
    
    public static final String SERVLET_NAME= "servlet-name";
    public static final String SERVLET_CLASS = "servlet-class";
    public static final String SERVLET_URL_PATTERN = "url-pattern";
    
    private static final Set<String> ignoredDirs = new HashSet<String>();
    
    static {
        ignoredDirs.add("classes");
        ignoredDirs.add("META-INF");
        ignoredDirs.add("WEB-INF");
        ignoredDirs.add("CVS");
    }
                                
    
    public ParseWebXml() {}
    
    private final Map<String,String> servletMap = new HashMap<String,String> ();
    private final List<Filter> filterList = new LinkedList<Filter>();
    
    public static void crappyXPathMethod(String webXmlFileName)
    throws FileNotFoundException, DocumentException
    {
        File file = new File(webXmlFileName);
        
        FileInputStream fis = new FileInputStream(file);
        SAXReader reader = new SAXReader();
        Document document = reader.read(fis);
        List<?> list = document.selectNodes("//web-app[@*]");
        //System.out.println("list.size() " +list.size());
        for (Iterator<?> ii=list.iterator(); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            Node n = elt.selectSingleNode("//web-app/servlet/name");
            //System.out.println("name: " +n.getText());
        }
    }
    
    public static ParseWebXml parse(String webXmlFileName)
    throws FileNotFoundException, DocumentException
    {
        File file = new File(webXmlFileName);
        
        FileInputStream fis = new FileInputStream(file);
        SAXReader reader = new SAXReader();
        Document document = reader.read(fis);
        
        ParseWebXml webXml = new ParseWebXml();
        
        Element root = document.getRootElement();

        for ( Iterator<?> ii=root.elementIterator( "servlet-mapping" ); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            //System.out.print("name: " +elt.getName());
            
            String urlPattern=null;
            String servletName=null;
            for (int jj=0; jj < elt.nodeCount(); jj++)
            {
                Node node = elt.node(jj);
                if (node.getName() == null)
                    continue;
                if (node.getName().equals(SERVLET_NAME)) {
                    servletName = node.getText().trim();
                    if (webXml.tryToMapServlet(servletName, urlPattern))
                        break;
                } else if (node.getName().equals(SERVLET_URL_PATTERN)) {
                    urlPattern = node.getText().trim();
                    if (webXml.tryToMapServlet(servletName, urlPattern))
                        break;
                }
            }
            //System.out.println(" is mapped thusly: " +servletName +" => "+ urlPattern);
        }
        
        for (Iterator<?> ii=root.elementIterator( "filter-mapping"); ii.hasNext();)
        {
            Element elt = (Element)ii.next();
            //System.out.print("name: " +elt.getName());
            
            String filterName=null;
            String urlPattern=null;
            for (int jj=0; jj < elt.nodeCount(); jj++)
            {
                Node node = elt.node(jj);
                if (node.getName() == null)
                    continue;
                if (node.getName().equals(FILTER_NAME)) {
                    filterName = node.getText().trim();
                    if (webXml.tryToCreateFilter(filterName, urlPattern))
                        break;
                } else if (node.getName().equals(FILTER_URL_PATTERN)) {
                    urlPattern = node.getText().trim();
                    if (webXml.tryToCreateFilter(filterName, urlPattern))
                        break;
                }
            }
            //System.out.println(" is mapped thusly: " +filterName+ " => "+ urlPattern);
                    
        }
        
        return webXml;
    }
    
    private Map<String, List<String>> urlFilters = new HashMap<String, List<String>>();
    public void addFilter(String filterName, String relativePath)
    {
        List<String> list=null;
        if (urlFilters.containsKey(relativePath))
            list = urlFilters.get(relativePath);
        else
            list = new LinkedList<String>();
        list.add(filterName);
        urlFilters.put(relativePath, list);
    }
    
    /**
     * @param webRootPath
     */
    public void parseWebRoot(final String webRootPath)
    {
        File file = new File(webRootPath);
        
        if (!file.isDirectory()) {
            throw new IllegalStateException(file.getAbsolutePath() +" is not a directory!");
        }
        
        // bit of a hack
        // I'm using a pseudo-visitor pattern here to visit all the files
        FileFilter fileFilter = new FileFilter() {
            @Override
			public boolean accept(File file) {
                if (file.isDirectory()) {
                    if (ignoredDirs.contains(file.getName()))
                        return false;
                    file.listFiles(this);
                    return false;
                }
                String relativePath = stripLeadingPath(webRootPath, file);
                //System.out.println("relativePath: " +relativePath);
                for (Iterator<Filter> ii=filterList.iterator(); ii.hasNext();)
                {
                    Filter filter = ii.next();
                    if (relativePath.matches(filter.regexp))
                    {
                        addFilter(filter.filterName, relativePath);
                    }
                }
                return false;
            }
        };
        file.listFiles(fileFilter);
        
        for (Iterator<String> ii=servletMap.values().iterator(); ii.hasNext();) {
            String urlPattern = ii.next();
            for (Iterator<Filter> jj=filterList.iterator(); jj.hasNext();) {
                Filter filter = jj.next();
                if (urlPattern.matches(filter.regexp)) {
                    addFilter(filter.filterName, urlPattern);
                }
            }
        }
        
    }
    
    @Override
	public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (Iterator<String> ii=urlFilters.keySet().iterator(); ii.hasNext();)
        {
            String url = ii.next();
            buf.append(url +": \n");
            List<String> list = urlFilters.get(url);
            for (Iterator<String> jj=list.iterator(); jj.hasNext();)
            {
                buf.append("\t" +jj.next()+ "\n");
            }
        }
        return buf.toString();
    }
    
    static String stripLeadingPath(String leadingPath, File file)
    {
        //System.out.println("leadingPath: " +leadingPath);

        String fullFilePath = file.getAbsolutePath();

        //System.out.println("fullFilePath: " +fullFilePath);
        
        // remove a trailing slash if this is a directory        
        return fullFilePath.replaceAll(leadingPath, "");
    }
    
    public static void main(String args[])
    throws Exception
    {
        String webXmlFile = "WebRoot/WEB-INF/web.xml";
    	ParseWebXml webXml = ParseWebXml.parse(webXmlFile);
    	
    	webXml.parseWebRoot("../SubmitServer2/WebRoot");
    	
    	System.out.println(webXml);
    }
    
    private boolean tryToMapServlet(String servletName, String urlPattern)
    {
        if (servletName == null || urlPattern == null)
            return false;
        servletMap.put(servletName, urlPattern);
        return true;
    }
    
    private boolean tryToCreateFilter(String filterName, String urlPattern)
    {
        if (filterName == null || urlPattern == null)
            return false;
        filterList.add(new Filter(filterName, urlPattern));
        return true;
    }
    
    private static class Filter
    {
        final String filterName;
        final String urlPattern;
        final String regexp;
        
        public Filter(String filterName, String urlPattern)
        {
            this.filterName = filterName;
            this.urlPattern = urlPattern;
            this.regexp = urlPattern.replaceAll("\\*", ".*");
        }
    }
}
