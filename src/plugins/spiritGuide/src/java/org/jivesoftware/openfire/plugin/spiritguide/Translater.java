package org.jivesoftware.openfire.plugin.spiritguide;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Translater {

    String url = "https://www.googleapis.com/language/translate/v2?key=AIzaSyBBuoPbUp8Vkv7n9UBqNw2_AJy5qXvxqP0&q=%message%&source=%src_lang%&target=en";

    public String translate(String msg, String srcLang) {
        CloseableHttpClient httpclient = HttpClients.createDefault();





        try {

            String encMsg = URLEncoder.encode(msg, "UTF-8");
            HttpGet httpget = new HttpGet(url.replaceAll("%src_lang%", srcLang).replaceAll("%message%", encMsg));


            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final org.apache.http.HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = null;
            try {
                responseBody = httpclient.execute(httpget, responseHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("----------------------------------------");
            System.out.println(responseBody);

            String result = null;

            JsonFactory factory = new JsonFactory();

            JsonParser p = factory.createParser(responseBody != null ? responseBody : "{}");

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.getCurrentName();
                p.nextToken();
                if("translatedText".equals(fieldName)) {
                    System.out.println(p.getValueAsString());
                    result = p.getValueAsString();
                    break;
                }

            }

            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
