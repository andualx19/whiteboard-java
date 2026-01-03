var currentColor = "#000000";
var currentWidth = 5;
var currentScale = 1.0;

var lastX = null;
var lastY = null;
var isDrawing = false;
var initialDist = null;

var baseWidth = 794;
var baseHeight = 1123;

var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var statusLabel = document.getElementById("status");

// Optimize clarity (DPR)
var dpr = window.devicePixelRatio || 1;
canvas.width = baseWidth * dpr;
canvas.height = baseHeight * dpr;
canvas.style.width = baseWidth + "px";
canvas.style.height = baseHeight + "px";
ctx.scale(dpr, dpr);

// Websocket connection
var wsUrl = "ws://" + window.location.hostname + ":5000";
var ws = new WebSocket(wsUrl);
ws.binaryType = "arraybuffer";

ws.onopen = function() {
    statusLabel.innerText = "Connected";
    statusLabel.style.color = "#00ff00";
};

ws.onclose = function() {
    statusLabel.innerText = "Disconnected";
    statusLabel.style.color = "#ff0000";
};

ws.onmessage = function(event) {
    if (event.data instanceof ArrayBuffer) {
        var view = new DataView(event.data);
        var x = view.getUint16(0);
        var y = view.getUint16(2);
        var r = view.getUint8(4);
        var g = view.getUint8(5);
        var b = view.getUint8(6);
        var width = view.getUint8(7);
        var type = view.getUint8(8);

        if (type === 1) { // DRAW
            drawRemoteLine(x, y, "rgb("+r+","+g+","+b+")", width);
        }
    }
};

var remoteLastX = {};
function drawRemoteLine(x, y, color, width) {
    ctx.lineCap = "round";
    ctx.lineJoin = "round";
    ctx.strokeStyle = color;
    ctx.lineWidth = width;

    ctx.beginPath();

    if (remoteLastX.main) {
        ctx.moveTo(remoteLastX.main.x, remoteLastX.main.y);
        ctx.lineTo(x, y);
    } else {
        ctx.moveTo(x, y);
        ctx.lineTo(x, y);
    }
    ctx.stroke();

    remoteLastX.main = { x: x, y: y };
}

// Coordonates and drawindg
function getCanvasPos(clientX, clientY){
    var rect = canvas.getBoundingClientRect();

    var scaleX = baseWidth / rect.width;
    var scaleY = baseHeight / rect.height;

    return {
        x: (clientX - rect.left) * scaleX,
        y: (clientY - rect.top) * scaleY
    };
}

function drawLine(x, y) {
    ctx.lineCap = "round";
    ctx.lineJoin = "round";
    ctx.lineWidth = currentWidth;
    ctx.strokeStyle = currentColor;

    ctx.beginPath();
    if (lastX !== null && lastY !== null) {
        ctx.moveTo(lastX, lastY);
        ctx.lineTo(x, y);
    } else {
        ctx.moveTo(x, y);
        ctx.lineTo(x, y);
    }
    ctx.stroke();

    if (ws.readyState === WebSocket.OPEN) {
        var rgb = hexToRgb(currentColor);

        var buffer = new ArrayBuffer(9);
        var view = new DataView(buffer);

        view.setUint16(0, Math.round(x));
        view.setUint16(2, Math.round(y));
        view.setUint8(4, rgb[0]);
        view.setUint8(5, rgb[1]);
        view.setUint8(6, rgb[2]);
        view.setUint8(7, currentWidth);
        view.setUint8(8, 1);

        ws.send(buffer);
    }

    lastX = x;
    lastY = y;
}

// Touch and zoom
canvas.addEventListener("touchstart", function(e){
    if(e.touches.length === 1){
        isDrawing = true;
        var pos = getCanvasPos(e.touches[0].clientX, e.touches[0].clientY);
        drawLine(pos.x, pos.y);
        e.preventDefault();
    } else if (e.touches.length === 2) {
        isDrawing = false;
        initialDist = Math.hypot(
            e.touches[0].pageX - e.touches[1].pageX,
            e.touches[0].pageY - e.touches[1].pageY
        );
    }
}, {passive: false});

