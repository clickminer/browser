self.port.on("injectScripts", function(injectscripts) {
  for (var i = 0; i < injectscripts.length; i++) {
	console.log(injectscripts[i]);
	var myScript = unsafeWindow.document.createElement('script');
	myScript.type = 'text/javascript';
	myScript.setAttribute('src',injectscripts[i]);
	unsafeWindow.document.getElementsByTagName('head')[0].appendChild(myScript);
  }
});
