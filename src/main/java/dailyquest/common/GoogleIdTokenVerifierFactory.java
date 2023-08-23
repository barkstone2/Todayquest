package dailyquest.common;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class GoogleIdTokenVerifierFactory {
    public GoogleIdTokenVerifier create(HttpTransport transport, JsonFactory jsonFactory, Collection<String> audience) {
        return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(audience)
                .build();
    }
}
