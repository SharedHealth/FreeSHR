var http = require('http')
var _ = require('underscore')
var cheerio = require('cheerio')

var baseUrl = 'http://hl7.org/implement/standards/fhir/';
var url_pattern = /\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[{};:'".,<>?«»“”‘’]|\]|\?))/ig;

function append(xs, x) {
    var list = xs || []
    list.push(x);
    return list;
}

function getBody(url, callback) {
    http.get(url, function (res) {
        var body = '';
        res.on('data', function (chunk) {
            body = body + chunk;
        });
        res.on('end', function () {
            callback(body);
        });
    });
}

function extractCategories(baseUrl, path, callback) {
    getBody(baseUrl + path, function (body) {
        var $ = cheerio.load(body);
        var categories = _.map($('table[class=codes] > tr > td > a'), function (category) {
            return baseUrl + category.attribs.href;
        });
        callback(categories);
    });
}

function extractCodes(category, handler) {

    function isValidCode(code) {
        return code && code.attribs.name
    }

    function toCodeValue(code) {
        return code.attribs.name
    }

    getBody(category, function (body) {
        var $ = cheerio.load(body);
        var codeElements = $('div[class=col-9] div > table > tr > td > a');
        handler($('div[class=col-9] > h1').text().match(url_pattern)[0], _.chain(codeElements).filter(isValidCode).map(toCodeValue).value().join(","));
    });
}


function handler(category, codes) {
    category = category.replace(":", "\\:");
    codes && codes.length > 0 ? console.log(category + ' = ' + codes) : null;
}

extractCategories(baseUrl, 'terminologies-valuesets.html', function (categories) {
    _.each(categories, function (category) {
        extractCodes(category, handler);
    })
});


