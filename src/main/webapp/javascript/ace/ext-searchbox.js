define("ace/ext/searchbox", ["require", "exports", "module", "ace/lib/dom", "ace/lib/lang", "ace/lib/event", "ace/keyboard/hash_handler", "ace/lib/keys"], function (e, t, n) {
	var r = e("../lib/dom"), i = e("../lib/lang"), s = e("../lib/event"), o = "/* ------------------------------------------------------------------------------------------* Editor Search Form* --------------------------------------------------------------------------------------- */.ace_search {background-color: #ddd;border: 1px solid #cbcbcb;border-top: 0 none;max-width: 297px;overflow: hidden;margin: 0;padding: 4px;padding-right: 6px;padding-bottom: 0;position: absolute;top: 0px;z-index: 99;}.ace_search.left {border-left: 0 none;border-radius: 0px 0px 5px 0px;left: 0;}.ace_search.right {border-radius: 0px 0px 0px 5px;border-right: 0 none;right: 0;}.ace_search_form, .ace_replace_form {border-radius: 3px;border: 1px solid #cbcbcb;float: left;margin-bottom: 4px;overflow: hidden;}.ace_search_form.ace_nomatch {outline: 1px solid red;}.ace_search_field {background-color: white;border-right: 1px solid #cbcbcb;border: 0 none;-webkit-box-sizing: border-box;-moz-box-sizing: border-box;box-sizing: border-box;display: block;float: left;height: 22px;outline: 0;padding: 0 7px;width: 214px;margin: 0;}.ace_searchbtn,.ace_replacebtn {background: #fff;border: 0 none;border-left: 1px solid #dcdcdc;cursor: pointer;display: block;float: left;height: 22px;margin: 0;padding: 0;position: relative;}.ace_searchbtn:last-child,.ace_replacebtn:last-child {border-top-right-radius: 3px;border-bottom-right-radius: 3px;}.ace_searchbtn:disabled {background: none;cursor: default;}.ace_searchbtn {background-position: 50% 50%;background-repeat: no-repeat;width: 27px;}.ace_searchbtn.prev {background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAFCAYAAAB4ka1VAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADFJREFUeNpiSU1NZUAC/6E0I0yACYskCpsJiySKIiY0SUZk40FyTEgCjGgKwTRAgAEAQJUIPCE+qfkAAAAASUVORK5CYII=);    }.ace_searchbtn.next {background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAFCAYAAAB4ka1VAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADRJREFUeNpiTE1NZQCC/0DMyIAKwGJMUAYDEo3M/s+EpvM/mkKwCQxYjIeLMaELoLMBAgwAU7UJObTKsvAAAAAASUVORK5CYII=);    }.ace_searchbtn_close {background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAcCAYAAABRVo5BAAAAZ0lEQVR42u2SUQrAMAhDvazn8OjZBilCkYVVxiis8H4CT0VrAJb4WHT3C5xU2a2IQZXJjiQIRMdkEoJ5Q2yMqpfDIo+XY4k6h+YXOyKqTIj5REaxloNAd0xiKmAtsTHqW8sR2W5f7gCu5nWFUpVjZwAAAABJRU5ErkJggg==) no-repeat 50% 0;border-radius: 50%;border: 0 none;color: #656565;cursor: pointer;display: block;float: right;font-family: Arial;font-size: 16px;height: 14px;line-height: 16px;margin: 5px 1px 9px 5px;padding: 0;text-align: center;width: 14px;}.ace_searchbtn_close:hover {background-color: #656565;background-position: 50% 100%;color: white;}.ace_replacebtn.prev {width: 54px}.ace_replacebtn.next {width: 27px}.ace_button {margin-left: 2px;cursor: pointer;-webkit-user-select: none;-moz-user-select: none;-o-user-select: none;-ms-user-select: none;user-select: none;overflow: hidden;opacity: 0.7;border: 1px solid rgba(100,100,100,0.23);padding: 1px;-moz-box-sizing: border-box;box-sizing:    border-box;color: black;}.ace_button:hover {background-color: #eee;opacity:1;}.ace_button:active {background-color: #ddd;}.ace_button.checked {border-color: #3399ff;opacity:1;}.ace_search_options{margin-bottom: 3px;text-align: right;-webkit-user-select: none;-moz-user-select: none;-o-user-select: none;-ms-user-select: none;user-select: none;}", u = e("../keyboard/hash_handler").HashHandler, a = e("../lib/keys");
	r.importCssString(o, "ace_searchbox");
	var f = '<div class="ace_search right">    <button type="button" action="hide" class="ace_searchbtn_close"></button>    <div class="ace_search_form">        <input class="ace_search_field" placeholder="Search for" spellcheck="false"></input>        <button type="button" action="findNext" class="ace_searchbtn next"></button>        <button type="button" action="findPrev" class="ace_searchbtn prev"></button>    </div>    <div class="ace_replace_form">        <input class="ace_search_field" placeholder="Replace with" spellcheck="false"></input>        <button type="button" action="replace" class="ace_replacebtn">Replace</button>        <button type="button" action="replaceAll" class="ace_replacebtn">All</button>    </div>    <div class="ace_search_options">        <span action="toggleRegexpMode" class="ace_button" title="RegExp Search">.*</span>        <span action="toggleCaseSensitive" class="ace_button" title="CaseSensitive Search">Aa</span>        <span action="toggleWholeWords" class="ace_button" title="Whole Word Search">\\b</span>    </div></div>'.replace(/>\s+/g, ">"), l = function (e, t, n) {
		var i = r.createElement("div");
		i.innerHTML = f, this.element = i.firstChild, this.$init(), this.setEditor(e)
	};
	(function () {
		this.setEditor = function (e) {
			e.searchBox = this, e.container.appendChild(this.element), this.editor = e
		}, this.$init = function () {
			var e = this.element;
			this.searchBox = e.querySelector(".ace_search_form"), this.replaceBox = e.querySelector(".ace_replace_form"), this.searchOptions = e.querySelector(".ace_search_options"), this.regExpOption = e.querySelector("[action=toggleRegexpMode]"), this.caseSensitiveOption = e.querySelector("[action=toggleCaseSensitive]"), this.wholeWordOption = e.querySelector("[action=toggleWholeWords]"), this.searchInput = this.searchBox.querySelector(".ace_search_field"), this.replaceInput = this.replaceBox.querySelector(".ace_search_field");
			var t = this;
			s.addListener(e, "mousedown", function (e) {
				setTimeout(function () {
					t.activeInput.focus()
				}, 0), s.stopPropagation(e)
			}), s.addListener(e, "click", function (e) {
				var n = e.target, r = n.getAttribute("action");
				r && t[r] ? t[r]() : t.$searchBarKb.commands[r] && t.$searchBarKb.commands[r].exec(t), s.stopPropagation(e)
			}), s.addCommandKeyListener(e, function (e, n, r) {
				var i = a.keyCodeToString(r), o = t.$searchBarKb.findKeyCommand(n, i);
				o && o.exec && (o.exec(t), s.stopEvent(e))
			}), this.$onChange = i.delayedCall(function () {
				t.find(!1, !1)
			}), s.addListener(this.searchInput, "input", function () {
				t.$onChange.schedule(20)
			}), s.addListener(this.searchInput, "focus", function () {
				t.activeInput = t.searchInput, t.searchInput.value && t.highlight()
			}), s.addListener(this.replaceInput, "focus", function () {
				t.activeInput = t.replaceInput, t.searchInput.value && t.highlight()
			})
		}, this.$closeSearchBarKb = new u([
			{bindKey: "Esc", name: "closeSearchBar", exec: function (e) {
				e.searchBox.hide()
			}}
		]), this.$searchBarKb = new u, this.$searchBarKb.bindKeys({"Ctrl-f|Command-f|Ctrl-H|Command-Option-F": function (e) {
			var t = e.isReplace = !e.isReplace;
			e.replaceBox.style.display = t ? "" : "none", e[t ? "replaceInput" : "searchInput"].focus()
		}, "Ctrl-G|Command-G": function (e) {
			e.findNext()
		}, "Ctrl-Shift-G|Command-Shift-G": function (e) {
			e.findPrev()
		}, esc: function (e) {
			setTimeout(function () {
				e.hide()
			})
		}, Return: function (e) {
			e.activeInput == e.replaceInput && e.replace(), e.findNext()
		}, "Shift-Return": function (e) {
			e.activeInput == e.replaceInput && e.replace(), e.findPrev()
		}, Tab: function (e) {
			(e.activeInput == e.replaceInput ? e.searchInput : e.replaceInput).focus()
		}}), this.$searchBarKb.addCommands([
			{name: "toggleRegexpMode", bindKey: {win: "Alt-R|Alt-/", mac: "Ctrl-Alt-R|Ctrl-Alt-/"}, exec: function (e) {
				e.regExpOption.checked = !e.regExpOption.checked, e.$syncOptions()
			}},
			{name: "toggleCaseSensitive", bindKey: {win: "Alt-C|Alt-I", mac: "Ctrl-Alt-R|Ctrl-Alt-I"}, exec: function (e) {
				e.caseSensitiveOption.checked = !e.caseSensitiveOption.checked, e.$syncOptions()
			}},
			{name: "toggleWholeWords", bindKey: {win: "Alt-B|Alt-W", mac: "Ctrl-Alt-B|Ctrl-Alt-W"}, exec: function (e) {
				e.wholeWordOption.checked = !e.wholeWordOption.checked, e.$syncOptions()
			}}
		]), this.$syncOptions = function () {
			r.setCssClass(this.regExpOption, "checked", this.regExpOption.checked), r.setCssClass(this.wholeWordOption, "checked", this.wholeWordOption.checked), r.setCssClass(this.caseSensitiveOption, "checked", this.caseSensitiveOption.checked), this.find(!1, !1)
		}, this.highlight = function (e) {
			this.editor.session.highlight(e || this.editor.$search.$options.re), this.editor.renderer.updateBackMarkers()
		}, this.find = function (e, t) {
			var n = this.editor.find(this.searchInput.value, {skipCurrent: e, backwards: t, wrap: !0, regExp: this.regExpOption.checked, caseSensitive: this.caseSensitiveOption.checked, wholeWord: this.wholeWordOption.checked});
			r.setCssClass(this.searchBox, "ace_nomatch", !n && this.searchInput.value), this.highlight()
		}, this.findNext = function () {
			this.find(!0, !1)
		}, this.findPrev = function () {
			this.find(!0, !0)
		}, this.replace = function () {
			this.editor.replace(this.replaceInput.value)
		}, this.replaceAll = function () {
			this.editor.replaceAll(this.replaceInput.value)
		}, this.hide = function () {
			this.element.style.display = "none", this.editor.keyBinding.removeKeyboardHandler(this.$closeSearchBarKb), this.editor.focus()
		}, this.show = function (e, t) {
			this.element.style.display = "", this.replaceBox.style.display = t ? "" : "none", this.isReplace = t, e && (this.searchInput.value = e), this.searchInput.focus(), this.searchInput.select(), this.editor.keyBinding.addKeyboardHandler(this.$closeSearchBarKb)
		}
	}).call(l.prototype), t.SearchBox = l, t.Search = function (e, t) {
		var n = e.searchBox || new l(e);
		n.show(e.session.getTextRange(), t)
	}, t.ISearch = function (e, t) {
		this.$changeListener = this.$changeListener.bind(this), this.startRange = e.selection.toOrientedRange(), this.options = t || {}
	}, function () {
		this.setSession = function (e) {
			this.session && this.session.removeListener(this.$changeListener), this.session = e, this.session.addListener(this.$changeListener)
		}, this.setSearchString = function () {
		}, this.getValue = function () {
			return this.value == null && (this.value = this.session.getValue()), this.value
		}, this.$changeListener = function () {
			this.value = null
		}, this.find = function () {
		}, this.$edgeBefore = function () {
			this.cursor = this.startRange[this.options.backwards ? "start" : "end"]
		}, this.$edgeAfter = function () {
		}, this.next = function (e) {
		}
	}.call(t.ISearch.prototype)
})