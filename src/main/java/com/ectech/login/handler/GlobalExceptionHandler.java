package com.ectech.login.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ModelAndView handleBadRequestException(HttpServletRequest request, Throwable ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return buildModelAndViewErrorPage(new ErrorResponse(status.value(), ex.getMessage()));
    }

    public static class ErrorResponse {
        private final int code;
        private final String message;

        ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    private ModelAndView buildModelAndViewErrorPage(ErrorResponse errorResponse) {
        ModelAndView mav = new ModelAndView(new MappingJackson2JsonView());
        mav.addObject("code", errorResponse.getCode());
        mav.addObject("message", errorResponse.getMessage());
        return mav;
    }

}