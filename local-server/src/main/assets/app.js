$(document).ready(function(){
  initializeSocket();
});



function initializeSocket(){
var socket = new WebSocket("ws://"+location.hostname+":8001");

socket.onopen = function(e) {
  console.log("[open] Connection established");

};
socket.onmessage = function(event) {
  var jsonData = JSON.parse(event.data);

  if(getDetails().length==0)
    create(jsonData);
  else
    update(jsonData);

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

function create(data){

    var string = "";
    Object.entries(data).forEach((entry) =>{
    const [key , value] = entry;
    var detail=$('<details/>',{id:"detail-"+key})
    detail.append('<summary>'+key+'</summary>')
    var table = $('<table>',{id:key+'table'})
    var body = $('<tbody>',{id:key+'body'})
    table.append(body)
    detail.append(table)
    table.append('<tr>  <th colspan="2">'+  key +'</th></tr>')
    Object.entries(value).forEach((entry1) =>{
    const [key1 , value1] = entry1;
    var row=$('<tr/>',{id:key1+"row",Text:key1})

    row.append("<td id='key-"+key1+"'>"+key1+"</td>")
    row.append("<td id=value-"+key+"-"+key1+">"+value1+"</td>")
    body.append(row)

})
$('div#json').append(detail)
})
};

var num = 0;
function update(data){
  var opened = getDetails().filter(detail=> detail.hasAttribute("open"))
   
  
   if(opened.length==0)
      return
   Object.entries(data).forEach((entry) =>
   {
    const [key , value] = entry;
    var detail = document.getElementById("detail-"+key)
    if(opened.indexOf(detail) != -1){
    Object.entries(value).forEach((entry1) =>
    {
      const[key1,value1] = entry1;
      var row =document.getElementById("value-"+key+"-"+key1)
     //row.innerHTML = " hello"
     row.innerHTML = value1
     console.log(key1 + " updated")
    
    })
    }
   }
   
   )

}

function getDetails(){

var matches = [];
var searchEles = document.getElementById("json").children;
for(var i = 0; i < searchEles.length; i++) {

        if(searchEles[i].id.indexOf('detail-') == 0) {
            matches.push(searchEles[i]);
        }

}
return matches;
}