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
    def logTailingAppenderName
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

        [logger: loggerName, level: level]
    }

    String getEffectiveLevel(String loggerName) {
        withLoggerClass({
            loggingClasses.logger.getLogger(loggerName).getEffectiveLevel()
        }, {
            LoggerFactory.getLogger(loggerName).getEffectiveLevel()
        })
    }

    def getAppenderByName() {
        def appenderName = logTailingAppenderName
        def appender
        withLoggerClass({
            loggingClasses.logger.getRootLogger().loggerRepository.currentLoggers.each {
                def a = it.getAppender(appenderName)
                if (a.hasProperty('name') && a.hasProperty('file')) {
                    appender = [name: a.name, properties: [file: a.file]]
                }
            }
        }, {
            LoggerFactory.getILoggerFactory().getLoggerList().each { logger ->
                def a = logger.getAppender(appenderName)
                if (a && logger.getLevel() != null) {
                    if (a.hasProperty('name') && a.hasProperty('file')) {
                        appender = [name: a.name, properties: [file: a.file]]
                    }
                }
            }
        })
        [name: appender?.name, path: appender?.properties?.file]
    }

    def withLoggerClass(log4jClosure, logBackClosure) {
        switch (loggingFramwork) {
            case LoggingFramework.LOG4J:
                log4jClosure()
                break
            case LoggingFramework.LOGBACK:
                logBackClosure()
                break
        }
    }

}
