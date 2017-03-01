package io.split.qos.server.register;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class QOSRegister {
    private static final Logger LOG = LoggerFactory.getLogger(QOSRegister.class);

    private static final String REGISTER_ENDPOINT = "register";
    private static final String API_ENDPOINT = "api";
    private final ScheduledExecutorService executor;
    private final HttpPoster poster;
    private final String serverName;

    @Inject
    public QOSRegister(HttpPoster poster,
                       @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.poster = Preconditions.checkNotNull(poster);
        this.serverName = Preconditions.checkNotNull(serverName);
    }

    public void register(String qosDashboardURL, String qosRunnerURL) {
        URL registerURL = generateRegisterURL(qosDashboardURL);
        URL apiURL = generateApiURL(qosRunnerURL);
        RegisterDTO dto = new RegisterDTO(serverName, apiURL.toString());
        executor.scheduleAtFixedRate(new Register(registerURL, poster, dto), 0, 5, TimeUnit.MINUTES);
    }

    private URL generateURL(String url, String endpoint) {
        if (url.endsWith("/")) {
            url= url.substring(0, url.length() - 1);
        }
        if (!url.endsWith(endpoint)) {
            url = String.format("%s/%s", url, endpoint);
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(String.format("Malformed QOS URL %s", url), e);
        }
    }

    private URL generateApiURL(String qosRunnerURL) {
        return generateURL(qosRunnerURL, API_ENDPOINT);
    }

    private URL generateRegisterURL(String registerURL) {
        return generateURL(registerURL, REGISTER_ENDPOINT);
    }

    private static class Register implements Runnable {

        private final URL registerURL;
        private final HttpPoster poster;
        private final RegisterDTO dto;

        private Register(URL registerURL, HttpPoster poster, RegisterDTO dto) {
            this.registerURL = Preconditions.checkNotNull(registerURL);
            this.poster = Preconditions.checkNotNull(poster);
            this.dto = Preconditions.checkNotNull(dto);
        }

        @Override
        public void run() {
            try {
                poster.post(registerURL, dto);
            } catch (Exception e) {
                LOG.error(String.format("Failed to register DTO %s into URL %s", dto, registerURL), e);
            }
        }
    }
}
