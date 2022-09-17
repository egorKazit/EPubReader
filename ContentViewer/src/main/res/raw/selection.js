var click = function (event){
                 var rangeForWord = document.caretRangeFromPoint(event.clientX, event.clientY);
                 if(rangeForWord.startContainer.nodeType === Node.ELEMENT_NODE){
                    var src = rangeForWord.startContainer.getElementsByTagName('img')[0].src;
                    JavascriptImageInteractionInterface.interact(src);
                 } else if (rangeForWord.startContainer.nodeType === Node.TEXT_NODE) {
                     rangeForWord.expand('word');
                     var rangeToSelectWord = document.createRange();
                     rangeToSelectWord.setStart(rangeForWord.startContainer, rangeForWord.startOffset);
                     rangeToSelectWord.setEnd(rangeForWord.endContainer, rangeForWord.endOffset);
                     if(rangeForWord.toString().trim() != '' && /^[a-zA-Z1-9\'\-]+$/.test(rangeForWord.toString().trim())){
                         var selection = window.getSelection();
                         selection.removeAllRanges();
                         selection.addRange(rangeToSelectWord);
                         selectedText = rangeToSelectWord.toString().trim();
                         JavascriptClickInteractionInterface.interact(selectedText);
                     }

                     var endNode = selection.focusNode;
                     var spaceCount = 2;
                     var startOffset = rangeForWord.startOffset - 2;
                     while(spaceCount > 0 && startOffset > 0){
                     	  if(endNode.textContent.charAt(startOffset) === ' ' ){
                           spaceCount--;
                        }
                        startOffset--;
                     };
                     startOffset++;
                     spaceCount = 3;
                     var endOffset = rangeForWord.endOffset;
                     while(spaceCount > 0 && endOffset < endNode.textContent.length){
                     	  if(endNode.textContent.charAt(endOffset) === ' ' ){
                           spaceCount--;
                        }
                        endOffset++;
                     }
                     endOffset--;
                     JavascriptPhraseInteractionInterface.interact(endNode.textContent.substring(startOffset,endOffset));

                 }
             };
document.addEventListener('click', click);
document.addEventListener('click', click);
document.addEventListener('mouseup', function(event) {
    if(window.getSelection().toString().length){
        JavascriptSelectInteractionInterface.interact(window.getSelection().toString());
    }
});
document.oncontextmenu = function(){
    if(window.getSelection().toString().length){
        JavascriptSelectInteractionInterface.interact(window.getSelection().toString());
    }
    return false;
};