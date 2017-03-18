package com.shekhargulati.urlcleaner;

import java.io.IOException;
import java.net.*;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * UrlCleaner provides API to normalize and unshorten URLs.
 * <p>
 * URL normalization is based on normalizations described in <a href="https://tools.ietf.org/html/rfc3986#section-6.">RFC 3986</a>.
 */
public abstract class UrlCleaner {

    private static final Map<String, Integer> DEFAULT_PORTS = Stream.of(
            new SimpleEntry<>("http", 80),
            new SimpleEntry<>("https", 443),
            new SimpleEntry<>("ftp", 21))
            .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    /**
     * Normalizes a URL in a standardized and consistent manner.
     *
     * @param inputUrl URL to process
     * @param options  option
     * @return normalized url
     */
    public static String normalizeUrl(final String inputUrl, final Options options) throws IllegalArgumentException {
        URI uri = Optional.ofNullable(inputUrl)
                .filter(u -> u.trim().length() > 0)
                .map(u -> u.replaceFirst("^//", "http://"))
                .map(URI::create)
                .map(u -> u.getScheme() == null ? URI.create("http://" + inputUrl) : u)
                .orElseThrow(() -> new IllegalArgumentException("url can't be empty of null."));

        String scheme = "http";
        String host = uri.getHost().toLowerCase();
        int port = -1; // URI does not add port iff it is -1
        if (uri.getPort() != DEFAULT_PORTS.get(scheme)) {
            port = uri.getPort();
        }
        if (options.isStripWWW()) {
            host = host.replaceFirst("^www\\.", "");
        }
        String fragment = uri.getFragment();
        if (options.isStripFragment()) {
            fragment = null;
        }
        String path = uri.getPath();
//        if (path != null) {
//            path = Paths.get(path).normalize().toString();
//        }
        String newUri = null;
        try {
            newUri = new URI(scheme, uri.getUserInfo(), host, port, path, sortQueryString(uri.getQuery()), fragment).normalize().toString();
            return newUri.replace(host, IDN.toUnicode(host)).replaceFirst("/$", "") + "/";
        } catch (URISyntaxException e) {
            throw new UrlCleanerException(e);
        }
    }

    /**
     * Normalizes a URL in a standardized and consistent manner.
     *
     * @param inputUrl URL to process
     * @return normalized url
     */
    public static String normalizeUrl(final String inputUrl) throws UrlCleanerException, IllegalArgumentException {
        return normalizeUrl(inputUrl, Options.DEFAULT_OPTIONS);
    }

    /**
     * Normalizes a list of urls
     *
     * @param inputUrls
     * @return normalized URLs
     * @throws UrlCleanerException
     * @throws IllegalArgumentException
     */
    public static List<String> normalizeUrl(final String... inputUrls) throws UrlCleanerException, IllegalArgumentException {
        return normalizeUrl(Arrays.asList(inputUrls), Options.DEFAULT_OPTIONS);
    }

    /**
     * Normalizes a list of urls
     *
     * @param inputUrls
     * @return normalized URLs
     * @throws UrlCleanerException
     * @throws IllegalArgumentException
     */
    public static List<String> normalizeUrl(final List<String> inputUrls) throws UrlCleanerException, IllegalArgumentException {
        return normalizeUrl(inputUrls, Options.DEFAULT_OPTIONS);
    }

    /**
     * Normalizes a list of urls using the give options
     *
     * @param urls    a list of urls
     * @param options option
     * @return list of normalized URLs
     * @throws UrlCleanerException
     * @throws IllegalArgumentException
     */
    public static List<String> normalizeUrl(List<String> urls, final Options options) throws UrlCleanerException, IllegalArgumentException {
        return urls.stream().map(inputUrl -> normalizeUrl(inputUrl, options)).collect(toList());
    }

    /**
     * Unshortens a given URL to its full form
     *
     * @param shortUrl short URL like bit.ly/abc
     * @return full URL
     */
    public static String unshortenUrl(final String shortUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(shortUrl).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();
        String url = connection.getHeaderField("Location");
        if (responseCode / 100 == 3 && url != null) {
            String expandedUrl = unshortenUrl(url);
            if (Objects.equals(expandedUrl, url))
                return url;
            else {
                return expandedUrl;
            }
        }
        return shortUrl;
    }

    private static String sortQueryString(final String query) {
        if (query == null || query.trim().length() == 0) {
            return null;
        }
        return Arrays.stream(query.split("&"))
                .map(p -> p.split("="))
                .map(p -> p.length == 2 ? new SimpleEntry<>(p[0], p[1]) : new SimpleEntry<>(p[0], ""))
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue())).collect(joining("&"));
    }

}
