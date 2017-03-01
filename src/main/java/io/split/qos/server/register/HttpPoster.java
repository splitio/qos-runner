package io.split.qos.server.register;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import org.apache.http.HttpException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Using plain Http since other libraries tend to collide with different versions of other libraries.
 *
 * Using GSON since is really plain.
 */
@Singleton
public class HttpPoster {

    public void post(URL url, Object postDTO) throws IOException, HttpException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            Gson gson = new Gson();
            String input = gson.toJson(postDTO);

            try(OutputStream os = conn.getOutputStream()) {
                os.write(input.getBytes());
                os.flush();
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new HttpException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())))) {
                //TODO Check here the response and log if error??
                //        String output;
                //        System.out.println("Output from Server .... \n");
                //        while ((output = br.readLine()) != null) {
                //            System.out.println(output);
                //        }
            };

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
