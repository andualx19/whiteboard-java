var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var statusLabel = document.getElementById("status");

canvas.width = window.innerWidth;
canvas.height = window.innerHeight - 50;

var wsUrl = "ws://" + window.location.hostname + ":5000";
var ws = new WebSocket(wsUrl);

ws.onopen = function() {
    statusLabel.innerText = "Connected";
    statusLabel.style.color = "#00ff00";
};

ws.onclose = function() {
    statusLabel.innerText = "Disconnected";
    statusLabel.style.color = "#ff0000";
};

function toggleSidebar() {
    var toolbar = document.getElementById('toolbar');
    if (toolbar.classList.contains('left')) {
        toolbar.classList.remove('left');
        toolbar.classList.add('right');
    } else {
        toolbar.classList.remove('right');
        toolbar.classList.add('left');
    }
}

var currentTool = 'pen';
function setTool(tool) {
    currentTool = tool;
    if(tool === 'highlighter') {
        changeWidth(15);
    } else {
        changeWidth(2);
    }
}

function changeColor(val) {
    currentColor = val;
}

function changeWidth(val) {
    currentWidth = val;
}

function getCanvasPos(clientX, clientY) {
    var rect = canvas.getBoundingClientRect();

    var scaleX = canvas.width / rect.width;
    var scaleY = canvas.height / rect.height;

    return {
        x: (clientX - rect.left) * scaleX,
        y: (clientY - rect.top) * scaleY
    };
}

function drawAndSend(x, y) {
    var size = 5;
    var r = 0, g = 0, b = 0;

    ctx.fillStyle = "rgb(" + r + "," + g + "," + b + ")";
    ctx.beginPath();
    ctx.arc(x, y, size/2, 0, Math.PI * 2);
    ctx.fill();

    if (ws.readyState === WebSocket.OPEN) {
        ws.send(Math.round(x) + "," + Math.round(y) + "," + r + "," + g + "," + b + "," + size);
    }
}

function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ws.send("CLEAR");
}

var currentScale = 1.0;

function changeZoom(delta) {
    currentScale += delta;

    if (currentScale < 0.5) currentScale = 0.5;
    if (currentScale > 2.0) currentScale = 2.0;

    var canvas = document.getElementById('canvas');
    var newWidth = 794 * currentScale;

    canvas.style.width = newWidth + "px";
    canvas.style.height = "auto";
}

function stopDrawing() {
    ws.send("STOP");
}

canvas.addEventListener("mousedown", function(e) {
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawAndSend(pos.x, pos.y);
});

canvas.addEventListener("mousemove", function(e) {
    if (e.buttons !== 1) return;
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawAndSend(pos.x, pos.y);
});

canvas.addEventListener("mouseup", stopDrawing);

canvas.addEventListener("touchstart", function(e) {
    var touch = e.touches[0];
    var pos = getCanvasPos(touch.clientX, touch.clientY);
    drawAndSend(pos.x, pos.y);
});

canvas.addEventListener("touchmove", function(e) {
    e.preventDefault();
    var touch = e.touches[0];
    var pos = getCanvasPos(touch.clientX, touch.clientY);
    drawAndSend(pos.x, pos.y);
}, false);

canvas.addEventListener("touchend", stopDrawing);