package com.gradleware.tooling.toolingutils.distribution;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
     * Creates a new instance based on version information available on services.gradle.org.
     *
     * @return the new instance
     */
    public static PublishedGradleVersions create() {
        // read versions from Gradle services end-point
        String json;
        try {
            CharSource charSource = Resources.asCharSource(createURL(VERSIONS_URL), Charset.forName("UTF-8"));
            json = charSource.read();
        } catch (IOException e) {
            throw new RuntimeException("Unable to download published Gradle versions.", e);
        }
        return create(json);
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
