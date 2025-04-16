package com.quanxiaoha.weblog.web.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quanxiaoha.weblog.web.service.impl.Ai_bot;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AiController {
    @PostMapping("/ai")
    public Map<String, Object> ai_response(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        String message = credentials.get("message"); // 获取请求体中的"message"字段
        System.out.println(message);
        Ai_bot bot = new Ai_bot();
        String answer =bot.aiBot(message +"不要回复表情,直接回复文本");
        result.put("status", "success");
        System.out.println("成功");
        result.put("status", "success");
        result.put("message", "Login successful");
        result.put("answer", answer);
        System.out.println(result);
        return result;
        }
    }












