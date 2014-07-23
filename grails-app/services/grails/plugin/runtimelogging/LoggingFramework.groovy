package grails.plugin.runtimelogging

/**
 * Created by rovrevik on 7/18/14.
 */
public enum LoggingFramework {
    LOG4J([logger:'org.apache.log4j.Logger', level:'org.apache.log4j.Level']),
    LOGBACK([logger:'ch.qos.logback.classic.Logger', level:'ch.qos.logback.classic.Level']),
    AUTO(),
    DISABLED()

    private final loggingClassNames = [:]

    LoggingFramework(loggingClassNames = [:]) {this.loggingClassNames = loggingClassNames}

    def getLoggingClassNames() {
        return loggingClassNames.asImmutable()
    }
}
