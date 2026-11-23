package com.packetinspector.dpi.model.entity;

public enum AppType {
    UNKNOWN("Unknown"),
    HTTP("HTTP"),
    HTTPS("HTTPS"),
    DNS("DNS"),
    TLS("TLS"),
    QUIC("QUIC"),
    GOOGLE("Google"),
    FACEBOOK("Facebook"),
    YOUTUBE("YouTube"),
    TWITTER("Twitter/X"),
    INSTAGRAM("Instagram"),
    NETFLIX("Netflix"),
    AMAZON("Amazon"),
    MICROSOFT("Microsoft"),
    APPLE("Apple"),
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram"),
    TIKTOK("TikTok"),
    SPOTIFY("Spotify"),
    ZOOM("Zoom"),
    DISCORD("Discord"),
    GITHUB("GitHub"),
    CLOUDFLARE("Cloudflare"),
    APP_COUNT("AppCount");

    private final String displayName;

    AppType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AppType fromDisplayName(String name) {
        if (name == null || name.isBlank()) return UNKNOWN;
        for (var t : values()) {
            if (t.name().equalsIgnoreCase(name) || t.displayName.equalsIgnoreCase(name)) {
                return t;
            }
        }
        return UNKNOWN;
    }

    public static AppType fromSni(String sni) {
        if (sni == null || sni.isBlank()) return UNKNOWN;
        var s = sni.toLowerCase(java.util.Locale.ROOT);
        if (s.contains("google") || s.contains("gstatic") || s.contains("googleapis") || s.contains("ggpht")
                || s.contains("gvt1")) return GOOGLE;
        if (s.contains("youtube") || s.contains("ytimg") || s.contains("youtu.be") || s.contains("yt3.ggpht")) return YOUTUBE;
        if (s.contains("facebook") || s.contains("fbcdn") || s.contains("fb.com") || s.contains("meta.com")) return FACEBOOK;
        if (s.contains("instagram") || s.contains("cdninstagram")) return INSTAGRAM;
        if (s.contains("twitter") || s.contains("twimg") || s.contains("t.co") || s.contains("x.com")) return TWITTER;
        if (s.contains("netflix") || s.contains("nflxvideo")) return NETFLIX;
        if (s.contains("amazon") || s.contains("aws") || s.contains("cloudfront")) return AMAZON;
        if (s.contains("microsoft") || s.contains("live.com") || s.contains("office.com") || s.contains("azure")) return MICROSOFT;
        if (s.contains("apple") || s.contains("icloud") || s.contains("aaplimg")) return APPLE;
        if (s.contains("whatsapp") || s.contains("whatsapp.net")) return WHATSAPP;
        if (s.contains("telegram") || s.contains("t.me")) return TELEGRAM;
        if (s.contains("tiktok") || s.contains("byteoversea")) return TIKTOK;
        if (s.contains("spotify") || s.contains("scdn.co")) return SPOTIFY;
        if (s.contains("zoom.us")) return ZOOM;
        if (s.contains("discord") || s.contains("discordapp")) return DISCORD;
        if (s.contains("github")) return GITHUB;
        if (s.contains("cloudflare")) return CLOUDFLARE;
        return HTTPS;
    }

    public String toDisplayName() {
        return displayName;
    }
}
