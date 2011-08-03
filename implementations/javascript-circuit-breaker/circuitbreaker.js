var assert = require("assert");

var fib = function(i) {
    if (i < 0) throw "fibfeil";
    if (i < 2) return i;
    return fib(i-2) + fib(i - 1);
};


var timer = function(timeout) {
    return function() {
        var start_time = Date.now();
        return function() {
            return Date.now() - start_time > timeout;
        };
    };
};

var fusify = function(f, threshold, f_timer) {
    f_timer = f_timer || function() {
        return null;
    };
    var counter = 0;
    var timer = null;
    var halfopen = false;
    return function() {
        if (counter >= threshold) {
            if (!timer || !timer())
                throw "fusefeil";
            halfopen = true;
            counter = 0;
        }
        try {
            var rv = f.apply(this, arguments);
            halfopen = false;
            return rv;
        } catch (x) {
            if (halfopen) {
                halfopen = false;
                counter = threshold;
            }
            counter += 1;
            if (counter >= threshold) timer = f_timer();
            throw x;
        }
    };
};

var test = function() {

    assert.equal(typeof fusify(fib), "function");

    assert.equal(fusify(fib, 100)(7), fib(7));

    assert.throws(function() {
        fusify(fib, 100)(-1);
    }, "fibfeil");

    var f = fusify(fib, 1);
    assert.throws(function() {
        f(-1);
    }, /fibfeil/);
    assert.throws(function() {
        f(-1);
    }, /fusefeil/);
    assert.throws(function() {
        f(1);
    }, /fusefeil/);

    var called = 0;
    g = function(will_work) {
        called++;
        if (will_work) return true;
        throw "ffeil";
    };
    f = fusify(g, 0);
    assert.throws(function() {
        f(false);
    }, /fusefeil/);
    assert.equal(called, 0);

    var timed_out = false;
    var timer = function() {
        return function() {
            return timed_out;
        };
    };

    f = fusify(g, 1, timer);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /fusefeil/);
    assert.throws(function() {
        f(true);
    }, /fusefeil/);
    timed_out = true;
    assert.ok(f(true));

    timed_out = false;
    f = fusify(g, 2, timer);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /fusefeil/);
    assert.throws(function() {
        f(true);
    }, /fusefeil/);
    timed_out = true;
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    timed_out = false;
    assert.throws(function() {
        f(true);
    }, /fusefeil/);

    timed_out = false;
    f = fusify(g, 2, timer);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /fusefeil/);
    assert.throws(function() {
        f(true);
    }, /fusefeil/);
    timed_out = true;
    assert.ok(f(true));
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(false);
    }, /ffeil/);
    assert.throws(function() {
        f(true);
    }, /fusefeil/);

};

test();
