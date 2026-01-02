var currentColor = "#000000";
var currentWidth = 5;
var currentScale = 1.0;

var lastX = null;
var lastY = null;
var isDrawing = false;

var baseWidth = 794;
var baseHeight = 1123;

var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var statusLabel = document.getElementById("status");

canvas.width = baseWidth;
canvas.height = baseHeight;

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

var widthMenu = document.getElementById('widthMenu');
for (var i = 5; i <= 20; i++) {
    var option = document.createElement('option');
    option.value = i;
    option.text = i;
    widthMenu.appendChild(option);
}

var colors = ["#000000","#FF0000","#00FF00","#0000FF","#FFFF00","#FF00FF","#00FFFF"];
var colorMenu = document.getElementById('colorMenu');
for (var j = 0; j < colors.length; j++) {
    var btn = document.createElement('button');
    btn.style.background = colors[j];
    btn.onclick = (function(c){ return function(){ changeColor(c); }; })(colors[j]);
    colorMenu.appendChild(btn);
}

function toggleSidebar() {
    var toolbar = document.getElementById('toolbar');
    toolbar.classList.toggle('left');
    toolbar.classList.toggle('right');
}

function setTool(tool) {
    if(tool === 'highlighter') {
        ctx.globalAlpha = 0.3;
        currentWidth = 15;
    } else {
        ctx.globalAlpha = 1.0;
        currentWidth = parseInt(widthMenu.value, 10);
    }
}

function changeColor(c) {
    currentColor = c;
}

function changeWidth(val) {
    currentWidth = parseInt(val,10);
}

function getCanvasPos(clientX, clientY){
    var rect = canvas.getBoundingClientRect();

    var scaleX = canvas.width / rect.width;
    var scaleY = canvas.height / rect.height;

    return {
        x: (clientX - rect.left) * scaleX,
        y: (clientY - rect.top) * scaleY
    };
}

function hexToRgb(hex) {
    hex = hex.replace("#", "");
    var bigint = parseInt(hex, 16);
    var r = (bigint >> 16) & 255;
    var g = (bigint >> 8) & 255;
    var b = bigint & 255;
    return [r, g, b];
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
        // Punct de start (sau dacă e doar un click scurt)
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

function clearCanvas() {
    ctx.clearRect(0,0,canvas.width,canvas.height);

    if(ws.readyState===WebSocket.OPEN) ws.send("CLEAR");
}

function stopDrawing() {
    lastX = null;
    lastY = null;
    if(ws.readyState===WebSocket.OPEN) ws.send("STOP");
}

function changeZoom(delta) {
    currentScale += delta;

    if(currentScale < 0.5) currentScale = 0.5;
    if(currentScale > 2.0) currentScale = 2.0;

    canvas.style.width = (baseWidth * currentScale) + "px";
    canvas.style.height = (baseHeight * currentScale) + "px";

    if(ws.readyState === WebSocket.OPEN) {
        ws.send("ZOOM," + currentScale);
    }
}

canvas.addEventListener("mousedown", function(e) {
    isDrawing = true;
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawLine(pos.x, pos.y); // Folosim numele corect
});

canvas.addEventListener("mousemove", function(e) {
    if(!isDrawing) return;
    var pos = getCanvasPos(e.clientX, e.clientY);
    drawLine(pos.x, pos.y);
});

canvas.addEventListener("touchstart", function(e){
    e.preventDefault();
    if(e.touches.length === 1){
        isDrawing = true;
        var t = e.touches[0];
        var pos = getCanvasPos(t.clientX, t.clientY);
        drawLine(pos.x, pos.y);
    }
}, {passive: false});

canvas.addEventListener("touchmove", function(e){
    e.preventDefault();
    if(isDrawing && e.touches.length === 1){
        var t = e.touches[0];
        var pos = getCanvasPos(t.clientX, t.clientY);
        drawLine(pos.x, pos.y);
    }
}, {passive: false});

function stopDrawing() {
    isDrawing = false;
    lastX = null;
    lastY = null;
    if(ws.readyState === WebSocket.OPEN) ws.send("STOP");
}

canvas.addEventListener("mouseup", stopDrawing);
canvas.addEventListener("touchend", stopDrawing);
canvas.addEventListener("mouseleave", stopDrawing);

var workspace = document.getElementById('workspace');

workspace.onscroll = function() {
    if (ws && ws.readyState === WebSocket.OPEN) {
        var scrollX = workspace.scrollLeft / workspace.scrollWidth;
        var scrollY = workspace.scrollTop / workspace.scrollHeight;

        ws.send("SCROLL," + scrollX + "," + scrollY);
    }
};