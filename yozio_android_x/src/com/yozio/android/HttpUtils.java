package com.yozio.android;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Utility methods for dealing with HTTP related tasks.
 */
class HttpUtils {
  
  private static final String LOGTAG = "HttpUtils";
  
  /**
   * Constructs a URL with the HTTP GET parameters appended.
   * 
   * @param baseUrl  the base url to append the parameters to.
   * @param params  the parameters to append.
   * @return the URL with the appended parameters.
   */
  static String urlWithParams(String baseUrl, List<NameValuePair> params) {
    String paramString = URLEncodedUtils.format(params, "utf-8");
    return baseUrl + "?" + paramString;
  }
  
  /**
   * Performs a blocking HTTP GET request to the specified uri.
   * 
   * @param httpClient  the client to execute the HTTP request.
   * @param uri  the uri to make the GET request to.
   * @return  the String response, or null if the request failed.
   */
  static String doGetRequest(HttpClient httpClient, String uri) {
    try {
      HttpGet httpGet = new HttpGet(uri);
      HttpResponse httpResponse = httpClient.execute(httpGet);
      HttpEntity httpEntity = httpResponse.getEntity();
      if (httpEntity != null) {
        String responseString = EntityUtils.toString(httpEntity);
        // Release the content.
        httpEntity.consumeContent();
        return responseString;
      }
    } catch (ClientProtocolException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    } catch (IOException e) {
      Log.e(LOGTAG, "doGetRequest", e);
    }
    return null;
  }
}
