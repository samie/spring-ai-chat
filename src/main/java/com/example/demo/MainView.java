package com.example.demo;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

import java.util.ArrayList;

@Route("") // map view to the root
class MainView extends VerticalLayout {

    private final ArrayList<Message> chatHistory = new ArrayList<>();

    VerticalLayout messageList = new VerticalLayout();
    Scroller messageScroller = new Scroller(messageList);
    MessageInput messageInput = new MessageInput();

    MainView(StreamingChatClient chatClient) {
        add(messageScroller, messageInput);
        setSizeFull();
        setMargin(false);
        messageScroller.setSizeFull();
        messageInput.setWidthFull();

        // Add system message to help the AI to behave
        chatHistory.add(new SystemMessage("Only if the user asks you about Vaadin, reply in bro style. Always show a piece a code."));

        messageInput.addSubmitListener(ev -> {
            // Add use input as markdown message
            chatHistory.add(new UserMessage(ev.getValue()));
            messageList.add(new MarkdownMessage(ev.getValue(),"Me"));

            // Placeholder message for the upcoming AI reply
            MarkdownMessage reply = new MarkdownMessage("Assistant");
            messageList.add(reply);

            // Ask AI and stream back the reply to UI
            Prompt prompt = new Prompt(chatHistory);
            chatClient.stream(prompt)
                    .doOnComplete(() -> chatHistory.add(new AssistantMessage(reply.getMarkdown())))
                    .subscribe(cr -> reply.appendMarkdownAsync(cr.getResult().getOutput().getContent()));
            reply.scrollIntoView();
        });
    }
}