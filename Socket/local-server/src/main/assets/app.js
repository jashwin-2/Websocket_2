$(document).ready(function(){
  initializeSocket();
});



function initializeSocket(){
var socket = new WebSocket("ws://"+location.host+"/live");

socket.onopen = function(e) {
  console.log("[open] Connection established");

};
socket.onmessage = function(event) {
  console.log(` Data received from server: ${event.data}`);
  var jsonData = JSON.parse(event.data);
   var treeBuilder = new JsonTreeBuilder();


   var tree1 = treeBuilder.build(jsonData);
   $('div#json').html("");
   $('div#json').append(tree1);


};

socket.onclose = function(event) {
  if (event.wasClean) {
    alert(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
  } else {

    alert('[close] Connection died');
  }
};

socket.onerror = function(error) {
  alert(`[error] ${error.message}`);
};

}


