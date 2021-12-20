'use strict';

let isLogPollEnabled = false;
let lastLogId = -1;
let lastLogDate;

let term;
let terminalIO;

function initContent() {
    setInterval(function() { getLatestLogs()} , 1000);
}

function getLatestLogs () {

    if (isLogPollEnabled) {

        let url = window.location.protocol + '//' + window.location.host + '/term-log';

        let xhr = new XMLHttpRequest();

        xhr.open("POST", url);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.setRequestHeader("Content-Type", "application/json");

        xhr.onreadystatechange = function () {

            if (xhr.readyState === 4) {

                let logResponse = JSON.parse(xhr.responseText);
                let isSuccessfulResponse = logResponse.success;

                if (isSuccessfulResponse) {

                    let logListObj = logResponse.logDataList;

                    for (let i = 0; i < logListObj.length; i++) {

                        let logLine = logListObj[i];
                        let logDate = new Date(logLine.date);

                        if (isLogLineShouldBePrinted(logLine)) {

                            terminalIO.println(logDate.toLocaleString() + ' ' + logDate.getMilliseconds()
                                + 'ms' + ' [' + logLine.host + '] [' + logLine.program + '] ' + '[' + logLine.id + '] '
                                + logLine.message);

                            lastLogId = logLine.id;
                            lastLogDate = logLine.date;
                        }
                    }
                } else {
                    terminalIO.println(logResponse.errorMessage);
                }
            }};

        let selectedHostElement = $("#selected-host option:selected");
        let hostIdValue = selectedHostElement.attr('value');

        let logRequest = {
            logId: lastLogId,
            hostId: hostIdValue === "" ? "All" : hostIdValue,
            hostName: selectedHostElement.text(),
            programName: $("#selected-program option:selected").text()
        }

        xhr.send(JSON.stringify(logRequest));
    }
}

function isLogLineShouldBePrinted(logline) {
    return lastLogId === undefined || logline.id > lastLogId || lastLogDate === undefined || logline.date > lastLogDate;
}

function logParameterChanged() {

    isLogPollEnabled = false;
    lastLogId = undefined;

    setupHterm();

    isLogPollEnabled = true;
}

function setupHterm() {

    term = new hterm.Terminal();

    term.onTerminalReady = function() {
        terminalIO = this.io.push();
        function printPrompt() {
            terminalIO.print(
                '\x1b[38:2:51:105:232mh' +
                '\x1b[38:2:213:15:37mt' +
                '\x1b[38:2:238:178:17me' +
                '\x1b[38:2:51:105:232mr' +
                '\x1b[38:2:0:153:37mm' +
                '\x1b[38:2:213:15:37m>' +
                '\x1b[0m ');
        }

        terminalIO.onVTKeystroke = (string) => {
            switch (string) {
                case '\r':
                    terminalIO.println('');
                    printPrompt();
                    break;
                case '\x7f':
                    // \x08 = backspace, \x1b[K = 'Erase in line'.
                    terminalIO.print('\x08\x1b[K');
                    break;
                default:
                    terminalIO.print(string);
                    break;
            }
        };
        terminalIO.sendString = terminalIO.print;
        isLogPollEnabled = true;
    };
    term.decorate(document.querySelector('#terminal'));

    // Useful for console debugging.
    window.term_ = term;
}

window.onload = async function() {
    await lib.init();
    setupHterm();
    initContent();
};