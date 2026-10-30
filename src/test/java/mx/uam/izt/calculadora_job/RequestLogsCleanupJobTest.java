package mx.uam.izt.calculadora_job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RequestLogsCleanupJobTest {

	@TempDir
	Path tempDir;

	@Test
	void clearsActiveLogAndPersistsCleanupDate() throws Exception {
		Path activeLog = tempDir.resolve("api-requests.txt");
		Files.writeString(activeLog, "GET /health 200\n", StandardCharsets.UTF_8);
		RequestLogsCleanupJob job = new RequestLogsCleanupJob(
				activeLog,
				Clock.fixed(Instant.parse("2026-10-07T00:05:00Z"), ZoneOffset.UTC));

		job.run(null);

		assertEquals(0, Files.size(activeLog));
		Path deletionRecord = tempDir.resolve("api-requests-cleanup-2026-10-07.txt");
		assertTrue(Files.exists(deletionRecord));
		assertEquals(
				"2026-10-07T00:05:00Z" + System.lineSeparator(),
				Files.readString(deletionRecord, StandardCharsets.UTF_8));
	}

	@Test
	void createsActiveLogWhenItDoesNotExist() throws Exception {
		Path activeLog = tempDir.resolve("api-requests.txt");
		RequestLogsCleanupJob job = new RequestLogsCleanupJob(
				activeLog,
				Clock.fixed(Instant.parse("2026-10-07T00:05:00Z"), ZoneOffset.UTC));

		job.run(null);

		assertTrue(Files.exists(activeLog));
		assertFalse(Files.exists(tempDir.resolve("api-requests.txt.2026-10-07")));
		assertTrue(Files.exists(tempDir.resolve("api-requests-cleanup-2026-10-07.txt")));
	}
}
