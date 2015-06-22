
// window.location.href = '/#/';
var WordCount;
(function($) {
    google.load('visualization', '1', {packages: ['corechart', 'line']});
    $(function() {

              var today = new Date(),
                  today = today.setDate(today.getDate() - 2);

              $('.button-collapse').sideNav();

              //=== ini dataRanger slider ===
              $('#data-ranger').dateRangeSlider({
                  bounds:{
                    min: new Date(2015, 5, 1),
                    max: new Date()
                  },
                  defaultValues:{
                    min: new Date(2015, 5, 18),
                    max: new Date(2015, 5, 20)
                  }
              });
              //=== ini dataRanger slider ===

              // === New WordCount View ===
              WordCount = new WordCountView();

              var AppRouter = Backbone.Router.extend({
                  routes: {
                      ":word/:minDate/:maxDate": "getWordData",
                  }
              });

              // Instantiate the router
              var app_router = new AppRouter;

              app_router.on('route:getWordData', function(word, minDate, maxDate){
                    // Get a specific keyword data.
                    console.log(minDate, maxDate);
                    var minDate = new Date(parseInt(minDate)),
                        minDay = minDate.getDate(),
                        minMonth = minDate.getMonth() + 1,
                        minYear = minDate.getFullYear(),
                        maxDate = new Date(parseInt(maxDate)),
                        maxDay = maxDate.getDate(),
                        maxMonth = maxDate.getMonth() + 1,
                        maxYear = maxDate.getFullYear();
                        minDateArr = [minYear + ' 年 ', minMonth + ' 月 ', minDay + ' 號 '],
                        maxDateArr = [maxYear + ' 年 ', maxMonth + ' 月 ', maxDay + ' 號 '];


                    // console.log(minYear, minMonth, minDay, maxYear, maxMonth, maxDay);
                    $('.header-word').html(word);
                    $('.modal-date').html('以下為在 '+ word + ' ' + minDateArr.join('') + '至 ' + maxDateArr.join('') + '的相關新聞');

                    //Get related word
                    $.ajax({
                      type: 'GET',
                      url: 'http://54.172.35.213:8080/newscloud/api/words/' + word +'/related',
                      dataType: 'json',
                      data: {start: minDate.getTime(), end: maxDate.getTime()},
                      minDate: minDate,
                      maxDate: maxDate,
                      success: function(d){
                          console.log(d);
                          var recomEle = $('.modal-recommendation'),
                              innerHTML = [];
                          for(var i = 0, len = d.length; i < len; i++){
                              innerHTML.push('<a class="" href="/#/' + d[i].word + '/' + this.minDate.getTime() + '/' + this.maxDate.getTime() + '">' + d[i].word + '</a>');
                          }
                          innerHTML = innerHTML.join(', ');
                          recomEle.html(innerHTML);
                      },
                      error: function(d){

                      }
                    });

                    //Get chart
                    $.ajax({
                      type: 'GET',
                      url: 'http://54.172.35.213:8080/newscloud/api/words/' + word,
                      data: {start: minDate.getTime(), end: maxDate.getTime()},
                      dataType: 'json',
                      word: word,
                      success: function(d){
                        // console.log(this.word, d);
                        WordCount.showChart(this.word, d);
                      },
                      error: function(d){
                        console.log(this.word, d);
                      }
                    });

                    //Get links
                    $.ajax({
                      type: 'GET',
                      url: 'http://54.172.35.213:8080/newscloud/api/words/' + word + '/news',
                      data: {start: minDate.getTime(), end: maxDate.getTime()},
                      dataType: 'json',
                      success: function(d){
                        WordCount.showRelatedLins(d);
                      },
                      error: function(d){
                        console.log(d);
                      }
                    });

                    //Open Modal
                    $('#modal-word').openModal();
                    $('.fb-comments').attr('data-href', 'http://heart-news.noip.me/#/' + word);
                    FB.XFBML.parse();
              });
              // Start Backbone history a necessary step for bookmarkable URL's
              Backbone.history.start();

    }); // end of document ready


    window.WordCountView = Backbone.View.extend({
  		el: $("body"),
  		initialize: function () {

          var date = $("#data-ranger").dateRangeSlider("values"),
              minDate = date.min.toString(),
              maxDate = date.max.toString();

          // console.log(new Date(minDate).getTime(), new Date(maxDate).getTime());

          $.ajax({
            type: 'GET',
            url: 'http://54.172.35.213:8080/newscloud/api/words',
            data: {start: new Date(minDate).getTime(), end: new Date(maxDate).getTime()},
            dataType: 'json',
            success: this.genWordCloud
          });

          $('#data-ranger').mouseup(function() {
                WordCount.getCloud();
          });

          $('.modal-trigger').leanModal({complete: function() {
                window.location = '/#/'
            }, dismissible: false});
  		},
  		events: {
  			"click #html-canvas span": "showWordModal"
      },
      getCloud: function(){
            var date = $("#data-ranger").dateRangeSlider("values"),
                minDate = date.min.toString(),
                maxDate = date.max.toString();

            // console.log('up :-)');

            $('#html-canvas').html('').removeClass('disable z-depth-3');

            $('.progress').removeClass('content-hidden');

            $.ajax({
              type: 'GET',
              url: 'http://54.172.35.213:8080/newscloud/api/words',
              data: {start: new Date(minDate).getTime(), end: new Date(maxDate).getTime()},
              dataType: 'json',
              success: WordCount.genWordCloud
            });
      },
      genWordCloud: function(d){

            if(d.length === 0){
                $('#html-canvas').addClass('disable z-depth-3').html('<h2>抱歉，目前沒有新聞喔!</h2>');
                $('.progress').addClass('content-hidden');
                return false;
            }
            console.log(d);
            var list = [];
            for(var i = 0, len = d.length; i < len; i++){
                list.push((d[i].score - d[d.length - 1].score)*10/(d[0].score - d[d.length - 1].score) + 1 + ' ' + d[i].word);
            }
            list = list.join(" ");

            //console.log(list);
            /**Fake List**/
            // list += ' 8 紅樓夢 3 賈寶玉 3 林黛玉 3 薛寶釵 3 王熙鳳 3 李紈 3 賈元春 3 賈迎春 3 賈探春 3 賈惜春';
            // list += ' 3 秦可卿 3 賈巧姐 3 史湘雲 3 妙玉 2 賈政 2 賈赦 2 賈璉 2 賈珍 2 賈環 2 賈母 2 王夫人 2 薛姨媽';
            // list += ' 2 尤氏 2 平兒 2 鴛鴦 2 襲人 2 晴雯 2 香菱 2 紫鵑 2 麝月 2 小紅 2 金釧 2 甄士隱 2 賈雨村';
            // list += ' 3 秦可卿 3 賈巧姐 3 史湘雲 3 妙玉 2 賈政 2 賈赦 2 賈璉 2 賈珍 2 賈環 2 賈母 2 王夫人 2 薛姨媽';
            // list += ' 2 尤氏 2 平兒 2 鴛鴦 2 襲人 2 晴雯 2 香菱 2 紫鵑 2 麝月 2 小紅 2 金釧 2 甄士隱 2 賈雨村';
            // list += ' 3 秦可卿 3 賈巧姐 3 史湘雲 3 妙玉 2 賈政 2 賈赦 2 賈璉 2 賈珍 2 賈環 2 賈母 2 王夫人 2 薛姨媽';
            // list += ' 2 尤氏 2 平兒 2 鴛鴦 2 襲人 2 晴雯 2 香菱 2 紫鵑 2 麝月 2 小紅 2 金釧 2 甄士隱 2 賈雨村';
            // list += ' 3 秦可卿 3 賈巧姐 3 史湘雲 3 妙玉 2 賈政 2 賈赦 2 賈璉 2 賈珍 2 賈環 2 賈母 2 王夫人 2 薛姨媽';
            /**Fake List**/

            var listObj = list.split(' ');

            var arr = [];
            for (var i = 1, len = listObj.length; i < len; i = i + 2) {
                  var e = [listObj[i], listObj[i-1]];
                  arr.push(e);
            }


            WordCloud(document.getElementById('html-canvas'),
              { list: arr,
                gridSize: Math.round(14 * $('#canvas').width() / 1024),
                weightFactor: 14,
                color: 'random-dark'
              });

            $('.progress').addClass('content-hidden');

      },
      showWordModal: function(e){
            // Get selected date now.
            var date = $("#data-ranger").dateRangeSlider("values"),
                minDate = date.min.toString(),
                maxDate = date.max.toString(),
                word = $(e.target).text();

            window.location.href = '/#/';
            window.location = '/#/'+ word + '/' + new Date(minDate).getTime() + '/' +  new Date(maxDate).getTime();
      },
      showChart: function(word, d){
            this.drawChat(word, d);
      },
      drawChat: function(word, d){
            var data = new google.visualization.DataTable(),
                trendData = [];

            data.addColumn('string', 'X');
            data.addColumn('number', word);

            for(var i = 0, len = d.length; i < len; i++){
              var date = new Date(d[i].timestamp);
              trendData.push([(date.getMonth() + 1) + '/' + date.getDate(), d[i].score]);
            }

            data.addRows(trendData);


            var options = {
              hAxis: {
                title: '時間'
              },
              vAxis: {
                title: '火紅指數'
              },
              backgroundColor: '#f1f8e9',
            };

            var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
            chart.draw(data, options);
        },
        showRelatedLins: function(d){
              // console.log(d);
              var linksSection = $('.modal-links'),
                  aHref = [];

              for(var i = 0, len = d.length; i < len; i++){
                  aHref.push('<li class="collection-item"><a href="' + d[i].url + '" target="_blank">' + d[i].title + '</a><span class="news-timestamp right">' + (new Date(parseInt(d[i].timestamp))).getFullYear() + '年' +  ((new Date(parseInt(d[i].timestamp))).getMonth() + 1) + '月' +
                  (new Date(parseInt(d[i].timestamp))).getDate() + '日' + '來自 ' + d[i].source + ' 的新聞</span></li>');
              }

              linksSection.html(aHref.join(''));

        }
  	});


    $('.progress').addClass('content-hidden');

})(jQuery); // end of jQuery name space
