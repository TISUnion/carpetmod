package carpet.utils;

import com.google.common.base.Suppliers;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class CommitHashFetcher
{
	private static final String COMMIT_HASH_FILE = "assets/carpet/commit_hash";
	private static final Supplier<Optional<String>> commitHash = Suppliers.memoize(CommitHashFetcher::getCommitHashImpl);

	private static Optional<String> getCommitHashImpl()
	{
		try
		{
			return Optional.of(IOUtils.toString(
					Objects.requireNonNull(CommitHashFetcher.class.getClassLoader().getResourceAsStream(COMMIT_HASH_FILE)),
					StandardCharsets.UTF_8
			).trim());
		}
		catch (NullPointerException | IOException e)
		{
			return Optional.empty();
		}
	}

	public static Optional<String> getCommitHash()
	{
		return commitHash.get();
	}
}
