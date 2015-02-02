package com.gradleware.tooling.toolingutils.distribution;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides information about the Gradle versions available from services.gradle.org.
 */
public final class PublishedGradleVersions {

    // end-point that provides full version information
    private static final String VERSIONS_URL = "https://services.gradle.org/versions/all";

    // the minimum Gradle version considered
    private static final String MINIMUM_SUPPORTED_GRADLE_VERSION = "1.0";

    // JSON keys
    private static final String VERSION = "version";
    private static final String SNAPSHOT = "snapshot";
    private static final String ACTIVE_RC = "activeRc";
    private static final String RC_FOR = "rcFor";
    private static final String BROKEN = "broken";

    // the file to cache the downloaded version information
    private static final File CACHE_FILE = new File("~/.tooling/gradle/versions.json");

    private static final Logger LOG = LoggerFactory.getLogger(PublishedGradleVersions.class);

    private final List<Map<String, String>> versions;

    private PublishedGradleVersions(List<Map<String, String>> versions) {
        this.versions = ImmutableList.copyOf(versions);
    }

    /**
     * Returns all final Gradle versions plus the latest active release candidate, if available.
     *
     * @return the matching versions
     */
    public List<GradleVersion> getVersions() {
        return FluentIterable.from(this.versions).filter(new Predicate<Map<String, String>>() {
            @Override
            public boolean apply(Map<String, String> input) {
                return (Boolean.valueOf(input.get(ACTIVE_RC)) || input.get(RC_FOR).equals("")) &&
                        !Boolean.valueOf(input.get(BROKEN)) &&
                        !Boolean.valueOf(input.get(SNAPSHOT));
            }
        }).transform(new Function<Map<String, String>, GradleVersion>() {
            @Override
            public GradleVersion apply(Map<String, String> input) {
                return GradleVersion.version(input.get(VERSION));
            }
        }).filter(new Predicate<GradleVersion>() {
            @Override
            public boolean apply(GradleVersion input) {
                return input.compareTo(GradleVersion.version(MINIMUM_SUPPORTED_GRADLE_VERSION)) >= 0;
            }
        }).toList();
    }

    /**
     * Creates a new instance based on the version information available on services.gradle.org. If caching is enabled, the version information is only retrieved remotely if the
     * version information is not cached or if the cached data has expired.
     *
     * @param enableCaching if {@code true} the version information is retrieved from the cache if available and if not outdated
     * @return the new instance
     */
    public static PublishedGradleVersions create(boolean enableCaching) {
        // try to read cached information if requested
        if (enableCaching) {
            if (CACHE_FILE.isFile() && CACHE_FILE.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)) {
                LOG.info("Found Gradle version information cache file that is not out-of-date. No remote download required.");
                try {
                    String json = Files.toString(CACHE_FILE, Charsets.UTF_8);
                    return create(json);
                } catch (IOException e) {
                    LOG.error("Cannot read found Gradle version information cache file. Remote download required.", e);
                    String json = downloadVersionInformation();
                    storeCacheFile(json);
                    return create(json);
                }
            } else {
                LOG.info("Gradle version information cache file is either not available or out-of-date. Remote download required.");
                String json = downloadVersionInformation();
                storeCacheFile(json);
                return create(json);
            }
        } else {
            // read versions from Gradle services end-point each time (do not cache result)
            String json = downloadVersionInformation();
            return create(json);
        }
    }

    private static String downloadVersionInformation() {
        try {
            return Resources.asCharSource(createURL(VERSIONS_URL), Charsets.UTF_8).read();
        } catch (IOException e) {
            throw new RuntimeException("Unable to download published Gradle versions.", e);
            // throw an exception if version information cannot be downloaded since we need this information
        }
    }

    private static void storeCacheFile(String json) {
        try {
            CharSource.wrap(json).copyTo(Files.asCharSink(CACHE_FILE, Charsets.UTF_8));
        } catch (IOException e) {
            LOG.error("Cannot write Gradle version information cache file.", e);
            // do not throw an exception if cache file cannot be written to be more robust against file system problems
        }
    }

    /**
     * Creates a new instance based on the provided version information.
     *
     * @return the new instance
     */
    public static PublishedGradleVersions create(String json) {
        // convert versions from JSON String to JSON Map
        Gson gson = new GsonBuilder().create();
        TypeToken<List<Map<String, String>>> typeToken = new TypeToken<List<Map<String, String>>>() {
        };
        List<Map<String, String>> versions = gson.fromJson(json, typeToken.getType());

        // create instance
        return new PublishedGradleVersions(versions);
    }

    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

}
