package com.packetinspector.dpi.util;

import com.packetinspector.dpi.model.AppType;

import java.util.Locale;
import java.util.Map;

public final class ApplicationClassifier {
    private static final Map<AppType, String[]> DOMAIN_PATTERNS = Map.ofEntries(
            Map.entry(AppType.GOOGLE, new String[]{"google", "gstatic", "googleapis", "ggpht", "gvt1"}),
            Map.entry(AppType.YOUTUBE, new String[]{"youtube", "ytimg", "youtu.be", "yt3.ggpht"}),
            Map.entry(AppType.FACEBOOK, new String[]{"facebook", "fbcdn", "fb.com", "fbsbx", "meta.com"}),
            Map.entry(AppType.INSTAGRAM, new String[]{"instagram", "cdninstagram"}),
            Map.entry(AppType.WHATSAPP, new String[]{"whatsapp", "wa.me"}),
            Map.entry(AppType.TWITTER, new String[]{"twitter", "twimg", "x.com", "t.co"}),
            Map.entry(AppType.NETFLIX, new String[]{"netflix", "nflxvideo", "nflximg"}),
            Map.entry(AppType.AMAZON, new String[]{"amazon", "amazonaws", "cloudfront", "aws"}),
            Map.entry(AppType.MICROSOFT, new String[]{"microsoft", "msn.com", "office", "azure",
                    "live.com", "outlook", "bing"}),
            Map.entry(AppType.APPLE, new String[]{"apple", "icloud", "mzstatic", "itunes"}),
            Map.entry(AppType.TELEGRAM, new String[]{"telegram", "t.me"}),
            Map.entry(AppType.TIKTOK, new String[]{"tiktok", "tiktokcdn", "musical.ly", "bytedance"}),
            Map.entry(AppType.SPOTIFY, new String[]{"spotify", "scdn.co"}),
            Map.entry(AppType.ZOOM, new String[]{"zoom"}),
            Map.entry(AppType.DISCORD, new String[]{"discord", "discordapp"}),
            Map.entry(AppType.GITHUB, new String[]{"github", "githubusercontent"}),
            Map.entry(AppType.CLOUDFLARE, new String[]{"cloudflare", "cf-"}));

    public AppType fromDomain(String domain) {
        if (domain == null || domain.isEmpty()) return AppType.UNKNOWN;
        String normalized = domain.toLowerCase(Locale.ROOT);
        for (AppType app : AppType.values()) {
            String[] patterns = DOMAIN_PATTERNS.get(app);
            if (patterns != null && containsAny(normalized, patterns)) return app;
        }
        return AppType.HTTPS;
    }

    private static boolean containsAny(String domain, String[] patterns) {
        for (String pattern : patterns) {
            if (domain.contains(pattern)) return true;
        }
        return false;
    }
}
