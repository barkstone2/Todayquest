package todayquest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {


        try {

            String acceptHeader = request.getHeader("accept");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            ObjectMapper om = new ObjectMapper();
            if ("application/json".equals(acceptHeader)) {
                int status = HttpServletResponse.SC_BAD_REQUEST;
                if(ex instanceof AccessDeniedException) status = HttpServletResponse.SC_FORBIDDEN;
                response.setStatus(status);

                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("ex", ex.getClass());
                errorResult.put("message", ex.getMessage());

                String result = om.writeValueAsString(errorResult);
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write(result);

            } else {
                if (ex instanceof IllegalArgumentException) {
                    ModelAndView modelAndView = new ModelAndView("error/error");
                    modelAndView.setStatus(HttpStatus.BAD_REQUEST);
                    ModelMap modelMap = modelAndView.getModelMap();
                    modelMap.put("message", ex.getMessage());

                    return modelAndView;
                } else if (ex instanceof AccessDeniedException) {
                    ModelAndView modelAndView = new ModelAndView("error/error");
                    modelAndView.setStatus(HttpStatus.FORBIDDEN);
                    ModelMap modelMap = modelAndView.getModelMap();
                    modelMap.put("message", ex.getMessage());

                    return modelAndView;
                } else if (ex instanceof IllegalStateException) {
                    ModelAndView modelAndView = new ModelAndView("error/error");
                    modelAndView.setStatus(HttpStatus.BAD_REQUEST);
                    ModelMap modelMap = modelAndView.getModelMap();
                    modelMap.put("message", ex.getMessage());

                    return modelAndView;
                }

                return new ModelAndView("error/error");
            }

        } catch (Exception e) {
            return new ModelAndView("error/error");
        }

        return null;
    }
}
