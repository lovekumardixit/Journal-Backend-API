package com.love.Backend.controller;


import com.love.Backend.entity.entry;
import com.love.Backend.enums.Sentiment;
import com.love.Backend.exception.ResourceNotFoundException;
import com.love.Backend.exception.BadRequestException;
import com.love.Backend.service.BackendEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Tag(name = "Sentiment APIs", description = "APIs for retrieving entries by sentiment")
@RestController
@RequestMapping("/sentiment")
public class sentimentController {

    @Autowired
    private BackendEntryService entryService;

    @Operation(summary = "Get entries by sentiment", description = "Retrieve entries filtered by sentiment value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entries found"),
            @ApiResponse(responseCode = "400", description = "Invalid sentiment value"),
            @ApiResponse(responseCode = "404", description = "No entries found")
    })
    @Parameter(name = "sentiment", description = "Sentiment value (e.g., SAD, HAPPY)", required = true)
    @GetMapping
    public ResponseEntity<List<entry>> getUsersBySentiment(@RequestParam String sentiment){

        if(sentiment == null || sentiment.isEmpty()){
            throw new BadRequestException("Sentiment is required");
        }
        Sentiment s;
        try {
            s = Sentiment.valueOf(sentiment.toUpperCase());
        }catch (Exception e){
            throw new BadRequestException("Invalid sentiment value");
        }
        List<entry> entries = entryService.getEntriesBySentiment(s);

        if(entries.isEmpty()){
            throw new ResourceNotFoundException("No entries found");
        }
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }
}
