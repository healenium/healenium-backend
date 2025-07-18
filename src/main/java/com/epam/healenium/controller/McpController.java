//package com.epam.healenium.controller;
//
//import com.anthropic.client.AnthropicClient;
//import com.anthropic.client.okhttp.AnthropicOkHttpClient;
//import com.anthropic.models.messages.Message;
//import com.anthropic.models.messages.MessageCreateParams;
//import com.anthropic.models.messages.Model;
//import com.openai.client.OpenAIClient;
//import com.openai.client.okhttp.OpenAIOkHttpClient;
//import com.openai.core.JsonValue;
//import com.openai.models.FunctionDefinition;
//import com.openai.models.chat.completions.ChatCompletion;
//import com.openai.models.chat.completions.ChatCompletionCreateParams;
//import com.openai.models.chat.completions.ChatCompletionTool;
//import io.modelcontextprotocol.client.McpSyncClient;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
////import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.tool.ToolCallback;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@RestController
//@RequestMapping("/healenium/mcp")
//@RequiredArgsConstructor
//public class McpController {
//
////    private final OpenAiChatModel chatModel;
//    private final McpSyncClient mcpClient;
//    private final SyncMcpToolCallbackProvider toolCallbackProvider;
//
////    @PostMapping("/chat")
////    public String getResponse(String userInput) {
////        Prompt prompt = new Prompt(userInput);
////        ChatResponse response = chatModel.call(prompt);
////        return response.getResult().getOutput().getText();
////    }
//
//    @PostMapping("/claude")
//    public String getClaudeResponse(String userInput) {
//        AnthropicClient client = AnthropicOkHttpClient.builder()
//                .apiKey()
//                .build();
//
//        MessageCreateParams params = MessageCreateParams.builder()
//                .maxTokens(1024L)
//                .addUserMessage("Hello, Claude")
//                .model(Model.CLAUDE_3_7_SONNET_LATEST)
//                .build();
//        Message message = client.messages().create(params);
//        return message.toString();
//    }
//
//    @PostMapping("/grok")
//    public String getGrokResponse(String userInput) {
//        OpenAIClient client = OpenAIOkHttpClient.builder()
//                .apiKey()
//                .baseUrl("https://api.x.ai/v1")
//                .build();
//
//        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
//        System.out.println("MCP Tools: " + List.of(tools).stream()
//                .map(t -> t.getToolDefinition().name())
//                .collect(Collectors.joining(", ")));
//
//        List<ChatCompletionTool> openAITools = List.of(tools).stream()
//                .map(tool -> ChatCompletionTool.builder()
//                        .type(JsonValue.from("function"))
//                        .function(FunctionDefinition.builder()
//                                .name(tool.getToolDefinition().name())
//                                .description(tool.getToolDefinition().description())
////                                .parameters(tool.getToolDefinition().inputSchema())
//                                .build())
//                        .build())
//                .collect(Collectors.toList());
//
//        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
//                .model("grok-3")
//                .messages(ChatCompletionCreateParams.builder()
//                        .addUserMessage(userInput)
//                        .build()
//                        .messages())
//                .tools(openAITools)
//                .temperature(0.7)
//                .maxCompletionTokens(100)
//                .build();
//
//        try {
//            ChatCompletion chatCompletion = client.chat().completions().create(params);
//            Optional<String> content = chatCompletion.choices().get(0).message().content();
//            return content.orElse(null);
//        } catch (Exception e) {
//            throw new RuntimeException("Ошибка при вызове Grok API: " + e.getMessage());
//        }
//    }
//
//}

