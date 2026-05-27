# Skill: Create Integration Test

> Create an integration test for a module.

## Parameters

- `{module}`: Module being tested
- `{entity}`: Primary entity being tested

## Template

```java
package com.example.skillora_platform.{module};

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class {Entity}IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Clean up test data
    }

    @Test
    void shouldCreate{Entity}() throws Exception {
        mockMvc.perform(post("/api/v1/{module}s")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGet{Entity}ById() throws Exception {
        mockMvc.perform(get("/api/v1/{module}s/1"))
                .andExpect(status().isOk());
    }
}
```

## Placement

`src/test/java/com/example/skillora_platform/{module}/{Entity}IntegrationTest.java`
