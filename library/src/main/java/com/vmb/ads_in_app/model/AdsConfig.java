package com.vmb.ads_in_app.model;

import java.util.ArrayList;
import java.util.List;

public class AdsConfig {

    private static AdsConfig adsConfig;

    public static AdsConfig getInstance() {
        if (adsConfig == null) {
            synchronized (AdsConfig.class) {
                adsConfig = new AdsConfig();
            }
        }
        return adsConfig;
    }

    public static void setInstance(AdsConfig popup) {
        adsConfig = popup;
    }

    private AdsConfig.config config;

    public AdsConfig.config getConfig() {
        return this.config;
    }

    public class config {
        private int time_start_show_popup = 0;
        private int offset_time_show_popup = 0;
        private int open_app_show_popup = 0;
        private int close_app_show_popup = 0;
        private int show_banner_ads = 0;

        public int getTime_start_show_popup() {
            return time_start_show_popup;
        }

        public int getOffset_time_show_popup() {
            return offset_time_show_popup;
        }

        public int getOpen_app_show_popup() {
            return open_app_show_popup;
        }

        public int getClose_app_show_popup() {
            return close_app_show_popup;
        }

        public int getShow_banner_ads() {
            return show_banner_ads;
        }
    }

    private int update_status = 0;
    private String update_title = "";
    private String update_message = "";
    private String update_url = "";

    public int getUpdate_status() {
        return update_status;
    }

    public String getUpdate_title() {
        return update_title;
    }

    public String getUpdate_message() {
        return update_message;
    }

    public String getUpdate_url() {
        return update_url;
    }

    private List<AdsConfig.shortcut> shortcut = new ArrayList<>();

    public List<AdsConfig.shortcut> getShortcut() {
        return shortcut;
    }

    public class shortcut {
        private String name = "";
        private String icon = "";
        private String url = "";
        private String packg = "";

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public String getUrl() {
            return url;
        }

        public String getPackg() {
            return packg;
        }
    }

    private List<AdsConfig.ads> ads = new ArrayList<>();

    public List<AdsConfig.ads> getAds() {
        return ads;
    }

    public class ads {
        private String type = "";

        public String getType() {
            return type;
        }

        private AdsConfig.ads.key key;

        public AdsConfig.ads.key getKey() {
            return key;
        }

        public class key {
            private String appid = "";
            private String banner = "";
            private String popup = "";
            private String video = "";
            private String thumbai = "";

            public String getAppid() {
                return appid;
            }

            public String getBanner() {
                return banner;
            }

            public String getPopup() {
                return popup;
            }

            public String getVideo() {
                return video;
            }

            public String getThumbai() {
                return thumbai;
            }
        }
    }

    private String link_share = "";
    private String link_more_apps = "";
    private String link_privacy_policy = "";

    public String getLink_share() {
        return link_share;
    }

    public String getLink_more_apps() {
        return link_more_apps;
    }

    public String getLink_privacy_policy() {
        return link_privacy_policy;
    }
}