# LLM + GuideBot Configuration Notes

## Model Setup

* Models are run using [Ollama](https://ollama.com/) — requires a local instance.
* Any model can be used as long as it supports **TOOL functionality**.

  * See available models: [https://ollama.com/search?c=tools](https://ollama.com/search?c=tools)
* **Model used in thesis:** `llama3.1:latest (7B)`

* Default URL to connect to Ollama server: http://localhost:11434

## Commands Overview

### Configuration Commands

* **Set model name**

  ```
  /llm setmodelname <modelname> 
  ```
  (Default is llama3.1:latest)
* **Guide interactions**

  * Right-click for interaction, puts `@GuideBot` in the chat.
  * You can talk to the guide when `@GuideBot` is in the chat.

## Current GuideBot Functions

| Command                                             | Description                                                               | Notes                                                                                             |
| --------------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| `"follow me"`                                       | Makes the guide follow the player.                                        | —                                                                                                 |
| `"tell me more about this painting IN FRONT OF ME"` | Provides information about the painting currently in front of the player. | The phrase **IN FRONT OF ME** is required — smaller models had trouble calling the API otherwise. |
| `"tell me more about artist X"`                     | Retrieves information about a specific artist.                            | May not work if limited data is available.                                                        |
| `"go to coordinate xyz"`                            | Moves the guide to the specified coordinates.                             | —                                                                                                 |
| `"tell me more about the painting X by artist Y"`   | Provides information about a specific painting and artist combination.    | —                                                                                                 |

## Notes

* Ensure the selected model supports the TOOL API required by GuideBot.
* When using smaller models, include explicit context phrases (e.g., **IN FRONT OF ME**) to improve API-calling reliability.
