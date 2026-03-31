package com.wms.ems.employee.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandlerTest.DummyController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    @RequestMapping("/dummy")
    static class DummyController {
        @GetMapping("/notfound")
        public void notFound() {
            throw new ResourceNotFoundException("Not found");
        }
        @GetMapping("/illegal")
        public void illegal() {
            throw new IllegalArgumentException("Illegal arg");
        }
        @PostMapping("/validate")
        public void validate(@Valid @RequestBody DummyRequest req) {
        }
    }
    static class DummyRequest {
        @NotBlank
        public String field;
    }

    @Test
    void testHandleResourceNotFoundException_Returns404() throws Exception {
        mockMvc.perform(get("/dummy/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Not found")));
    }

    @Test
    void testHandleIllegalArgumentException_Returns400() throws Exception {
        mockMvc.perform(get("/dummy/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Illegal arg")));
    }

    @Test
    void testHandleValidationException_Returns400() throws Exception {
        DummyRequest req = new DummyRequest();
        req.field = "";
        mockMvc.perform(post("/dummy/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed")));
    }
}
