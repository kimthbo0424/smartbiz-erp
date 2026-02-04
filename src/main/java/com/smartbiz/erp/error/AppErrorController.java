package com.smartbiz.erp.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (Exception e) {
                statusCode = 500;
            }
        }

        String message = "";
        Object msgObj = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (msgObj != null) {
            message = msgObj.toString();
        }

        Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String causeLine = "";
        if (ex != null) {
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            String rootMsg = root.getMessage();
            if (rootMsg == null || rootMsg.isBlank()) {
                rootMsg = root.toString();
            }
            causeLine = rootMsg;
        }

        Object uriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = (uriObj == null) ? "" : uriObj.toString();

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("path", path);
        model.addAttribute("message", message);
        model.addAttribute("causeLine", causeLine);

        return "error/error";
    }
}
