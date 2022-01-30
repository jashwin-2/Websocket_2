
const MessageTypes = {
  AUDIO_STATS: 1,
  VIDEO_STATS: 2,
  LOG_MESSAGE: 3
};

const LogLevels = {
    INFO : "Info",
    WARN : "Warning",
    ERROR : "Error"
};

Object.freeze(MessageTypes);
Object.freeze(LogLevels);


var  log_queue;
var logCatDiv = document.getElementById("log_cat_div")
var latestAudioStats;
var latestVideoStats;

$(document).ready(function(){
  log_queue = new LogQueue();
  initializeSocket();
  document.getElementById('log_cat_div').scrollIntoView({ behavior: 'smooth', block: 'end' });
});


function initializeSocket(){
var socket = new WebSocket("ws://"+location.hostname+":8001");

socket.onopen = function(e) {
  console.log("Connection established");
};
socket.onmessage = function(event) {
  var jsonData = JSON.parse(event.data);


  if(jsonData.type == MessageTypes.AUDIO_STATS){
   if(getDetails(1).length==0)
      create(jsonData.json,MessageTypes.AUDIO_STATS);
    else
      update(jsonData.json,MessageTypes.AUDIO_STATS);
  }
  else if(jsonData.type == MessageTypes.VIDEO_STATS){
      if(getDetails(2).length==0)
        create(jsonData.json,MessageTypes.VIDEO_STATS);
      else
        update(jsonData.json,MessageTypes.VIDEO_STATS);
  }
  else{
      log_queue.enqueue(jsonData.json);
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

function create(data,type){
    var stats = JSON.parse(data)
    Object.entries(stats).forEach((entry) =>{
    const [key , value] = entry;
    var detail=$('<details/>',{id:"detail-"+type+key,class:"details"})
    detail.append('<summary style=\"word-break: break-all\" >'+key+'</summary>')
    var table = $('<table class=\"table table-borderless\">',{id:key+'table'})
    var body = $('<tbody>',{id:key+'body'})
    table.append(body)
    detail.append(table)
    Object.entries(value).forEach((entry1) =>{
    const [key1 , value1] = entry1;
    var row=$('<tr/>',{id:key1+"row",Text:key1})

    row.append("<td  id='key-"+key1+"'>"+key1+"</td>")
    row.append("<td style=\"word-break: break-all\" id=value-"+key+"-"+key1+">"+value1+"</td>")
    body.append(row)

})
    if(type == MessageTypes.AUDIO_STATS)
         $('td#audio_col').append(detail)
    else
         $('td#video_col').append(detail)

})
setToggleListener();
};


function update(data,type){
  var opened = getDetails(type).filter(detail=> detail.hasAttribute("open"))
   var stats = JSON.parse(data)

   if(type == MessageTypes.AUDIO_STATS)
   latestAudioStats = stats
   else
   latestVideoStats =stats


   if(opened.length==0)
      return

   Object.entries(stats).forEach((entry) =>
   {
    const [key , value] = entry;
    var detail = document.getElementById("detail-"+type+key)
    if(opened.indexOf(detail) != -1){
    Object.entries(value).forEach((entry1) =>
    {
      const[key1,value1] = entry1;
      var row =document.getElementById("value-"+key+"-"+key1)
     row.innerHTML = value1
    })
         console.log(key + " updated with currently received data")

    }
   }
   
   )
  
}

function getDetails(statsType){

var matches = [];
var type =  ""
if(statsType==MessageTypes.AUDIO_STATS)
    type= "audio_col"
else
    type = "video_col"
var searchEles = document.getElementById(type).children;
for(var i = 0; i < searchEles.length; i++) {

        if(searchEles[i].id.indexOf('detail-') == 0) {
            matches.push(searchEles[i]);
        }

}
return matches;
}


class LogQueue
{
    constructor()
    {
        this.logs = [];

    }
                  
    enqueue(log)
    {
      var message = JSON.parse(log);
      if(this.logs.length==50){
        this.dequeue
      }
      addToLogCat(message);
      this.logs.push(message);
    }
    dequeue()
    {
       this.logs.shift();
    }

    isEmpty()
    {
        return this.logs.length == 0;
    }
}

function addToLogCat(log) {

  const isScrolledToBottom = logCatDiv.scrollHeight - logCatDiv.clientHeight <= logCatDiv.scrollTop + 1
          const newLogMessage = document.createElement("div")
              newLogMessage.innerHTML = "<p>"+log.time +"&ensp;"+log.logLevel+":&emsp;"+log.logMessage+"</p>";
            if(log.logLevel ==LogLevels.ERROR)
               newLogMessage.style.color ='red'
            else if(log.logLevel == LogLevels.INFO)
              newLogMessage.style.color = 'lightblue'

           logCatDiv.appendChild(newLogMessage);

    // scroll to bottom if isScrolledToBottom is true
    if (isScrolledToBottom) {
      logCatDiv.scrollTop = logCatDiv.scrollHeight - logCatDiv.clientHeight
    }
}
function setToggleListener(){

  const detailsElements = document.querySelectorAll('details')

detailsElements.forEach((detail) => {
  detail.addEventListener('toggle', () => {
    if (detail.open) {
      updateStats(detail);
    }
  })
})

}

function updateStats(detail){
  var id = detail.id;
  var updatedStats;
  if(id[7]==1)
    updatedStats = latestAudioStats;
  else
    updatedStats = latestVideoStats;

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
  console.log(rowTitle + " updated with stored data")

  }


