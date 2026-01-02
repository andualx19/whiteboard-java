var currentColor = "#000000";
var currentWidth = 5;
var currentScale = 1.0;

var baseWidth = 794;
var baseHeight = 1123;

var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var statusLabel = document.getElementById("status");

canvas.width = baseWidth;
canvas.height = baseHeight;

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

//function resizeCanvas() {
//    var containerWidth = window.innerWidth;
//    var containerHeight = window.innerHeight;
//
//    var scaleX = containerWidth / baseWidth;
//    var scaleY = containerHeight / baseHeight;
//
//    // scale uniform, încape tot
//    currentScale = Math.min(scaleX, scaleY);
//
//    canvas.style.width = baseWidth * currentScale + "px";
//    canvas.style.height = baseHeight * currentScale + "px";
//    canvas.style.transform = "none";
//}
//window.addEventListener("resize", resizeCanvas);
//resizeCanvas();

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

function drawPoint(x, y) {
    ctx.fillStyle = currentColor;
    ctx.beginPath();
    ctx.arc(x, y, currentWidth / 2, 0, Math.PI * 2);
    ctx.fill();

    if (ws.readyState === WebSocket.OPEN) {
        var rgb = hexToRgb(currentColor);
        ws.send(
            Math.round(x) + "," +
            Math.round(y) + "," +
            rgb[0] + "," + rgb[1] + "," + rgb[2] + "," +
            currentWidth
        );
    }
}

function clearCanvas() {
    ctx.clearRect(0,0,canvas.width,canvas.height);

    if(ws.readyState===WebSocket.OPEN) ws.send("CLEAR");
}

function stopDrawing() {
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
    drawPoint(getCanvasPos(e.clientX,e.clientY).x,getCanvasPos(e.clientX,e.clientY).y);
});

canvas.addEventListener("mousemove", function(e) {
    if(e.buttons!==1) return;
    drawPoint(getCanvasPos(e.clientX,e.clientY).x,getCanvasPos(e.clientX,e.clientY).y);
});

canvas.addEventListener("mouseup", stopDrawing);

var lastDist = 0;

canvas.addEventListener("touchstart", function(e){
    e.preventDefault();
    if(e.touches.length === 1){
        var t = e.touches[0];
        var pos = getCanvasPos(t.clientX, t.clientY);
        drawPoint(pos.x, pos.y);
    }
}, false);

canvas.addEventListener("touchmove", function(e){
    if(e.touches.length === 1){
        e.preventDefault();
        var t = e.touches[0];
        var pos = getCanvasPos(t.clientX, t.clientY);
        drawPoint(pos.x, pos.y);
    } else if(e.touches.length === 2){
        e.preventDefault();
        var t1 = e.touches[0];
        var t2 = e.touches[1];
        var dx = t2.clientX - t1.clientX;
        var dy = t2.clientY - t1.clientY;
        var dist = Math.sqrt(dx*dx + dy*dy);

        if(lastDist > 0){
            var delta = (dist - lastDist) / 200;
            changeZoom(delta);
        }
        lastDist = dist;
    }
}, false);

canvas.addEventListener("touchend", function(e){
    if(e.touches.length < 2) lastDist = 0;
    stopDrawing();
}, false);