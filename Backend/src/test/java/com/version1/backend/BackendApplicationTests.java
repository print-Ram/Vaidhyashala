package com.version1.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
@EnableAutoConfiguration(exclude = {
    org.springframework.cloud.autoconfigure.RefreshAutoConfiguration.class
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
