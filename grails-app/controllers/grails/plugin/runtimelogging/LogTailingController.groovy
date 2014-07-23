package grails.plugin.runtimelogging

import grails.converters.JSON
import org.apache.log4j.Logger

class LogTailingController {

    def logTailingService

    def index() { }

    def getLogData() {
        def logData
        if (params.startFromLines) {
            logData = getLatestLogItemsByLines(params.startFromLines as int)
        }
        else {
            logData = getCompleteLog()
        }
        def result = [log: logData.output, lastLineNumber: logData.lineAt]
        render result as JSON
    }

    public def getLogPaths() {
        def result = []
        Logger.getRootLogger().loggerRepository.currentLoggers.each {
            (it as Logger).allAppenders.each { appender ->
                def name = appender.name
                def path = appender.properties.file
                result.add(name:name, path:path)
            }
        }
        return result
    }

    public def getCompleteLog() {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def logData = logTailingService.getAll(path)
        return logData
    }

    public def getLatestLogItemsByLines(def fromLines) {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def logData = logTailingService.getLastLines(path, fromLines as int)
        return logData
    }

    public def getLatestLogItemsByTime(def fromTime) {
        def allLogPaths = getLogPaths()
        String path = allLogPaths[0].path
        def lastModified = new Date(new File(path).lastModified())
        def logData = ""
        if ((lastModified > fromTime) || fromTime == null) {
            logData = logTailingService.getAll(path)
        }
        return logData
    }

}
