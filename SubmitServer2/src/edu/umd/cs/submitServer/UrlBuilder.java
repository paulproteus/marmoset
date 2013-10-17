package edu.umd.cs.submitServer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Builder class for creating complex urls. Can also URL encode them for use as
 * a GET parameter.
 * 
 * @author rwsims
 * 
 */
public class UrlBuilder {
  private final String scheme;
  private final String host;
  private final int port;
  private final List<String> pathElements = Lists.newArrayList();
  private final Map<String, String> parameters = Maps.newLinkedHashMap();

  /**
   * Create a new UrlBuilder based on the servlet request. The url will be
   * initialized with the scheme, port and server name of the request, and the
   * first path element will be the requests's context path.
   * 
   * @param req
   */
  public UrlBuilder(HttpServletRequest req) {
    this(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());
  }

  public UrlBuilder(String scheme, String host, int port, String contextPath) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    pathElements.add(contextPath);
  }



  public UrlBuilder addPathElement(String element) {
    pathElements.add(element);
    return this;
  }

  public UrlBuilder setParameter(String key, String value) {
    parameters.put(key, value);
    return this;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    String path = Joiner.on("/").join(pathElements);
    if (scheme.equals("http") && port == 80 || scheme.equals("https") && port == 443) {
      builder.append(String.format("%s://%s%s", scheme, host, path));
    } else
      builder.append(String.format("%s://%s:%d%s", scheme, host, port, path));
    if (!parameters.isEmpty()) {
      StringBuilder paramString = new StringBuilder("?");
      Iterator<Entry<String, String>> iter = parameters.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<String, String> param = iter.next();
        paramString.append(String.format("%s=%s", encode(param.getKey()), encode(param.getValue())));
        if (iter.hasNext()) {
          paramString.append("&");
        }
      }
      builder.append(paramString.toString());
    }
    return builder.toString();
  }

  public String toEncodedString() {
    return encode(toString());
  }

  private static String encode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }
  
}
