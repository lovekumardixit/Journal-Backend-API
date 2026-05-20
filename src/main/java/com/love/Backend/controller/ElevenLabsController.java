package com.love.Backend.controller;

import com.love.Backend.service.TextToSpeechService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Text-to-Speech APIs", description = "APIs for converting text to speech using ElevenLabs")
@RestController
public class ElevenLabsController {

    @Autowired
    private TextToSpeechService service;

    @Operation(summary = "Convert text to speech", description = "Generate audio from text input")
    @ApiResponse(responseCode = "200", description = "Audio generated successfully")
    @Parameter(name = "text", description = "Text to convert to speech", required = true)
    @GetMapping("/speak")
    public ResponseEntity<byte[]> speak(@RequestParam String text){
        byte[] audio = service.convertTextToSpeech(text);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(audio);
    }

}
