const MessageTypes = {
    TABLE_DATA: 1,
    LOG_MESSAGE: 2,
    GRAPH_DATA: 3,
    INITIAL_DATA: 4
};

const LogLevels = {
    INFO: "Info",
    WARN: "Warning",
    ERROR: "Error"
};

Object.freeze(MessageTypes);
Object.freeze(LogLevels);


var latestStats = new Map();
var log_queues = new Map();
var ids = new Map();
var tables;
var logs;
var graphs;

$(document).ready(function() {
    initializeSocket();
});


function initializeSocket() {
    var socket = new WebSocket("ws://" + location.hostname + ":8001");

    socket.onopen = function(e) {
        console.log("Connection established");
    };
    socket.onmessage = function(event) {
        var jsonData = JSON.parse(event.data);

        if (jsonData.type == MessageTypes.TABLE_DATA) {
            if (getDetails(jsonData.id).length == 0)
                createTable(jsonData.json, jsonData.id);
            else
                update(jsonData.json, jsonData.id);
        } else if (jsonData.type == MessageTypes.LOG_MESSAGE) {
            log_queues.get(parseInt(jsonData.id)).enqueue(jsonData.json);
        } else if (jsonData.type == MessageTypes.INITIAL_DATA) {
            setIds(jsonData.json);
        }


    };

    socket.onclose = function(event) {
        if (event.wasClean) {
            alert(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
        } else {

            alert('Connection died');
        }
    };

    socket.onerror = function(error) {
        alert(`[error] ${error.message}`);
    };

}

function setIds(data) {
    tables = $.map(data.tables, mapper);
    graphs = $.map(data.graphs, mapper);
    logs = $.map(data.logs, mapper);
    console.log(tables)
    console.table(tables);
    addToMap(tables)
    addToMap(graphs)
    addToMap(logs)
    console.log(ids)
    initializeWebPage();

}

function mapper(value, key) {
    return [
        [parseInt(key), value]
    ];
}

function addToMap(idsList) {
    for (let i = 0; i < idsList.length; i++) {
        ids.set(idsList[i][0], idsList[i][1].replace(/ /g, ''))
    }
}

function initializeWebPage() {

    if (tables.length != 0) {
        $('body#body').append('<h1><b>Tables</b></h1>');
        $('body#body').append('<div id=\"no_data\"class=\"no_data_div \"> No data </div>')
        var parentTable = $('<table/>', { id: "parent_table", class: "table table-borderless" })
        var tableBody = $('<tbody>')
        parentTable.append(tableBody);
        var i = 0;


        while (i < tables.length) {
            if ((i + 1) < tables.length) {

                var row = $('<tr/>')

                var col1 = $('<td/>', { id: tables[i][1].replace(/ /g, '') })
                var col2 = $('<td/>', { id: tables[i + 1][1].replace(/ /g, '') })

                row.append(col1)
                row.append(col2)

                var detail = $('<details/>', { id: 'parent_detail-' + tables[i][1].replace(/ /g, '') })
                detail.append('<summary , class="title_summary"><b>' + tables[i][1] + '</b></summary>')
                var detail1 = $('<details/>', { id: 'parent_detail-' + tables[i + 1][1].replace(/ /g, '') })
                detail1.append('<summary , class="title_summary" ><b>' + tables[i + 1][1] + '</b></summary>')
                col1.append(detail)
                col2.append(detail1)
                tableBody.append(row)

                i = i + 2;
            } else {
                var row = $('<tr/>')
                var col1 = $('<td/>', { id: tables[i][1].replace(/ /g, '') })
                row.append(col1)
                var detail = $('<details/>', { id: 'parent_detail-' + tables[i][1].replace(/ /g, '') })
                detail.append('<summary , class="title_summary"><b>' + tables[i][1] + '</b></summary>')
                col1.append(detail)
                tableBody.append(row)
                break;
            }
        }
        var div = $('<div/>', { id: "parent_table_div", class: "table_div" });
        div.append(parentTable)
        $('body#body').append(div);
        document.getElementById("parent_table_div").style.visibility = "hidden"
    }

    if (logs.length != 0) {
        $('body#body').append('<h1><b>Logs</b></h1>');
        var div = $('<div/>', { class: "table_div" });
        $('body#body').append(div)
        var logTable = $('<table/>', { id: "log_table", class: "table table-borderless" })
        div.append(logTable)

        for (let i = 0; i < logs.length; i++) {
            var detail = $('<details/>')
            detail.append('<summary class="title_summary"><b>' + logs[i][1] + '</b></summary>')
            var log_div = $('<div/>', { id: 'log_cat-' + logs[i][0], style: "overflow:auto", class: "log_cat_div" })
            detail.append(log_div);
            var row = $('<tr/>')
            row.append(detail)
            logTable.append(row)
            log_queue = new LogQueue('log_cat-' + logs[i][0]);
            log_queues.set(parseInt(logs[i][0]), log_queue)
            console.log(log_queues)
        }


    }

}

function createTable(stats, id) {

    Object.entries(stats).forEach((entry) => {
        const [key, value] = entry;
        var detail = $('<details/>', { id: "detail-" + id + key, class: "details" })
        detail.append('<summary style=\"word-break: break-all\" >' + key + '</summary>')
        var table = $('<table class=\"table table-borderless\">', { id: key + 'table' })
        var body = $('<tbody>', { id: key + 'body', class: "inner-table-body" })
        table.append(body)
        detail.append(table)
        Object.entries(value).forEach((entry1) => {
                const [key1, value1] = entry1;
                var row = $('<tr/>', { id: key1 + "row", Text: key1 })

                row.append("<td  id='key-" + key1 + "'>" + key1 + "</td>")
                row.append("<td style=\"word-break: break-all\" id=value-" + key + "-" + key1 + ">" + value1 + "</td>")
                body.append(row)

            })
            // $('td#' + ids.get(id)).append(detail)
        $('details#parent_detail-' + ids.get(id)).append(detail)
        addToggleListener(id, key)

    })

    var parent_table = document.getElementById('parent_table_div')
    if (parent_table.style.visibility === "hidden") {
        document.getElementById('no_data').style.display = "none"
        parent_table.style.visibility = "visible"
    }
}

function addToggleListener(id, key) {
    var detail = document.getElementById("detail-" + id + key)
    detail.addEventListener('toggle', () => {
        if (detail.open) {
            updateStats(detail);
        }
    })
}

function update(stats, id) {
    var opened = getDetails(id).filter(detail => detail.hasAttribute("open"))
    latestStats.set(id, stats)

    if (opened.length == 0)
        return

    Object.entries(stats).forEach((entry) => {
            const [key, value] = entry;
            var detail = document.getElementById("detail-" + id + key)
            if (opened.indexOf(detail) != -1) {
                Object.entries(value).forEach((entry1) => {
                    const [key1, value1] = entry1;
                    var row = document.getElementById("value-" + key + "-" + key1)
                    row.innerHTML = value1
                })
                console.log(key + " updated using currently received data")

            }
        }

    )

}

function getDetails(statsType) {

    var matches = [];

    var type = ids.get(statsType)

    var elements = document.getElementById('parent_detail-' + type).children;
    for (var i = 0; i < elements.length; i++) {

        if (elements[i].id.indexOf('detail-') == 0) {
            matches.push(elements[i]);
        }

    }
    return matches;
}

class LogQueue {
    constructor(log_div) {
        this.logs = [];
        this.isLoggerIsActive = false;
        this.log_div = log_div
    }

    enqueue(message) {
        this.logs.push(message);

        if (!this.isLoggerIsActive) {
            var queue = this
            this.logger = setInterval(function() { activateLogger(queue); }, 500);
            this.isLoggerIsActive = true;

        }
    }


    dequeue() {
        if (this.isEmpty()) {
            clearInterval(this.logger);
            this.isLoggerIsActive = false;
            return null;
        }
        return this.logs.shift();
    }

    isEmpty() {
        return this.logs.length == 0;
    }
}

function activateLogger(queue) {
    var log = queue.dequeue();
    var div = document.getElementById(queue.log_div)
    if (log == null)
        return
    const isScrolledToBottom = div.scrollHeight - div.clientHeight <= div.scrollTop + 1
    const newLogMessage = document.createElement("div")
    newLogMessage.innerHTML = "<p>" + log.time + "&ensp;" + log.logLevel + ":&emsp;" + log.logMessage + "</p>";
    if (log.logLevel == LogLevels.ERROR)
        newLogMessage.style.color = 'red'
    else if (log.logLevel == LogLevels.INFO)
        newLogMessage.style.color = 'lightblue'

    div.appendChild(newLogMessage);
    if (isScrolledToBottom) {
        div.scrollTop = div.scrollHeight - div.clientHeight
    }

    removeLogIfExceedLimit(queue.log_div);

    // scroll to bottom if isScrolledToBottom is true

}


function updateStats(detail) {
    var id = detail.id;
    var updatedStats = latestStats.get(parseInt(id[7]));
    if (updatedStats == null)
        return
    var rowTitle = id.substring(8);
    updateRow(updatedStats[rowTitle], rowTitle);
}

function updateRow(data, rowTitle) {
    Object.entries(data).forEach((entry) => {
        const [key, value] = entry;
        var row = document.getElementById("value-" + rowTitle + "-" + key)
        row.innerHTML = value

    })
    console.log(rowTitle + " updated using stored data")

}

function removeLogIfExceedLimit(div) {
    var len = $("#" + div).children().length;
    if (len >= 50) {
        $("#" + div).find('div').first().remove();
    }
}