package mors.museumguide.llm;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import mors.museumguide.tools.*;

import java.util.concurrent.CompletableFuture;

public class initLLM {

    private static final String BASE_URL = "http://localhost:11434";
    private static String MODEL_NAME = "llama3.2:3b";
    private Assistant assistant;
    private static String[] modelNames;

    public static String[] getModelNames() {
        return modelNames;
    }

    public interface Assistant {
        @SystemMessage("""
You are a helpful, friendly museum guide. Your main goal is to make museum visits enjoyable and informative for everyone, especially first-timers.

**Your responses must be based *solely* on the information retrieved from your tools. Do not use outside knowledge or invent details.**

Explain things simply and clearly, like a real person, not a textbook or a robot. If someone asks something complicated, break it down into easy steps or give an example.

**If the information is not available from your tools, or if you genuinely don't know, honestly state that you cannot find the details. Never make up facts, stories, or descriptions that are not provided by the tools.**

Keep your answers concise and easy to digest, typically within 3-4 short sentences.
""")        
        String chat(String userMessage);
    }

    public initLLM() {
        try {
            System.out.println("Creating Ollama model...");
            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            System.out.println("Building AI assistant...");
            assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .tools(new functionTools(), new signTools(), new paintingTools(),
                            new guideTools(), new ClevelandArtApiTools())
                    .build();
            reinitialize();
            System.out.println("LLM initialization complete");
        } catch (Exception e) {
            System.err.println("Error initializing LLM: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String processMessage(String userMessage) {
        try {
            System.out.println("[GUIDE] Processing user request: " + userMessage);
            if (assistant == null) {
                System.out.println("[GUIDE] Assistant is null!");
                return "LLM service is not properly initialized.";
            }
            System.out.println("[GUIDE] Sending to LLM...");
            String response = assistant.chat(userMessage);
            System.out.println("[GUIDE] Received response: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("[GUIDE] Error processing message: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, I couldn't process your request due to an error.";
        }
    }

    public CompletableFuture<String> processMessageAsync(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            return processMessage(userMessage);
        });
    }
    public static String getBASE_URL() {
        return BASE_URL;
    }

    public static void setModelNames(String[] modelNames) {
        initLLM.modelNames = modelNames;
    }
    public static void setModelName(String modelName) {
        initLLM.MODEL_NAME = modelName;
    }
    public static String getModelName()    {
        return MODEL_NAME;
    }
    // java
// In initLLM.java, modify the reinitialize() method
    public void reinitialize() {
        try {
            System.out.println("Re-initializing LLM service...");
            ChatLanguageModel model = OllamaChatModel.builder()
                    .baseUrl(BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(model)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .tools(new functionTools(), new signTools(), new paintingTools(),
                            new guideTools(), new ClevelandArtApiTools())
                    .build();

            System.out.println("LLM re-initialization complete");
        } catch (Exception e) {
            System.err.println("Error re-initializing LLM: " + e.getMessage());
            e.printStackTrace();
        }
    }


}