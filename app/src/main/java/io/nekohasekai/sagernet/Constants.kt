package io.nekohasekai.sagernet

const val CONNECTION_TEST_URL = "http://cp.cloudflare.com/"

object Key {

    const val DB_PUBLIC = "configuration.db"
    const val DB_PROFILE = "sager_net.db"

    const val PERSIST_ACROSS_REBOOT = "isAutoConnect"

    const val APP_EXPERT = "isExpert"
    const val APP_THEME = "appTheme"
    const val NIGHT_THEME = "nightTheme"
    const val SERVICE_MODE = "serviceMode"
    const val MODE_VPN = "vpn"

    const val GLOBAL_CUSTOM_CONFIG = "globalCustomConfig"

    const val REMOTE_DNS = "remoteDns"
    const val DIRECT_DNS = "directDns"
    const val ENABLE_DNS_ROUTING = "enableDnsRouting"
    const val ENABLE_FAKEDNS = "enableFakeDns"

    const val IPV6_MODE = "ipv6Mode"

    const val PROXY_APPS = "proxyApps"
    const val BYPASS_MODE = "bypassMode"
    const val INDIVIDUAL = "individual"
    const val METERED_NETWORK = "meteredNetwork"

    const val TRAFFIC_SNIFFING = "trafficSniffing"
    const val RESOLVE_DESTINATION = "resolveDestination"

    const val BYPASS_LAN = "bypassLan"
    const val BYPASS_LAN_IN_CORE = "bypassLanInCore"

    const val MIXED_PORT = "mixedPort"
    const val ALLOW_ACCESS = "allowAccess"
    const val SPEED_INTERVAL = "speedInterval"
    const val SHOW_DIRECT_SPEED = "showDirectSpeed"

    const val APPEND_HTTP_PROXY = "appendHttpProxy"

    const val CONNECTION_TEST_URL = "connectionTestURL"

    const val NETWORK_CHANGE_RESET_CONNECTIONS = "networkChangeResetConnections"
    const val WAKE_RESET_CONNECTIONS = "wakeResetConnections"
    const val RULES_PROVIDER = "rulesProvider"
    const val LOG_LEVEL = "logLevel"
    const val LOG_BUF_SIZE = "logBufSize"
    const val MTU = "mtu"

    // Protocol Settings
    const val GLOBAL_ALLOW_INSECURE = "globalAllowInsecure"

    const val ACQUIRE_WAKE_LOCK = "acquireWakeLock"
    const val SHOW_BOTTOM_BAR = "showBottomBar"

    const val ALLOW_INSECURE_ON_REQUEST = "allowInsecureOnRequest"

    const val TUN_IMPLEMENTATION = "tunImplementation"
    const val PROFILE_TRAFFIC_STATISTICS = "profileTrafficStatistics"

    const val PROFILE_ID = "profileId"
    const val PROFILE_NAME = "profileName"
    const val PROFILE_GROUP = "profileGroup"
    const val PROFILE_CURRENT = "profileCurrent"

    const val SERVER_ADDRESS = "serverAddress"
    const val SERVER_PORT = "serverPort"

    const val PROTOCOL_VERSION = "protocolVersion"

    const val APP_TLS_VERSION = "appTLSVersion"
    const val ENABLE_CLASH_API = "enableClashAPI"

    const val COUNT_DOWN_REMAIN_TIME = "count_down_remain_time"
}

object TunImplementation {
    const val GVISOR = 0
    const val SYSTEM = 1
}

object IPv6Mode {
    const val DISABLE = 0
    const val ENABLE = 1
    const val PREFER = 2
    const val ONLY = 3
}

object GroupType {
    const val BASIC = 0
    const val SUBSCRIPTION = 1
}

object GroupOrder {
    const val ORIGIN = 0
}

object Action {
    const val SERVICE = "io.nekohasekai.sagernet.SERVICE"
    const val CLOSE = "io.nekohasekai.sagernet.CLOSE"
    const val RELOAD = "io.nekohasekai.sagernet.RELOAD"

    // const val SWITCH_WAKE_LOCK = "io.nekohasekai.sagernet.SWITCH_WAKELOCK"
    const val RESET_UPSTREAM_CONNECTIONS = "moe.nb4a.RESET_UPSTREAM_CONNECTIONS"

    const val CHECK_FOREGROUND_OR_BACKGROUND = "io.nekohasekai.sagernet.check_foreground_or_background"
}
