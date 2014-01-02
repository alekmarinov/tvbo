Keyboard = function(options) {
    if (options == null) {
        options = {};
    }

    // key codes
    this.keys = {
        left : 37,
        right : 39,
        up : 38,
        down: 40,
        enter: 13,
    }
    if (options.keys) {
        this.keys = options.keys;
    }

    // whether to bind to keyDown event
    this.bindKeydown = true;
    if (options.bindKeydown == false) {
        this.bindKeydown = false;
    }

    // layout name
    this.layoutName = "zen";
    if (options.layout) {
        this.layoutName = options.layout;
    }

    this.layout = null;
    this.nav = null;
    this.pages = [];
    this.pageIndex = 0;

    // initialize keyboard
    this.initialize();
}

Keyboard.prototype.initialize = function() {
    var $this = this;

    // bind to keydown events
    if (this.bindKeydown) {
        $(document).bind("keydown", function(event) {
            $this.handleKey(event.keyCode);
        });
    }

    // load keyboard html
    this.loadHtml(function(result) {
        $this.loadLayout();
    });
}

Keyboard.prototype.loadHtml = function(onComplete) {
    $.ajax({
        type: 'GET',
        url: "base/base.tmpl",
        success: function(result) {
            // todo: this is probably ineffective
            document.body.innerHTML += result;
            onComplete();
        },
        error: function() {
        },
        dataType: 'html'
    });
}

Keyboard.prototype.loadLayout = function() {
    var $this = this;

    // layout settings
    $.ajax({
        type: 'GET',
        url: "layouts/layout.json",
        success: function(result) {
            $this.layout = result;
            $this.parseLayout($this.layout);
        },
        error: function() {
        },
        dataType: 'json'
    });

    // nav settings
    $.ajax({
        type: 'GET',
        url: "layouts/nav.json",
        success: function(result) {
            $this.nav = result;
        },
        error: function() {
        },
        dataType: 'json'
    });
}

Keyboard.prototype.parseLayout = function(layout) {
    for (var id in layout) {
        var value = layout[id];
        
        if (id == "pages") {
            this.parsePages(value);
        } else if (id == 'default') {
            var element = document.getElementById(layout[id]);
            $(element).addClass('selected');
            continue;
        } else if (id == "alias") {
            this.processAlias(value);
            continue;
        }
    }

    // check for empty li items
    $(".zenkeyboard").find("li").each(function(){
        var li = this;
        if (li.innerHTML == "") {
            $(li).addClass("empty");
        }
    });

}

Keyboard.prototype.parsePages = function(pages) {
    this.pages = pages;
    this.pageIndex = pages.length - 1;
    this.showNextPage();
}

Keyboard.prototype.showNextPage = function() {
    this.pageIndex = (this.pageIndex + 1) % this.pages.length;
    var page = this.pages[this.pageIndex];
    for (var id in page) {
        //console.log(id, page[id]);
        document.getElementById(id).innerHTML = page[id];
        // todo: no support for changed aliases on a key, just values;
    }
}

/*
alias is a mapping: id -> class
alias is set as a css class on the html li element
*/
Keyboard.prototype.processAlias = function(alias) {
    for (var id in alias) {
        var value = alias[id];
        if (!(value instanceof Array)) {
            value = [value];
        }
        for (var v in value) {
            var element = document.getElementById(id);
            $(element).addClass(value[v]);
        }
    }
}

Keyboard.prototype.getCurrent = function() {
    return $(".selected")[0];
}

Keyboard.prototype.handleKey = function(code) {
    var current = this.getCurrent();
    var curId = current.id;
    if (curId == null) {
        console.error("no element id found", current);
        return;
    }

    var nextId = null;
    switch (code) {
        case this.keys.left:
            nextId = this.nav['l'][curId];
            break;

        case this.keys.right:
            nextId = this.nav['r'][curId];
            break;

        case this.keys.up:
            nextId = this.nav['u'][curId];
            break;

        case this.keys.down:
            nextId = this.nav['d'][curId];
            break;

        case this.keys.enter:
            nextId = this.nav['i'][curId];
            if (nextId == null) {
                this.processEnter(current);
            }
            break;
    }

    if (nextId != null) {
        var next = document.getElementById(nextId);
        if (next == null) {
            console.error("no element found with id: " + nextId);
        }
        $(current).removeClass("selected");
        $(next).addClass("selected");
    }

}

Keyboard.prototype.processEnter = function(element) {
    // check special actions
    if ($(element).hasClass("shift")) {
        this.processShift(element);

    } else if ($(element).hasClass("backspace")) {
        this.triggerKeyChangedEvent("backspace", element);

    } else if ($(element).hasClass("ok")) {
        this.triggerKeyChangedEvent("ok", element);

    } else if ($(element).hasClass("pages")) {
        this.showNextPage();

    } else if ($(element).hasClass("delete")) {
        this.triggerKeyChangedEvent("delete", element);

    } else if ($(element).hasClass("delall")) {
        this.triggerKeyChangedEvent("delall", element);

    } else if ($(element).hasClass("space")) {
        this.triggerKeyChangedEvent(" ", element);

    } else if ($(element).hasClass("left")) {
        this.triggerKeyChangedEvent("left", element);

    } else if ($(element).hasClass("right")) {
        this.triggerKeyChangedEvent("right", element);

    } else {
        this.triggerKeyChangedEvent(element.innerHTML, element);
    }
}

Keyboard.prototype.triggerKeyChangedEvent = function(value, element) {
    var event = $.Event("key-changed");
    event.letter = value;
    $(document).trigger(event);
}

Keyboard.prototype.processShift = function(element) {
    // those li annotated with class "letter" will change case
    $(document).find(".letter").each(function() {
        var element = this;
        var value = element.innerHTML;
        if (value == value.toLowerCase()) {
            value = value.toUpperCase();
        } else {
            value = value.toLowerCase();
        }
        element.innerHTML = value;
    });
}

