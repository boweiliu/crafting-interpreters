# Spec:

Types

* bool
* doubles
* strings
* nil

Expression operators

* math and negation
* string concat
* comparisons
* boolean algebra
* parens

Statements

* print
* blocks
* declaration
* assignment
* clock()

Control
* if/else
* while
* for
* fncall
* fndef
* return

* Comments via "//"

* Closures
 * and closure scope
* Classes
 * class methods
 * class properties (via assignment)
 * this
 * class constructors (init())
 * class instantiation (calling the constructor)
 * inheritance
 * super

## spec additions from me

* Type comments via ":"
* floor()
* chr(65) == "A", ord("A") == 65
* variable name reuse aka shadowing aka redeclaration
* inline named function definitions
* interfaces and "restricts" (compiler-enabled checks to ensure interfaces are respected)

note that lox has:
* no arrays (linked lists via classes or closures)
* dicts (via named object properties)

# Examples

```lox
print "hello"
```


```lox
// Converts a decimal number to a string.

fn dtos(aa: number): string {
  if (aa == 0) {
    return "0";
  } else if (aa == 1) {
    return "1";
  } else if (aa == 2) {
    return "2";
  } else if (aa == 3) {
    return "3";
  } else if (aa == 4) {
    return "4";
  } else if (aa == 5) {
    return "5";
  } else if (aa == 6) {
    return "6";
  } else if (aa == 7) {
    return "7";
  } else if (aa == 8) {
    return "8";
  } else if (aa == 9) {
    return "9";
  } else {
    return nil;
  }
}

fn mprint(s: string|nil): nil {
  if (s != nil) {
    print s
  }
}

fn itos(aa: number): string {
  if (aa == floor(aa)) {
    var nn = aa;
    if (nn < 0) {
      return "-" + itos(-nn)
    else if (nn <= 9) {
      return dtos(nn)
    } else {
      var ones = nn % 10;
      var rest = (nn - nn % 10) / 10;
      return itos(rest) + itos(ones);
    }
  } else {
    return nil;
  }
}

var xx: number = 314;
print itos(xx);

```

```lox
// poc for arrays implemented via linked list
fn isUint(xx: number): number {
  return (xx == floor(xx) and xx >= 0);
}

class _Cons {
  init(data: T, next: any): _Cons {
    this.car = data;
    this.cdr = next;
  }

  at(idx: number): _Cons {
    if (!isUint(idx)) {
      return nil;
    }
    if (idx == 0) {
      return this;
    } else {
      return this.cdr.at(idx - 1);
    }
  }
}

var hh = _Cons("a", _Cons("b", nil))

fn _makeEmptyCons(emptyVal: T, size: number): _Cons {
  if (!isUint(size)) {
    return nil;
  }
  if (size == 0) {
    return nil;
  } else {
    return _Cons(emptyVal, _makeEmptyCons(size - 1));
  }
}

class Array {
  init(size: number): Array {
    // initialize a linked list to store nils
    this.size = size;
    this.list = _makeEmptyCons(nil, size)
    this.setted = _makeEmptyCons(false, size)
  }
  set(idx: number, val: T): bool|nil {
    if (!isUint(idx) || idx >= size) { return nil; }
    var wasSetted = this.setted.at(idx).car;
    this.list.at(idx).car = val;
    this.setted.at(idx).car = true;
    return wasSetted;
  }
  getOr(idx: number, default: U): T|U|nil {
    if (!isUint(idx) || idx >= size) { return nil; }
    if (this.setted.at(idx).car) {
      return this.list.at(idx).car;
    } else {
      return default;
    }
  }
  del(idx: number): bool|nil {
    if (!isUint(idx) || idx >= size) { return nil; }
    var wasSetted = this.setted.at(idx).car;
    this.list.at(idx).car = nil;
    this.setted.at(idx).car = false;
    return wasSetted;
  }
  doForEach(cb: (data:T,idx:number)=>nil): nil {
    for (var ii=0; ii<this.size; i++) {
      if (this.setted.at(ii)) {
        cb(this.list.at(ii), ii);
      }
    }
  }
}

var ns: Array = Array(8);
ns.set(0, "hello");
ns.set(1, "world");
ns.set(2, "!");

ns.doForEach(fn _(it, idx) { print it; })
```


