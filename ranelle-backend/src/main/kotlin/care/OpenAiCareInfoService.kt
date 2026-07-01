package care

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import web.dto.CareInfoResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

class OpenAiCareInfoService(
    private val apiKey: String? = configValue("OPENAI_API_KEY"),
    private val model: String = configValue("OPENAI_MODEL") ?: "gpt-4o-mini",
    private val promptId: String? = configValue("OPENAI_PROMPT_ID"),
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    suspend fun generate(commonName: String, scientificName: String?, notes: String?): CareInfoResponse {
        val key = apiKey?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("OPENAI_API_KEY is not configured")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/responses"))
            .timeout(Duration.ofSeconds(30))
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody(commonName, scientificName, notes)))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("OpenAI request failed with status ${response.statusCode()}")
        }

        return parseCareInfo(response.body())
    }

    private fun requestBody(commonName: String, scientificName: String?, notes: String?): String {
        val input = """
            Return care guidance for this plant for an indoor automatic plant monitor.
            Plant common name: $commonName
            Scientific name: ${scientificName ?: "unknown"}
            User notes: ${notes ?: "none"}

            Values must be practical ranges for common houseplant care:
            - soil moisture is percent, 0 to 100
            - temperature is Celsius
            - air moisture is relative humidity percent, 0 to 100
            - notes should be one concise paragraph with actionable care advice
        """.trimIndent()

        return json.encodeToString(
            OpenAiResponsesRequest(
                model = model.takeUnless { promptId?.isNotBlank() == true },
                prompt = promptId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { OpenAiPromptReference(id = it) },
                input = input,
                text = TextConfig(
                    format = JsonSchemaFormat(
                        name = "plant_care_info",
                        strict = true,
                        schema = buildJsonObject {
                            put("type", JsonPrimitive("object"))
                            put(
                                "additionalProperties",
                                JsonPrimitive(false),
                            )
                            put(
                                "required",
                                JsonArray(
                                    listOf(
                                        "idealMoistureMin",
                                        "idealMoistureMax",
                                        "idealTempMin",
                                        "idealTempMax",
                                        "idealAirMoistureMin",
                                        "idealAirMoistureMax",
                                        "careNotes",
                                    ).map(::JsonPrimitive),
                                ),
                            )
                            put(
                                "properties",
                                buildJsonObject {
                                    put("idealMoistureMin", careIntSchema(0, 100))
                                    put("idealMoistureMax", careIntSchema(0, 100))
                                    put("idealTempMin", careIntSchema(5, 45))
                                    put("idealTempMax", careIntSchema(5, 45))
                                    put("idealAirMoistureMin", careIntSchema(0, 100))
                                    put("idealAirMoistureMax", careIntSchema(0, 100))
                                    put(
                                        "careNotes",
                                        buildJsonObject {
                                            put("type", JsonPrimitive("string"))
                                        },
                                    )
                                },
                            )
                        },
                    ),
                ),
            ),
        )
    }

    private fun careIntSchema(min: Int, max: Int): JsonObject =
        buildJsonObject {
            put("type", JsonPrimitive("integer"))
            put("minimum", JsonPrimitive(min))
            put("maximum", JsonPrimitive(max))
        }

    private fun parseCareInfo(responseBody: String): CareInfoResponse {
        val root = json.parseToJsonElement(responseBody).jsonObject
        val outputText = root["output_text"]?.jsonPrimitive?.contentOrNull
            ?: root["output"]?.jsonArray
                ?.asSequence()
                ?.mapNotNull { output ->
                    output.jsonObject["content"]?.jsonArray?.firstNotNullOfOrNull { content ->
                        content.jsonObject["text"]?.jsonPrimitive?.contentOrNull
                    }
                }
                ?.firstOrNull()
            ?: throw IllegalStateException("OpenAI response did not include output text")

        val care = json.parseToJsonElement(outputText).jsonObject
        return CareInfoResponse(
            idealMoistureMin = care.getValue("idealMoistureMin").jsonPrimitive.int,
            idealMoistureMax = care.getValue("idealMoistureMax").jsonPrimitive.int,
            idealTempMin = care.getValue("idealTempMin").jsonPrimitive.int,
            idealTempMax = care.getValue("idealTempMax").jsonPrimitive.int,
            idealAirMoistureMin = care.getValue("idealAirMoistureMin").jsonPrimitive.int,
            idealAirMoistureMax = care.getValue("idealAirMoistureMax").jsonPrimitive.int,
            careNotes = care.getValue("careNotes").jsonPrimitive.contentOrNull ?: "",
        )
    }
}

private fun configValue(key: String): String? =
    System.getenv(key)
        ?: loadDotEnv()[key]

private fun loadDotEnv(): Map<String, String> {
    val dotEnvPath = Path.of(".env")
    if (!Files.isRegularFile(dotEnvPath)) return emptyMap()

    return Files.readAllLines(dotEnvPath)
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separatorIndex = line.indexOf('=')
            if (separatorIndex <= 0) return@mapNotNull null

            val key = line.substring(0, separatorIndex).trim()
            val value = line.substring(separatorIndex + 1).trim().trim('"', '\'')
            key to value
        }
        .toMap()
}

@Serializable
private data class OpenAiResponsesRequest(
    val model: String? = null,
    val prompt: OpenAiPromptReference? = null,
    val input: String,
    val text: TextConfig,
)

@Serializable
private data class OpenAiPromptReference(
    val id: String,
)

@Serializable
private data class TextConfig(
    val format: JsonSchemaFormat,
)

@Serializable
private data class JsonSchemaFormat(
    val type: String = "json_schema",
    val name: String,
    val strict: Boolean,
    val schema: JsonObject,
)
