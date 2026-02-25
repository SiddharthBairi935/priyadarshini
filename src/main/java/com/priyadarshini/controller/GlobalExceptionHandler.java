package com.priyadarshini.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleError(MaxUploadSizeExceededException e, RedirectAttributes redirectAttributes) {
        // This catches the exact error you just got!
        redirectAttributes.addFlashAttribute("errorMessage", "The image file is too large! Please upload a file smaller than 50MB.");
        return "redirect:/register";
    }
}