<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title></title>

    <script src="//code.jquery.com/jquery-1.11.0.min.js"></script>
    <script src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>

    <style type="text/css">

    ::-webkit-scrollbar {
        -webkit-appearance: none;
        width: 15px;
        height: 20px;
    }
    ::-webkit-scrollbar-thumb {
        border-radius: 4px;
        background-color: rgba(0,0,0,.5);
        -webkit-box-shadow: 0 0 1px rgba(255,255,255,.5);
        background-color:slategray;
        height: 100px;
        border-radius: 0px;
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
    }

    .miniMapContainer {
        float: right;
        min-height: 50px;
        margin-left: 10px;
        maring-right: 10px;
        width: 150px;
        height: 90%;
    }
        
    .miniMap {
        height: 100%;
        padding: 5px;
        background-color: grey;
        opacity: .3;
    }
    </style>
</head>

<body>

<input type="button" value="Refresh" onclick="refreshLogContainer();">
&nbsp;&nbsp;&nbsp;<label for="autoRefresh">Auto-Refresh</label><input id="autoRefresh" type="checkbox" onclick="toggleAutoRefresh();">
&nbsp;&nbsp;&nbsp;<label for="autoScroll">Auto-Scroll</label><input id="autoScroll" type="checkbox" checked="checked" onclick=""toggleAuthScroll();>
&nbsp;&nbsp;&nbsp;is scrolled: <span id="isScrolled"></span>
<hr>

<div id="mainContainer" class="mainContainer clear">
    <!--
    <div id="miniMapContainer" class="miniMapContainer">
        <div class="miniMap">x
        </div>
    </div>
    -->
    <div id="logContainer" class="logContainer"></div>
</div>


<script>

    var refreshInterval;
    var autoScroll;
    var scrolled = false;

    $(document).ready(function () {
        toggleAutoRefresh();

        //debug
        setInterval(function() {
            $("#isScrolled").text(scrolled);
        }, 100);
    });

    function updateScroll() {
        if (!scrolled) {
            var element = document.getElementById("logContainer");
            element.scrollTop = element.scrollHeight;
            $("#logContainer").css('border-bottom', 'inherit');
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
            console.log('running on auto refresh');
            refreshInterval = setInterval(function() { refreshLogContainer(); }, 1000);
        }
        else
        {
            console.log('no auto refresh');
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
        $.ajax({
            type: 'POST',
            dataType: 'JSON',
            url: '${createLink(controller: 'runtimeLogging', action: 'tail_getLogData')}',
            data: { startFromLines: startFromLines},
            success: function (data) {
                var d = data.log.output;
                $('#logContainer').append(d);
                updateScroll();
                startFromLines = data.log.lineAt;
            }
        });
    }

</script>

</body>
</html>