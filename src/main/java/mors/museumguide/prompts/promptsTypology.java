package mors.museumguide.prompts;

public class promptsTypology {
    private String prompt;

    // Aktuelle Instanz für statische Zugriffe
    private static promptsTypology currentInstance;

    // Statische Instanzen mit vordefiniertem Prompt-Text
    private static final promptsTypology layPerson;
    private static final promptsTypology educators;
    private static final promptsTypology experts;

    // Initialisiere die statischen Variablen
    static {
        layPerson = new promptsTypology();
        educators = new promptsTypology();
        experts = new promptsTypology();

        layPerson.prompt =
                """
                You are a friendly and entertaining virtual museum guide. Using only the information provided to you, explain what they are seeing in a fun and easy-to-understand way. Keep your explanation short, favorably to 1-2 sentences, avoid technical terms, and do not invent any facts or interpretations beyond the supplied content. If appropriate, you may highlight a surprising or curious detail from the material.
                """;

        educators.prompt =
                """
                    Only answer in slang.
                """;

        experts.prompt =
                """
                You are a scholarly virtual museum guide assisting an expert-level visitor. Provide a detailed and critical explanation strictly based on the available information. Include discussion of techniques, historical influences, and relevant artistic movements only if mentioned in the content. Do not introduce external facts, assumptions, or interpretations. If appropriate, you may reference scholarly debates—but only those explicitly provided in the source material.
                """;

        // Setze den Standard
        currentInstance = layPerson;
    }

    // Instanzmethode
    public String getPrompt() {
        return prompt;
    }

    // Setter für den Prompt
    public void setPrompt(String newPrompt) {
        this.prompt = newPrompt;
    }

    // Statische Methoden
    public static String getPromptByLevel(int level) {
        return switch (level) {
            case 1 -> layPerson.prompt;
            case 2 -> educators.prompt;
            case 3 -> experts.prompt;
            default -> layPerson.prompt;
        };
    }

    public static promptsTypology getPromptInstanceByLevel(int level) {
        return switch (level) {
            case 1 -> layPerson;
            case 2 -> educators;
            case 3 -> experts;
            default -> layPerson;
        };
    }

    public static void setPromptLevel(int level) {
        currentInstance = getPromptInstanceByLevel(level);
    }

    public static String getCurrentPrompt() {
        return currentInstance.prompt;
    }

    // Konstruktor für spezifische Level
    public promptsTypology() {
        // Leerer Konstruktor - wird durch static-Block befüllt
    }

    public promptsTypology(int level) {
        this.prompt = getPromptByLevel(level);
    }
}