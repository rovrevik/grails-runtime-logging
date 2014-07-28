<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Log Tailing</title>
    <meta name="layout" content="main"/>

    <script>
        if(typeof jQuery=='undefined') {
            var headTag = document.getElementsByTagName("head")[0];
            var jqTag = document.createElement('script');
            jqTag.type = 'text/javascript';
            jqTag.src = '//code.jquery.com/jquery-1.11.0.min.js';
            headTag.appendChild(jqTag);

            var headTag2 = document.getElementsByTagName("head")[0];
            var jqTag2 = document.createElement('script');
            jqTag2.type = 'text/javascript';
            jqTag2.src = '//code.jquery.com/jquery-migrate-1.2.1.min.js';
            headTag2.appendChild(jqTag2);
        }
    </script>

    <style type="text/css">
    .logContainer::-webkit-scrollbar {
        -webkit-appearance: none;
        width: 15px;
        height: 20px;
    }
    .logContainer::-webkit-scrollbar-thumb {
        background-color: rgba(0,0,0,.5);
        -webkit-box-shadow: 0 0 1px rgba(255,255,255,.5);
        background-color: slategray;
        height: 100px;
        border-radius: 15px;
    }
    .mainContainer {
        margin: 0;
        width: auto;
    }
    .logContainer {
        overflow: auto;
        padding: 5px;
        margin: 5px;
        background-color: #000000;
        color: #ffffff;
        height: 90%;
        min-height: 90%;
    }
    input[type='range'] {
        -webkit-appearance: none;
        /*border-radius: 5px;
        box-shadow: inset 0 0 5px #333;*/
        background-color: #999;
        height: 5px;
        vertical-align: middle;
    }
    input[type='range']::-moz-range-track {
        -moz-appearance: none;
        background-color: #999;
        height: 5px;
    }
    input[type='range']::-webkit-slider-thumb {
        -webkit-appearance: none !important;
        background-color: #FFF;
        border: 1px solid #999;
        height: 15px;
        width: 15px;
    }
    input[type='range']::-moz-range-thumb {
        -moz-appearance: none;
        background-color: #FFF;
        border: 1px solid #999;
        height: 15px;
        width: 15px;
    }
    </style>
</head>

<body>

<div style="margin-bottom: 5px; padding-bottom: 5px; border-bottom: 1px solid darkgray;">
    <input type="button" class="btn" value="Refresh" onclick="refreshLogContainer();">
    &nbsp;&nbsp;&nbsp;&nbsp;<label for="autoScroll">Auto-Scroll:</label><input id="autoScroll" type="checkbox" checked="checked" onclick="toggleAutoScroll();">
    &nbsp;&nbsp;&nbsp;&nbsp;<label for="autoRefresh">Auto-Refresh:</label><input id="autoRefresh" type="checkbox" onclick="toggleAutoRefresh();">
    &nbsp;&nbsp;&nbsp;&nbsp;<label for="speed">Auto-Refresh Speed:</label><input id="speed" type="range" min="1000" max="30000" value="3000" step="1000" onchange="setSpeed(value);" style="width: 100px;" /><output id="rangevalue" style="color: darkgrey; font-size: .9em"></output>
</div>
<div id="mainContainer" class="mainContainer clear" style="height: 74%; position: absolute; width: 98%;">
    <div id="logContainer" class="logContainer" style="height: 100%; border-radius: 10px; white-space: nowrap;"></div>
</div>


<script>
    var refreshInterval;
    var autoScroll;
    var scrolled = false;

    function setSpeed(v) {
        //clearInterval(refreshInterval);
        var speedInSecondsOutput = (v/1000);
        if (speedInSecondsOutput > 1) { speedInSecondsOutput += ' seconds'; }
        else { speedInSecondsOutput += ' second'; }
        $("#rangevalue").val(' ' + speedInSecondsOutput);
        //refreshInterval = setInterval(function() { refreshLogContainer(); }, $('#speed').val());
    }

    $(document).ready(function () {
        refreshLogContainer();
        toggleAutoRefresh();
        setSpeed(3000);
    });

    function updateScroll() {
        if (!scrolled) {
            var element = document.getElementById("logContainer");
            element.scrollTop = element.scrollHeight;
            $("#logContainer").css('border-bottom', '2px solid blue');
        }
    }

    $("#logContainer").on('scroll', function() {
        var out = document.getElementById("logContainer");
        var isScrolledToBottom = out.scrollHeight - out.clientHeight <= out.scrollTop + 1;
        scrolled = !isScrolledToBottom;
        if (!scrolled) {
            $("#logContainer").css('border-bottom', '2px solid blue');
        }
        else
        {
            $("#logContainer").css('border-bottom', 'inherit');
        }
    });

    function toggleAutoRefresh() {
        var autoRefreshButton = $("#autoRefresh");
        if (!(autoRefreshButton.attr('checked') === 'undefined')
                && autoRefreshButton.attr('checked') == "checked") {
            refreshInterval = setInterval(function() { refreshLogContainer(); }, $('#speed').val());
        }
        else
        {
            clearInterval(refreshInterval);
        }
    }

    function toggleAutoScroll() {
        autoScroll = ($("#autoScroll").attr('checked') == "checked");
        if (autoScroll) {
            scrollToBottom();
        }
    }

    function scrollToBottom() {
        var element = document.getElementById("logContainer");
        element.scrollTop = element.scrollHeight;
    }

    var startFromLines = '';

    function refreshLogContainer() {
        //console.log('Refreshing : ' + new Date());

        $.ajax({
            type: 'POST',
            dataType: 'JSON',
            url: '${createLink(controller: 'logTailing', action: 'getLogData')}',
            data: { startFromLines: startFromLines},
            success: function (data) {
                var d = data.log;
                $('#logContainer').append(d);
                updateScroll();
                startFromLines = data.lastLineNumber;
            }
        });
    }

</script>

</body>
</html>