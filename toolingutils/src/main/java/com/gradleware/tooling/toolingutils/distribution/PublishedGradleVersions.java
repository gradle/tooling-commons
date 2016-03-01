/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingutils.distribution;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gradleware.tooling.toolingutils.ImmutableCollection;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provides information about the Gradle versions available from services.gradle.org. The version information can optionally be cached on the local file system.
 *
 * @author Etienne Studer
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
    @ImmutableCollection
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
        if (enableCaching) {
            File cacheFile = getCacheFile();
            // allow to read from and write to cache file
            if (cacheFile.isFile() && cacheFile.exists()) {
                // if cache file exists, try to make use of it
                Optional<String> cachedVersions = readCacheVersionsFile(cacheFile);
                if (cacheFile.lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)) {
                    // cache file is current and its version information can be used
                    LOG.info("Gradle version information cache file is not out-of-date. No remote download required.");
                    if (cachedVersions.isPresent()) {
                        // local cache file is valid, use it
                        return create(cachedVersions.get());
                    } else {
                        // local cache file is not valid, download latest version information
                        LOG.info("Cannot read found Gradle version information cache file. Remote download required.");
                        String json = downloadVersionInformation();
                        storeCacheVersionsFile(json, cacheFile);
                        return create(json);
                    }
                } else {
                    // cache file is out-of-date, download latest version information, but fall back to cached version in case of download problems
                    LOG.info("Gradle version information cache file is out-of-date. Remote download required.");
                    String json;
                    try {
                        json = downloadVersionInformation();
                    } catch (RuntimeException e) {
                        if (cachedVersions.isPresent()) {
                            // download failed, but local cache file is valid, use it
                            return create(cachedVersions.get());
                        } else {
                            // download failed and local cache file is invalid, too
                            throw new RuntimeException("Cannot collect Gradle version information remotely nor locally.", e);
                        }
                    }
                    storeCacheVersionsFile(json, cacheFile);
                    return create(json);
                }
            } else {
                // if no previous cache file exists, download and cache the version information
                LOG.info("Gradle version information cache file is not available. Remote download required.");
                String json = downloadVersionInformation();
                storeCacheVersionsFile(json, cacheFile);
                return create(json);
            }
        } else {
            // read versions from Gradle services end-point each time (do not cache downloaded version information)
            String json = downloadVersionInformation();
            return create(json);
        }
    }

    private static String downloadVersionInformation() {
        HttpURLConnection connection = null;
        InputStreamReader reader = null;
        try {
            URL url = createURL(VERSIONS_URL);
            connection = (HttpURLConnection) url.openConnection();
            reader = new InputStreamReader(connection.getInputStream(), Charsets.UTF_8);
            return CharStreams.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Cannot download published Gradle versions.", e);
            // throw an exception if version information cannot be downloaded since we need this information
        } finally {
            try {
                Closeables.close(reader, false);
            } catch (IOException e) {
                LOG.warn("Can't close stream after downloading published Gradle versions", e);
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void storeCacheVersionsFile(String json, File cacheFile) {
        //noinspection ResultOfMethodCallIgnored
        cacheFile.getParentFile().mkdirs();

        try {
            CharSource.wrap(json).copyTo(Files.asCharSink(cacheFile, Charsets.UTF_8));
        } catch (IOException e) {
            LOG.warn("Cannot write Gradle version information cache file.", e);
            // do not throw an exception if cache file cannot be written to be more robust against file system problems
        }
    }

    private static Optional<String> readCacheVersionsFile(File cacheFile) {
        try {
            return Optional.of(Files.toString(cacheFile, Charsets.UTF_8));
        } catch (IOException e) {
            LOG.warn("Cannot read found Gradle version information cache file.", e);
            // do not throw an exception if cache file cannot be read to be more robust against file system problems
            return Optional.absent();
        }
    }

    /**
     * Creates a new instance based on the provided version information.
     *
     * @param json the json string containing the version information
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

    private static File getCacheFile() {
        return new File(System.getProperty("user.home"), ".tooling/gradle/versions.json");
    }

}
