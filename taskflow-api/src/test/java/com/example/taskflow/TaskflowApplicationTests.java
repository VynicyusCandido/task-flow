package com.example.taskflow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requer um banco de dados PostgreSQL rodando. Desativado para CI de testes unitários até que o Testcontainers ou H2 seja configurado.")
class TaskflowApplicationTests {

	@Test
	void contextLoads() {
	}

}
