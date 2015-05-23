(function (){
  // Fail Or Win.
  var winImages = ["http://i.imgur.com/B0wld.gif",
                   "http://i.imgur.com/J4dPghF.gif",
                   "http://i.imgur.com/lz7hOlC.gif",
                   "http://rgifs.gifbin.com/082014/1407174672_lumberjack_drops_tree_between_in_narrow_spot.gif",
                   "http://i.imgur.com/miaYwKZ.gif",
                   "http://i.imgur.com/2QGTn3g.gif",
                   "http://i.imgur.com/3loUt2t.gif",
                   "http://i.imgur.com/Q4bI5.gif",
                   "https://i.imgur.com/7sTQBRX.jpg",
                   "http://i.imgur.com/NAzlZUW.gif",
"http://media.giphy.com/media/xNBcChLQt7s9a/giphy.gif",
                   "https://media4.giphy.com/media/mp1JYId8n0t3y/200.gif",
                   "https://media1.giphy.com/media/9xrSBojGBTLOg/200.gif",
                   "https://media1.giphy.com/media/aWRWTF27ilPzy/200.gif",
                   "https://media0.giphy.com/media/EWWdvQngcLt6g/200.gif",
                   "http://cdn2.list25.com/wp-content/uploads/2011/11/benchflip.gif",
                   "http://cdn2.list25.com/wp-content/uploads/2011/11/flipping.gif",
                   "http://cdn3.list25.com/wp-content/uploads/2011/11/haters.gif",
                   "http://cdn2.list25.com/wp-content/uploads/2011/11/ballers.gif",
                   "http://cdn2.list25.com/wp-content/uploads/2011/11/obama.gif"],
      failImages = ["http://4.bp.blogspot.com/-k_NKHgmpCj4/VHPXUOj5lAI/AAAAAAAA5TI/TijrAwm9se8/s1600/SkateboardReport.gif",
                    "http://i.imgur.com/7oDTAeC.gif",
                    "http://i.imgur.com/CN0ZRJw.gif",
                    "http://i.imgur.com/NiPAP3M.gif",
                    "http://31.media.tumblr.com/d2e1e2d190e76b02d467e38a6880d3e7/tumblr_mu07mwSZwJ1scz79jo1_400.gif",
                    "http://i.imgur.com/oiifnQh.gif",
                    "http://media20.giphy.com/media/JZLFC4GJR3h6M/giphy.gif",
                    "http://i.imgur.com/TKGPXQI.gif",
                    "https://i.imgur.com/o8eD6Zr.jpg",
                    "http://www.reactiongifs.com/wp-content/uploads/2013/01/lol-left-for-dead.gif",
                    "http://i.imgur.com/7sKRArO.gif",
                    "http://i.imgur.com/hY9eXEY.gif",
                    "http://img.photobucket.com/albums/v640/shivadancing/lebron-smallest-violin.gif",
                    "http://media.giphy.com/media/O92uuQTL8HWkU/giphy.gif",
                    "http://media.giphy.com/media/m2cwjSBcq67fi/giphy.gif",
                    "http://media.giphy.com/media/aGkOGAM36rFOo/giphy.gif",
                    "http://media.giphy.com/media/xQVGkvzhkGoY8/giphy.gif",
                    "http://media.giphy.com/media/W61C4GdBd61QA/giphy.gif",
                    "http://media.giphy.com/media/zbquAu7ojGxbO/giphy.gif",
                    "http://media.giphy.com/media/cS83sLRzgVOeY/giphy.gif"],
      randomItem = function (items) {
        var selectedIndex = Math.floor(Math.random() * items.length);
        return items[selectedIndex];
      },
      uglyWriteImage = function (url) {
        document.write("<img src='" + url + "'>");
      };


  window.fow = {
    win: function () {
      uglyWriteImage(randomItem(winImages));
    },
    fail: function () {
      uglyWriteImage(randomItem(failImages));
    }
  };
}());
