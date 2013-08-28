define(
	"ace/mode/csp",
	["require","exports","module","ace/lib/oop","ace/mode/text","ace/tokenizer", "ace/mode/csp_highlighter", "ace/mode/matching_brace_outdent", "ace/mode/folding/csp_style"],
	function(require, exports, module) {
		"use strict";
		var oop = require("../lib/oop");
		// defines the parent mode
		var TextMode = require("./text").Mode;
		var Tokenizer = require("../tokenizer").Tokenizer;
		var MatchingBraceOutdent = require("./matching_brace_outdent").MatchingBraceOutdent;
		// defines the language specific highlighters and folding rules
		var MyNewHighlightRules = require("./csp_highlighter").HighlightRules;
		//var MyNewFoldMode = require("./folding/csp_style").FoldMode;
		var Mode = function() {
			// set everything up
			var highlighter = new MyNewHighlightRules();
			this.$outdent = new MatchingBraceOutdent();
			//this.foldingRules = new MyNewFoldMode();
			this.$tokenizer = new Tokenizer(highlighter.getRules());
		};
		oop.inherits(Mode, TextMode);
		(function() {
			// Extra logic goes here--we won't be covering all of this
			/* These are all optional pieces of code!
			 this.getNextLineIndent = function(state, line, tab) {
			 var indent = this.$getIndent(line);
			 return indent;
			 };
			 this.checkOutdent = function(state, line, input) {
			 return this.$outdent.checkOutdent(line, input);
			 };
			 this.autoOutdent = function(state, doc, row) {
			 this.$outdent.autoOutdent(doc, row);
			 };
			 this.createWorker = function(session) {
			 var worker = new WorkerClient(["ace"], "ace/mode/mynew_worker", "NewWorker");
			 worker.attachToDocument(session.getDocument());
			 return worker;
			 };
			 */
		}).call(Mode.prototype);
	exports.Mode = Mode;
});

define(
	"ace/mode/csp_highlighter",
	["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
	"use strict";
	var oop = require("../lib/oop");
	var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;
	var HighlightRules = function() {
		// regexp must not have capturing parentheses. Use (?:) instead.
		// regexps are ordered -> the first match is used
		this.$rules = {
			"start" : [
				{
					token: "comment",
					regex: /^#.*$/
				}, {
					// tokens that define metadata
					token: "keyword",
					regex: "^CHECKSUM\\s*=|^ID\\s*=|^Title\\s*=|^Subtitle\\s*=|^Abstract\\s*=|^Product\\s*=|^Version\\s*=|^Edition\\s*=" +
						"|^DTD\\s*=|^Copyright Holder\\s*=|^Brand\\s*=|^publican\\.cfg\\s*=|^BZProduct\\s*=|^BZComponent\\s*=|^BZVersion\\s*=" +
						"|^Pubsnumber\\s*=|^Bug Links\\s*=|^BZPRODUCT\\s*=|^BZCOMPONENT\\s*=|^BZVERSION\\s*=|^BZURL\\s*=|^Type\\s*=" +
						"|^Book Version\\s*=|^Copyright\\s*=|^Output Style\\s*=|^BZServer\\s*=|^JIRAServer\\s*=|^JIRAProject\\s*=" +
						"|^JIRAComponent\\s*=|^JIRAVersion\\s*=|^JIRALabels\\s*="
				}, {
					// tokens that define structure
					token: "constant.language",
					regex: "^\\s*Chapter:|^\\s*Section:|^\\s*Appendix:|^\\s*Part:|^\\s*Procedure:|Refer-to:|Prerequisite:|T\\d+|R:\\d+|P:\\d+|\\bN\\d*\\b|X\\d+"
				},  {
					token : "constant.numeric",
					regex : "\\b[0-9]+\\b"
				},  {
					token : "lparen",
					regex : "[[({]"
				}, {
					token : "rparen",
					regex : "[\\])}]"
				},  {
					// tokens that appear in brackets as values
					token : "constant.language",
					regex : "Test Topic|Concept|Reference|Task|condition\\s*=|writer\\s*=|URL\\s*=|Description\\s*=|rev:"
				},
			]
		};
	};
	oop.inherits(HighlightRules, TextHighlightRules);
	exports.HighlightRules = HighlightRules;
});

define(
	"ace/mode/folding/csp_style",
	["require","exports","module","ace/lib/oop","ace/range","ace/mode/folding/fold_mode"],
	function(require, exports, module) {
		"use strict";
		var oop = require("../../lib/oop");
		var Range = require("../../range").Range;
		var BaseFoldMode = require("./fold_mode").FoldMode;
		var FoldMode = exports.FoldMode = function() {};
		oop.inherits(FoldMode, BaseFoldMode);
		(function() {
			// regular expressions that identify starting and stopping points
			this.foldingStartMarker = //;
			this.foldingStopMarker = //;
			this.getFoldWidgetRange = function(session, foldStyle, row) {
				var line = session.getLine(row);
				// test each line, and return a range of segments to collapse
			};
		}).call(FoldMode.prototype);
});

define("ace/mode/matching_brace_outdent",["require","exports","module","ace/range"],function(e,t,n){var r=e("../range").Range,i=function(){};(function(){this.checkOutdent=function(e,t){return/^\s+$/.test(e)?/^\s*\}/.test(t):!1},this.autoOutdent=function(e,t){var n=e.getLine(t),i=n.match(/^(\s*\})/);if(!i)return 0;var s=i[1].length,o=e.findMatchingBracket({row:t,column:s});if(!o||o.row==t)return 0;var u=this.$getIndent(e.getLine(o.row));e.replace(new r(t,0,t,s-1),u)},this.$getIndent=function(e){return e.match(/^\s*/)[0]}}).call(i.prototype),t.MatchingBraceOutdent=i});