canvas.addEventListener("touchmove", function(e){
    if(isDrawing && e.touches.length === 1){
        var pos = getCanvasPos(e.touches[0].clientX, e.touches[0].clientY);
        drawLine(pos.x, pos.y);
        e.preventDefault();
    } else if (e.touches.length === 2) {
        var currentDist = Math.hypot(
            e.touches[0].pageX - e.touches[1].pageX,
            e.touches[0].pageY - e.touches[1].pageY
        );
        var delta = (currentDist > initialDist) ? 0.02 : -0.02;
        changeZoom(delta);
        initialDist = currentDist;
        e.preventDefault();
    }
}, {passive: false});

// Controll functions

// ZOOM
function changeZoom(delta) {
    currentScale += delta;

    if(currentScale < 0.5) currentScale = 0.5;
    if(currentScale > 3.0) currentScale = 3.0;

    canvas.style.width = (baseWidth * currentScale) + "px";
    canvas.style.height = (baseHeight * currentScale) + "px";

    if(ws.readyState === WebSocket.OPEN) {
        ws.send("ZOOM," + currentScale.toFixed(2));
    }
}

// STOP
function stopDrawing() {
    isDrawing = false;
    initialDist = null;
    lastX = null;
    lastY = null;
    remoteLastX.main = null;
    if(ws.readyState === WebSocket.OPEN) ws.send("STOP");
}

// CLEAR
function clearCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.beginPath();
    if(ws.readyState === WebSocket.OPEN) ws.send("CLEAR");
}

// Menus & UI
var widthMenu = document.getElementById('widthMenu');
for (var i = 2; i <= 30; i += 2) {
    var option = document.createElement('option');
    option.value = i;
    option.text = i + " px";
    if (i === 6) option.selected = true;
    widthMenu.appendChild(option);
}

var colors = ["#000000","#FF0000","#008000","#0000FF","#FFFF00","#FF00FF","#00FFFF"];
var colorMenu = document.getElementById('colorMenu');
colors.forEach(function(c) {
    var btn = document.createElement('button');
    btn.style.background = c;
    btn.onclick = function() {
        currentColor = c;
        ctx.globalAlpha = 1.0;
    };
    colorMenu.appendChild(btn);
});

function setTool(tool) {
    if(tool === 'highlighter') {
        ctx.globalAlpha = 0.3;
        currentWidth = 20;
    } else {
        ctx.globalAlpha = 1.0;
        currentWidth = parseInt(widthMenu.value, 10);
    }
}

function changeWidth(val) {
    currentWidth = parseInt(val, 10);
}

function toggleSidebar() {
    var toolbar = document.getElementById('toolbar');
    if (toolbar) {
        toolbar.classList.toggle('right');
        toolbar.classList.toggle('left');
    }
}

function changeColor(c) {
    currentColor = c;
}

// Mouse & Scroll
canvas.addEventListener("mousedown", function(e) {
    isDrawing = true;
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawLine(pos.x, pos.y);
});

canvas.addEventListener("mousemove", function(e) {
    if(!isDrawing) return;
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawLine(pos.x, pos.y);
});

canvas.addEventListener("mouseup", stopDrawing);
canvas.addEventListener("touchend", stopDrawing);
canvas.addEventListener("mouseleave", stopDrawing);

var workspace = document.getElementById('workspace');
var lastScrollSend = 0;
workspace.onscroll = function() {
    var now = Date.now();
    if (now - lastScrollSend > 60) {
        if (ws && ws.readyState === WebSocket.OPEN) {
            var sX = workspace.scrollLeft / (workspace.scrollWidth - workspace.clientWidth || 1);
            var sY = workspace.scrollTop / (workspace.scrollHeight - workspace.clientHeight || 1);
            ws.send("SCROLL," + sX.toFixed(3) + "," + sY.toFixed(3));
        }
        lastScrollSend = now;
    }
};

// Utils
function hexToRgb(hex) {
    hex = hex.replace("#", "");
    var bigint = parseInt(hex, 16);
    return [(bigint >> 16) & 255, (bigint >> 8) & 255, bigint & 255];
}