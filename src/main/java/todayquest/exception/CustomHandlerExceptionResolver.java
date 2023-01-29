package todayquest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {


        ex.printStackTrace();

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

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
}
