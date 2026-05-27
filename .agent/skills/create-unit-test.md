# Skill: Create Unit Test

> Create a unit test for a service class.

## Template

```java
package com.example.skillora_platform.{module}.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {

    @Mock
    private {Entity}Repository repository;

    @InjectMocks
    private {Entity}Service service;

    @Test
    void shouldCreate{Entity}() {
        // given
        // when
        // then
    }
}
```
