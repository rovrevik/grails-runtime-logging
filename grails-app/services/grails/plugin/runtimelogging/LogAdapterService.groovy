package grails.plugin.runtimelogging

import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * All logging implementation stuff should go in this service or, at least, NOT in the controller.
 *
 * TODO separate into different service implementations that don't need to continually do Class.forName.
 */

class LogAdapterService {
    static transactional = false
    def loggingFramwork
    def loggingClasses = [:]

    def setLoggerLevel(String loggerName, String levelName) {
        def level
        def logger

        withLoggerClass({
            logger = loggerName ? loggingClasses.logger.getLogger(loggerName) : loggingClasses.logger.getRootLogger()
            level = loggingClasses.level.toLevel(levelName)
            logger.level = level

        }, {
            logger = loggerName ? LoggerFactory.getLogger(loggerName) : LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            level = loggingClasses.level.toLevel(levelName)
            logger.level = level
        })

        return [logger: loggerName, level: level]
    }

    String getEffectiveLevel(String loggerName) {
        withLoggerClass({
            loggingClasses.logger.getLogger(loggerName).getEffectiveLevel()
        }, {
            LoggerFactory.getLogger(loggerName).getEffectiveLevel()
        })
    }

    def withLoggerClass(log4jClosure, logBackClosure) {
        switch (loggingFramwork) {
            case LoggingFramework.LOG4J:
                log4jClosure()
            case LoggingFramework.LOGBACK:
                logBackClosure()
        }
    }
}
