//function Refresh(){
//this.start=function(tree, option){
//          var h = 1;
//         setInterval(function(){
//         h++;
//
//         var name ;
//         if(option == 1)
//            name = 'audio/'
//         else
//           name = 'video/'
//         var audio = new XMLHttpRequest();
//         audio.open('GET',window.location.href+name);
//         audio.onload=function(){
//         var data = JSON.parse(audio.responseText);
//
//          const array = [];
//                  var j = 0;
//
//          $('li > ul', tree).each(function() {
//                       array[j++] = $(this);
//                  });
//
//          var i = 0;
//          Object.entries(data).forEach((entry) =>{
//          const [key , value] = entry;
//          array[i].text(" ");
//
//          var ind = 0;
//          Object.entries(value).forEach((entry1) =>{
//          const [key1 , value1] = entry1;
//                var li = $('<li>');
//                var node;
//                if(h%2!=0)
//                    node= document.createTextNode(' : ' + value1);
//                else
//                    node= document.createTextNode('  -->  ' + value1);
//              array[i].append(li.text(key1).append(node));
//          })
//          i++;
//        });
//                 console.log("called");
//        }
//        audio.send();
//
//        } , 1000);
//        }
//
//var generateTree = function (data) {
//        if (typeof (data) == 'object' && data != null) {
//            var ul = $('<ul>');
//            for (var i in data) {
//                var li = $('<li>');
//
//
//                ul.append(li.text(i).append(generateTree(data[i])));
//            }
//            return ul;
//        } else {
//            var v = (data == undefined) ? '[empty]' : data;
//            var textNode = document.createTextNode(' : ' + v);
//            return textNode;
//        }
//    };
//}