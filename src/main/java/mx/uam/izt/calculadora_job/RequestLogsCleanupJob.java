package mx.uam.izt.calculadora_job;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class RequestLogsCleanupJob implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogsCleanupJob.class);
	private static final DateTimeFormatter LOG_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	private final Path requestLogFile;
	private final Clock clock;

	public RequestLogsCleanupJob(
			@Value("${cleanup.request-log.file:/app/logs/api-requests.txt}") String requestLogFile) {
		this(requiredLogPath(requestLogFile), Clock.systemUTC());
	}

	RequestLogsCleanupJob(Path requestLogFile, Clock clock) {
		if (requestLogFile == null || requestLogFile.getFileName() == null) {
			throw new IllegalArgumentException("Request log file must be configured");
		}
		this.requestLogFile = requestLogFile;
		this.clock = clock;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Path absoluteLogFile = requestLogFile.toAbsolutePath();
		Path logDirectory = absoluteLogFile.getParent();
		Files.createDirectories(logDirectory);
		Files.write(
				absoluteLogFile,
				new byte[0],
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE);

		Instant deletedAt = Instant.now(clock);
		LocalDate deletionDate = LocalDate.ofInstant(deletedAt, ZoneOffset.UTC);
		Path deletionRecord = logDirectory.resolve(
				"api-requests-cleanup-" + LOG_DATE.format(deletionDate) + ".txt");
		Files.writeString(
				deletionRecord,
				deletedAt.toString() + System.lineSeparator(),
				StandardCharsets.UTF_8);

		LOGGER.info("Cleared request log {} and recorded cleanup in {}", absoluteLogFile, deletionRecord);
	}

	private static Path requiredLogPath(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Request log file must be configured");
		}
		return Path.of(value);
	}
}
