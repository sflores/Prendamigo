var accordionItems = new Array();
function init()
{
    var divs = document.getElementsByTagName('div');
    for (var i = 0; i < divs.length; i++)
    {
        if (divs[i].className == 'accordionItem') accordionItems.push(divs[i]);
    }

    for (var i = 0; i < accordionItems.length; i++)
    {
        var h2 = getFirstChildWithTagName(accordionItems[i], 'H2');
        h2.onclick = toggleItem;
    }

    for (var i = 1; i < accordionItems.length; i++)
    {
        accordionItems[i].className = 'accordionItem hide';
    }
}

function toggleItem()
{
    var itemClass = this.parentNode.className;

    for (var i = 0; i < accordionItems.length; i++)
    {
        accordionItems[i].className = 'accordionItem hide';
    }

    if (itemClass == 'accordionItem hide')
    {
        this.parentNode.className = 'accordionItem';
    }
}

function getFirstChildWithTagName(element, tagName)
{
    for (var i = 0; i < element.childNodes.length; i++)
    {
        if (element.childNodes[i].nodeName == tagName) return element.childNodes[i];
    }
}

window.onload = init;

