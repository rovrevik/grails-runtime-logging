package grails.plugin.runtimelogging

import grails.converters.JSON

class LogTailingController {

    def logTailingService
    def logAdapterService

    def index() {}

    def getLogData() {
        def logData = null
        def output
        def lineAt
        if (params.startFromLines) {
            logData = getLatestLogItemsByLines(params.startFromLines as int)
        } else {
            logData = getLatestLogItemsByLines(null)
        }
        if (logData) {
            output = logData.output
            lineAt = logData.lineAt
        }
        def result = [log: output, lastLineNumber: lineAt]
        render result as JSON
    }

    private def getLatestLogItemsByLines(def fromLines) {
        def logData
        def appender = logAdapterService.getAppenderByName()
        if (appender?.path) {
            if (fromLines) {
                logData = logTailingService.getLastLines(appender.path, fromLines as int)
            } else {
                logData = logTailingService.getLog(appender.path)
            }
        }
        else
        {
            logData = ['output':'<br>&nbsp;&nbsp;&nbsp;Log does not appear to be set up or accessible.', 'lineAt':1]
        }
        return logData
    }

}
