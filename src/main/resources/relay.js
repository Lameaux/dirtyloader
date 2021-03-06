var system = require('system');
var args = system.args;
if (args.length === 1) {
  phantom.exit();
}

var url = args[1];
var timeout = args[2];

var page = require('webpage').create();
page.settings.userAgent = 'Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A406 Safari/8536.25';
page.settings.resourceTimeout = timeout;
page.settings.loadImages=false;


var headers = '';

page.onResourceTimeout = function(e) {
  phantom.exit();
};

page.open(url, function(status) {
  if(status === "success") {
    console.log(page.content);
  }
  phantom.exit();
});
