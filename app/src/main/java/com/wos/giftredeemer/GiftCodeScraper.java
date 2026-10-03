package com.wos.giftredeemer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GiftCodeScraper {

    public interface Callback {
        void success(List<String> codes);
        void error(String message);
    }

    private static final String PAGE_URL = "https://wostools.net/gift-codes";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 16) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/151.0.0.0 Mobile Safari/537.36";

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    /*
     * We deliberately parse the server HTML instead of using WebView.
     *
     * The current WoSTools markup is:
     *
     * <div class="known-code-item ...">
     *     ...
     *     <span class="known-code-text">CODE</span>
     *     ...
     *     <span class="known-code-status ...">Active</span>
     *     ...
     * </div>
     *
     * Pattern matching is limited to the known-codes-list section.
     */
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "<span\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bknown-code-text\\b[^\"']*[\"'][^>]*>" +
            "\\s*([^<]+?)\\s*</span>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern STATUS_PATTERN = Pattern.compile(
            "<span\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bknown-code-status\\b[^\"']*[\"'][^>]*>" +
            "(.*?)</span>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<[^>]+>",
            Pattern.DOTALL
    );

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor;
    private volatile boolean cancelled;

    public GiftCodeScraper(Activity activity) {
        // Kept for API compatibility with the existing app.
        // No WebView or Activity reference is needed for HTTP scraping.
    }

    public void fetch(Callback callback) {
        destroy();

        cancelled = false;
        executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                connection = openConnection();

                int statusCode = connection.getResponseCode();

                if (statusCode < 200 || statusCode >= 300) {
                    throw new Exception(
                            "WoSTools returned HTTP " + statusCode
                    );
                }

                String html = readAll(connection.getInputStream());

                if (cancelled) return;

                List<String> codes = parseActiveCodes(html);

                if (codes.isEmpty()) {
                    throw new Exception(
                            "Page loaded, but no active codes were found in the Known Gift Codes section."
                    );
                }

                deliverSuccess(callback, codes);

            } catch (Exception e) {
                if (!cancelled) {
                    String message = e.getMessage();

                    if (message == null || message.trim().isEmpty()) {
                        message = e.getClass().getSimpleName();
                    }

                    deliverError(
                            callback,
                            "Could not fetch gift codes: " + message
                    );
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private HttpURLConnection openConnection() throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(PAGE_URL).openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(true);

        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        );
        connection.setRequestProperty(
                "Accept-Language",
                "en-US,en;q=0.9"
        );
        connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
        );

        return connection;
    }

    private static String readAll(InputStream inputStream) throws Exception {
        StringBuilder html = new StringBuilder(350_000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            char[] buffer = new char[8192];
            int count;

            while ((count = reader.read(buffer)) != -1) {
                html.append(buffer, 0, count);
            }
        }

        return html.toString();
    }

    static List<String> parseActiveCodes(String html) {
        Set<String> result = new LinkedHashSet<>();

        if (html == null || html.isEmpty()) {
            return new ArrayList<>();
        }

        /*
         * Restrict parsing to Known Gift Codes.
         * This prevents similarly named elements elsewhere on the page
         * from being treated as redeemable codes.
         */
        int cardStart = indexOfIgnoreCase(
                html,
                "known-codes-card",
                0
        );

        if (cardStart < 0) {
            return new ArrayList<>();
        }

        int listStart = indexOfIgnoreCase(
                html,
                "known-codes-list",
                cardStart
        );

        if (listStart < 0) {
            return new ArrayList<>();
        }

        /*
         * Search each code text and then inspect the markup following that code
         * up to the next code text. This avoids trying to match nested DIVs
         * with a regex, which is unreliable.
         */
        Matcher codeMatcher = CODE_PATTERN.matcher(html);
        codeMatcher.region(listStart, html.length());

        List<CodePosition> foundCodes = new ArrayList<>();

        while (codeMatcher.find()) {
            String code = decodeHtml(codeMatcher.group(1)).trim();

            if (!code.isEmpty()) {
                foundCodes.add(
                        new CodePosition(
                                code,
                                codeMatcher.start(),
                                codeMatcher.end()
                        )
                );
            }
        }

        for (int i = 0; i < foundCodes.size(); i++) {
            CodePosition current = foundCodes.get(i);

            int sectionEnd =
                    (i + 1 < foundCodes.size())
                            ? foundCodes.get(i + 1).start
                            : findKnownCodesSectionEnd(
                                    html,
                                    current.end
                            );

            if (sectionEnd <= current.end) {
                sectionEnd = Math.min(
                        html.length(),
                        current.end + 5000
                );
            }

            String afterCode =
                    html.substring(current.end, sectionEnd);

            Matcher statusMatcher =
                    STATUS_PATTERN.matcher(afterCode);

            if (!statusMatcher.find()) {
                continue;
            }

            String status = stripTags(
                    statusMatcher.group(1)
            );

            status = decodeHtml(status)
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if ("active".equals(status)) {
                result.add(current.code);
            }
        }

        return new ArrayList<>(result);
    }

    private static int findKnownCodesSectionEnd(
            String html,
            int from
    ) {
        /*
         * Known codes are followed by the Auto-Redeem/How to Redeem area.
         * If the page layout changes and neither marker is found, simply
         * use the end of the document.
         */
        int autoRedeem = indexOfIgnoreCase(
                html,
                "Auto-Redeem",
                from
        );

        int howToRedeem = indexOfIgnoreCase(
                html,
                "How to Redeem",
                from
        );

        int end = html.length();

        if (autoRedeem >= 0) {
            end = Math.min(end, autoRedeem);
        }

        if (howToRedeem >= 0) {
            end = Math.min(end, howToRedeem);
        }

        return end;
    }

    private static int indexOfIgnoreCase(
            String text,
            String search,
            int from
    ) {
        return text.toLowerCase(Locale.ROOT).indexOf(
                search.toLowerCase(Locale.ROOT),
                Math.max(0, from)
        );
    }

    private static String stripTags(String value) {
        if (value == null) return "";
        return TAG_PATTERN.matcher(value).replaceAll("");
    }

    private static String decodeHtml(String value) {
        if (value == null) return "";

        /*
         * Gift codes themselves are plain text, but decode the common HTML
         * entities so the parser remains safe if text is escaped.
         */
        return value
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");
    }

    private void deliverSuccess(
            Callback callback,
            List<String> codes
    ) {
        mainHandler.post(() -> {
            if (!cancelled) {
                callback.success(codes);
            }
        });
    }

    private void deliverError(
            Callback callback,
            String message
    ) {
        mainHandler.post(() -> {
            if (!cancelled) {
                callback.error(message);
            }
        });
    }

    public void destroy() {
        cancelled = true;

        mainHandler.removeCallbacksAndMessages(null);

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private static final class CodePosition {
        final String code;
        final int start;
        final int end;

        CodePosition(
                String code,
                int start,
                int end
        ) {
            this.code = code;
            this.start = start;
            this.end = end;
        }
    }
}
