package todayquest.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Controller
public class CsrfController {

    private static final String[] ALLOWED_DOMAINS = {"localhost"};

    @ResponseBody
    @GetMapping("/token")
    public String token(HttpServletRequest request, CsrfToken csrfToken) {
        String referer = request.getHeader("Referer");

        if (referer == null || referer.isEmpty()) {
            return null;
        }

        try {
            URL refererUrl = new URL(referer);
            String refererDomain = refererUrl.getHost();
            if (Arrays.stream(ALLOWED_DOMAINS).noneMatch(refererDomain::equals)) {
                return null;
            }
        } catch (MalformedURLException e) {
            return null;
        }

        return csrfToken.getToken();
    }
}
