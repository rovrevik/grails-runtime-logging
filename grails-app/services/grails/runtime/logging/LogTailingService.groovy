package grails.runtime.logging

class LogTailingService {

    def getLastLines(filePathAndName, int startFromLines) {
        def returnValue = [:]
        def output = ''
        int lineCount = countLines(filePathAndName) as int
        int linesToGet = lineCount - startFromLines
        def cmd = ["bash", "-c", "cat ${filePathAndName}| tail -${linesToGet}"].execute().text.eachLine { output += '<div>' + it + '</div>' }
        returnValue['output'] = output
        returnValue['lineAt'] = lineCount
        return returnValue
    }

    def getAll(filePathAndName, int max = 100) {
        def returnValue = [:]
        def output = ''
        int lineCount = countLines(filePathAndName) as int
        if (lineCount > max) {
            output = getLastLines(filePathAndName, max)
        } else {
            def cmd = ["bash","-c","cat ${filePathAndName}"].execute().text.eachLine { output += '<div>' + it + '</div>' }
        }
        returnValue['output'] = output
        returnValue['lineAt'] = lineCount
        return returnValue
    }

    def countLines(filePathAndName) {
        def lineCount = ["bash","-c","cat ${filePathAndName}| wc -l | awk '{print \$1}'"].execute().text
        return lineCount.trim()
    }

}
