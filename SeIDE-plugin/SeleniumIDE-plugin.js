
var currWin = null; // the current window
var currWinURL = null; // the URL related to the current window
var currEvent = null; // the current type of event
var currWinID = 0;  
var currFrameID = 0;      
        
Recorder.prototype.customEventHandler = function(event) {
                
                var prevEvent = currEvent;
                currEvent = event.type;

                var w = event.target.ownerDocument.defaultView; // window object
                if(w == currWin && w.location == currWinURL && prevEvent == currEvent && currEvent != 'keypress') { return; } // not interested in all mouse movements on the same window/frame and URL
                
                if(currEvent == 'click' && prevEvent == 'contextmenu' && event.button == 2) { return; } // this is a mouse rightclick, we should not record it as a click
                
                
                
                
                var time = new Date().getTime();
                
                currWin = w;
                currWinURL = w.location;
                
                var doc = event.target.ownerDocument; // document object
                var ref = doc.referrer; // previous page
                
                var type = event.type;
                var docURL = doc.URL;
                var docTitle = doc.title;
                // var eContent = event.target.innerHTML.substring(0,255);
                var eContent = null;

                var eTag = null;
                // if (event.target.parentNode != null) { eTag = event.target.parentNode.innerHTML.substring(0,255); }

                var eParentTag = null;
                // if (event.target.parentNode.parentNode != null) { eParentTag = event.target.parentNode.parentNode.innerHTML.substring(0,255); }

                var winOpener = w.opener;
                var winOpenerURL = null;
                var winOpenerName = null;
                if(winOpener != null) { winOpenerName = winOpener.name; winOpenerURL = winOpener.location;}
                
                var winHistoryLen = winHistoryLen = w.history.length;
                var winOrFrameStr = 'Window';
                if(w != w.parent) { winOrFrameStr = 'Frame'; }
                
                var currWinName = currWin.name;
                if(currWinName == null || currWinName.length == 0) { 
                    if(winOrFrameStr == 'Window') {
                        currWinID++; 
                        currWin.name = 'W' + currWinID;
                    }
                    else if(winOrFrameStr == 'Frame') {
                        currFrameID++; 
                        currWin.name = 'F' + currFrameID;
                    }
                }
                
                // store frame path
                var cw = w;
                var framePath = '';

                while(cw != cw.parent) {
                    if(framePath.length == 0) 
                        framePath = '[' + cw.name + '] ' + cw.location;
                    else
                        framePath =  ' [' + cw.name + '] ' + cw.location + ' => ' + framePath;
                    cw = cw.parent;
                }
                if(framePath.length == 0)
                    framePath = '[' + cw.name + '] ' + cw.location;
                else 
                    framePath =  ' [' + cw.name + '] ' + cw.location + ' => ' + framePath;


                var locator = this.findLocator(event.target);

                // store locator path
                var et = event.target;
                var locatorPath = locator;
                var k = 0;
                while (et.parentNode.parentNode != null) {
                    locatorPath = this.findLocator(et.parentNode) + ' => ' + locatorPath;
                    et = et.parentNode;
                    k++;
                    if(k >= 5)
                        break;
                }


                var et = event.target;
                var clickHandlers = '';
                while (et.parentNode != null) {
                    if(et.getAttribute('href') != null)
                        clickHandlers += ' => href='+et.getAttribute('href');
                    if(et.getAttribute('onclick') != null)
                        clickHandlers += ' => onclick='+et.getAttribute('onclick');
                    if(et.getAttribute('onmousedown') != null)
                        clickHandlers += ' => onmousedown='+et.getAttribute('onmousedown');
                    /* 
                    // flashvars are not exactly a click handler but may be useful...    
                    if(et.getAttribute('flashvars') != null)
                        clickHandlers += ' => fvs='+et.getAttribute('flashvars');
                    */
                    et = et.parentNode;
                }



                var action = null;
                if(event.type == 'click')
                    action = 'clickAt';
                else if(event.type == 'mouseover')
                    action = 'mouseOver';
                else if(event.type == 'mousedown')
                    action = 'mouseDown';
                else if(event.type == 'keypress')
                    action = 'keyPress';
                else if(event.type == 'contextmenu')
                    action = 'contextMenu';
                // else if(event.type == 'type')
                //    action = 'type';



                // var px = editor.seleniumAPI.Selenium.prototype.getElementPositionLeft(event.target);
                // var py = editor.seleniumAPI.Selenium.prototype.getElementPositionTop(event.target);

                var px = 0;
                var py = 0;
                var cx = event.clientX;
                var cy = event.clientY;
                
                this.record(action, locator, ':: Event Time :: '+ time);
                this.record(action, locator, ':: Event Type :: '+ type);
                this.record(action, locator, ':: '+ winOrFrameStr +' ID :: '+ w.name);
                this.record(action, locator, ':: '+ winOrFrameStr +' URL :: '+ docURL);
                this.record(action, locator, ':: '+ winOrFrameStr +' Path :: '+ framePath);
                this.record(action, locator, ':: '+ winOrFrameStr +' Referrer :: '+ ref);
                this.record(action, locator, ':: '+ winOrFrameStr +' Title :: '+ docTitle);
                this.record(action, locator, ':: Window Opener :: '+ winOpenerName);
                this.record(action, locator, ':: Window History Length :: '+ winHistoryLen);
                this.record(action, locator, ':: Click Handlers :: '+ clickHandlers);
                this.record(action, locator, ':: Element Locator Path :: '+ locatorPath);
                this.record(action, locator, ':: Element Parent Tag :: '+ eParentTag);
                this.record(action, locator, ':: Element Tag :: '+ eTag);
                this.record(action, locator, ':: Element Content :: '+ eContent);
                this.record(action, locator, ':: Element Position :: ('+px+','+py+')');
                this.record(action, locator, ':: Event Position :: ('+cx+','+cy+')');
                
                if(event.type == 'keypress') {
                    var chCode = event.which;
                    var chStr = null;
                    if(chCode == 13)
                        chStr = '[Enter]';
                    else
                        chStr = String.fromCharCode(chCode);
                        
                    this.record(action, locator, ':: Target :: '+ chStr);  // target key
                }
                else
                    this.record(action, locator, ':: Target :: '+ event.target); // target URL, if on the same element
                    

};


Recorder.removeEventHandler('click');
Recorder.addEventHandler('clickAt', 'click', function(event) { this.customEventHandler(event); }, { capture: true });

Recorder.removeEventHandler('mousedown');
Recorder.addEventHandler('mouseDown', 'mousedown', function(event) { this.customEventHandler(event); }, { capture: true });

Recorder.removeEventHandler('mouseover');
Recorder.addEventHandler('mouseOver', 'mouseover', function(event) { this.customEventHandler(event); }, { capture: true });

Recorder.removeEventHandler('contextmenu');
Recorder.addEventHandler('contextMenu', 'contextmenu', function(event) { this.customEventHandler(event); }, { capture: true });

Recorder.removeEventHandler('keypress');
Recorder.addEventHandler('keyPress', 'keypress', function(event) { this.customEventHandler(event); }, { capture: true });

// Recorder.removeEventHandler('type');
// Recorder.addEventHandler('type', 'type', function(event) { this.customEventHandler(event); }, { capture: true });
