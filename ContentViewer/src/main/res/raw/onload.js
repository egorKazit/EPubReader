var images = document.getElementsByTagName('img');
for (var i = 0; i < images.length; i++) {
    var img = images[i];
    var targetWidth = Math.round(placeHolder * img.width);
    targetWidth = targetWidth < 80 ? 80 : targetWidth;
    console.log('targetWidth = ' + targetWidth);
    img.width = targetWidth;
};
console.log("Images set was set");