var data = require("self").data;

var injectscripts = [data.url("sizzle.js"),
	data.url("atoms.js"),
	data.url("tools.js"),
	data.url("htmlutils.js"),
	data.url("ui-element.js"),
	data.url("selenium-browserdetect.js"),
	data.url("selenium-browserbot.js"),
	data.url("locatorBuilders.js")];

var pageMod = require("page-mod");
pageMod.PageMod({
  include: "*",
  contentScriptFile: data.url("inject.js"),
  onAttach: function(worker) {
    worker.port.emit("injectScripts", injectscripts);
  }
});
