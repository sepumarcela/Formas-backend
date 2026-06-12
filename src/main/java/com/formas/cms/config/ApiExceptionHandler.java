package com.formas.cms.config;

import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({
      IllegalArgumentException.class,
      IllegalStateException.class,
      IOException.class,
      MultipartException.class,
      MaxUploadSizeExceededException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleKnownErrors(Exception error) {
    return Map.of("message", cleanMessage(error));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, Object> handleUnexpectedErrors(Exception error) {
    return Map.of("message", cleanMessage(error));
  }

  private String cleanMessage(Exception error) {
    String message = error.getMessage();
    if (message == null || message.isBlank()) {
      return "No se pudo completar la operacion.";
    }
    return message;
  }
}
