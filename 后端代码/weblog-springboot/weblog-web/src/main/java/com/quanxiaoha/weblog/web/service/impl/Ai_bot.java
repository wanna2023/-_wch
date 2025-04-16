package com.quanxiaoha.weblog.web.service.impl;
import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.config.Consts;
import com.coze.openapi.service.service.CozeAPI;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;
public class Ai_bot {
    public String aiBot(String message) {
        final StringBuilder result = new StringBuilder();
        // 申请的令牌
        String token = "pat_h16XbuQlgghlmE6vZgbHFooDDbvJ6Ozus6P8iycBXllT3hkVsngFt4rZVq0L6NFS";
        //智能体ID
        String botID = "7479636568690098212";
        //user_id：标识当前与智能体交互的用户。调试时可将此参数固定为一个任意字符串，例如 123。
        String userID = "123";
        TokenAuth authCli = new TokenAuth(token);
        // Init the Coze client through the access_token.
        CozeAPI coze = new CozeAPI.Builder()
                .baseURL(Consts.COZE_CN_BASE_URL)
                .auth(authCli)
                .readTimeout(100000)
                .build();
        /*
         * Step one, create chat
         * Call the coze.chat().stream() method to create a chat. The create method is a streaming
         * chat and will return a Flowable ChatEvent. Developers should iterate the iterator to get
         * chat event and handle them.
         * */
        CreateChatReq req = CreateChatReq.builder()
                .botID(botID)
                .userID(userID)
                .messages(Collections.singletonList(Message.buildUserQuestionText(message)))
                .build();

        Flowable<ChatEvent> resp = coze.chat().stream(req);
        resp.blockingForEach(event -> {
            if (ChatEventType.CONVERSATION_MESSAGE_DELTA.equals(event.getEvent())) {
                System.out.print(event.getMessage().getContent());
                // 将消息内容追加到结果中
                result.append(event.getMessage().getContent().toString());
            }
            if (ChatEventType.CONVERSATION_CHAT_COMPLETED.equals(event.getEvent())) {
                System.out.println("Token usage:" + event.getChat().getUsage().getTokenCount());
            }
        });

        System.out.println("done");
        coze.shutdownExecutor();
        return result.toString();
    }
}
