
const MessageTypes = {
  TABLE_DATA : 1,
  LOG_MESSAGE : 2,
  GRAPH_DATA : 3,
  INITIAL_DATA : 4
};

const LogLevels = {
    INFO : "Info",
    WARN : "Warning",
    ERROR : "Error"
};

Object.freeze(MessageTypes);
Object.freeze(LogLevels);


var  log_queue;
var logCatDiv;
var latestStats=new Map();
var isLoggerIsActive = false;
var ids =new Map();
var tables;
var log;
var graphs;

$(document).ready(function(){
  log_queue = new LogQueue();
  initializeSocket();
});


function initializeSocket(){
var socket = new WebSocket("ws://"+location.hostname+":8001");

socket.onopen = function(e) {
  console.log("Connection established");
};
socket.onmessage = function(event) {
  var jsonData = JSON.parse(event.data);

  if(jsonData.type == MessageTypes.TABLE_DATA){
   if(getDetails(jsonData.id).length==0)
      createTable(jsonData.json,jsonData.id);
    else
      update(jsonData.json,jsonData.id);
  }
  else if(jsonData.type == MessageTypes.LOG_MESSAGE){
      log_queue.enqueue(jsonData.json);
  }
  else if(jsonData.type == MessageTypes.INITIAL_DATA){
     var data = JSON.parse(jsonData.json);
     setIds(data);
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

function initializeWebPage(){

  if(tables.length!=0)
    {
      $('body#body').append('<h1><b>Tables</b></h1>');
      $('body#body').append('<div id=\"no_data\"class=\"no_data_div \"> No data </div>')
      var parentTable =$('<table/>',{id:"parent_table",class:"table table-borderless"})
      var tableBody =  $('<tbody>')
      parentTable.append(tableBody);
      var i = 0;
      while(i<tables.length){
        if((i+1)<tables.length){
          tableBody.append('<th scope=\"col\">'+tables[i].second+'</th>')
          tableBody.append('<th scope=\"col\">'+tables[i+1].second+'</th>')
          tableBody.append('<tr><td id=\"'+tables[i].second.replace(/ /g,'')+'\"></td><td id =\"'+tables[i+1].second.replace(/ /g,'') +'\"></td></tr>')
          i=i+2;
        }
        else{
          tableBody.append('<th scope=\"col\">'+tables[i].second+'</th>')
          tableBody.append('<tr><td id=\"'+tables[i].second.replace(/ /g,'')+'\"></td></tr>')
          break;
        }
      }
      var div = $('<div/>',{id :"parent_table_div",class:"table_div"});
      div.append(parentTable)
      $('body#body').append(div);
       document.getElementById("parent_table_div").style.visibility = "hidden"
    }

    if(log!=null){
      $('body#body').append('<h1><b>'+log.second+'</b></h1><div id="log_cat_div" style="overflow:auto" class="table-wrapper-scroll-y my-custom-scrollbar">');
      logCatDiv = document.getElementById("log_cat_div")

    }

}

function createTable(data,id){
    var stats = JSON.parse(data)

    Object.entries(stats).forEach((entry) =>{
    const [key , value] = entry;
    var detail=$('<details/>',{id:"detail-"+id+key,class:"details"})
    detail.append('<summary style=\"word-break: break-all\" >'+key+'</summary>')
    var table = $('<table class=\"table table-borderless\">',{id:key+'table'})
    var body = $('<tbody>',{id:key+'body',class:"inner-table-body"})
    table.append(body)
    detail.append(table)
    Object.entries(value).forEach((entry1) =>{
    const [key1 , value1] = entry1;
    var row=$('<tr/>',{id:key1+"row",Text:key1})

    row.append("<td  id='key-"+key1+"'>"+key1+"</td>")
    row.append("<td style=\"word-break: break-all\" id=value-"+key+"-"+key1+">"+value1+"</td>")
    body.append(row)

})
  $('td#'+ids.get(id)).append(detail)
  addToggleListener(id,key)
  
})

 var parent_table = document.getElementById('parent_table_div')
if(parent_table.style.visibility === "hidden"){
  document.getElementById('no_data').style.display = "none"
  parent_table.style.visibility = "visible"
}
}

function addToggleListener(id,key){
  var detail = document.getElementById("detail-"+id+key)
  detail.addEventListener('toggle', () => {
   if (detail.open) {
     updateStats(detail);
   }
 })
}

function setIds(data){
  tables = data.tables
  graphs = data.graphs
  log = data.log
  addToMap(tables)
  addToMap(graphs)
  console.log(ids)
  if(log!=null)
  ids.set(log.first,log.second)
  initializeWebPage();

}

function addToMap(idsList){
  for(let i=0;i<idsList.length;i++){
    ids.set(idsList[i].first,idsList[i].second.replace(/ /g,''))
  }
}


function update(data,id){
  var opened = getDetails(id).filter(detail=> detail.hasAttribute("open"))
   var stats = JSON.parse(data)

   latestStats.set(id,stats)

   if(opened.length==0)
      return

   Object.entries(stats).forEach((entry) =>
   {
    const [key , value] = entry;
    var detail = document.getElementById("detail-"+id+key)
    if(opened.indexOf(detail) != -1){
    Object.entries(value).forEach((entry1) =>
    {
      const[key1,value1] = entry1;
      var row =document.getElementById("value-"+key+"-"+key1)
     row.innerHTML = value1
    })
         console.log(key + " updated using currently received data")

    }
   }
   
   )
  
}

function getDetails(statsType){

var matches = [];

var type =  ids.get(statsType)

var searchEles = document.getElementById(type).children;
for(var i = 0; i < searchEles.length; i++) {

        if(searchEles[i].id.indexOf('detail-') == 0) {
            matches.push(searchEles[i]);
        }

}
return matches;
}

var logger;
class LogQueue
{
    constructor()
    {
        this.logs = [];

    }
                  
    enqueue(log)
    {
      var message = JSON.parse(log);

if(!isLoggerIsActive)
    {
        logger= setInterval(activateLogger, 500);
        isLoggerIsActive = true;
                console.log("logger Started")

    }
           this.logs.push(message);
    }


    dequeue()
    {
       if(this.isEmpty()){
        clearInterval(logger);
        isLoggerIsActive = false;
        console.log("logger Stoped")
        return null;
       }
       return this.logs.shift();
    }

    isEmpty()
    {
        return this.logs.length == 0;
    }
}
function activateLogger() {

              var log = log_queue.dequeue();

              if(log==null)
                return
              const isScrolledToBottom = logCatDiv.scrollHeight - logCatDiv.clientHeight <= logCatDiv.scrollTop + 1
                      const newLogMessage = document.createElement("div")
                          newLogMessage.innerHTML = "<p>"+log.time +"&ensp;"+log.logLevel+":&emsp;"+log.logMessage+"</p>";
                        if(log.logLevel ==LogLevels.ERROR)
                           newLogMessage.style.color ='red'
                        else if(log.logLevel == LogLevels.INFO)
                          newLogMessage.style.color = 'lightblue'

                       logCatDiv.appendChild(newLogMessage);
                       removeLogIfExceedLimit();

                // scroll to bottom if isScrolledToBottom is true
                if (isScrolledToBottom) {
                  logCatDiv.scrollTop = logCatDiv.scrollHeight - logCatDiv.clientHeight
                }
            }


function updateStats(detail){
  var id = detail.id;
  var updatedStats=latestStats.get(parseInt(id[7]));
  if(updatedStats == null)
    return
  var rowTitle = id.substring(8);
  updateRow(updatedStats[rowTitle],rowTitle);
}

function updateRow(data,rowTitle){
  Object.entries(data).forEach((entry) =>
  {
   const [key , value] = entry;
   var row =document.getElementById("value-"+rowTitle+"-"+key)
    row.innerHTML = value

   })
  console.log(rowTitle + " updated using stored data")

  }

function removeLogIfExceedLimit() {
  var len =$("#log_cat_div").children().length;

    if(len>=50){
           $('#log_cat_div').find('div').first().remove();
    }
}