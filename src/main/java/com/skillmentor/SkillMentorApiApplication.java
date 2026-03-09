package com.skillmentor;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
@org.springframework.scheduling.annotation.EnableScheduling
public class SkillMentorApiApplication {

	public static void main(String[] args) {
		System.out.println("=== SkillMentor API Startup Diagnostics ===");
		String userDir = System.getProperty("user.dir");
		System.out.println("Current Working Directory (CWD): " + userDir);

		// Paths to check
		String[] pathsToCheck = { "./.env", "./api/.env" };

		for (String path : pathsToCheck) {
			java.io.File envFile = new java.io.File(path);
			System.out.println(
					"Checking for .env at: " + envFile.getAbsolutePath() + " [Exists: " + envFile.exists() + "]");

			if (envFile.exists()) {
				try {
					Dotenv dotenv = Dotenv.configure()
							.directory(envFile.getParent())
							.filename(envFile.getName())
							.ignoreIfMissing()
							.load();

					dotenv.entries().forEach(entry -> {
						System.setProperty(entry.getKey(), entry.getValue());
						// Also put in env if possible (though System.setProperty is what Spring uses
						// for ${VAR})
					});
					System.out.println("Loaded variables from: " + envFile.getAbsolutePath());
				} catch (Exception e) {
					System.err.println("Failed to load .env from " + path + ": " + e.getMessage());
				}
			}
		}

		// Final Check
		String dbUrl = System.getProperty("DB_URL");
		if (dbUrl == null || dbUrl.isEmpty() || dbUrl.contains("${")) {
			System.err.println("!!! CRITICAL: DB_URL is NOT SET or is a placeholder: " + dbUrl);
		} else {
			System.out.println(">>> SUCCESS: DB_URL is active. <<<");
		}

		SpringApplication.run(SkillMentorApiApplication.class, args);
	}
}
